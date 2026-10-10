package com.breakinblocks.nautec.client.renderer.entity;

import com.breakinblocks.nautec.client.render.NTRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class EmissiveGeoLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    private final ResourceLocation texture;

    public EmissiveGeoLayer(GeoRenderer<T> renderer, ResourceLocation texture) {
        super(renderer);
        this.texture = texture;
    }

    protected boolean shouldRender(T animatable) {
        return !(animatable instanceof Entity entity) || !entity.isInvisible();
    }

    protected int color(T animatable, float partialTick, int packedLight) {
        return getRenderer().getRenderColor(animatable, partialTick, packedLight).argbInt();
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType, MultiBufferSource bufferSource,
                       @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!shouldRender(animatable)) {
            return;
        }
        RenderType glow = NTRenderTypes.emissiveOverlay(this.texture);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glow, bufferSource.getBuffer(glow), partialTick,
                LightTexture.FULL_BRIGHT, packedOverlay, color(animatable, partialTick, packedLight));
    }
}
