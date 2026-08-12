package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class FocusingLensBlockEntity extends LaserBlockEntity {
    public FocusingLensBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.FOCUSING_LENS.get(), blockPos, blockState);
    }

    public Direction getOutputDirection() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    @Override
    public void commonTick() {
        super.commonTick();

        transmitPower(getPower());
    }

    @Override
    protected float outgoingPurity(Direction direction) {
        return getPurity() > 0 ? getPurity() + (float) NTConfig.lensPurityBonus : 0f;
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(getOutputDirection().getOpposite());
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of(getOutputDirection());
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }
}
