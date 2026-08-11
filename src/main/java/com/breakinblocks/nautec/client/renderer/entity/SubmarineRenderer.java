package com.breakinblocks.nautec.client.renderer.entity;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.model.entity.SubmarineModel;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class SubmarineRenderer extends GeoEntityRenderer<SubmarineEntity, EntityRenderState> {
    private static final DataTicket<Float> YAW = DataTicket.create("nautec:submarine_yaw", Float.class);
    private static final DataTicket<Float> PITCH = DataTicket.create("nautec:submarine_pitch", Float.class);

    private static final Identifier EMISSIVE = Nautec.rl("textures/entity/submarine_e.png");

    private static final float PITCH_PIVOT = 4.5F / 16F;

    public SubmarineRenderer(EntityRendererProvider.Context context) {
        super(context, new SubmarineModel());
        this.shadowRadius = 1.1F * SubmarineEntity.MODEL_SCALE;
        withScale(SubmarineEntity.MODEL_SCALE);
        withRenderLayer(new SubmarineGlowLayer(this));
    }

    @Override
    public void addRenderData(SubmarineEntity submarine, Void relatedObject, EntityRenderState state, float partialTick) {
        super.addRenderData(submarine, relatedObject, state, partialTick);
        state.addGeckolibData(SubmarineEntity.DEPLOYED, submarine.isDeployed());
        state.addGeckolibData(YAW, Mth.rotLerp(partialTick, submarine.yRotO, submarine.getYRot()));
        state.addGeckolibData(PITCH, Mth.rotLerp(partialTick, submarine.xRotO, submarine.getXRot()));
    }

    @Override
    public RenderType getRenderType(EntityRenderState state, Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    @Override
    protected void applyRotations(RenderPassInfo<EntityRenderState> pass, PoseStack poseStack, float scale) {
        poseStack.translate(0F, PITCH_PIVOT + SubmarineEntity.MODEL_Y_OFFSET, 0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180F - pass.getOrDefaultGeckolibData(YAW, 0F)));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pass.getOrDefaultGeckolibData(PITCH, 0F)));
        poseStack.translate(0F, -PITCH_PIVOT, -SubmarineEntity.MODEL_Z_OFFSET);
    }

    private static class SubmarineGlowLayer extends AutoGlowingGeoLayer<SubmarineEntity, Void, EntityRenderState> {
        SubmarineGlowLayer(GeoRenderer<SubmarineEntity, Void, EntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        protected Identifier getTextureResource(EntityRenderState state) {
            return EMISSIVE;
        }

        @Override
        protected boolean shouldAddZOffset(EntityRenderState state) {
            return true;
        }

    }
}
