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

public class SiltSkipperModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart tail;
    private final ModelPart tailTip;
    private final ModelPart leftFin;
    private final ModelPart rightFin;

    public SiltSkipperModel(ModelPart root) {
        super(root);
        this.tail = root.getChild("tail");
        this.tailTip = this.tail.getChild("tail_tip");
        this.leftFin = root.getChild("left_fin");
        this.rightFin = root.getChild("right_fin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 6.0F)
                .texOffs(43, 1).addBox(-1.5F, 1.1F, -3.1F, 3.0F, 1.0F, 6.0F)
                .texOffs(77, 1).addBox(-2.5F, -1.5F, -3.5F, 5.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 21.0F, 0.0F));

        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(22, 1).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 4.0F)
                .texOffs(1, 12).addBox(-2.5F, -1.0F, -4.0F, 5.0F, 2.0F, 2.0F)
                .texOffs(52, 12).addBox(-2.0F, 1.1F, -4.1F, 4.0F, 1.0F, 2.0F)
                .texOffs(16, 12).addBox(-2.7F, -3.5F, -2.5F, 2.0F, 2.0F, 2.0F)
                .texOffs(85, 12).addBox(-2.2F, -3.2F, -2.65F, 1.0F, 1.0F, 1.0F)
                .texOffs(25, 12).addBox(-3.1F, -1.0F, -0.8F, 1.0F, 2.0F, 2.0F)
                .texOffs(16, 12).addBox(0.7F, -3.5F, -2.5F, 2.0F, 2.0F, 2.0F)
                .texOffs(85, 12).addBox(1.2F, -3.2F, -2.65F, 1.0F, 1.0F, 1.0F)
                .texOffs(25, 12).addBox(2.1F, -1.0F, -0.8F, 1.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 20.9F, -3.0F));

        PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(62, 1).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 3.0F, 4.0F),
                PartPose.offset(0.1F, 21.0F, 2.5F));

        tail.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                .texOffs(101, 1).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 3.0F)
                .texOffs(94, 1).addBox(-0.5F, -2.0F, 1.5F, 1.0F, 4.0F, 2.0F)
                .texOffs(32, 12).addBox(-0.5F, -3.0F, 3.5F, 1.0F, 2.0F, 2.0F)
                .texOffs(32, 12).addBox(-0.5F, 1.0F, 3.5F, 1.0F, 2.0F, 2.0F),
                PartPose.offset(-0.1F, 0.0F, 3.0F));

        root.addOrReplaceChild("dorsal_0", CubeListBuilder.create()
                .texOffs(112, 1).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 19.0F, -1.0F));

        root.addOrReplaceChild("dorsal_1", CubeListBuilder.create()
                .texOffs(112, 1).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 19.0F, 1.0F));

        root.addOrReplaceChild("dorsal_2", CubeListBuilder.create()
                .texOffs(32, 12).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 19.0F, 3.0F));

        root.addOrReplaceChild("left_fin", CubeListBuilder.create()
                .texOffs(65, 12).addBox(0.0F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F)
                .texOffs(39, 12).addBox(1.5F, -0.4F, -0.5F, 3.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(2.0F, 21.5F, -1.5F, 0.0F, -0.2618F, 0.3491F));

        root.addOrReplaceChild("left_pelvic", CubeListBuilder.create()
                .texOffs(76, 12).addBox(0.0F, 0.0F, -0.5F, 2.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(1.0F, 22.5F, 1.0F, 0.0F, 0.0F, 0.2094F));

        root.addOrReplaceChild("right_fin", CubeListBuilder.create()
                .texOffs(65, 12).addBox(-3.0F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F)
                .texOffs(39, 12).addBox(-4.5F, -0.4F, -0.5F, 3.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(-2.0F, 21.5F, -1.5F, 0.0F, 0.2618F, -0.3491F));

        root.addOrReplaceChild("right_pelvic", CubeListBuilder.create()
                .texOffs(76, 12).addBox(-2.0F, 0.0F, -0.5F, 2.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(-1.0F, 22.5F, 1.0F, 0.0F, 0.0F, -0.2094F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float amplitude = state.isInWater ? 1.0F : 1.6F;
        float wave = Mth.sin(0.4F * state.ageInTicks);
        this.tail.yRot = -amplitude * 0.3F * wave;
        this.tailTip.yRot = -amplitude * 0.4F * Mth.sin(0.4F * state.ageInTicks - 0.65F);
        this.leftFin.zRot += 0.2F * wave;
        this.rightFin.zRot -= 0.2F * wave;
    }
}
