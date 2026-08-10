package com.breakinblocks.nautec.client.renderer.items;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.items.SubmarineItem;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class SubmarineItemRenderer extends GeoItemRenderer<SubmarineItem> {
    private static final Identifier EMISSIVE = Nautec.rl("textures/entity/submarine_e.png");

    /** The hull is 35 model units long, so it has to come down to roughly one block to sit in a slot. */
    private static final float SCALE = 0.65F;
    private static final float CENTRE_Y = 7.5F / 16F;
    private static final float CENTRE_Z = -2.5F / 16F;

    public SubmarineItemRenderer() {
        super(new DefaultedEntityGeoModel<>(Nautec.rl("submarine")));
        withRenderLayer(new EmissiveLayer(this));
    }

    @Override
    public RenderType getRenderType(GeoRenderState state, Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState> pass) {
        super.adjustRenderPose(pass);
        PoseStack poseStack = pass.poseStack();
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.translate(0F, -CENTRE_Y, -CENTRE_Z);
    }

    private static class EmissiveLayer extends AutoGlowingGeoLayer<SubmarineItem, GeoItemRenderer.RenderData, GeoRenderState> {
        EmissiveLayer(GeoItemRenderer<SubmarineItem> renderer) {
            super(renderer);
        }

        @Override
        protected Identifier getTextureResource(GeoRenderState state) {
            return EMISSIVE;
        }

        @Override
        protected boolean shouldAddZOffset(GeoRenderState state) {
            return true;
        }
    }
}
