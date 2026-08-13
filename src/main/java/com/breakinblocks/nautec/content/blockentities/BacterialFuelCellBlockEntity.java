package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTSounds;
import com.breakinblocks.nautec.utils.MachineSounds;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class BacterialFuelCellBlockEntity extends LaserBlockEntity {
    private static final int BURN_PERIOD = 70;

    private float burnBuffer;

    public BacterialFuelCellBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.BACTERIAL_FUEL_CELL.get(), blockPos, blockState);
        addBacteriaStorage(1);
    }

    @Override
    public void commonTick() {
        super.commonTick();

        BacteriaInstance bacteria = getBacteriaStorage().getBacteria(0);
        if (bacteria.isEmpty()) {
            this.burnBuffer = 0;
            transmitPower(0);
            setPurity(0);
            return;
        }

        if (getLaserDistances().getInt(getOutputDirection()) <= 0) {
            transmitPower(0);
            setPurity(0);
            return;
        }

        transmitPower(powerOutput(bacteria));
        setPurity(purityOutput(bacteria));

        if (!level.isClientSide()) {
            burn(bacteria);
            MachineSounds.interval(level, worldPosition, NTSounds.FUEL_CELL_BURN, BURN_PERIOD, 0.4f, 0.8f);
        }
    }

    private void burn(BacteriaInstance bacteria) {
        this.burnBuffer += burnPerTick(bacteria);
        if (this.burnBuffer < 1f) {
            return;
        }

        long consumed = (long) this.burnBuffer;
        this.burnBuffer -= consumed;
        getBacteriaStorage().extractBacteria(0, consumed, false);
    }

    public static int powerOutput(BacteriaInstance bacteria) {
        if (bacteria.isEmpty()) {
            return 0;
        }
        return Math.max(1, Math.round(bacteria.getStats().productionRate() * NTConfig.fuelCellPowerBase));
    }

    public static float purityOutput(BacteriaInstance bacteria) {
        if (bacteria.isEmpty()) {
            return 0f;
        }
        float cap = (float) NTConfig.bacteriaMutationResistanceCap;
        float ratio = cap <= 0 ? 1f : Math.min(1f, bacteria.getStats().mutationResistance() / cap);
        return ratio * (float) NTConfig.fuelCellMaxPurity;
    }

    public static float burnPerTick(BacteriaInstance bacteria) {
        if (bacteria.isEmpty()) {
            return 0f;
        }
        return (float) (bacteria.getStats().productionRate() * NTConfig.fuelCellBurnRate);
    }

    public boolean isActive() {
        return !getBacteriaStorage().getBacteria(0).isEmpty();
    }

    public Direction getOutputDirection() {
        return getBlockState().getValue(BlockStateProperties.FACING).getOpposite();
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of();
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        if (isActive()) {
            return ObjectSet.of(getOutputDirection());
        }
        return Collections.emptySet();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.burnBuffer = in.getFloatOr("burn_buffer", 0f);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putFloat("burn_buffer", this.burnBuffer);
    }
}
