package com.breakinblocks.nautec.client.renderer.items;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.items.WaveJetItem;
import com.geckolib.constant.DataTickets;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;

public class WaveJetItemRenderer extends GeoItemRenderer<WaveJetItem> {
    private static final Identifier EMISSIVE = Nautec.rl("textures/entity/wave_jet_e.png");

    private static final float CENTRE_Y = 5.5625F / 16F;
    private static final float CENTRE_Z = 5.125F / 16F;

    public WaveJetItemRenderer() {
        super(new DefaultedEntityGeoModel<>(Nautec.rl("wave_jet")));
        withRenderLayer(new EmissiveLayer(this));
    }

    private static float scaleFor(ItemDisplayContext context) {
        return switch (context) {
            case GUI, FIXED, GROUND -> 0.85F;
            default -> 1.0F;
        };
    }

    private static boolean isThrusting(RenderData data) {
        LivingEntity holder = data.itemOwner() == null ? null : data.itemOwner().asLivingEntity();
        return holder != null && holder.isUsingItem() && holder.getUseItem() == data.itemStack();
    }

    @Override
    public void addRenderData(WaveJetItem item, RenderData data, GeoRenderState state, float partialTick) {
        super.addRenderData(item, data, state, partialTick);
        state.addGeckolibData(WaveJetItem.THRUSTING, isThrusting(data));
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState> pass) {
        super.adjustRenderPose(pass);
        PoseStack poseStack = pass.poseStack();
        float scale = scaleFor(pass.renderState().getOrDefaultGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE, ItemDisplayContext.GUI));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0F, -CENTRE_Y, -CENTRE_Z);
    }

    private static class EmissiveLayer extends AutoGlowingGeoLayer<WaveJetItem, GeoItemRenderer.RenderData, GeoRenderState> {
        EmissiveLayer(GeoItemRenderer<WaveJetItem> renderer) {
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
