package com.breakinblocks.nautec.client.renderer.entity;

import com.breakinblocks.nautec.content.entities.ThrownNeptunesTrident;
import com.breakinblocks.nautec.registries.NTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class NeptunesTridentRenderer extends EntityRenderer<ThrownNeptunesTrident> {
    private static final float SHAFT_CENTRE = 0.4375F;
    private static final float TIP_REACH = -1.0F;
    private final ItemRenderer itemRenderer;
    private ItemStack trident;
    private ItemStack enchantedTrident;

    public NeptunesTridentRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ThrownNeptunesTrident entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        if (this.trident == null) {
            this.trident = new ItemStack(NTItems.NEPTUNES_TRIDENT.get());
            this.enchantedTrident = this.trident.copy();
            this.enchantedTrident.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        float yRot = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(xRot - 90.0F));
        poseStack.translate(SHAFT_CENTRE, TIP_REACH, SHAFT_CENTRE);
        this.itemRenderer.renderStatic(entity.isFoil() ? this.enchantedTrident : this.trident, ItemDisplayContext.NONE, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, buffers, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownNeptunesTrident entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
