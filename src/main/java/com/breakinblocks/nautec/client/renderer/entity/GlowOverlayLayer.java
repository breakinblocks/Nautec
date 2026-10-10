package com.breakinblocks.nautec.client.renderer.entity;

import com.breakinblocks.nautec.client.render.NTRenderTypes;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class GlowOverlayLayer<T extends Entity, M extends EntityModel<T>> extends EyesLayer<T, M> {
    private final RenderType renderType;

    public GlowOverlayLayer(RenderLayerParent<T, M> renderer, ResourceLocation texture) {
        super(renderer);
        this.renderType = NTRenderTypes.eyesOverlay(texture);
    }

    @Override
    public RenderType renderType() {
        return this.renderType;
    }
}
