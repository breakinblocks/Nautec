package com.breakinblocks.nautec.client.renderer.entity;

import com.breakinblocks.nautec.content.entities.ThrownNeptunesTrident;
import com.breakinblocks.nautec.registries.NTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class NeptunesTridentRenderer extends EntityRenderer<ThrownNeptunesTrident, NeptunesTridentRenderer.State> {
    private static final float SHAFT_CENTRE = 0.4375F;
    private static final float TIP_REACH = -1.0F;
    private final ItemModelResolver itemModelResolver;
    private ItemStack trident;
    private ItemStack enchantedTrident;

    public NeptunesTridentRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot - 90.0F));
        poseStack.translate(SHAFT_CENTRE, TIP_REACH, SHAFT_CENTRE);
        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ThrownNeptunesTrident entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        if (this.trident == null) {
            this.trident = new ItemStack(NTItems.NEPTUNES_TRIDENT.get());
            this.enchantedTrident = this.trident.copy();
            this.enchantedTrident.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        this.itemModelResolver.updateForNonLiving(
                state.item, entity.isFoil() ? this.enchantedTrident : this.trident, ItemDisplayContext.NONE, entity);
    }

    public static class State extends EntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public float xRot;
        public float yRot;
    }
}
