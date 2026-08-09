package com.breakinblocks.nautec.content.blocks;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BreakerBlock extends LaserBlock {
    public BreakerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean waterloggable() {
        return false;
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return null;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(BreakerBlock::new);
    }
}
