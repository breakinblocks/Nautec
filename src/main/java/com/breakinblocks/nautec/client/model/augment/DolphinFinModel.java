package com.breakinblocks.nautec.client.model.augment;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.model.augments.AugmentModel;
import com.breakinblocks.nautec.content.augments.DolphinFinAugment;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public class DolphinFinModel extends AugmentModel<DolphinFinAugment> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Nautec.rl("dolphin_fin"), "main");
    public static final RenderType RENDER_TYPE = RenderType.entitySolid(Nautec.rl("textures/augments/dolphin_fin.png"));

    public DolphinFinModel(ModelPart root) {
        super(root, RenderType::entitySolid);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-0.5F, -6.0F, -1.0F, 1.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, 2.0F, -115.0F * Mth.DEG_TO_RAD, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

}
