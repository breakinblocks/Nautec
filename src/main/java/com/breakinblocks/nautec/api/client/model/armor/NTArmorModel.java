package com.breakinblocks.nautec.api.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.function.Consumer;

public class NTArmorModel extends HumanoidModel<LivingEntity> {

    private final EquipmentSlot slot;

    public NTArmorModel(ModelPart root, EquipmentSlot slot) {
        super(root);
        this.slot = slot;
        setPartVisibility(slot);
    }

    public static LayerDefinition createLayer(int textureWidth, int textureHeight, Consumer<PartsDefinition> partsConsumer) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);

        partsConsumer.accept(new PartsDefinition(root));

        return LayerDefinition.create(mesh, textureWidth, textureHeight);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        setPartVisibility(slot);
        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    protected void setPartVisibility(EquipmentSlot slot) {
        head.visible = false;
        hat.visible = false;
        body.visible = false;
        leftArm.visible = false;
        rightArm.visible = false;
        leftLeg.visible = false;
        rightLeg.visible = false;
        switch (slot) {
            case HEAD -> {
                head.visible = true;
                hat.visible = true;
            }
            case CHEST -> {
                body.visible = true;
                rightArm.visible = true;
                leftArm.visible = true;
            }
            case LEGS -> {
                body.visible = true;
                rightLeg.visible = true;
                leftLeg.visible = true;
            }
            case FEET -> {
                rightLeg.visible = true;
                leftLeg.visible = true;
            }
        }
    }

    public record PartsDefinition(PartDefinition root) {

        public PartDefinition getHat() {
            return root().getChild("hat");
        }

        public PartDefinition getHead() {
            return root().getChild("head");
        }

        public PartDefinition getBody() {
            return root().getChild("body");
        }

        public PartDefinition getLeftArm() {
            return root().getChild("left_arm");
        }

        public PartDefinition getRightArm() {
            return root().getChild("right_arm");
        }

        public PartDefinition getLeftLeg() {
            return root().getChild("left_leg");
        }

        public PartDefinition getRightLeg() {
            return root().getChild("right_leg");
        }

    }
}
