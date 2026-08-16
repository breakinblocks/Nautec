package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.items.WaveJetSpotlight;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class WaveJetLightRenderer {
    private static final int SEGMENTS = 14;
    private static final int CORE_COLOR = 0xBFE8FF;

    private static final float NEAR_DISTANCE = 0.55F;
    private static final float NEAR_RADIUS = 0.12F;
    private static final float SPREAD = 0.22F;

    private static final int NEAR_ALPHA = 70;
    private static final int FAR_ALPHA = 0;

    public static void render(PoseStack poseStack, SubmitNodeCollector collector, Vec3 cameraPos, float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        for (Player player : level.players()) {
            if (WaveJetSpotlight.litWaveJet(player) != null) {
                renderCone(level, player, poseStack, collector, cameraPos, partialTick);
            }
        }
    }

    private static void renderCone(ClientLevel level, Player player, PoseStack poseStack,
                                   SubmitNodeCollector collector, Vec3 cameraPos, float partialTick) {
        Vec3 origin = muzzle(player, partialTick);
        Vec3 direction = player.getViewVector(partialTick);
        float length = reach(level, player, origin, direction);
        if (length <= NEAR_DISTANCE) {
            return;
        }

        Vec3 side = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 1.0E-4D) {
            side = direction.cross(new Vec3(1.0D, 0.0D, 0.0D));
        }
        side = side.normalize();
        Vec3 up = side.cross(direction).normalize();

        Vec3 nearCentre = origin.add(direction.scale(NEAR_DISTANCE)).subtract(cameraPos);
        Vec3 farCentre = origin.add(direction.scale(length)).subtract(cameraPos);
        float farRadius = NEAR_RADIUS + (length - NEAR_DISTANCE) * SPREAD;

        int nearColor = ARGB.color(NEAR_ALPHA, CORE_COLOR);
        int farColor = ARGB.color(FAR_ALPHA, CORE_COLOR);
        Vec3 axisSide = side;
        Vec3 axisUp = up;

        collector.submitCustomGeometry(poseStack, NTRenderTypes.spotlightCone(), (pose, buffer) -> {
            for (int segment = 0; segment < SEGMENTS; segment++) {
                float from = (float) (Math.PI * 2.0 * segment / SEGMENTS);
                float to = (float) (Math.PI * 2.0 * (segment + 1) / SEGMENTS);

                Vec3 nearFrom = ring(nearCentre, axisSide, axisUp, from, NEAR_RADIUS);
                Vec3 nearTo = ring(nearCentre, axisSide, axisUp, to, NEAR_RADIUS);
                Vec3 farFrom = ring(farCentre, axisSide, axisUp, from, farRadius);
                Vec3 farTo = ring(farCentre, axisSide, axisUp, to, farRadius);

                vertex(pose, buffer, nearFrom, nearColor);
                vertex(pose, buffer, nearTo, nearColor);
                vertex(pose, buffer, farTo, farColor);
                vertex(pose, buffer, farFrom, farColor);
            }
        });
    }

    private static Vec3 ring(Vec3 centre, Vec3 side, Vec3 up, float angle, float radius) {
        return centre
                .add(side.scale(Mth.cos(angle) * radius))
                .add(up.scale(Mth.sin(angle) * radius));
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, Vec3 position, int color) {
        buffer.addVertex(pose, (float) position.x, (float) position.y, (float) position.z).setColor(color);
    }

    private static Vec3 muzzle(Player player, float partialTick) {
        Vec3 eyes = player.getEyePosition(partialTick);
        Vec3 view = player.getViewVector(partialTick);
        Vec3 side = view.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 1.0E-4D) {
            return eyes.subtract(0.0D, 0.15D, 0.0D);
        }
        double offset = player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT ? 0.25D : -0.25D;
        return eyes.add(side.normalize().scale(offset)).subtract(0.0D, 0.2D, 0.0D);
    }

    private static float reach(ClientLevel level, Player player, Vec3 origin, Vec3 direction) {
        Vec3 end = origin.add(direction.scale(NTConfig.waveJetLightRange));
        BlockHitResult hit = level.clip(new ClipContext(origin, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.BLOCK
                ? (float) origin.distanceTo(hit.getLocation())
                : NTConfig.waveJetLightRange;
    }

    private WaveJetLightRenderer() {
    }
}
