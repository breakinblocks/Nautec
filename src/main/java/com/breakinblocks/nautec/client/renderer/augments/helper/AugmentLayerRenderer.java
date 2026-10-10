package com.breakinblocks.nautec.client.renderer.augments.helper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentType;
import com.breakinblocks.nautec.api.client.renderer.augments.AugmentRenderer;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import com.breakinblocks.nautec.client.AugmentClientHelper;

public class AugmentLayerRenderer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final Object2ObjectMap<AugmentType<?>, AugmentRendererProvider<?>> RENDERER_PROVIDERS = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<AugmentType<?>, AugmentRenderer<?>> RENDERERS = new Object2ObjectOpenHashMap<>();

    public AugmentLayerRenderer(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int packedLight, @NotNull T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        Iterable<Augment> augments = AugmentClientHelper.forEntity(entity).values();
        for (Augment augment : augments) {
            if (augment != null) {
                poseStack.pushPose();
                {
                    renderAugmentModel(poseStack, buffers, packedLight, augment);
                }
                poseStack.popPose();
            }
        }
    }

    private void renderAugmentModel(PoseStack poseStack, MultiBufferSource buffers, int packedLight, Augment augment) {
        AugmentRenderer<Augment> renderer = getRenderer(augment);
        if (renderer != null) {
            renderer.render(augment, this, poseStack, buffers, packedLight);
        }
    }

    @SuppressWarnings("unchecked")
    private static AugmentRenderer<Augment> getRenderer(Augment augment) {
        return (AugmentRenderer<Augment>) RENDERERS.get(augment.getAugmentType());
    }

    public static <T extends Augment> void registerRenderer(AugmentType<T> augmentType, AugmentRendererProvider<T> augmentRenderer) {
        RENDERER_PROVIDERS.put(augmentType, augmentRenderer);
    }

    public static void createRenderers() {
        AugmentRenderer.Context ctx = new AugmentRenderer.Context(Minecraft.getInstance().getEntityModels());
        ObjectSet<AugmentType<?>> augmentTypes = RENDERER_PROVIDERS.keySet();
        for (AugmentType<?> key : augmentTypes) {
            RENDERERS.put(key, RENDERER_PROVIDERS.get(key).create(ctx));
        }
    }

    @FunctionalInterface
    public interface AugmentRendererProvider<T extends Augment> {
        AugmentRenderer<T> create(AugmentRenderer.Context ctx);
    }
}
