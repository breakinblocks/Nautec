package com.breakinblocks.nautec.content.blockentities;


import com.breakinblocks.nautec.api.blockentities.NTBlockEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DecorativePrismarineCrystalBlockEntity extends NTBlockEntity {
    public DecorativePrismarineCrystalBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.DECORATIVE_PRISMARINE_CRYSTAL.get(), blockPos, blockState);
    }
}