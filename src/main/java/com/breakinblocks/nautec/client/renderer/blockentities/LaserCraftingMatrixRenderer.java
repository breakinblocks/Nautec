package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.LaserCraftingMatrixBlockEntity;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.resources.Identifier;

public class LaserCraftingMatrixRenderer extends GeoBlockRenderer<LaserCraftingMatrixBlockEntity, BlockEntityRenderState> {
    private static final Identifier GLOWMASK = Nautec.rl("textures/block/laser_crafting_matrix_glowmask.png");

    public LaserCraftingMatrixRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new DefaultedBlockGeoModel<>(Nautec.rl("laser_crafting_matrix")));
        withRenderLayer(new GlowLayer(this));
    }

    @Override
    public void addRenderData(LaserCraftingMatrixBlockEntity matrix, Void relatedObject, BlockEntityRenderState renderState, float partialTick) {
        super.addRenderData(matrix, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(LaserCraftingMatrixBlockEntity.WORKING, matrix.isRunning());
    }

    private static class GlowLayer extends AutoGlowingGeoLayer<LaserCraftingMatrixBlockEntity, Void, BlockEntityRenderState> {
        GlowLayer(GeoRenderer<LaserCraftingMatrixBlockEntity, Void, BlockEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        protected Identifier getTextureResource(BlockEntityRenderState state) {
            return GLOWMASK;
        }

        @Override
        protected boolean shouldAddZOffset(BlockEntityRenderState state) {
            return true;
        }
    }
}
