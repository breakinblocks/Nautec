package com.breakinblocks.nautec.client.model.entity;

import com.breakinblocks.nautec.content.entities.mobs.LanternJelly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class LanternJellyModel extends EntityModel<LivingEntityRenderState> {
    private static final int TENDRIL_COUNT = 8;

    private final ModelPart bell;
    private final ModelPart skirt;
    private final ModelPart[] tendrils = new ModelPart[TENDRIL_COUNT];
    private final ModelPart[] tendrilTips = new ModelPart[TENDRIL_COUNT];

    public LanternJellyModel(ModelPart root) {
        super(root);
        this.bell = root.getChild("bell");
        this.skirt = this.bell.getChild("skirt");
        for (int i = 0; i < TENDRIL_COUNT; i++) {
            this.tendrils[i] = this.skirt.getChild("tendril_" + i);
            this.tendrilTips[i] = this.tendrils[i].getChild("tendril_tip_" + i);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bell = root.addOrReplaceChild("bell", CubeListBuilder.create()
                .texOffs(105, 1).addBox(-2.0F, -5.0F, -2.0F, 4.0F, 2.0F, 4.0F)
                .texOffs(34, 1).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 3.0F, 6.0F)
                .texOffs(1, 1).addBox(-4.0F, -2.5F, -4.0F, 8.0F, 3.0F, 8.0F)
                .texOffs(39, 13).addBox(-3.0F, -0.5F, -5.0F, 6.0F, 2.0F, 2.0F)
                .texOffs(39, 13).addBox(-3.0F, -0.5F, 3.0F, 6.0F, 2.0F, 2.0F)
                .texOffs(59, 1).addBox(3.0F, -0.5F, -3.0F, 2.0F, 2.0F, 6.0F)
                .texOffs(59, 1).addBox(-5.0F, -0.5F, -3.0F, 2.0F, 2.0F, 6.0F)
                .texOffs(56, 13).addBox(-3.1F, -3.6F, -3.1F, 1.0F, 3.0F, 1.0F)
                .texOffs(56, 13).addBox(-3.1F, -3.6F, 2.1F, 1.0F, 3.0F, 1.0F)
                .texOffs(56, 13).addBox(2.1F, -3.6F, -3.1F, 1.0F, 3.0F, 1.0F)
                .texOffs(56, 13).addBox(2.1F, -3.6F, 2.1F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, 14.0F, 0.0F));

        PartDefinition skirt = bell.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(76, 1).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 1.0F, 7.0F)
                .texOffs(1, 13).addBox(-1.5F, 0.5F, -1.5F, 3.0F, 3.0F, 3.0F)
                .texOffs(66, 13).addBox(-3.6F, 0.2F, -3.6F, 1.0F, 2.0F, 1.0F)
                .texOffs(66, 13).addBox(-0.6F, 0.2F, -4.6F, 1.0F, 2.0F, 1.0F)
                .texOffs(66, 13).addBox(2.6F, 0.2F, -3.6F, 1.0F, 2.0F, 1.0F)
                .texOffs(66, 13).addBox(3.6F, 0.2F, -0.6F, 1.0F, 2.0F, 1.0F)
                .texOffs(66, 13).addBox(2.6F, 0.2F, 2.6F, 1.0F, 2.0F, 1.0F)
                .texOffs(66, 13).addBox(-0.6F, 0.2F, 3.6F, 1.0F, 2.0F, 1.0F)
                .texOffs(66, 13).addBox(-3.6F, 0.2F, 2.6F, 1.0F, 2.0F, 1.0F)
                .texOffs(66, 13).addBox(-4.6F, 0.2F, -0.6F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition tendril_0 = skirt.addOrReplaceChild("tendril_0", CubeListBuilder.create()
                .texOffs(24, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, 1.0F, -3.0F, -0.1396F, 0.0F, 0.1396F));

        tendril_0.addOrReplaceChild("tendril_tip_0", CubeListBuilder.create()
                .texOffs(61, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 3.4F, 0.0F, 0.1396F, 0.0F, 0.1047F));

        PartDefinition tendril_1 = skirt.addOrReplaceChild("tendril_1", CubeListBuilder.create()
                .texOffs(14, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 1.0F, -4.0F, -0.1396F, 0.0F, -0.1396F));

        tendril_1.addOrReplaceChild("tendril_tip_1", CubeListBuilder.create()
                .texOffs(29, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 4.4F, 0.0F, 0.1396F, 0.0F, 0.1047F));

        PartDefinition tendril_2 = skirt.addOrReplaceChild("tendril_2", CubeListBuilder.create()
                .texOffs(24, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, 1.0F, -3.0F, -0.1396F, 0.0F, -0.1396F));

        tendril_2.addOrReplaceChild("tendril_tip_2", CubeListBuilder.create()
                .texOffs(19, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 3.4F, 0.0F, 0.1396F, 0.0F, 0.1047F));

        PartDefinition tendril_3 = skirt.addOrReplaceChild("tendril_3", CubeListBuilder.create()
                .texOffs(14, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(4.0F, 1.0F, 0.0F, 0.1396F, 0.0F, -0.1396F));

        tendril_3.addOrReplaceChild("tendril_tip_3", CubeListBuilder.create()
                .texOffs(61, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 4.4F, 0.0F, 0.1396F, 0.0F, 0.1047F));

        PartDefinition tendril_4 = skirt.addOrReplaceChild("tendril_4", CubeListBuilder.create()
                .texOffs(24, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, 1.0F, 3.0F, 0.1396F, 0.0F, -0.1396F));

        tendril_4.addOrReplaceChild("tendril_tip_4", CubeListBuilder.create()
                .texOffs(29, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 3.4F, 0.0F, 0.1396F, 0.0F, 0.1047F));

        PartDefinition tendril_5 = skirt.addOrReplaceChild("tendril_5", CubeListBuilder.create()
                .texOffs(14, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 1.0F, 4.0F, 0.1396F, 0.0F, -0.1396F));

        tendril_5.addOrReplaceChild("tendril_tip_5", CubeListBuilder.create()
                .texOffs(19, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 4.4F, 0.0F, 0.1396F, 0.0F, 0.1047F));

        PartDefinition tendril_6 = skirt.addOrReplaceChild("tendril_6", CubeListBuilder.create()
                .texOffs(24, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, 1.0F, 3.0F, 0.1396F, 0.0F, 0.1396F));

        tendril_6.addOrReplaceChild("tendril_tip_6", CubeListBuilder.create()
                .texOffs(61, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 3.4F, 0.0F, 0.1396F, 0.0F, 0.1047F));

        PartDefinition tendril_7 = skirt.addOrReplaceChild("tendril_7", CubeListBuilder.create()
                .texOffs(14, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(-4.0F, 1.0F, 0.0F, 0.1396F, 0.0F, 0.1396F));

        tendril_7.addOrReplaceChild("tendril_tip_7", CubeListBuilder.create()
                .texOffs(29, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 4.4F, 0.0F, 0.1396F, 0.0F, 0.1047F));

        skirt.addOrReplaceChild("oral_arm_0", CubeListBuilder.create()
                .texOffs(34, 13).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(-0.8F, 3.0F, -0.8F, -0.2094F, 0.0F, -0.2094F));

        skirt.addOrReplaceChild("oral_arm_1", CubeListBuilder.create()
                .texOffs(34, 13).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.8F, 3.0F, -0.8F, -0.2094F, 0.0F, 0.2094F));

        skirt.addOrReplaceChild("oral_arm_2", CubeListBuilder.create()
                .texOffs(34, 13).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(-0.8F, 3.0F, 0.8F, 0.2094F, 0.0F, -0.2094F));

        skirt.addOrReplaceChild("oral_arm_3", CubeListBuilder.create()
                .texOffs(34, 13).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.8F, 3.0F, 0.8F, 0.2094F, 0.0F, 0.2094F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float pulse = swimPulse(state.ageInTicks);
        this.bell.y += pulse * 0.5F;
        this.bell.xScale = 1.0F - pulse * 0.06F;
        this.bell.zScale = 1.0F - pulse * 0.06F;
        this.bell.yScale = 1.0F + pulse * 0.08F;
        for (int i = 0; i < TENDRIL_COUNT; i++) {
            float angle = i * Mth.TWO_PI / TENDRIL_COUNT - 3.0F * Mth.PI / 4.0F;
            float drift = Mth.sin(0.09F * state.ageInTicks - 0.55F + i * 0.2F);
            this.tendrils[i].xRot += drift * 0.16F * Mth.sin(angle);
            this.tendrils[i].zRot -= drift * 0.16F * Mth.cos(angle);
            this.tendrilTips[i].xRot += 0.2F * Mth.sin(0.09F * state.ageInTicks - 1.1F + i * 0.2F);
            this.tendrilTips[i].zRot += 0.12F * Mth.cos(0.07F * state.ageInTicks + i);
        }
    }

    public static float swimPulse(float ageInTicks) {
        return Mth.sin(LanternJelly.SWIM_PULSE_SPEED * ageInTicks);
    }

    public static float glowBrightness(LivingEntityRenderState state, float ageInTicks) {
        // A short, smooth flash at the contracted end of the swimming stroke.
        float flash = state.isInWater ? Mth.clamp((swimPulse(ageInTicks) - 0.6F) / 0.4F, 0.0F, 1.0F) : 0.0F;
        return 0.12F + 0.88F * flash * flash * (3.0F - 2.0F * flash);
    }
}
