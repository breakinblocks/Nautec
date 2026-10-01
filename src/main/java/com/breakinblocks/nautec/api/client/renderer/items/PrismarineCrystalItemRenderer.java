package com.breakinblocks.nautec.api.client.renderer.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.breakinblocks.nautec.client.render.PrismarineCrystalRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.util.Util;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public class PrismarineCrystalItemRenderer implements NoDataSpecialModelRenderer {
    private static final float ITEM_LIFT = 2.0F;

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int packedOverlay, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5F, ITEM_LIFT, 0.5F);
        float ticks = (Util.getMillis() / 50L % PrismarineCrystalRenderer.TICK_WRAP) + (Util.getMillis() % 50L) / 50F;
        PrismarineCrystalRenderer.submit(poseStack, collector, ticks, 0L, 0F, false);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PrismarineCrystalRenderer.extents(corner -> output.accept(new Vector3f(corner).add(0F, ITEM_LIFT, 0F)));
    }

    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public PrismarineCrystalItemRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new PrismarineCrystalItemRenderer();
        }
    }
}
