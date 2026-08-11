package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.utils.LaserRendererHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class SubmarineLaserRenderer {
    private static final int BEAM_COLOR = 0xFFFF6A50;
    private static final float BEAM_RADIUS = 0.09F;
    private static final float GLOW_RADIUS = 0.16F;
    private static final double MUZZLE_SIDE = 1.0D;
    private static final double MUZZLE_FORWARD = 2.0D;
    private static final double MUZZLE_UP = 0.3D;

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
        long gameTime = submarine.level().getGameTime();

        float scroll = LaserRendererHelper.beamScroll(gameTime, partialTick);
        float spin = LaserRendererHelper.beamSpin(gameTime, partialTick);

        for (int beam = 0; beam < 2; beam++) {
            float length = submarine.getLaserLength(beam == 0);
            if (length <= 0F) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(position.x - cameraPos.x, position.y - cameraPos.y, position.z - cameraPos.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(180F - yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
            poseStack.translate(beam == 0 ? MUZZLE_SIDE : -MUZZLE_SIDE, MUZZLE_UP, MUZZLE_FORWARD);
            poseStack.mulPose(Axis.XP.rotationDegrees(90F));

            float coreV1 = -1.0F + scroll;
            float coreV0 = length * (0.5F / BEAM_RADIUS) + coreV1;
            float glowV0 = length + coreV1;

            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(spin));
            collector.submitCustomGeometry(poseStack, RenderTypes.beaconBeam(LaserRendererHelper.BEAM_LOCATION, false),
                    (pose, buffer) -> LaserRendererHelper.beamColumnAlongY(pose, buffer, BEAM_COLOR,
                            0F, length, BEAM_RADIUS, coreV0, coreV1));
            poseStack.popPose();

            collector.submitCustomGeometry(poseStack, RenderTypes.beaconBeam(LaserRendererHelper.BEAM_LOCATION, true),
                    (pose, buffer) -> LaserRendererHelper.beamColumnAlongY(pose, buffer, ARGB.color(48, BEAM_COLOR),
                            0F, length, GLOW_RADIUS, glowV0, coreV1));

            poseStack.popPose();
        }
    }
}
