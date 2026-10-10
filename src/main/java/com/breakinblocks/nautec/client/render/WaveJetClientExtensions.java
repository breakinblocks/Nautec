package com.breakinblocks.nautec.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public final class WaveJetClientExtensions implements IClientItemExtensions {
    public static final EnumProxy<HumanoidModel.ArmPose> ARM_POSE = new EnumProxy<>(
            HumanoidModel.ArmPose.class, true, (IArmPoseTransformer) WaveJetClientExtensions::poseArms);

    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        return ARM_POSE.getValue();
    }

    private static void poseArms(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        float swimming = Mth.clamp(entity.getSwimAmount(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false)), 0.0F, 1.0F);
        float pitch = Mth.lerp(swimming, -Mth.HALF_PI + model.head.xRot, -2.5F);
        model.rightArm.xRot = pitch;
        model.leftArm.xRot = pitch;
        model.rightArm.yRot = (model.head.yRot - 0.22F) * (1.0F - swimming);
        model.leftArm.yRot = (model.head.yRot + 0.22F) * (1.0F - swimming);
        model.rightArm.zRot = 0.15F * swimming;
        model.leftArm.zRot = -0.15F * swimming;
    }

    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm,
                                           ItemStack stack, float partialTick, float equipProcess, float swingProcess) {
        poseStack.translate(0.0F, -0.35F - equipProcess * 0.6F, -0.9F);
        return true;
    }
}
