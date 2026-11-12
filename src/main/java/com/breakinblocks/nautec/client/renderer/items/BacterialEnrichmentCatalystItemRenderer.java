package com.breakinblocks.nautec.client.renderer.items;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.items.blocks.BacterialEnrichmentCatalystItem;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import net.minecraft.resources.Identifier;

public class BacterialEnrichmentCatalystItemRenderer extends GeoItemRenderer<BacterialEnrichmentCatalystItem> {
    private static final Identifier GLOWMASK = Nautec.rl("textures/block/bacterial_enrichment_catalyst_glowmask.png");

    public BacterialEnrichmentCatalystItemRenderer() {
        super(new DefaultedBlockGeoModel<>(Nautec.rl("bacterial_enrichment_catalyst")));
        withRenderLayer(new GlowLayer(this));
    }

    private static class GlowLayer extends AutoGlowingGeoLayer<BacterialEnrichmentCatalystItem, GeoItemRenderer.RenderData, GeoRenderState> {
        GlowLayer(GeoItemRenderer<BacterialEnrichmentCatalystItem> renderer) {
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
