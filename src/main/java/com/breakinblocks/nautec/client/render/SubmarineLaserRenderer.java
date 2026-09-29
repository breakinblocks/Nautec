package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.entities.submarine.SubmarineModules;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class SubmarineLaserRenderer {
    private static final float BEAM_HALF_WIDTH = 0.24F;
    private static final float MUZZLE_FLARE_RADIUS = 0.3F;
    private static final float IMPACT_FLARE_RADIUS = 0.6F;

    private SubmarineLaserRenderer() {
    }

    public static void render(SubmarineEntity submarine, PoseStack poseStack, SubmitNodeCollector collector,
                              Vec3 cameraPos, float partialTick) {
        if (!submarine.isLaserActive()) {
            return;
        }

        float yaw = Mth.rotLerp(partialTick, submarine.yRotO, submarine.getYRot());
        float pitch = Mth.rotLerp(partialTick, submarine.xRotO, submarine.getXRot());
        Vec3 position = submarine.getPosition(partialTick);
        Vec3 forward = Vec3.directionFromRotation(pitch, yaw);

        for (int beam = 0; beam < 2; beam++) {
            boolean left = beam == 0;
            float length = submarine.getLaserLength(left);
            if (length <= 0F) {
                continue;
            }

            Vec3 muzzleWorld = SubmarineModules.laserMuzzle(position, forward, left);
            Vector3f muzzle = muzzleWorld.subtract(cameraPos).toVector3f();
            Vector3f end = muzzleWorld.add(forward.scale(length)).subtract(cameraPos).toVector3f();
            LaserBeamRenderer.submitBeam(poseStack, collector, muzzle, end, BEAM_HALF_WIDTH, LaserBeamRenderer.CYAN);
            LaserBeamRenderer.submitFlare(poseStack, collector, muzzle, MUZZLE_FLARE_RADIUS, LaserBeamRenderer.CYAN);
            if (length < NTConfig.submarineLaserRange - 0.01D) {
                LaserBeamRenderer.submitFlare(poseStack, collector, end, IMPACT_FLARE_RADIUS, LaserBeamRenderer.CYAN);
            }
        }
    }
}
