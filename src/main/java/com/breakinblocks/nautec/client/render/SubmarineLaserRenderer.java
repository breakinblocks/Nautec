package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.entities.submarine.SubmarineModules;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import com.breakinblocks.nautec.utils.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class SubmarineLaserRenderer {
    private static final float HALF_WIDTH_COLD = 0.14F;
    private static final float HALF_WIDTH_HOT = 0.34F;
    private static final float INTENSITY_COLD = 0.6F;
    private static final float INTENSITY_HOT = 1.0F;
    private static final float MUZZLE_FLARE_COLD = 0.2F;
    private static final float MUZZLE_FLARE_HOT = 0.42F;
    private static final float IMPACT_FLARE_COLD = 0.45F;
    private static final float IMPACT_FLARE_HOT = 0.9F;
    private static final float CHARGE_FLARE = 0.3F;

    private SubmarineLaserRenderer() {
    }

    public static void render(SubmarineEntity submarine, PoseStack poseStack, MultiBufferSource buffers,
                              Vec3 cameraPos, float partialTick) {
        if (!submarine.isLaserEngaged()) {
            return;
        }

        float yaw = Mth.rotLerp(partialTick, submarine.yRotO, submarine.getYRot());
        float pitch = Mth.rotLerp(partialTick, submarine.xRotO, submarine.getXRot());
        Vec3 position = submarine.getPosition(partialTick);
        Vec3 forward = Vec3.directionFromRotation(pitch, yaw);
        float firing = submarine.getLaserFiringTicks(partialTick);

        for (int beam = 0; beam < 2; beam++) {
            boolean left = beam == 0;
            Vec3 muzzleWorld = SubmarineModules.laserMuzzle(position, forward, left);
            Vector3f muzzle = muzzleWorld.subtract(cameraPos).toVector3f();

            if (firing < 0F) {
                float charge = Mth.clamp(1F + firing / Math.max(1, NTConfig.submarineLaserChargeTicks), 0F, 1F);
                int glow = ARGB.color(Math.round(charge * 255F), LaserBeamRenderer.CYAN);
                LaserBeamRenderer.submitFlare(poseStack, buffers, muzzle, CHARGE_FLARE * charge, glow, true);
                continue;
            }

            float length = submarine.getLaserLength(left);
            if (length <= 0F) {
                continue;
            }
            float ramp = SubmarineEntity.laserRamp(firing);
            int color = ARGB.color(Math.round(Mth.lerp(ramp, INTENSITY_COLD, INTENSITY_HOT) * 255F), LaserBeamRenderer.CYAN);
            Vector3f end = muzzleWorld.add(forward.scale(length)).subtract(cameraPos).toVector3f();
            LaserBeamRenderer.submitBeam(poseStack, buffers, muzzle, end, Mth.lerp(ramp, HALF_WIDTH_COLD, HALF_WIDTH_HOT), color, true);
            LaserBeamRenderer.submitFlare(poseStack, buffers, muzzle, Mth.lerp(ramp, MUZZLE_FLARE_COLD, MUZZLE_FLARE_HOT), color, true);
            if (length < NTConfig.submarineLaserRange - 0.01D) {
                LaserBeamRenderer.submitFlare(poseStack, buffers, end, Mth.lerp(ramp, IMPACT_FLARE_COLD, IMPACT_FLARE_HOT), color, true);
            }
        }
    }
}
