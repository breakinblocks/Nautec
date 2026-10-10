package com.breakinblocks.nautec.api.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface RemovalAware {
    void preRemoveSideEffects(BlockPos pos, BlockState state);
}
