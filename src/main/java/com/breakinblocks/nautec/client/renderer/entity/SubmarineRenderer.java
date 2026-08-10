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
    private static final DataTicket<Float> ROLL = DataTicket.create("nautec:submarine_roll", Float.class);

    private static final Identifier EMISSIVE = Nautec.rl("textures/entity/submarine_e.png");

    /** Height of the hull's visual centre above the entity origin, used as the pitch pivot. */
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
        state.addGeckolibData(ROLL, submarine.getRoll(partialTick));
    }

    /**
     * The canopy and cabin glass are painted at partial alpha, which GeckoLib's default
     * {@code entityCutout} would render fully opaque.
     */
    @Override
    public RenderType getRenderType(EntityRenderState state, Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    @Override
    protected void applyRotations(RenderPassInfo<EntityRenderState> pass, PoseStack poseStack, float scale) {
        poseStack.translate(0F, PITCH_PIVOT + SubmarineEntity.MODEL_Y_OFFSET, 0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(pass.getOrDefaultGeckolibData(YAW, 0F)));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pass.getOrDefaultGeckolibData(PITCH, 0F)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(pass.getOrDefaultGeckolibData(ROLL, 0F)));
        poseStack.translate(0F, -PITCH_PIVOT, 0F);
    }

    private static class SubmarineGlowLayer extends AutoGlowingGeoLayer<SubmarineEntity, Void, EntityRenderState> {
        SubmarineGlowLayer(GeoRenderer<SubmarineEntity, Void, EntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        protected Identifier getTextureResource(EntityRenderState state) {
            return EMISSIVE;
        }

        /**
         * Without this the glow sits at exactly the hull's depth, which fights with it now that the
         * hull is drawn translucent. GeckoLib only offsets armour layers by default.
         */
        @Override
        protected boolean shouldAddZOffset(EntityRenderState state) {
            return true;
        }

    }
}
