package com.breakinblocks.nautec.api.client.renderer.blockentities;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BERenderState {
    public BlockPos blockPos = BlockPos.ZERO;
    public BlockState blockState = Blocks.AIR.defaultBlockState();
    public int lightCoords = LightTexture.FULL_BRIGHT;
    public int overlayCoords = OverlayTexture.NO_OVERLAY;

    public static void extractBase(BlockEntity blockEntity, BERenderState state) {
        state.blockPos = blockEntity.getBlockPos();
        state.blockState = blockEntity.getBlockState();
        state.lightCoords = blockEntity.getLevel() != null ? LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos()) : LightTexture.FULL_BRIGHT;
    }
}
