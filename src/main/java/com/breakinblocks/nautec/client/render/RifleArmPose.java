package com.breakinblocks.nautec.client.render;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

public final class RifleArmPose {
    private static final float QUARTER_PI = (float) (Math.PI / 4);
    private static final float CROUCH_LIFT = 0.4F;

    public static final EnumProxy<HumanoidModel.ArmPose> RIFLE = new EnumProxy<>(
            HumanoidModel.ArmPose.class, true, true, (IArmPoseTransformer) RifleArmPose::apply);

    private static void apply(HumanoidModel<?> model, HumanoidRenderState state, HumanoidArm arm) {
        boolean rightHanded = arm == HumanoidArm.RIGHT;
        ModelPart shootingArm = rightHanded ? model.rightArm : model.leftArm;
        ModelPart supportArm = rightHanded ? model.leftArm : model.rightArm;
        ModelPart head = model.head;

        shootingArm.z++;
        shootingArm.yRot = head.yRot;
        shootingArm.xRot = (float) (-Math.PI / 2) + head.xRot + 0.1F;

        supportArm.yRot = (rightHanded ? 0.8F : -0.8F) + head.yRot;
        supportArm.xRot = -1.5F + head.xRot;
        supportArm.z -= 3.0F;

        float turn = -Math.min(head.yRot, QUARTER_PI) / QUARTER_PI;
        if (head.yRot > 0.0F) {
            supportArm.x += turn * 4.0F;
            supportArm.z += turn * 2.0F;
            shootingArm.z -= turn * 2.0F;
        } else {
            supportArm.z += turn * 4.0F;
        }

        if (state.isCrouching) {
            supportArm.xRot -= CROUCH_LIFT;
            shootingArm.xRot -= CROUCH_LIFT;
        }

        supportArm.y++;
        shootingArm.y += 2.0F;
    }

    private RifleArmPose() {
    }
}
