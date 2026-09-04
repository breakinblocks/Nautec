package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.content.items.AtlanteanRifleItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public final class AtlanteanRifleBeamRenderer {
    private static final int CORE_COLD = 0xFFCAD6E0;
    private static final int CORE_HOT = 0xFFE49CFF;
    private static final int GLOW_COLD = 0xFF5E85A4;
    private static final int GLOW_HOT = 0xFF9A3CFF;
    private static final float CORE_RADIUS_COLD = 0.035F;
    private static final float CORE_RADIUS_HOT = 0.065F;
    private static final float GLOW_RADIUS_COLD = 0.07F;
    private static final float GLOW_RADIUS_HOT = 0.13F;
    private static final int GLOW_ALPHA_COLD = 48;
    private static final int GLOW_ALPHA_HOT = 110;
    private static final double MIN_LENGTH = 0.05D;

    private static final double FALLBACK_FORWARD = 0.7D;
    private static final double FALLBACK_SIDE = 0.28D;
    private static final double FALLBACK_DOWN = 0.36D;
    private static final long SAMPLE_LIFETIME_NANOS = 100_000_000L;

    private record MuzzleSample(Vec3 worldPos, long capturedAt) {
    }

    private static final Map<Integer, MuzzleSample> MUZZLES = new HashMap<>();

    private AtlanteanRifleBeamRenderer() {
    }

    public static void trackMuzzle(int ownerId, Vec3 worldPos) {
        MUZZLES.put(ownerId, new MuzzleSample(worldPos, System.nanoTime()));
    }

    public static Vec3 muzzleWorld(Player player) {
        MuzzleSample sample = MUZZLES.get(player.getId());
        if (sample != null && System.nanoTime() - sample.capturedAt() <= SAMPLE_LIFETIME_NANOS) {
            return sample.worldPos();
        }
        Vec3 look = player.getLookAngle();
        Vec3 right = look.cross(new Vec3(0D, 1D, 0D)).normalize();
        Vec3 up = right.cross(look).normalize();
        double side = player.getMainArm() == HumanoidArm.LEFT ? -1D : 1D;
        return player.getEyePosition()
                .add(look.scale(FALLBACK_FORWARD))
                .add(right.scale(FALLBACK_SIDE * side))
                .subtract(up.scale(FALLBACK_DOWN));
    }

    public static void forget() {
        MUZZLES.clear();
    }

    public static void submitBeam(PoseStack poseStack, SubmitNodeCollector collector, Vec3 from, Vec3 to,
                                  float radiusScale, float firing, long gameTime, float partialTick) {
        Vec3 delta = to.subtract(from);
        float length = (float) delta.length();
        if (length < MIN_LENGTH) {
            return;
        }
        Vec3 direction = delta.normalize();

        float ramp = AtlanteanRifleItem.rampProgress(firing);
        int core = ARGB.srgbLerp(ramp, CORE_COLD, CORE_HOT);
        int glow = ARGB.color(Math.round(Mth.lerp(ramp, GLOW_ALPHA_COLD, GLOW_ALPHA_HOT)), ARGB.srgbLerp(ramp, GLOW_COLD, GLOW_HOT));
        float coreRadius = Mth.lerp(ramp, CORE_RADIUS_COLD, CORE_RADIUS_HOT) * radiusScale;
        float glowRadius = Mth.lerp(ramp, GLOW_RADIUS_COLD, GLOW_RADIUS_HOT) * radiusScale;
        float worldLength = length / radiusScale;

        float scroll = LaserRendererHelper.beamScroll(gameTime, partialTick);
        float spin = LaserRendererHelper.beamSpin(gameTime, partialTick);
        float coreV1 = -1.0F + scroll;
        float coreV0 = worldLength * (0.5F / (coreRadius / radiusScale)) + coreV1;
        float glowV0 = worldLength + coreV1;

        float pitch = (float) Math.toDegrees(Math.acos(Mth.clamp(direction.y, -1D, 1D)));
        float yaw = (float) Math.toDegrees(Math.PI / 2 - Math.atan2(direction.z, direction.x));

        poseStack.pushPose();
        poseStack.translate(from.x, from.y, from.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        collector.submitCustomGeometry(poseStack, RenderTypes.beaconBeam(LaserRendererHelper.BEAM_LOCATION, false),
                (pose, buffer) -> LaserRendererHelper.beamColumnAlongY(pose, buffer, core,
                        0F, length, coreRadius, coreV0, coreV1));
        poseStack.popPose();

        collector.submitCustomGeometry(poseStack, RenderTypes.beaconBeam(LaserRendererHelper.BEAM_LOCATION, true),
                (pose, buffer) -> LaserRendererHelper.beamColumnAlongY(pose, buffer, glow,
                        0F, length, glowRadius, glowV0, coreV1));

        poseStack.popPose();
    }
}
