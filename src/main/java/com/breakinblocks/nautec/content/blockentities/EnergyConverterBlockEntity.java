package com.breakinblocks.nautec.content.blockentities;

import net.minecraft.world.level.block.Block;
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
    private static final String SENDING_KEY = "sending";

    private int sending;

    private final SimpleEnergyHandler feBuffer = new SimpleEnergyHandler(MAX_FE, MAX_FE, 0) {
        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    };

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
            transmitPower(connectedOutputs() > 0 ? sending : 0);
            return;
        }

        int energyToConvert = Math.min(FE_CONVERSION_RATE, feBuffer.getAmountAsInt());
        int sent = 0;
        if (energyToConvert > 0 && connectedOutputs() > 0) {
            sent = energyToConvert;
            feBuffer.set(feBuffer.getAmountAsInt() - energyToConvert);
        }
        transmitPower(sent);
        if (sent != sending) {
            sending = sent;
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public int getSending() {
        return sending;
    }

    public int getBeams() {
        return connectedOutputs();
    }

    public int getFeStored() {
        return feBuffer.getAmountAsInt();
    }

    public static int maxFe() {
        return MAX_FE;
    }

    @Override
    protected int outgoingPower(Direction direction) {
        return getPowerToTransfer() / Math.max(1, connectedOutputs());
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        feBuffer.serialize(out.child(FE_BUFFER_KEY));
        out.putInt(SENDING_KEY, sending);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        feBuffer.deserialize(in.childOrEmpty(FE_BUFFER_KEY));
        sending = in.getIntOr(SENDING_KEY, 0);
    }
}
