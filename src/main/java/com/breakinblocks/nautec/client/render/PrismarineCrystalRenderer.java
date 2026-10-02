package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.Nautec;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public final class PrismarineCrystalRenderer {
    public static final Identifier TEXTURE = Nautec.rl("textures/entity/prismarine_crystal.png");

    public static final float MIN_Y = -2.7F;
    public static final float MAX_Y = 2.95F;
    public static final float RADIUS = 0.8F;
    public static final long TICK_WRAP = 2_400_000L;

    private static final int SHELL_COLOR = 0xFFFFFFFF;
    private static final int CORE_COLOR = 0xFF9AF4FF;
    private static final int HALO_COLOR = 0xB04CCBE0;

    private static final float FLOOR_Y = -2.98F;
    private static final float HALO_RADIUS = 1.5F;

    private static final Shard[] SHARDS = {
            new Shard(1.05F, -1.35F, 0.0F, 0.62F, 0.85F, 0.18F),
            new Shard(1.2F, 0.35F, 2.1F, -0.48F, 1.0F, -0.22F),
            new Shard(0.95F, 1.75F, 4.2F, 0.75F, 0.7F, 0.3F),
    };

    private PrismarineCrystalRenderer() {
    }

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, float ticks, long seed, float flash, boolean world) {
        float phase = (seed & 0xFFFF) / 65535F * Mth.TWO_PI;
        float bob = Mth.sin(ticks * 0.045F + phase) * 0.045F;
        float shellYaw = phase * Mth.RAD_TO_DEG + ticks * 0.22F;
        int shellColor = tint(SHELL_COLOR, flash);

        if (world) {
            submitHalo(poseStack, collector, ticks, phase);
        }

        poseStack.pushPose();
        poseStack.translate(0F, bob, 0F);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(shellYaw));
        JsonMesh.submitTranslucent(poseStack, collector, JsonMesh.PRISMARINE_CRYSTAL.part("shell"), NTRenderTypes.crystalShell(TEXTURE), shellColor, world);
        poseStack.popPose();

        JsonMesh.Part core = JsonMesh.PRISMARINE_CRYSTAL.part("core");
        poseStack.pushPose();
        poseStack.translate(core.origin().x(), core.origin().y(), core.origin().z());
        poseStack.mulPose(Axis.YP.rotationDegrees(-ticks * 1.1F - phase * Mth.RAD_TO_DEG));
        float pulse = 1F + Mth.sin(ticks * 0.11F + phase) * 0.04F;
        poseStack.scale(pulse, 1F, pulse);
        JsonMesh.submitTranslucent(poseStack, collector, core, NTRenderTypes.crystalCore(TEXTURE), tint(CORE_COLOR, flash), world);
        poseStack.popPose();

        JsonMesh.Part shard = JsonMesh.PRISMARINE_CRYSTAL.part("shard");
        for (Shard s : SHARDS) {
            float orbit = s.start + phase + ticks * 0.012F * Math.signum(s.spin);
            poseStack.pushPose();
            poseStack.translate(Mth.cos(orbit) * s.radius, s.height + Mth.sin(ticks * 0.06F + s.start) * 0.08F, Mth.sin(orbit) * s.radius);
            poseStack.mulPose(Axis.YP.rotation(ticks * 0.04F * s.spin));
            poseStack.mulPose(Axis.ZP.rotation(s.tilt));
            poseStack.scale(s.scale, s.scale, s.scale);
            JsonMesh.submitTranslucent(poseStack, collector, shard, NTRenderTypes.crystalShell(TEXTURE), shellColor, world);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    public static long seed(BlockPos pos) {
        return (pos.asLong() * 0x9E3779B97F4A7C15L) >>> 20;
    }

    public static void extents(Consumer<Vector3fc> output) {
        for (float x : new float[]{-RADIUS, RADIUS}) {
            for (float y : new float[]{MIN_Y, MAX_Y}) {
                for (float z : new float[]{-RADIUS, RADIUS}) {
                    output.accept(new Vector3f(x + 0.5F, y, z + 0.5F));
                }
            }
        }
    }

    public static int tint(int color, float flash) {
        if (flash <= 0F) {
            return color;
        }
        int r = (int) Mth.lerp(flash, (color >> 16) & 0xFF, 255);
        int g = (int) Mth.lerp(flash, (color >> 8) & 0xFF, 255);
        int b = (int) Mth.lerp(flash, color & 0xFF, 255);
        return (color & 0xFF000000) | r << 16 | g << 8 | b;
    }

    private static void submitHalo(PoseStack poseStack, SubmitNodeCollector collector, float ticks, float phase) {
        float strength = 0.8F + 0.2F * Mth.sin(ticks * 0.11F + phase);
        int color = ((int) (((HALO_COLOR >>> 24) & 0xFF) * strength) << 24) | (HALO_COLOR & 0xFFFFFF);
        ShaderPackOverlay.submit(poseStack, collector, NTRenderTypes.crystalHalo(), (pose, buffer) -> {
            glowVertex(buffer, pose, -HALO_RADIUS, FLOOR_Y, -HALO_RADIUS, 0F, 0F, color);
            glowVertex(buffer, pose, -HALO_RADIUS, FLOOR_Y, HALO_RADIUS, 0F, 1F, color);
            glowVertex(buffer, pose, HALO_RADIUS, FLOOR_Y, HALO_RADIUS, 1F, 1F, color);
            glowVertex(buffer, pose, HALO_RADIUS, FLOOR_Y, -HALO_RADIUS, 1F, 0F, color);
        });
    }

    private static void glowVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setColor(color);
    }

    private record Shard(float radius, float height, float start, float spin, float scale, float tilt) {
    }
}
