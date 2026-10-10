package com.breakinblocks.nautec.client.model.augment;

import com.mojang.blaze3d.vertex.PoseStack;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.model.augments.AugmentModel;
import com.breakinblocks.nautec.content.augments.GuardianEyeAugment;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class GuardianEyeModel extends AugmentModel<GuardianEyeAugment> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Nautec.rl("guardian_eye"), "main");
    public static final RenderType RENDER_TYPE = RenderType.entitySolid(Nautec.rl("textures/augments/guardian_eye.png"));
    private final ModelPart main;

    public GuardianEyeModel(ModelPart root) {
        super(root, RenderType::entitySolid);
        this.main = root.getChild("main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, 13.0F, 1.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -6.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void submit(PoseStack poseStack, MultiBufferSource buffers, RenderType renderType, int packedLight, int packedOverlay) {
        this.main.render(poseStack, buffers.getBuffer(renderType), packedLight, packedOverlay);
    }
}
