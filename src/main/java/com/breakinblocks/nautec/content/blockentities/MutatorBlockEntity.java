package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.utils.BeamOverclock;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.utils.RecipeRevision;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.bacteria.DishPort;
import com.breakinblocks.nautec.content.bacteria.SimpleBacteriaStats;
import com.breakinblocks.nautec.content.bacteria.SimpleCollapsedStats;
import com.breakinblocks.nautec.content.menus.MutatorMenu;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import com.breakinblocks.nautec.content.recipes.inputs.BacteriaRecipeInput;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTCriteriaTriggers;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class MutatorBlockEntity extends LaserBlockEntity implements MenuProvider {
    private final BeamOverclock overclock = new BeamOverclock();

    @Override
    public int getRequiredPower() {
        return NTConfig.mutatorPowerUsage;
    }

    public static final int CATALYST = 0;
    public static final int DISH_IN = 1;
    public static final int DISH_OUT = 2;
    public static final int DISH_EMPTY_OUT = 3;
    public static final int BOOSTER = 4;
    private static final int[] LOAD_SLOTS = {0};

    private final RecipeRevision recipeRevision = new RecipeRevision();
    private BacteriaMutationRecipe recipe;
    private boolean active;
    private int progress;

    private static final SlotRoles ITEM_ROLES = SlotRoles.of(new int[]{0, DISH_IN, BOOSTER}, new int[]{DISH_OUT, DISH_EMPTY_OUT});

    public MutatorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.MUTATOR.get(), blockPos, blockState);
        addBacteriaStorage(2);
        addItemHandler(5, slot -> slot == 0 || slot == BOOSTER ? 64 : 1, (slot, stack) -> slot == 0
                || (slot == DISH_IN && DishPort.isDish(stack))
                || (slot == BOOSTER && stack.is(NTItems.ELECTROLYTE_ALGAE_SERUM_VIAL.get())));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) recipeRevision.changed(server);

        checkRecipe();
    }

    @Override
    protected void onItemsChanged(int slot) {
        super.onItemsChanged(slot);

        checkRecipe();
    }

    private void checkRecipe() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ItemStack catalyst = getItemStackHandler().getStackInSlot(0);
        BacteriaInstance inputBacteria = getBacteriaStorage().getBacteria(0);
        BacteriaInstance resultBacteria = getBacteriaStorage().getBacteria(1);
        BacteriaMutationRecipe recipe1 = BacteriaMutationRecipe.find(serverLevel, new BacteriaRecipeInput(inputBacteria, catalyst)).orElse(null);
        this.recipe = (recipe1 != null && resultBacteria.isEmpty()) ? recipe1 : null;

        if (this.active != (this.recipe != null)) {
            this.active = this.recipe != null;
            update();
        }
    }

    @Override
    public void onBacteriaChanged(int slot) {
        super.onBacteriaChanged(slot);

        checkRecipe();
    }

    @Override
    protected boolean acceptsNow(int slot, ItemResource resource) {
        if (slot != DISH_IN) {
            return true;
        }
        return DishPort.accepts(this, resource, DishPort.NONE, DISH_OUT, LOAD_SLOTS, this::mutationReady, this::canMutate);
    }

    private int mutationReady() {
        return getBacteriaStorage().getBacteria(1).isEmpty() ? -1 : 1;
    }

    private boolean canMutate(BacteriaInstance colony) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }
        ItemStack catalyst = getItemStackHandler().getStackInSlot(0);
        return !catalyst.isEmpty() && BacteriaMutationRecipe.find(serverLevel, new BacteriaRecipeInput(colony, catalyst)).isPresent();
    }

    public static float computeSuccessChance(BacteriaMutationRecipe recipe, BacteriaInstance input) {
        long cap = Math.max(1, NTConfig.bacteriaColonySizeCap);
        float sizeFactor = 1f - 0.25f * ((float) Math.min(input.getSize(), cap) / cap);
        float resistanceFactor = NTConfig.bacteriaMutationResistanceCap <= 0
                ? 1f
                : 1f - 0.5f * (input.getStats().mutationResistance() / NTConfig.bacteriaMutationResistanceCap);

        return Math.max(0f, (recipe.chance() / 100f) * resistanceFactor * sizeFactor);
    }

    public static float computeSuccessChance(BacteriaMutationRecipe recipe, BacteriaInstance input, boolean boosted) {
        float chance = computeSuccessChance(recipe, input);
        return boosted ? Math.min(1f, (float) (chance * NTConfig.mutatorBoosterMultiplier)) : chance;
    }

    public boolean isBoosted() {
        return !getItemStackHandler().getStackInSlot(BOOSTER).isEmpty();
    }

    public static long computeFailureShrink(BacteriaInstance input) {
        float resistanceFactor = NTConfig.bacteriaMutationResistanceCap <= 0
                ? 1f
                : 1f - input.getStats().mutationResistance() / NTConfig.bacteriaMutationResistanceCap;
        long shrink = (long) Math.ceil(input.getSize() * NTConfig.mutatorFailureShrink * Math.max(0f, resistanceFactor));

        return Math.max(0, Math.min(shrink, input.getSize() - 1));
    }

    public static long computeFailureShrink(BacteriaInstance input, boolean boosted) {
        long shrink = computeFailureShrink(input);
        return boosted ? (long) Math.floor(shrink * NTConfig.mutatorBoostedFailureShrink) : shrink;
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (level instanceof ServerLevel server && recipeRevision.changed(server)) {
            progress = 0;
            checkRecipe();
        }

        if (level instanceof ServerLevel server && server.getGameTime() % DishPort.INTERVAL == 0) {
            DishPort.reclaim(this, DISH_IN, DISH_EMPTY_OUT);
            DishPort.tick(this, DISH_IN, DishPort.NONE, DISH_OUT, LOAD_SLOTS, this::mutationReady, slot -> {
            });
        }

        boolean canRun = level.isClientSide() ? this.active : this.recipe != null;

        if (canRun) {
            if (getPower() >= NTConfig.mutatorPowerUsage) {
                if (progress >= NTConfig.mutatorCraftingSpeed) {
                    if (!level.isClientSide()) {
                        mutate();
                    }

                    progress = 0;
                } else {
                    progress = Math.min(NTConfig.mutatorCraftingSpeed, progress + overclock.advance(beamSpeed()));
                }
            }
        } else {
            progress = 0;
        }
    }

    private void mutate() {
        BacteriaInstance inputBacteria = getBacteriaStorage().getBacteria(0);
        boolean boosted = isBoosted();

        if (level.getRandom().nextFloat() < computeSuccessChance(recipe, inputBacteria, boosted)) {
            if (boosted) {
                getItemStackHandler().extractItem(BOOSTER, 1, false);
            }
            BacteriaInstance result = buildResult(recipe.resultBacteria(), inputBacteria, recipe.refines());
            getBacteriaStorage().extractBacteria(0, inputBacteria.getSize(), false);
            getBacteriaStorage().insertBacteria(1, result, false);
            if (level instanceof ServerLevel serverLevel) {
                NTCriteriaTriggers.triggerNear(NTCriteriaTriggers.BACTERIA_MUTATED.get(), serverLevel, worldPosition, 16.0);
            }
        } else {
            getBacteriaStorage().extractBacteria(0, computeFailureShrink(inputBacteria, boosted), false);
        }
    }

    private BacteriaInstance buildResult(ResourceKey<Bacteria> resultBacteria, BacteriaInstance input, boolean refine) {
        Bacteria result = BacteriaHelper.getBacteria(level.registryAccess(), resultBacteria);

        if (input.getStats() instanceof SimpleCollapsedStats simpleStats && result.stats() instanceof SimpleBacteriaStats resultBase) {
            SimpleCollapsedStats drift = simpleStats.rollStats();
            return new BacteriaInstance(
                    resultBacteria,
                    refine ? input.getSize() : result.rollSize(),
                    new SimpleCollapsedStats(resultBase, drift.growthRate(), drift.mutationResistance(), drift.productionRate(), drift.lifespan(), resultBase.color()),
                    false,
                    0
            );
        }

        return BacteriaInstance.roll(resultBacteria, level.registryAccess());
    }

    @Override
    public SlotRoles itemRoles() {
        return ITEM_ROLES;
    }

    public int getProgress() {
        return progress;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(Direction.UP, Direction.DOWN);
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MutatorMenu(containerId, playerInventory, this);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        getItemStackHandler().ensureSize(5);
        this.progress = in.getIntOr("progress", 0);
        this.active = in.getBooleanOr("active", false);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putInt("progress", this.progress);
        out.putBoolean("active", this.active);
    }
}
