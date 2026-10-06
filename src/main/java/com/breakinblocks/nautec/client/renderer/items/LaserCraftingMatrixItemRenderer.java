package com.breakinblocks.nautec.client.renderer.items;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.items.blocks.LaserCraftingMatrixItem;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import net.minecraft.resources.Identifier;

public class LaserCraftingMatrixItemRenderer extends GeoItemRenderer<LaserCraftingMatrixItem> {
    private static final Identifier GLOWMASK = Nautec.rl("textures/block/laser_crafting_matrix_glowmask.png");

    public LaserCraftingMatrixItemRenderer() {
        super(new DefaultedBlockGeoModel<>(Nautec.rl("laser_crafting_matrix")));
        withRenderLayer(new GlowLayer(this));
    }

    private static class GlowLayer extends AutoGlowingGeoLayer<LaserCraftingMatrixItem, GeoItemRenderer.RenderData, GeoRenderState> {
        GlowLayer(GeoItemRenderer<LaserCraftingMatrixItem> renderer) {
            super(renderer);
        }

        @Override
        protected Identifier getTextureResource(GeoRenderState state) {
            return GLOWMASK;
        }

        @Override
        protected boolean shouldAddZOffset(GeoRenderState state) {
            return true;
        }
    }
}
