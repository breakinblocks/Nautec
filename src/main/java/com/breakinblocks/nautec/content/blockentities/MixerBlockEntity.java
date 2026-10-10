package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.utils.BeamOverclock;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.utils.RecipeRevision;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.fluid.FluidTank;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.menus.MixerMenu;
import com.breakinblocks.nautec.content.recipes.MixingRecipe;
import com.breakinblocks.nautec.content.recipes.inputs.MixingRecipeInput;
import com.breakinblocks.nautec.content.recipes.utils.RecipeUtils;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;

import net.neoforged.neoforge.fluids.FluidStack;
import com.breakinblocks.nautec.transfer.transaction.Transaction;
import com.breakinblocks.nautec.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MixerBlockEntity extends LaserBlockEntity implements MenuProvider {
    private final BeamOverclock overclock = new BeamOverclock();

    @Override
    public int getRequiredPower() {
        return NTConfig.mixerPower;
    }

    public static final int INPUT_SLOTS = 4;
    public static final int OUTPUT_SLOT = 4;
    private final RecipeRevision recipeRevision = new RecipeRevision();
    private boolean running;
    private int maxDuration;

    private float independentAngle;
    private float chasingVelocity;
    private int speed;

    private int duration;

    private MixingRecipe recipe;

    private static final SlotRoles ITEM_ROLES = SlotRoles.of(new int[]{0, 1, 2, 3}, new int[]{OUTPUT_SLOT});
    private static final SlotRoles FLUID_ROLES = SlotRoles.of(new int[]{0}, new int[]{1});

    public MixerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.MIXER.get(), blockPos, blockState);
        addItemHandler(5, (slot, stack) -> slot != 4);
        addFluidTank(NTConfig.mixerInputCapacity);
        addSecondaryFluidTank(NTConfig.mixerOutputCapacity, fluidStack -> false);
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(Direction.values());
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
    public SlotRoles itemRoles() {
        return ITEM_ROLES;
    }

    @Override
    public SlotRoles fluidRoles() {
        return FLUID_ROLES;
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (level instanceof ServerLevel server && recipeRevision.changed(server)) {
            duration = 0;
            this.recipe = getRecipe().orElse(null);
        }

        float actualSpeed = getSpeed();
        chasingVelocity += ((actualSpeed * 10 / 3f) - chasingVelocity) * .25f;
        independentAngle += chasingVelocity;

        if (!level.isClientSide()) {
            boolean wasRunning = running;
            int oldDuration = duration;
            performRecipe();
            maxDuration = recipe == null ? 0 : recipe.duration();
            if (oldDuration != duration || wasRunning != running) {
                setChanged();
                if (wasRunning != running || duration == 0 || level.getGameTime() % 10 == 0) update();
            }
        }

        if (running) {
            this.speed = 20;
        } else {
            this.speed = 0;
        }
    }

    private void performRecipe() {
        if (recipe != null && getPower() >= NTConfig.mixerPower) {
            this.running = true;
            if (duration >= recipe.duration()) {
                duration = 0;
                this.running = false;
                MixingRecipe currentRecipe = this.recipe;
                if (removeInputs(currentRecipe)) setOutputs(currentRecipe);
                this.recipe = getRecipe().orElse(null);
            } else {
                duration = Math.min(recipe.duration(), duration + overclock.advance(beamSpeed()));
            }
        } else {
            this.running = false;
            // Laser connections are rebuilt after loading. Keep valid work while
            // waiting for power, including the progress restored from disk.
            if (recipe == null) duration = 0;
        }
    }

    private boolean removeInputs(MixingRecipe mixingRecipe) {
        if (mixingRecipe == null) return false;
        ItemStackHandler handler = getItemStackHandler();
        List<ItemStack> inputs = new ArrayList<>();
        for (int slot = 0; slot < OUTPUT_SLOT; slot++) inputs.add(handler.getStackInSlot(slot));
        int[] plan = RecipeUtils.consumptionPlan(inputs, mixingRecipe.ingredients());
        if (plan == null || !canInsertItem(mixingRecipe.result()) || !canInsertFluid(mixingRecipe.fluidResult())) return false;
        try (Transaction transaction = Transaction.openRoot()) {
            for (int i = 0; i < plan.length; i++) {
                int amount = mixingRecipe.ingredients().get(i).count();
                if (handler.extract(plan[i], handler.getResource(plan[i]), amount, transaction) != amount) return false;
            }
            FluidStack fluid = mixingRecipe.fluidIngredient();
            if (!fluid.isEmpty() && getFluidTank().extract(0, FluidResource.of(fluid), fluid.getAmount(), transaction) != fluid.getAmount()) return false;
            transaction.commit();
        }
        return true;
    }

    @Override
    public void onPowerChanged() {
        super.onPowerChanged();
        this.recipe = getRecipe().orElse(null);
    }

    private void setOutputs(MixingRecipe mixingRecipe) {
        if (mixingRecipe == null) {
            return;
        }

        ItemStack itemResult = mixingRecipe.result();
        if (!itemResult.isEmpty()) {
            ItemStackHandler handler = getItemStackHandler();
            int newCount = itemResult.getCount() + handler.getStackInSlot(OUTPUT_SLOT).getCount();
            handler.setStackInSlot(OUTPUT_SLOT, itemResult.copyWithCount(newCount));
        }
        FluidStack fluidResult = mixingRecipe.fluidResult();
        if (!fluidResult.isEmpty()) {
            FluidTank tank = getSecondaryFluidTank();
            tank.setFluid(fluidResult.copyWithAmount(fluidResult.getAmount() + tank.getFluidAmount()));
        }
    }

    private Optional<MixingRecipe> getRecipe() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        ItemStackHandler itemHandler = getItemStackHandler();
        int slots = itemHandler.getSlots();
        List<ItemStack> itemHandlerStacksList = new ArrayList<>(slots);
        for (int i = 0; i < slots - 1; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                itemHandlerStacksList.add(stack);
            }
        }
        
        MixingRecipeInput input = new MixingRecipeInput(itemHandlerStacksList, getFluidTank().getFluid());
        Optional<MixingRecipe> recipe = serverLevel.getRecipeManager()
                .getRecipeFor(MixingRecipe.Type.INSTANCE, input, level).map(RecipeHolder::value);
        
        if (recipe.isPresent() && canInsertItem(recipe.get().result()) && canInsertFluid(recipe.get().fluidResult())) {
            return recipe;
        }
        return Optional.empty();
    }
    
    private boolean canInsertItem(ItemStack result) {
        ItemStack stack = getItemStackHandler().getStackInSlot(OUTPUT_SLOT);
        boolean itemMatches = result.isEmpty() || stack.isEmpty() || ItemStack.isSameItemSameComponents(result, stack);
        int stackLimit = stack.isEmpty() ? result.getMaxStackSize() : stack.getMaxStackSize();
        boolean amountMatches = result.getCount() + stack.getCount() <= Math.min(stackLimit, getItemStackHandler().getSlotLimit(OUTPUT_SLOT));
        return itemMatches && amountMatches;
    }

    private boolean canInsertFluid(FluidStack fluidStack) {
        boolean fluidMatches = fluidStack.isEmpty() || getSecondaryFluidTank().isEmpty() || FluidResource.of(fluidStack).equals(FluidResource.of(getSecondaryFluidTank().getFluid()));
        int fluidAmount = getSecondaryFluidTank().getFluidAmount();
        boolean amountMatches = fluidAmount + fluidStack.getAmount() <= getSecondaryFluidTank().getCapacity();
        return fluidMatches && amountMatches;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) recipeRevision.changed(server);
        this.recipe = getRecipe().orElse(null);
    }

    @Override
    protected void onItemsChanged(int slot) {
        super.onItemsChanged(slot);
        refreshRecipe();
    }

    @Override
    protected void onFluidChanged() {
        super.onFluidChanged();
        refreshRecipe();
    }

    private void refreshRecipe() {
        MixingRecipe next = getRecipe().orElse(null);
        if (next != recipe) duration = 0;
        recipe = next;
    }

    public int getSpeed() {
        return speed;
    }

    public float getIndependentAngle(float partialTicks) {
        return (independentAngle + partialTicks * chasingVelocity) / 360;
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.duration = in.getIntOr("duration", 0);
        this.running = in.getBooleanOr("running", false);
        this.maxDuration = in.getIntOr("max_duration", 0);
        if (level == null || !level.isClientSide()) {
            this.independentAngle = in.getFloatOr("independentAngle", 0);
        }
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putInt("duration", this.duration);
        out.putBoolean("running", this.running);
        out.putInt("max_duration", this.maxDuration);
        out.putFloat("independentAngle", this.independentAngle);
    }

    public FluidStack getInputFluid() {
        return getFluidTank().getFluid();
    }

    public int getInputFluidAmount() {
        return getFluidTank().getFluidAmount();
    }

    public FluidStack getOutputFluid() {
        return getSecondaryFluidTank().getFluid();
    }

    public int getOutputFluidAmount() {
        return getSecondaryFluidTank().getFluidAmount();
    }

    public int getDuration() {
        return this.duration;
    }

    public boolean isActive() {
        return this.running;
    }

    public int getMaxDuration() {
        return level != null && level.isClientSide() ? maxDuration : getRecipe().map(MixingRecipe::duration).orElse(0);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MixerMenu(containerId, playerInventory, this);
    }
}
