package com.breakinblocks.nautec.client.renderer.entity;

import com.breakinblocks.nautec.utils.ARGB;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class EmissiveGlowLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final Function<T, ResourceLocation> textureProvider;
    private final AlphaFunction<? super T> alphaFunction;
    private final M model;
    private final Function<ResourceLocation, RenderType> bufferProvider;
    private final boolean alwaysVisible;

    public EmissiveGlowLayer(RenderLayerParent<T, M> renderer, Function<T, ResourceLocation> textureProvider, AlphaFunction<? super T> alphaFunction,
                             M model, Function<ResourceLocation, RenderType> bufferProvider, boolean alwaysVisible) {
        super(renderer);
        this.textureProvider = textureProvider;
        this.alphaFunction = alphaFunction;
        this.model = model;
        this.bufferProvider = bufferProvider;
        this.alwaysVisible = alwaysVisible;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, T entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible() && !this.alwaysVisible) {
            return;
        }
        float alpha = this.alphaFunction.apply(entity, ageInTicks);
        if (alpha <= 1.0E-5F) {
            return;
        }
        RenderType renderType = this.bufferProvider.apply(this.textureProvider.apply(entity));
        this.model.renderToBuffer(poseStack, buffers.getBuffer(renderType), packedLight,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F), ARGB.white(alpha));
    }

    @FunctionalInterface
    public interface AlphaFunction<T> {
        float apply(T entity, float ageInTicks);
    }
}
