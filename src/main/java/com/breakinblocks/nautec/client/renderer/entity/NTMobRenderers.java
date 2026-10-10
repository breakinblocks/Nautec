package com.breakinblocks.nautec.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.render.NTRenderTypes;
import com.breakinblocks.nautec.client.model.entity.AbyssalMawModel;
import com.breakinblocks.nautec.client.model.entity.LanternJellyModel;
import com.breakinblocks.nautec.client.model.entity.SiltSkipperModel;
import com.breakinblocks.nautec.client.model.entity.VentCrawlerModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

public final class NTMobRenderers {
    public static final ModelLayerLocation SILT_SKIPPER_LAYER = layer("silt_skipper");
    public static final ModelLayerLocation LANTERN_JELLY_LAYER = layer("lantern_jelly");
    public static final ModelLayerLocation VENT_CRAWLER_LAYER = layer("vent_crawler");
    public static final ModelLayerLocation ABYSSAL_MAW_LAYER = layer("abyssal_maw");

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(Nautec.rl(name), "main");
    }

    private static ResourceLocation texture(String name) {
        return Nautec.rl("textures/entity/" + name + ".png");
    }

    public static class SiltSkipperRenderer extends SimpleMobRenderer<Mob, SiltSkipperModel<Mob>> {
        public SiltSkipperRenderer(EntityRendererProvider.Context context) {
            super(context, new SiltSkipperModel<>(context.bakeLayer(SILT_SKIPPER_LAYER)), 0.3F, texture("silt_skipper"));
        }

        @Override
        protected void setupRotations(Mob entity, PoseStack poseStack, float bob, float bodyRot, float partialTick, float entityScale) {
            super.setupRotations(entity, poseStack, bob, bodyRot, partialTick, entityScale);
            poseStack.mulPose(Axis.YP.rotationDegrees(4.0F * Mth.sin(0.6F * bob)));
            if (!entity.isInWater()) {
                poseStack.translate(0.1F, 0.1F, -0.1F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }
        }
    }

    public static class LanternJellyRenderer extends SimpleMobRenderer<Mob, LanternJellyModel<Mob>> {
        public LanternJellyRenderer(EntityRendererProvider.Context context) {
            super(context, new LanternJellyModel<>(context.bakeLayer(LANTERN_JELLY_LAYER)), 0.5F, texture("lantern_jelly"));
            ResourceLocation glow = texture("lantern_jelly_glow");
            this.addLayer(new EmissiveGlowLayer<>(this, entity -> glow,
                    LanternJellyModel::glowBrightness, this.getModel(), NTRenderTypes::emissiveOverlay, false));
        }
    }

    public static class VentCrawlerRenderer extends SimpleMobRenderer<Mob, VentCrawlerModel<Mob>> {
        public VentCrawlerRenderer(EntityRendererProvider.Context context) {
            super(context, new VentCrawlerModel<>(context.bakeLayer(VENT_CRAWLER_LAYER)), 0.5F, texture("vent_crawler"));
            this.addLayer(new GlowOverlayLayer<>(this, texture("vent_crawler_glow")));
        }
    }

    public static class AbyssalMawRenderer extends SimpleMobRenderer<Mob, AbyssalMawModel<Mob>> {
        public AbyssalMawRenderer(EntityRendererProvider.Context context) {
            super(context, new AbyssalMawModel<>(context.bakeLayer(ABYSSAL_MAW_LAYER)), 0.8F, texture("abyssal_maw"));
            this.addLayer(new GlowOverlayLayer<>(this, texture("abyssal_maw_glow")));
        }
    }

    public static class SimpleMobRenderer<T extends Mob, M extends EntityModel<T>> extends MobRenderer<T, M> {
        private final ResourceLocation texture;

        public SimpleMobRenderer(EntityRendererProvider.Context context, M model, float shadowRadius, ResourceLocation texture) {
            super(context, model, shadowRadius);
            this.texture = texture;
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
            return this.texture;
        }
    }

    private NTMobRenderers() {
    }
}
