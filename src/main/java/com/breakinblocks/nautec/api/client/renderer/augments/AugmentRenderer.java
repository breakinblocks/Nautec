package com.breakinblocks.nautec.api.client.renderer.augments;

import com.mojang.blaze3d.vertex.PoseStack;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.client.renderer.augments.helper.AugmentLayerRenderer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;

public abstract class AugmentRenderer<T extends Augment> {
    public AugmentRenderer(Context ctx) {
    }

    public abstract void render(T augment, AugmentLayerRenderer<?, ?> superRenderer, PoseStack poseStack, MultiBufferSource buffers, int packedLight);

    public record Context(EntityModelSet entityModelSet) {
    }
}
