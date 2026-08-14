package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public class EnergyConverterBlockEntity extends LaserBlockEntity {
    private static final int FE_CONVERSION_RATE = 100;
    private static final int MAX_FE = 100000;
    private static final String FE_BUFFER_KEY = "fe_buffer";

    private final SimpleEnergyHandler feBuffer = new SimpleEnergyHandler(MAX_FE, MAX_FE, 0);

    public EnergyConverterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.ENERGY_CONVERTER.get(), blockPos, blockState);
    }

    public EnergyHandler getFeBuffer() {
        return feBuffer;
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of();
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of(Direction.values());
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public void commonTick() {
        super.commonTick();

        if (level.isClientSide()) {
            return;
        }

        int energyToConvert = Math.min(FE_CONVERSION_RATE, feBuffer.getAmountAsInt());
        if (energyToConvert > 0 && connectedOutputs() > 0) {
            transmitPower(energyToConvert);
            feBuffer.set(feBuffer.getAmountAsInt() - energyToConvert);
        } else {
            transmitPower(0);
        }
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        feBuffer.serialize(out.child(FE_BUFFER_KEY));
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        feBuffer.deserialize(in.childOrEmpty(FE_BUFFER_KEY));
    }
}
