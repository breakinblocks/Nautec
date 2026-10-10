package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.renderer.entity.EmissiveGeoLayer;
import com.breakinblocks.nautec.client.renderer.items.GeoStateData;
import com.breakinblocks.nautec.content.resonantstorage.ResonantVaultBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

import java.util.function.BiConsumer;

public class ResonantVaultRenderer extends GeoBlockRenderer<ResonantVaultBlockEntity> {
    public static final float LIGHT_FRONT = 2.0f;
    public static final float LIGHT_BOTTOM = 9.5f;
    public static final float LIGHT_TOP = 11.5f;
    private static final ResourceLocation GLOWMASK = Nautec.rl("textures/block/resonant_vault_glowmask.png");

    public ResonantVaultRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
        addRenderLayer(new EmissiveGeoLayer<>(this, GLOWMASK));
    }

    @Override
    public void render(ResonantVaultBlockEntity vault, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        super.render(vault, partialTick, poseStack, buffers, packedLight, packedOverlay);
        ResonantLights.submit(poseStack, buffers, getFacing(vault), vault.channel().address(), LIGHT_FRONT, LIGHT_BOTTOM, LIGHT_TOP);
    }

    private static final class Model extends DefaultedBlockGeoModel<ResonantVaultBlockEntity> {
        private Model() {
            super(Nautec.rl("resonant_vault"));
        }

        @Override
        public void addAdditionalStateData(ResonantVaultBlockEntity vault, long instanceId,
                                           BiConsumer<DataTicket<ResonantVaultBlockEntity>, ResonantVaultBlockEntity> dataConsumer) {
            super.addAdditionalStateData(vault, instanceId, dataConsumer);
            GeoStateData.put(dataConsumer, ResonantVaultBlockEntity.OPEN, vault.isOpen());
        }
    }
}
