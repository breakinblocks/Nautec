package com.breakinblocks.nautec.client.model.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public abstract class SimpleModel extends Model {
    protected final ModelPart root;

    protected SimpleModel(ModelPart root, Function<ResourceLocation, RenderType> renderType) {
        super(renderType);
        this.root = root;
    }

    public ModelPart root() {
        return this.root;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    public void submit(PoseStack poseStack, MultiBufferSource buffers, RenderType renderType, int packedLight, int packedOverlay) {
        renderToBuffer(poseStack, buffers.getBuffer(renderType), packedLight, packedOverlay, -1);
    }

    public static void renderPart(ModelPart part, PoseStack poseStack, MultiBufferSource buffers, RenderType renderType, int packedLight, int packedOverlay) {
        part.render(poseStack, buffers.getBuffer(renderType), packedLight, packedOverlay);
    }
}
