package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.api.client.renderer.blockentities.BERenderState;
import com.breakinblocks.nautec.api.client.renderer.blockentities.NTBERenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.breakinblocks.nautec.content.blockentities.ChargerBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ChargerBERenderer extends NTBERenderer<ChargerBlockEntity, ChargerBERenderer.ChargerRenderState> {

    public ChargerBERenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ChargerRenderState createRenderState() {
        return new ChargerRenderState();
    }

    @Override
    public void extractRenderState(ChargerBlockEntity blockEntity, ChargerRenderState state, float partialTick, Vec3 cameraPos) {
        BERenderState.extractBase(blockEntity, state);
        ItemStack stack = blockEntity.getItemStackHandler().getStackInSlot(0);
        state.scale = stack.getItem() instanceof BlockItem ? 0.95F : 0.75F;
        state.item.update(stack, ItemDisplayContext.GROUND, blockEntity.getLevel(), 0);
    }

    @Override
    public void submit(ChargerRenderState state, PoseStack poseStack, MultiBufferSource buffers, Vec3 cameraPos) {
        if (!state.item.isEmpty()) {
            poseStack.pushPose();
            {
                poseStack.translate(0.5D, 0.4f, 0.5D);
                poseStack.scale(state.scale, state.scale, state.scale);
                double tick = System.currentTimeMillis() / 800.0D;
                poseStack.translate(0.0D, Math.sin(tick % (2 * Math.PI)) * 0.065D, 0.0D);
                poseStack.mulPose(Axis.YP.rotationDegrees((float) ((tick * 40.0D) % 360)));
                state.item.submit(poseStack, buffers, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            }
            poseStack.popPose();
        }
    }

    public static class ChargerRenderState extends BERenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public float scale = 0.75F;
    }
}
