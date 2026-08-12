package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PrismaticMirrorBlock extends OpticsBlock {
    public PrismaticMirrorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.PRISMATIC_MIRROR.get();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(PrismaticMirrorBlock::new);
    }
}
