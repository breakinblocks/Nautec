package com.breakinblocks.nautec.client.model.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class AbyssalMawModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart lowerJaw;
    private final ModelPart tail;
    private final ModelPart lureRod;
    private final ModelPart tailTip;
    private final ModelPart leftFin;
    private final ModelPart rightFin;

    public AbyssalMawModel(ModelPart root) {
        super(root);
        this.lowerJaw = root.getChild("lower_jaw");
        this.tail = root.getChild("tail");
        this.lureRod = root.getChild("lure_rod");
        this.tailTip = this.tail.getChild("tail_tip");
        this.leftFin = root.getChild("left_fin");
        this.rightFin = root.getChild("right_fin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-4.5F, -4.0F, -4.0F, 9.0F, 9.0F, 9.0F)
                .texOffs(38, 1).addBox(-5.0F, -3.0F, -5.0F, 10.0F, 7.0F, 5.0F)
                .texOffs(10, 20).addBox(-3.5F, -5.0F, -3.0F, 7.0F, 2.0F, 6.0F)
                .texOffs(69, 1).addBox(-3.5F, 3.5F, -4.1F, 7.0F, 2.0F, 8.0F)
                .texOffs(100, 1).addBox(-3.0F, -2.0F, 4.0F, 6.0F, 5.0F, 4.0F)
                .texOffs(1, 30).addBox(-3.45F, -1.05F, -5.15F, 7.0F, 5.0F, 1.0F)
                .texOffs(37, 20).addBox(-5.1F, -2.0F, -6.5F, 2.0F, 5.0F, 3.0F)
                .texOffs(118, 30).addBox(-5.2F, -3.1F, -5.6F, 1.0F, 2.0F, 2.0F)
                .texOffs(36, 37).addBox(-5.5F, -2.6F, -5.7F, 1.0F, 1.0F, 1.0F)
                .texOffs(1, 37).addBox(-5.25F, -1.0F, -0.9F, 1.0F, 3.0F, 1.0F)
                .texOffs(1, 37).addBox(-5.25F, -1.0F, 1.1F, 1.0F, 3.0F, 1.0F)
                .texOffs(1, 37).addBox(-5.25F, -1.0F, 3.1F, 1.0F, 3.0F, 1.0F)
                .texOffs(37, 20).addBox(3.1F, -2.0F, -6.5F, 2.0F, 5.0F, 3.0F)
                .texOffs(118, 30).addBox(4.2F, -3.1F, -5.6F, 1.0F, 2.0F, 2.0F)
                .texOffs(36, 37).addBox(4.5F, -2.6F, -5.7F, 1.0F, 1.0F, 1.0F)
                .texOffs(1, 37).addBox(4.25F, -1.0F, -0.9F, 1.0F, 3.0F, 1.0F)
                .texOffs(1, 37).addBox(4.25F, -1.0F, 1.1F, 1.0F, 3.0F, 1.0F)
                .texOffs(1, 37).addBox(4.25F, -1.0F, 3.1F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        root.addOrReplaceChild("upper_jaw", CubeListBuilder.create()
                .texOffs(94, 20).addBox(-4.0F, -1.5F, -4.0F, 8.0F, 2.0F, 5.0F)
                .texOffs(41, 37).addBox(-3.95F, 0.1F, -4.1F, 8.0F, 1.0F, 1.0F)
                .texOffs(31, 37).addBox(-3.5F, 0.8F, -3.7F, 1.0F, 2.0F, 1.0F)
                .texOffs(6, 37).addBox(-1.5F, 0.8F, -3.7F, 1.0F, 3.0F, 1.0F)
                .texOffs(6, 37).addBox(0.5F, 0.8F, -3.7F, 1.0F, 3.0F, 1.0F)
                .texOffs(31, 37).addBox(2.5F, 0.8F, -3.7F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 14.0F, -5.0F));

        root.addOrReplaceChild("lower_jaw", CubeListBuilder.create()
                .texOffs(48, 20).addBox(-4.0F, 0.0F, -5.0F, 8.0F, 2.0F, 6.0F)
                .texOffs(68, 30).addBox(-3.0F, 1.3F, -4.0F, 6.0F, 1.0F, 4.0F)
                .texOffs(18, 30).addBox(-3.5F, -0.3F, -4.8F, 7.0F, 1.0F, 5.0F)
                .texOffs(41, 37).addBox(-4.05F, -0.6F, -5.1F, 8.0F, 1.0F, 1.0F)
                .texOffs(31, 37).addBox(-2.5F, -2.2F, -4.6F, 1.0F, 2.0F, 1.0F)
                .texOffs(31, 37).addBox(-0.5F, -2.2F, -4.6F, 1.0F, 2.0F, 1.0F)
                .texOffs(31, 37).addBox(1.5F, -2.2F, -4.6F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 19.1F, -4.5F));

        PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(77, 20).addBox(-2.0F, -2.0F, -0.5F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, 17.1F, 6.5F));

        tail.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                .texOffs(43, 30).addBox(-1.0F, -1.5F, -0.5F, 2.0F, 3.0F, 3.0F)
                .texOffs(1, 20).addBox(-0.5F, -3.0F, 1.5F, 1.0F, 6.0F, 3.0F)
                .texOffs(11, 37).addBox(-0.5F, -4.0F, 4.5F, 1.0F, 2.0F, 2.0F)
                .texOffs(11, 37).addBox(-0.5F, 2.0F, 4.5F, 1.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -0.1F, 3.0F));

        root.addOrReplaceChild("left_fin", CubeListBuilder.create()
                .texOffs(18, 37).addBox(0.0F, -0.5F, -1.0F, 3.0F, 1.0F, 3.0F)
                .texOffs(89, 30).addBox(2.0F, -0.4F, 0.0F, 3.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(4.0F, 18.0F, -1.0F, -0.1745F, -0.2618F, 0.3491F));

        root.addOrReplaceChild("right_fin", CubeListBuilder.create()
                .texOffs(18, 37).addBox(-3.0F, -0.5F, -1.0F, 3.0F, 1.0F, 3.0F)
                .texOffs(89, 30).addBox(-5.0F, -0.4F, 0.0F, 3.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(-4.0F, 18.0F, -1.0F, -0.1745F, 0.2618F, -0.3491F));

        root.addOrReplaceChild("dorsal_0", CubeListBuilder.create()
                .texOffs(54, 30).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 11.5F, 0.0F));

        root.addOrReplaceChild("dorsal_1", CubeListBuilder.create()
                .texOffs(54, 30).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 12.1F, 3.0F));

        root.addOrReplaceChild("dorsal_2", CubeListBuilder.create()
                .texOffs(54, 30).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 12.7F, 6.0F));

        PartDefinition lure_rod = root.addOrReplaceChild("lure_rod", CubeListBuilder.create()
                .texOffs(63, 30).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 5.0F, 1.0F),
                PartPose.offset(0.0F, 12.0F, -4.0F));

        PartDefinition lure_arch = lure_rod.addOrReplaceChild("lure_arch", CubeListBuilder.create()
                .texOffs(104, 30).addBox(-0.38F, -3.5F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -3.5F, 0.0F, 0.6109F, 0.0F, 0.0F));

        PartDefinition lure_hook = lure_arch.addOrReplaceChild("lure_hook", CubeListBuilder.create()
                .texOffs(104, 30).addBox(-0.62F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 1.1345F, 0.0F, 0.0F));

        lure_hook.addOrReplaceChild("lure_bulb", CubeListBuilder.create()
                .texOffs(109, 30).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, -2.5F, 0.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        this.tail.yRot = -0.45F * Mth.sin(0.2F * state.ageInTicks);
        this.tailTip.yRot = -0.35F * Mth.sin(0.2F * state.ageInTicks - 0.6F);
        this.lowerJaw.xRot = 0.16F + 0.16F * Mth.sin(0.09F * state.ageInTicks);
        float sway = 0.22F * Mth.sin(0.13F * state.ageInTicks);
        this.lureRod.xRot = sway;
        this.leftFin.zRot += 0.12F * Mth.sin(0.13F * state.ageInTicks);
        this.rightFin.zRot -= 0.12F * Mth.sin(0.13F * state.ageInTicks);
    }
}
