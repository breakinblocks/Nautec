package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.content.resonantstorage.ResonantVaultBlockEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

public class ResonantVaultRenderer extends GeoBlockRenderer<ResonantVaultBlockEntity, BlockEntityRenderState> {
    public static final float LIGHT_FRONT = 2.0f;
    public static final float LIGHT_BOTTOM = 9.5f;
    public static final float LIGHT_TOP = 11.5f;
    private static final DataTicket<Integer> ADDRESS = DataTicket.create("nautec:resonant_vault_address", Integer.class);
    private static final Identifier GLOWMASK = Nautec.rl("textures/block/resonant_vault_glowmask.png");

    public ResonantVaultRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new DefaultedBlockGeoModel<>(Nautec.rl("resonant_vault")));
        withRenderLayer(new GlowLayer(this));
    }

    @Override
    public void addRenderData(ResonantVaultBlockEntity vault, Void relatedObject, BlockEntityRenderState renderState, float partialTick) {
        super.addRenderData(vault, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(ResonantVaultBlockEntity.OPEN, vault.isOpen());
        renderState.addGeckolibData(ADDRESS, vault.channel().address().pack());
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        Direction facing = state.getOrDefaultGeckolibData(DIRECTION_FACING, Direction.NORTH);
        GatewayAddress address = GatewayAddress.unpack(state.getOrDefaultGeckolibData(ADDRESS, GatewayAddress.DEFAULT.pack()));
        ResonantLights.submit(poseStack, collector, facing, address, LIGHT_FRONT, LIGHT_BOTTOM, LIGHT_TOP);
    }

    private static class GlowLayer extends AutoGlowingGeoLayer<ResonantVaultBlockEntity, Void, BlockEntityRenderState> {
        GlowLayer(GeoRenderer<ResonantVaultBlockEntity, Void, BlockEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        protected Identifier getTextureResource(BlockEntityRenderState state) {
            return GLOWMASK;
        }

        @Override
        protected boolean shouldAddZOffset(BlockEntityRenderState state) {
            return true;
        }
    }
}
