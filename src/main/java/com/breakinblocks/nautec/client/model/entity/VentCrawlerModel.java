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

public class VentCrawlerModel extends EntityModel<LivingEntityRenderState> {
    private static final int LEG_COUNT = 6;

    private final ModelPart[] legs = new ModelPart[LEG_COUNT];
    private final ModelPart head;
    private final ModelPart leftClaw;
    private final ModelPart rightClaw;
    private final ModelPart leftPincer;
    private final ModelPart rightPincer;

    public VentCrawlerModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.leftClaw = root.getChild("left_claw");
        this.rightClaw = root.getChild("right_claw");
        this.leftPincer = this.leftClaw.getChild("left_pincer");
        this.rightPincer = this.rightClaw.getChild("right_pincer");
        for (int i = 0; i < LEG_COUNT; i++) {
            this.legs[i] = root.getChild("leg_" + i);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("shell", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-5.0F, -2.0F, -5.0F, 10.0F, 3.0F, 10.0F)
                .texOffs(42, 1).addBox(-4.0F, -3.5F, -4.0F, 8.0F, 2.0F, 8.0F)
                .texOffs(44, 15).addBox(-3.5F, -0.8F, 4.0F, 7.0F, 2.0F, 3.0F)
                .texOffs(75, 1).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 2.0F, 8.0F)
                .texOffs(103, 15).addBox(-2.0F, -4.0F, -4.1F, 4.0F, 1.0F, 2.0F)
                .texOffs(103, 15).addBox(-2.0F, -4.0F, -1.1F, 4.0F, 1.0F, 2.0F)
                .texOffs(103, 15).addBox(-2.0F, -4.0F, 2.1F, 4.0F, 1.0F, 2.0F)
                .texOffs(76, 15).addBox(-4.5F, -2.8F, -3.1F, 1.0F, 2.0F, 2.0F)
                .texOffs(76, 15).addBox(-4.5F, -2.8F, -0.1F, 1.0F, 2.0F, 2.0F)
                .texOffs(76, 15).addBox(-4.5F, -2.8F, 2.9F, 1.0F, 2.0F, 2.0F)
                .texOffs(76, 15).addBox(3.5F, -2.8F, -3.1F, 1.0F, 2.0F, 2.0F)
                .texOffs(76, 15).addBox(3.5F, -2.8F, -0.1F, 1.0F, 2.0F, 2.0F)
                .texOffs(76, 15).addBox(3.5F, -2.8F, 2.9F, 1.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 20.0F, 0.0F));

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(1, 15).addBox(-3.0F, -1.5F, -2.0F, 6.0F, 3.0F, 3.0F)
                .texOffs(83, 15).addBox(-1.5F, 0.0F, -3.0F, 3.0F, 2.0F, 2.0F)
                .texOffs(116, 15).addBox(-2.5F, -3.0F, -1.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(121, 15).addBox(-2.55F, -3.3F, -2.1F, 1.0F, 1.0F, 1.0F)
                .texOffs(116, 15).addBox(1.5F, -3.0F, -1.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(121, 15).addBox(1.55F, -3.3F, -2.1F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 20.0F, -5.0F));

        head.addOrReplaceChild("antenna_n1", CubeListBuilder.create()
                .texOffs(65, 15).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -1.0F, -0.4363F, -0.384F, 0.0F));

        head.addOrReplaceChild("antenna_1", CubeListBuilder.create()
                .texOffs(65, 15).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(1.0F, -1.0F, -1.0F, -0.4363F, 0.384F, 0.0F));

        PartDefinition leg_0 = root.addOrReplaceChild("leg_0", CubeListBuilder.create()
                .texOffs(94, 15).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                .texOffs(1, 22).addBox(-3.5F, -0.6F, -0.5F, 4.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-4.5F, 20.5F, -3.0F, 0.0F, -0.3142F, -0.2094F));

        leg_0.addOrReplaceChild("shin_0", CubeListBuilder.create()
                .texOffs(94, 15).addBox(-0.7F, -0.8F, -0.8F, 2.0F, 2.0F, 2.0F)
                .texOffs(12, 22).addBox(-2.7F, -0.4F, -0.4F, 3.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.9076F));

        PartDefinition leg_1 = root.addOrReplaceChild("leg_1", CubeListBuilder.create()
                .texOffs(94, 15).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                .texOffs(1, 22).addBox(-3.5F, -0.6F, -0.5F, 4.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-4.5F, 20.5F, 0.0F, 0.0F, 0.0F, -0.2094F));

        leg_1.addOrReplaceChild("shin_1", CubeListBuilder.create()
                .texOffs(94, 15).addBox(-0.7F, -0.8F, -0.8F, 2.0F, 2.0F, 2.0F)
                .texOffs(12, 22).addBox(-2.7F, -0.4F, -0.4F, 3.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.9076F));

        PartDefinition leg_2 = root.addOrReplaceChild("leg_2", CubeListBuilder.create()
                .texOffs(94, 15).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                .texOffs(1, 22).addBox(-3.5F, -0.6F, -0.5F, 4.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-4.5F, 20.5F, 3.0F, 0.0F, 0.3142F, -0.2094F));

        leg_2.addOrReplaceChild("shin_2", CubeListBuilder.create()
                .texOffs(94, 15).addBox(-0.7F, -0.8F, -0.8F, 2.0F, 2.0F, 2.0F)
                .texOffs(12, 22).addBox(-2.7F, -0.4F, -0.4F, 3.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.9076F));

        PartDefinition leg_3 = root.addOrReplaceChild("leg_3", CubeListBuilder.create()
                .texOffs(94, 15).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                .texOffs(1, 22).addBox(-0.5F, -0.6F, -0.5F, 4.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(4.5F, 20.5F, -3.0F, 0.0F, 0.3142F, 0.2094F));

        leg_3.addOrReplaceChild("shin_3", CubeListBuilder.create()
                .texOffs(94, 15).addBox(-0.7F, -0.8F, -0.8F, 2.0F, 2.0F, 2.0F)
                .texOffs(12, 22).addBox(-0.3F, -0.4F, -0.4F, 3.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.9076F));

        PartDefinition leg_4 = root.addOrReplaceChild("leg_4", CubeListBuilder.create()
                .texOffs(94, 15).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                .texOffs(1, 22).addBox(-0.5F, -0.6F, -0.5F, 4.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(4.5F, 20.5F, 0.0F, 0.0F, 0.0F, 0.2094F));

        leg_4.addOrReplaceChild("shin_4", CubeListBuilder.create()
                .texOffs(94, 15).addBox(-0.7F, -0.8F, -0.8F, 2.0F, 2.0F, 2.0F)
                .texOffs(12, 22).addBox(-0.3F, -0.4F, -0.4F, 3.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.9076F));

        PartDefinition leg_5 = root.addOrReplaceChild("leg_5", CubeListBuilder.create()
                .texOffs(94, 15).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                .texOffs(1, 22).addBox(-0.5F, -0.6F, -0.5F, 4.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(4.5F, 20.5F, 3.0F, 0.0F, -0.3142F, 0.2094F));

        leg_5.addOrReplaceChild("shin_5", CubeListBuilder.create()
                .texOffs(94, 15).addBox(-0.7F, -0.8F, -0.8F, 2.0F, 2.0F, 2.0F)
                .texOffs(12, 22).addBox(-0.3F, -0.4F, -0.4F, 3.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.9076F));

        PartDefinition left_claw = root.addOrReplaceChild("left_claw", CubeListBuilder.create()
                .texOffs(108, 1).addBox(-1.0F, -0.4F, -4.0F, 2.0F, 2.0F, 5.0F)
                .texOffs(20, 15).addBox(-1.5F, -1.0F, -6.0F, 3.0F, 3.0F, 3.0F)
                .texOffs(33, 15).addBox(0.6F, -0.5F, -9.0F, 1.0F, 2.0F, 4.0F),
                PartPose.offsetAndRotation(3.0F, 20.0F, -5.0F, 0.0F, -0.3491F, 0.0F));

        left_claw.addOrReplaceChild("left_pincer", CubeListBuilder.create()
                .texOffs(33, 15).addBox(-1.5F, -0.6F, -3.0F, 1.0F, 2.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -5.0F, 0.0F, 0.3142F, 0.0F));

        PartDefinition right_claw = root.addOrReplaceChild("right_claw", CubeListBuilder.create()
                .texOffs(108, 1).addBox(-1.0F, -0.4F, -4.0F, 2.0F, 2.0F, 5.0F)
                .texOffs(20, 15).addBox(-1.5F, -1.0F, -6.0F, 3.0F, 3.0F, 3.0F)
                .texOffs(33, 15).addBox(-1.6F, -0.5F, -9.0F, 1.0F, 2.0F, 4.0F),
                PartPose.offsetAndRotation(-3.0F, 20.0F, -5.0F, 0.0F, 0.3491F, 0.0F));

        right_claw.addOrReplaceChild("right_pincer", CubeListBuilder.create()
                .texOffs(33, 15).addBox(0.5F, -0.6F, -3.0F, 1.0F, 2.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -5.0F, 0.0F, -0.3142F, 0.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        for (int i = 0; i < LEG_COUNT; i++) {
            float phase = (i % 3) * 1.0471976F + (i < 3 ? 0.0F : 3.1415927F);
            float swing = Mth.cos(state.walkAnimationPos * 0.6F + phase) * 0.45F * state.walkAnimationSpeed;
            this.legs[i].zRot += swing * 0.5F;
            this.legs[i].yRot += Mth.sin(state.walkAnimationPos * 0.6F + phase) * 0.2F * state.walkAnimationSpeed;
        }
        this.head.xRot = 0.06F * Mth.sin(0.08F * state.ageInTicks);
        float feel = Mth.sin(0.07F * state.ageInTicks);
        this.leftClaw.yRot += 0.06F * feel;
        this.rightClaw.yRot -= 0.06F * feel;
        this.leftPincer.yRot += 0.1F * feel;
        this.rightPincer.yRot -= 0.1F * feel;
    }
}
