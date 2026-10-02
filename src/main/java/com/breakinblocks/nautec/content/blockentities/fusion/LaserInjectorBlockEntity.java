package com.breakinblocks.nautec.content.blockentities.fusion;

import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.blocks.fusion.LaserInjectorBlock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class LaserInjectorBlockEntity extends LaserBlockEntity {
    public LaserInjectorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.LASER_INJECTOR.get(), blockPos, blockState);
    }

    public Direction getFacing() {
        return getBlockState().getValue(LaserInjectorBlock.FACING);
    }

    @Override
    public void commonTick() {
        super.commonTick();
        transmitPower(getPower());
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(getFacing().getOpposite());
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of(getFacing());
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }
}
