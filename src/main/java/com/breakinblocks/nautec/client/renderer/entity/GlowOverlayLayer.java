package com.breakinblocks.nautec.client.renderer.entity;

import com.breakinblocks.nautec.client.render.NTRenderTypes;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public class GlowOverlayLayer<M extends EntityModel<LivingEntityRenderState>> extends EyesLayer<LivingEntityRenderState, M> {
    private final RenderType renderType;

    public GlowOverlayLayer(RenderLayerParent<LivingEntityRenderState, M> renderer, Identifier texture) {
        super(renderer);
        this.renderType = NTRenderTypes.eyesOverlay(texture);
    }

    @Override
    public RenderType renderType() {
        return this.renderType;
    }
}
