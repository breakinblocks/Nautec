package com.breakinblocks.nautec.client.renderer.items;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.resonantstorage.ResonantVaultItem;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import net.minecraft.resources.Identifier;

public class ResonantVaultItemRenderer extends GeoItemRenderer<ResonantVaultItem> {
    private static final Identifier GLOWMASK = Nautec.rl("textures/block/resonant_vault_glowmask.png");

    public ResonantVaultItemRenderer() {
        super(new DefaultedBlockGeoModel<>(Nautec.rl("resonant_vault")));
        withRenderLayer(new GlowLayer(this));
    }

    private static class GlowLayer extends AutoGlowingGeoLayer<ResonantVaultItem, GeoItemRenderer.RenderData, GeoRenderState> {
        GlowLayer(GeoItemRenderer<ResonantVaultItem> renderer) {
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
