package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTSounds;
import com.breakinblocks.nautec.utils.MachineSounds;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class BeamSplitterBlockEntity extends LaserBlockEntity {
    private static final int HUM_PERIOD = 100;

    public BeamSplitterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.BEAM_SPLITTER.get(), blockPos, blockState);
    }

    public Direction getInputDirection() {
        return getBlockState().getValue(BlockStateProperties.FACING).getOpposite();
    }

    @Override
    public void commonTick() {
        super.commonTick();

        transmitPower(getPower());

        if (getPower() > 0) {
            MachineSounds.interval(level, worldPosition, NTSounds.OPTICS_HUM, HUM_PERIOD, 0.3f, 1.5f);
        }
    }

    @Override
    protected int outgoingPower(Direction direction) {
        return getPower() / Math.max(1, connectedOutputs());
    }

    @Override
    protected float outgoingPurity(Direction direction) {
        return (float) (getPurity() * NTConfig.splitterPurityFactor);
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(getInputDirection());
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        Direction axis = getInputDirection();
        Set<Direction> outputs = new ObjectOpenHashSet<>();
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != axis.getAxis()) {
                outputs.add(direction);
            }
        }
        return outputs;
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }
}
