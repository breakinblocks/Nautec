package com.breakinblocks.nautec.content.enchanter;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.beam.IBeamReceiver;
import com.breakinblocks.nautec.capabilities.fluid.FluidTank;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.menus.AbyssalEnchanterMenu;
import com.breakinblocks.nautec.content.recipes.LaserEnchantingRecipe;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.tags.NTTags;
import com.breakinblocks.nautec.utils.RecipeRevision;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AbyssalEnchanterBlockEntity extends LaserBlockEntity implements MenuProvider, IBeamReceiver {
    public static final int TARGET_SLOT = 0;
    public static final int REAGENT_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int SLOTS = 3;
    public static final int BUTTON_LEVEL_DOWN = 0;
    public static final int BUTTON_LEVEL_UP = 1;
    public static final int BUTTON_NEXT_RECIPE = 2;

    private static final SlotRoles ITEM_ROLES = SlotRoles.of(new int[]{TARGET_SLOT, REAGENT_SLOT}, new int[]{OUTPUT_SLOT});
    private static final SlotRoles FLUID_ROLES = SlotRoles.of(new int[]{0}, new int[]{0});
    private static final int REFRESH_INTERVAL = 10;

    public enum Status {
        NO_REAGENT,
        NO_RECIPE,
        BLOCKED,
        NO_TARGET,
        CANNOT_APPLY,
        ALREADY_HAS,
        NEED_REAGENT,
        NEED_XP,
        OUTPUT_FULL,
        NO_POWER,
        LOW_POWER,
        WORKING
    }

    private final Long2IntOpenHashMap beamPower = new Long2IntOpenHashMap();
    private final Long2FloatOpenHashMap beamPurity = new Long2FloatOpenHashMap();
    private final FloatArrayList purities = new FloatArrayList();
    private final RecipeRevision recipeRevision = new RecipeRevision();
    private @Nullable RecipeHolder<LaserEnchantingRecipe> recipe;
    private @Nullable Holder<Enchantment> enchantment;
    private boolean recipeDirty = true;
    private int totalPower;
    private float totalPurity;
    private int recipeChoice;
    private int recipeCount;
    private boolean blocked;
    private String enchantmentId = "";
    private int requestedLevel;
    private int enchantLevel;
    private int maxLevel;
    private int levelCap;
    private boolean bookCapped;
    private int sustained;
    private long total;
    private int experienceMb;
    private int reagentNeeded;
    private long progress;
    private boolean running;
    private Status status = Status.NO_REAGENT;
    private float spin;
    private float prevSpin;
    private float spinSpeed;

    public AbyssalEnchanterBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.ABYSSAL_ENCHANTER.get(), pos, state);
        addItemHandler(SLOTS, (slot, stack) -> slot == REAGENT_SLOT || slot == TARGET_SLOT && enchantable(stack));
        addFluidTank(NTConfig.abyssalEnchanterXpCapacity, stack -> stack.is(NTTags.Fluids.EXPERIENCE));
    }

    public static boolean enchantable(ItemStack stack) {
        return EnchantingRules.isBook(stack) || stack.isEnchantable() || EnchantmentHelper.hasAnyEnchantments(stack);
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(Direction.DOWN);
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of();
    }

    @Override
    public void receiveBeam(int power, float purity, Direction travel, BlockPos origin, LongSet trail) {
        long key = origin.asLong();
        beamPower.put(key, power);
        beamPurity.put(key, purity);
    }

    @Override
    public int getPower() {
        return totalPower;
    }

    @Override
    public float getPurity() {
        return totalPurity;
    }

    @Override
    public int getRequiredPower() {
        return sustained;
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public SlotRoles itemRoles() {
        return ITEM_ROLES;
    }

    @Override
    public SlotRoles fluidRoles() {
        return FLUID_ROLES;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) {
            recipeRevision.changed(server);
        }
        recipeDirty = true;
    }

    @Override
    protected void onItemsChanged(int slot) {
        super.onItemsChanged(slot);
        recipeDirty = true;
    }

    @Override
    protected void onFluidChanged() {
        super.onFluidChanged();
        recipeDirty = true;
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (!(level instanceof ServerLevel server)) {
            clientTick();
            return;
        }
        int oldPower = totalPower;
        float oldPurity = totalPurity;
        collectBeams(super.getPower(), super.getPurity());
        if (recipeRevision.changed(server)) {
            recipeDirty = true;
        }
        if (recipeDirty || server.getGameTime() % REFRESH_INTERVAL == 0) {
            refresh(server);
        }

        boolean wasRunning = running;
        long oldProgress = progress;
        Status oldStatus = status;
        status = computeStatus();
        running = status == Status.WORKING;
        if (running) {
            long step = Math.max(1, total / Math.max(1, NTConfig.abyssalEnchanterMinTicks));
            progress = Math.min(total, progress + Math.min(totalPower, step));
            if (progress >= total) {
                finish(server);
                progress = 0;
                refresh(server);
                status = computeStatus();
                running = status == Status.WORKING;
            }
        }
        if (wasRunning != running) {
            BlockState state = getBlockState();
            if (state.hasProperty(AbyssalEnchanterBlock.ACTIVE) && state.getValue(AbyssalEnchanterBlock.ACTIVE) != running) {
                server.setBlock(worldPosition, state.setValue(AbyssalEnchanterBlock.ACTIVE, running), Block.UPDATE_CLIENTS);
            }
        }
        boolean changed = wasRunning != running || oldProgress != progress || oldStatus != status || oldPower != totalPower || oldPurity != totalPurity;
        if (changed) {
            setChanged();
            if (wasRunning != running || oldStatus != status || progress == 0 || server.getGameTime() % SYNC_INTERVAL == 0) {
                update();
            }
        }
    }

    private void clientTick() {
        BlockState state = getBlockState();
        float target = state.hasProperty(AbyssalEnchanterBlock.ACTIVE) && state.getValue(AbyssalEnchanterBlock.ACTIVE) ? 1F : 0F;
        spinSpeed += (target - spinSpeed) * 0.04F;
        if (Math.abs(target - spinSpeed) < 0.001F) {
            spinSpeed = target;
        }
        prevSpin = spin;
        spin += spinSpeed * 1.6F;
        if (spin >= 360F) {
            spin -= 360F;
            prevSpin -= 360F;
        }
    }

    public float spin(float partialTick) {
        return prevSpin + (spin - prevSpin) * partialTick;
    }

    public float spinSpeed() {
        return spinSpeed;
    }

    private void collectBeams(int ownPower, float ownPurity) {
        purities.clear();
        long sum = ownPower;
        if (ownPower > 0) {
            purities.add(ownPurity);
        }
        for (Long2IntMap.Entry entry : beamPower.long2IntEntrySet()) {
            int power = entry.getIntValue();
            if (power > 0) {
                sum += power;
                purities.add(beamPurity.get(entry.getLongKey()));
            }
        }
        beamPower.clear();
        beamPurity.clear();
        totalPower = (int) Math.min(Integer.MAX_VALUE, sum);
        totalPurity = purities.isEmpty() ? 0F : mergedPurity(purities);
    }

    private List<RecipeHolder<LaserEnchantingRecipe>> matching(ServerLevel server, ItemStack reagent, ItemStack target) {
        List<RecipeHolder<LaserEnchantingRecipe>> all = new ArrayList<>();
        List<RecipeHolder<LaserEnchantingRecipe>> fitting = new ArrayList<>();
        for (RecipeHolder<LaserEnchantingRecipe> holder : server.recipeAccess().recipeMap().byType(LaserEnchantingRecipe.Type.INSTANCE)) {
            LaserEnchantingRecipe candidate = holder.value();
            if (!candidate.input().ingredient().test(reagent)) {
                continue;
            }
            Holder<Enchantment> resolved = candidate.enchantment(server.registryAccess()).orElse(null);
            if (resolved == null) {
                continue;
            }
            if (!EnchantingRules.allowed(resolved)) {
                blocked = true;
                continue;
            }
            all.add(holder);
            if (!target.isEmpty() && EnchantingRules.accepts(target, resolved)) {
                fitting.add(holder);
            }
        }
        List<RecipeHolder<LaserEnchantingRecipe>> result = fitting.isEmpty() ? all : fitting;
        result.sort(Comparator.comparing(holder -> holder.id().identifier().toString()));
        return result;
    }

    private void refresh(ServerLevel server) {
        recipeDirty = false;
        ItemStackHandler handler = getItemStackHandler();
        ItemStack reagent = handler.getStackInSlot(REAGENT_SLOT);
        ItemStack target = handler.getStackInSlot(TARGET_SLOT);
        blocked = false;
        List<RecipeHolder<LaserEnchantingRecipe>> options = reagent.isEmpty() ? List.of() : matching(server, reagent, target);
        recipeCount = options.size();
        RecipeHolder<LaserEnchantingRecipe> next = options.isEmpty() ? null : options.get(Math.floorMod(recipeChoice, options.size()));
        Identifier oldId = recipe == null ? null : recipe.id().identifier();
        Identifier newId = next == null ? null : next.id().identifier();
        recipe = next;
        enchantment = next == null ? null : next.value().enchantment(server.registryAccess()).orElse(null);
        String newEnchantment = enchantment == null ? "" : enchantment.getRegisteredName();
        int oldLevel = enchantLevel;
        int oldCap = levelCap;
        boolean oldBookCapped = bookCapped;
        if (enchantment == null) {
            maxLevel = 0;
            levelCap = 0;
            bookCapped = false;
            enchantLevel = 0;
            sustained = 0;
            total = 0;
            experienceMb = 0;
            reagentNeeded = 0;
        } else {
            float multiplier = next.value().costMultiplier();
            maxLevel = EnchantingRules.maxLevel(enchantment);
            levelCap = EnchantingRules.levelCap(enchantment, target);
            bookCapped = levelCap < EnchantingRules.levelCap(enchantment);
            enchantLevel = requestedLevel <= 0 ? maxLevel : Math.clamp(requestedLevel, 1, levelCap);
            sustained = EnchantingRules.sustainedPower(enchantment, enchantLevel, multiplier);
            total = EnchantingRules.totalPower(enchantment, enchantLevel, multiplier);
            experienceMb = EnchantingRules.experienceMb(enchantment, enchantLevel, multiplier);
            reagentNeeded = next.value().reagentCount(Math.min(enchantLevel, maxLevel));
        }
        boolean sameRecipe = oldId == null ? newId == null : oldId.equals(newId);
        if (!sameRecipe || oldLevel != enchantLevel || !newEnchantment.equals(enchantmentId)) {
            progress = 0;
            enchantmentId = newEnchantment;
            update();
        } else if (oldCap != levelCap || oldBookCapped != bookCapped) {
            update();
        }
    }

    private Status computeStatus() {
        ItemStackHandler handler = getItemStackHandler();
        ItemStack reagent = handler.getStackInSlot(REAGENT_SLOT);
        ItemStack target = handler.getStackInSlot(TARGET_SLOT);
        if (reagent.isEmpty()) {
            return Status.NO_REAGENT;
        }
        if (recipe == null || enchantment == null) {
            return blocked ? Status.BLOCKED : Status.NO_RECIPE;
        }
        if (target.isEmpty()) {
            return Status.NO_TARGET;
        }
        if (!EnchantingRules.accepts(target, enchantment)) {
            return Status.CANNOT_APPLY;
        }
        if (EnchantingRules.existingLevel(target, enchantment) >= enchantLevel) {
            return Status.ALREADY_HAS;
        }
        if (reagent.getCount() < reagentNeeded) {
            return Status.NEED_REAGENT;
        }
        FluidTank tank = getFluidTank();
        if (tank.getFluidAmount() < experienceMb) {
            return Status.NEED_XP;
        }
        if (!handler.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            return Status.OUTPUT_FULL;
        }
        if (totalPower <= 0) {
            return Status.NO_POWER;
        }
        if (totalPower < sustained) {
            return Status.LOW_POWER;
        }
        return Status.WORKING;
    }

    private void finish(ServerLevel server) {
        if (enchantment == null || recipe == null) {
            return;
        }
        ItemStackHandler handler = getItemStackHandler();
        ItemStack target = handler.getStackInSlot(TARGET_SLOT);
        ItemStack reagent = handler.getStackInSlot(REAGENT_SLOT);
        if (target.isEmpty() || reagent.getCount() < reagentNeeded || getFluidTank().getFluidAmount() < experienceMb) {
            return;
        }
        EnchantingOutcome outcome = EnchantingOutcome.roll(server.registryAccess(), target, enchantment, enchantLevel, totalPurity, server.getRandom());
        getFluidTank().drain(experienceMb);
        ItemStack remainingReagent = reagent.copy();
        remainingReagent.shrink(reagentNeeded);
        handler.setStackInSlot(REAGENT_SLOT, remainingReagent);
        ItemStack remainingTarget = target.copy();
        remainingTarget.shrink(1);
        handler.setStackInSlot(TARGET_SLOT, remainingTarget);
        handler.setStackInSlot(OUTPUT_SLOT, outcome.result());
        server.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F,
                outcome.kind() == EnchantingOutcome.Kind.EXACT ? 1.0F : 0.7F);
        server.sendParticles(ParticleTypes.ENCHANT, worldPosition.getX() + 0.5, worldPosition.getY() + 1.6, worldPosition.getZ() + 0.5,
                40, 0.6, 0.4, 0.6, 0.5);
        if (outcome.curse() != null) {
            server.playSound(null, worldPosition, SoundEvents.WITHER_AMBIENT, SoundSource.BLOCKS, 0.4F, 1.6F);
        }
    }

    public void pressButton(int id) {
        if (id == BUTTON_LEVEL_DOWN || id == BUTTON_LEVEL_UP) {
            if (maxLevel <= 0) {
                return;
            }
            int current = enchantLevel <= 0 ? maxLevel : enchantLevel;
            int next = id == BUTTON_LEVEL_UP ? current + 1 : current - 1;
            if (next > levelCap) {
                next = 1;
            } else if (next < 1) {
                next = levelCap;
            }
            requestedLevel = next == maxLevel ? 0 : next;
        } else if (id == BUTTON_NEXT_RECIPE) {
            recipeChoice++;
        } else {
            return;
        }
        progress = 0;
        recipeDirty = true;
        if (level instanceof ServerLevel server) {
            refresh(server);
        }
        setChanged();
        update();
    }

    public Status status() {
        return status;
    }

    public boolean isRunning() {
        return running;
    }

    public long getProgress() {
        return progress;
    }

    public long getTotal() {
        return total;
    }

    public float progressFraction() {
        return total <= 0 ? 0F : (float) Math.min(1.0, progress / (double) total);
    }

    public int getEnchantLevel() {
        return enchantLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getLevelCap() {
        return levelCap;
    }

    public boolean isBookCapped() {
        return bookCapped;
    }

    public int getExperienceMb() {
        return experienceMb;
    }

    public int getReagentNeeded() {
        return reagentNeeded;
    }

    public int getRecipeCount() {
        return recipeCount;
    }

    public @Nullable Holder<Enchantment> displayedEnchantment() {
        if (level == null || enchantmentId.isEmpty()) {
            return null;
        }
        Identifier id = Identifier.tryParse(enchantmentId);
        if (id == null) {
            return null;
        }
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(id).orElse(null);
    }

    public float stability() {
        return 1F - EnchantingRules.instability(totalPurity);
    }

    public float curseChance() {
        return EnchantingRules.curseChance(totalPurity);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        progress = in.getLongOr("progress", 0L);
        running = in.getBooleanOr("running", false);
        requestedLevel = in.getIntOr("requested_level", 0);
        recipeChoice = in.getIntOr("recipe_choice", 0);
        recipeCount = in.getIntOr("recipe_count", 0);
        enchantmentId = in.getStringOr("enchantment", "");
        enchantLevel = in.getIntOr("enchant_level", 0);
        maxLevel = in.getIntOr("max_level", 0);
        levelCap = in.getIntOr("level_cap", 0);
        bookCapped = in.getBooleanOr("book_capped", false);
        sustained = in.getIntOr("sustained", 0);
        total = in.getLongOr("total", 0L);
        experienceMb = in.getIntOr("experience_mb", 0);
        reagentNeeded = in.getIntOr("reagent_needed", 0);
        totalPower = in.getIntOr("total_power", 0);
        totalPurity = in.getFloatOr("total_purity", 0F);
        int statusIndex = in.getIntOr("status", 0);
        status = Status.values()[Math.clamp(statusIndex, 0, Status.values().length - 1)];
        recipeDirty = true;
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putLong("progress", progress);
        out.putBoolean("running", running);
        out.putInt("requested_level", requestedLevel);
        out.putInt("recipe_choice", recipeChoice);
        out.putInt("recipe_count", recipeCount);
        out.putString("enchantment", enchantmentId);
        out.putInt("enchant_level", enchantLevel);
        out.putInt("max_level", maxLevel);
        out.putInt("level_cap", levelCap);
        out.putBoolean("book_capped", bookCapped);
        out.putInt("sustained", sustained);
        out.putLong("total", total);
        out.putInt("experience_mb", experienceMb);
        out.putInt("reagent_needed", reagentNeeded);
        out.putInt("total_power", totalPower);
        out.putFloat("total_purity", totalPurity);
        out.putInt("status", status.ordinal());
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AbyssalEnchanterMenu(containerId, playerInventory, this);
    }
}
