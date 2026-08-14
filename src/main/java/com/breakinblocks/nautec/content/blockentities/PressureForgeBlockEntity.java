package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.recipes.PressureForgingRecipe;
import com.breakinblocks.nautec.content.recipes.inputs.PressureForgingRecipeInput;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTSounds;
import com.breakinblocks.nautec.utils.MachineSounds;
import com.breakinblocks.nautec.utils.SidedCapUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class PressureForgeBlockEntity extends LaserBlockEntity {
    private static final int WORK_PERIOD = 40;

    private int progress;

    public PressureForgeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.PRESSURE_FORGE.get(), blockPos, blockState);
        addItemHandler(2);
        addFluidTank(NTConfig.pressureForgeCapacity,
                fluidStack -> fluidStack.getFluid() == NTFluids.ETCHING_ACID.getStillFluid());
    }

    public int getProgress() {
        return progress;
    }

    public static boolean hasPressure(Level level, BlockPos pos) {
        if (pos.getY() > NTConfig.pressureForgeDepth) {
            return false;
        }

        int required = NTConfig.pressureForgeWaterColumn;
        for (int i = 1; i <= required; i++) {
            BlockPos above = pos.above(i);
            if (level.isOutsideBuildHeight(above) || !level.getFluidState(above).isSource()) {
                return false;
            }
        }
        return true;
    }

    public boolean isPressurised() {
        return hasPressure(level, worldPosition);
    }

    @Override
    public void commonTick() {
        super.commonTick();

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        PressureForgingRecipe recipe = currentRecipe(serverLevel);
        if (recipe == null || getPower() < NTConfig.pressureForgePowerUsage
                || getFluidTank().getFluidAmount() < NTConfig.pressureForgeAcidUsage) {
            this.progress = 0;
            return;
        }

        if (this.progress < recipe.duration()) {
            this.progress++;
            MachineSounds.interval(serverLevel, worldPosition, NTSounds.PRESSURE_FORGE_WORK, WORK_PERIOD, 0.5f, 0.7f);
            return;
        }

        this.progress = 0;
        forge(recipe);
    }

    private void forge(PressureForgingRecipe recipe) {
        ItemStack result = recipe.result();
        if (!getItemStackHandler().insertItem(1, result, true).isEmpty()) {
            return;
        }

        getItemStackHandler().extractItem(0, 1, false);
        getItemStackHandler().insertItem(1, result, false);
        getFluidTank().drain(NTConfig.pressureForgeAcidUsage);
        MachineSounds.play(level, worldPosition, NTSounds.PRESSURE_FORGE_COMPLETE, 0.8f, 0.9f);
    }

    private @Nullable PressureForgingRecipe currentRecipe(ServerLevel level) {
        ItemStack input = getItemStackHandler().getStackInSlot(0);
        if (input.isEmpty() || !isPressurised()) {
            return null;
        }

        return level.recipeAccess()
                .getRecipeFor(PressureForgingRecipe.Type.INSTANCE,
                        new PressureForgingRecipeInput(input, getPurity(), worldPosition.getY()), level)
                .map(RecipeHolder::value)
                .orElse(null);
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP);
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return SidedCapUtils.allInsert(0);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.progress = in.getIntOr("progress", 0);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putInt("progress", this.progress);
    }
}
