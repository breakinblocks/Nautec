package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.fluid.FluidTank;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.menus.LaserCraftingMatrixMenu;
import com.breakinblocks.nautec.content.recipes.LaserCraftingRecipe;
import com.breakinblocks.nautec.content.recipes.inputs.LaserCraftingRecipeInput;
import com.breakinblocks.nautec.content.recipes.utils.RecipeUtils;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.utils.BeamOverclock;
import com.breakinblocks.nautec.utils.RecipeRevision;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.util.GeckoLibUtil;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LaserCraftingMatrixBlockEntity extends LaserBlockEntity implements MenuProvider, GeoBlockEntity {
    public static final int INPUT_SLOTS = LaserCraftingRecipe.MAX_ITEM_INPUTS;
    public static final int OUTPUT_START = INPUT_SLOTS;
    public static final int SLOTS = INPUT_SLOTS + LaserCraftingRecipe.MAX_ITEM_OUTPUTS;
    public static final int FLUID_INPUTS = LaserCraftingRecipe.MAX_FLUID_INPUTS;
    public static final DataTicket<Boolean> WORKING = DataTicket.create("nautec:laser_crafting_matrix_working", Boolean.class);

    private static final SlotRoles ITEM_ROLES = SlotRoles.of(SlotRoles.range(0, INPUT_SLOTS), SlotRoles.range(OUTPUT_START, SLOTS));
    private static final SlotRoles FLUID_ROLES = SlotRoles.of(new int[]{0, 1}, new int[]{2, 3});
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WORKING_ANIMATION = RawAnimation.begin().thenLoop("working");
    private static final int REFRESH_INTERVAL = 10;

    private final AnimatableInstanceCache animatableCache = GeckoLibUtil.createInstanceCache(this);
    private final BeamOverclock overclock = new BeamOverclock();
    private final RecipeRevision recipeRevision = new RecipeRevision();
    private final FluidTank[] outputTanks = new FluidTank[LaserCraftingRecipe.MAX_FLUID_OUTPUTS];
    private @Nullable RecipeHolder<LaserCraftingRecipe> recipe;
    private @Nullable Identifier recipeId;
    private boolean recipeDirty = true;
    private boolean running;
    private int progress;
    private int maxProgress;
    private int recipePower;
    private float recipePurity;
    private float neededPurity;

    public enum Status {
        NO_RECIPE,
        WORKING,
        NO_POWER,
        LOW_POWER,
        LOW_PURITY,
        OUTPUT_FULL
    }

    public LaserCraftingMatrixBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.LASER_CRAFTING_MATRIX.get(), pos, state);
        addItemHandler(SLOTS, (slot, stack) -> slot < INPUT_SLOTS);
        addFluidTank(NTConfig.laserCraftingMatrixTankCapacity, stack -> !holdsFluid(getSecondaryFluidTank(), stack));
        addSecondaryFluidTank(NTConfig.laserCraftingMatrixTankCapacity, stack -> !holdsFluid(getFluidTank(), stack));
        for (int i = 0; i < outputTanks.length; i++) {
            outputTanks[i] = new FluidTank(NTConfig.laserCraftingMatrixTankCapacity) {
                @Override
                protected void onContentsChanged(int index, FluidStack stack) {
                    super.onContentsChanged(index, stack);
                    update();
                    onFluidChanged();
                }

                @Override
                public boolean isValid(int index, @NotNull FluidResource resource) {
                    return false;
                }
            };
        }
    }

    private static boolean holdsFluid(@Nullable FluidTank other, FluidStack stack) {
        return other != null && !other.isEmpty() && FluidStack.isSameFluidSameComponents(other.getFluid(), stack);
    }

    @Override
    protected boolean acceptsNow(int slot, ItemResource resource) {
        if (slot >= INPUT_SLOTS) {
            return true;
        }
        ItemStackHandler handler = getItemStackHandler();
        for (int other = 0; other < INPUT_SLOTS; other++) {
            if (other != slot) {
                ItemStack held = handler.getStackInSlot(other);
                if (!held.isEmpty() && ItemResource.of(held).equals(resource)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(Direction.UP);
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of();
    }

    @Override
    public int getRequiredPower() {
        return recipePower;
    }

    @Override
    public float beamSpeed() {
        if (recipePower <= 0) {
            return maxProgress > 0 && getPower() > 0 ? 1F : 0F;
        }
        return super.beamSpeed();
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
    public List<FluidTank> fluidTanks() {
        List<FluidTank> tanks = new ArrayList<>(super.fluidTanks());
        tanks.addAll(List.of(outputTanks));
        return tanks;
    }

    public FluidTank inputTank(int index) {
        return index == 0 ? getFluidTank() : getSecondaryFluidTank();
    }

    public FluidTank outputTank(int index) {
        return outputTanks[index];
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) recipeRevision.changed(server);
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
    public void onPowerChanged() {
        super.onPowerChanged();
        recipeDirty = true;
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        if (recipeRevision.changed(server)) {
            recipeDirty = true;
            recipe = null;
        }
        if (recipeDirty || (progress == 0 && server.getGameTime() % REFRESH_INTERVAL == 0)) {
            refreshRecipe(server);
        }

        boolean wasRunning = running;
        int oldProgress = progress;
        running = canWork();
        if (running) {
            progress = Math.min(maxProgress, progress + overclock.advance(beamSpeed()));
            if (progress >= maxProgress) {
                craft();
                progress = 0;
                overclock.reset();
                refreshRecipe(server);
                running = canWork();
            }
        }
        if (wasRunning != running || oldProgress != progress) {
            setChanged();
            if (wasRunning != running || progress == 0 || server.getGameTime() % SYNC_INTERVAL == 0) {
                update();
            }
        }
    }

    private boolean canWork() {
        if (recipe == null || maxProgress <= 0) {
            return false;
        }
        LaserCraftingRecipe value = recipe.value();
        return getPower() > 0 && getPower() >= value.power() && getPurity() >= value.purity() && hasRoomFor(value);
    }

    private LaserCraftingRecipeInput currentInput() {
        ItemStackHandler handler = getItemStackHandler();
        List<ItemStack> items = new ArrayList<>(INPUT_SLOTS);
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            items.add(handler.getStackInSlot(slot));
        }
        List<FluidStack> fluids = List.of(getFluidTank().getFluid(), getSecondaryFluidTank().getFluid());
        return new LaserCraftingRecipeInput(items, fluids, getPurity());
    }

    private void refreshRecipe(ServerLevel server) {
        recipeDirty = false;
        RecipeHolder<LaserCraftingRecipe> next = LaserCraftingRecipe.findBest(server, currentInput()).orElse(null);
        Identifier nextId = next == null ? null : next.id().identifier();
        if (nextId == null ? recipeId != null : !nextId.equals(recipeId)) {
            progress = 0;
            overclock.reset();
            update();
        }
        recipe = next;
        recipeId = nextId;
        float needed = next == null ? lowestPurityNeeded(server) : 0F;
        if (needed != neededPurity) {
            neededPurity = needed;
            update();
        }
        int power = next == null ? 0 : next.value().power();
        float purity = next == null ? 0F : next.value().purity();
        int duration = next == null ? 0 : next.value().duration();
        if (power != recipePower || purity != recipePurity || duration != maxProgress) {
            recipePower = power;
            recipePurity = purity;
            maxProgress = duration;
            update();
        }
    }

    private float lowestPurityNeeded(ServerLevel server) {
        LaserCraftingRecipeInput input = currentInput();
        LaserCraftingRecipeInput unlimited = new LaserCraftingRecipeInput(input.items(), input.fluids(), Float.MAX_VALUE);
        float lowest = 0F;
        for (RecipeHolder<LaserCraftingRecipe> holder : server.recipeAccess().recipeMap().byType(LaserCraftingRecipe.Type.INSTANCE)) {
            LaserCraftingRecipe candidate = holder.value();
            if (candidate.purity() > getPurity() && candidate.matches(unlimited, server) && (lowest == 0F || candidate.purity() < lowest)) {
                lowest = candidate.purity();
            }
        }
        return lowest;
    }

    public Status status() {
        if (recipe == null) {
            return neededPurity > 0F ? Status.LOW_PURITY : Status.NO_RECIPE;
        }
        LaserCraftingRecipe value = recipe.value();
        if (!hasRoomFor(value)) {
            return Status.OUTPUT_FULL;
        }
        if (getPower() <= 0) {
            return Status.NO_POWER;
        }
        if (getPower() < value.power()) {
            return Status.LOW_POWER;
        }
        if (getPurity() < value.purity()) {
            return Status.LOW_PURITY;
        }
        return Status.WORKING;
    }

    public List<ItemStack> previewResults() {
        return recipe == null ? List.of() : recipe.value().results();
    }

    public List<FluidStack> previewFluidResults() {
        return recipe == null ? List.of() : recipe.value().fluidResults();
    }

    public float getNeededPurity() {
        return neededPurity;
    }

    private boolean hasRoomFor(LaserCraftingRecipe value) {
        ItemStackHandler handler = getItemStackHandler();
        List<ItemStack> outputs = new ArrayList<>();
        for (int slot = OUTPUT_START; slot < SLOTS; slot++) {
            outputs.add(handler.getStackInSlot(slot).copy());
        }
        for (ItemStack result : value.results()) {
            if (placeItem(outputs, result, handler) < 0) {
                return false;
            }
        }
        List<FluidStack> fluids = new ArrayList<>();
        for (FluidTank tank : outputTanks) {
            fluids.add(tank.getFluid().copy());
        }
        for (FluidStack result : value.fluidResults()) {
            if (placeFluid(fluids, result) < 0) {
                return false;
            }
        }
        return true;
    }

    private static int placeItem(List<ItemStack> outputs, ItemStack result, ItemStackHandler handler) {
        if (result.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < outputs.size(); i++) {
            ItemStack held = outputs.get(i);
            int limit = Math.min(handler.getSlotLimit(OUTPUT_START + i), result.getMaxStackSize());
            if (!held.isEmpty() && ItemStack.isSameItemSameComponents(held, result) && held.getCount() + result.getCount() <= limit) {
                held.grow(result.getCount());
                return i;
            }
        }
        for (int i = 0; i < outputs.size(); i++) {
            if (outputs.get(i).isEmpty() && result.getCount() <= Math.min(handler.getSlotLimit(OUTPUT_START + i), result.getMaxStackSize())) {
                outputs.set(i, result.copy());
                return i;
            }
        }
        return -1;
    }

    private int placeFluid(List<FluidStack> fluids, FluidStack result) {
        if (result.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < fluids.size(); i++) {
            FluidStack held = fluids.get(i);
            if (!held.isEmpty() && FluidStack.isSameFluidSameComponents(held, result)
                    && held.getAmount() + result.getAmount() <= outputTanks[i].getCapacity()) {
                held.grow(result.getAmount());
                return i;
            }
        }
        for (int i = 0; i < fluids.size(); i++) {
            if (fluids.get(i).isEmpty() && result.getAmount() <= outputTanks[i].getCapacity()) {
                fluids.set(i, result.copy());
                return i;
            }
        }
        return -1;
    }

    private void craft() {
        if (recipe == null) {
            return;
        }
        LaserCraftingRecipe value = recipe.value();
        LaserCraftingRecipeInput input = currentInput();
        if (!value.matches(input, level) || !hasRoomFor(value)) {
            return;
        }
        int[] itemPlan = RecipeUtils.consumptionPlan(input.items(), value.ingredients());
        int[] fluidPlan = value.fluidPlan(input.fluids());
        if (itemPlan == null || fluidPlan == null) {
            return;
        }
        ItemStackHandler handler = getItemStackHandler();
        try (Transaction transaction = Transaction.openRoot()) {
            for (int i = 0; i < itemPlan.length; i++) {
                int amount = value.ingredients().get(i).count();
                if (handler.extract(itemPlan[i], handler.getResource(itemPlan[i]), amount, transaction) != amount) {
                    return;
                }
            }
            for (int i = 0; i < fluidPlan.length; i++) {
                FluidTank tank = inputTank(fluidPlan[i]);
                int amount = value.fluidIngredients().get(i).amount();
                if (tank.extract(0, tank.getResource(0), amount, transaction) != amount) {
                    return;
                }
            }
            transaction.commit();
        }
        for (ItemStack result : value.results()) {
            List<ItemStack> outputs = new ArrayList<>();
            for (int slot = OUTPUT_START; slot < SLOTS; slot++) {
                outputs.add(handler.getStackInSlot(slot).copy());
            }
            int index = placeItem(outputs, result, handler);
            if (index >= 0) {
                handler.setStackInSlot(OUTPUT_START + index, outputs.get(index));
            }
        }
        for (FluidStack result : value.fluidResults()) {
            List<FluidStack> fluids = new ArrayList<>();
            for (FluidTank tank : outputTanks) {
                fluids.add(tank.getFluid().copy());
            }
            int index = placeFluid(fluids, result);
            if (index >= 0) {
                outputTanks[index].setFluid(fluids.get(index));
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public float getRecipePurity() {
        return recipePurity;
    }

    public boolean hasRecipe() {
        return maxProgress > 0;
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        for (int i = 0; i < outputTanks.length; i++) {
            in.child("output_tank_" + i).ifPresent(outputTanks[i]::deserialize);
        }
        progress = in.getIntOr("progress", 0);
        maxProgress = in.getIntOr("max_progress", 0);
        running = in.getBooleanOr("running", false);
        recipePower = in.getIntOr("recipe_power", 0);
        recipePurity = in.getFloatOr("recipe_purity", 0F);
        neededPurity = in.getFloatOr("needed_purity", 0F);
        overclock.load(in, "overclock");
        recipeDirty = true;
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        for (int i = 0; i < outputTanks.length; i++) {
            outputTanks[i].serialize(out.child("output_tank_" + i));
        }
        out.putInt("progress", progress);
        out.putInt("max_progress", maxProgress);
        out.putBoolean("running", running);
        out.putInt("recipe_power", recipePower);
        out.putFloat("recipe_purity", recipePurity);
        out.putFloat("needed_purity", neededPurity);
        overclock.save(out, "overclock");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<LaserCraftingMatrixBlockEntity>("matrix", 6,
                state -> state.setAndContinue(state.getDataOrDefault(WORKING, false) ? WORKING_ANIMATION : IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animatableCache;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new LaserCraftingMatrixMenu(containerId, playerInventory, this);
    }
}
