package com.breakinblocks.nautec.client.renderer.items;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.items.SubmarineItem;
import com.geckolib.constant.DataTickets;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;

public class SubmarineItemRenderer extends GeoItemRenderer<SubmarineItem> {
    private static final Identifier EMISSIVE = Nautec.rl("textures/entity/submarine_e.png");

    private static final float CENTRE_Y = 7.5F / 16F;
    private static final float CENTRE_Z = -2.5F / 16F;

    public SubmarineItemRenderer() {
        super(new DefaultedEntityGeoModel<>(Nautec.rl("submarine")));
        withRenderLayer(new EmissiveLayer(this));
    }

    private static float scaleFor(ItemDisplayContext context) {
        return switch (context) {
            case GUI, FIXED -> 1.1F;
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> 2.6F;
            default -> 1.0F;
        };
    }

    @Override
    public RenderType getRenderType(GeoRenderState state, Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState> pass) {
        super.adjustRenderPose(pass);
        PoseStack poseStack = pass.poseStack();
        float scale = scaleFor(pass.renderState().getOrDefaultGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE, ItemDisplayContext.GUI));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0F, -CENTRE_Y, CENTRE_Z);
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
