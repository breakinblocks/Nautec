package com.breakinblocks.nautec.content.blockentities;


import com.breakinblocks.nautec.api.blockentities.NTBlockEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AnchorBlockEntity extends NTBlockEntity {
    public AnchorBlockEntity(BlockPos pos, BlockState blockState) {
        super(NTBlockEntityTypes.ANCHOR.get(), pos, blockState);
    }
}
