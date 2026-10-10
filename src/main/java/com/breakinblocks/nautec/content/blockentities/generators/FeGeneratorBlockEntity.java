package com.breakinblocks.nautec.content.blockentities.generators;

import com.breakinblocks.nautec.transfer.TransferCapabilities;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

import com.breakinblocks.nautec.transfer.energy.EnergyHandler;
import com.breakinblocks.nautec.transfer.energy.EnergyHandlerUtil;
import com.breakinblocks.nautec.transfer.energy.SimpleEnergyHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class FeGeneratorBlockEntity extends ContainerBlockEntity {
    private final SimpleEnergyHandler energy;
    private final List<BlockCapabilityCache<EnergyHandler, @Nullable Direction>> outputs = new ArrayList<>();
    private int output;

    protected FeGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int capacity) {
        super(type, pos, state);
        this.energy = new SimpleEnergyHandler(capacity, 0, Integer.MAX_VALUE) {
            @Override
            protected void onEnergyChanged(int previousAmount) {
                setChanged();
            }
        };
    }

    public EnergyHandler getEnergyOutput() {
        return energy;
    }

    public SimpleEnergyHandler getEnergyStorage() {
        return energy;
    }

    public int getOutput() {
        return output;
    }

    protected int space() {
        return energy.getCapacityAsInt() - energy.getAmountAsInt();
    }

    protected void generate(int amount) {
        this.output = Math.max(0, Math.min(amount, space()));
        if (output > 0) {
            energy.set(energy.getAmountAsInt() + output);
        }
    }

    protected abstract void serverTick(ServerLevel level);

    @Override
    public void commonTick() {
        super.commonTick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        this.output = 0;
        serverTick(serverLevel);
        push(serverLevel);
    }

    private void push(ServerLevel serverLevel) {
        if (outputs.isEmpty()) {
            for (Direction direction : Direction.values()) {
                outputs.add(BlockCapabilityCache.create(TransferCapabilities.Energy.BLOCK, serverLevel, worldPosition.relative(direction), direction.getOpposite()));
            }
        }
        if (energy.getAmountAsInt() <= 0) {
            return;
        }
        for (BlockCapabilityCache<EnergyHandler, @Nullable Direction> cache : outputs) {
            EnergyHandler target = cache.getCapability();
            if (target != null) {
                EnergyHandlerUtil.move(energy, target, energy.getAmountAsInt(), null);
                if (energy.getAmountAsInt() <= 0) {
                    return;
                }
            }
        }
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        energy.serialize(out.child("energy"));
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        energy.deserialize(in.childOrEmpty("energy"));
    }
}
