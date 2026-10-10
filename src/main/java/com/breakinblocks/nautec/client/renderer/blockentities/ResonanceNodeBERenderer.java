package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.api.client.renderer.blockentities.LaserBlockEntityRenderer;
import com.breakinblocks.nautec.api.client.renderer.blockentities.LaserRenderState;
import com.breakinblocks.nautec.client.render.PrismarineCrystalRenderer;
import com.breakinblocks.nautec.content.resonance.ResonanceNodeBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ResonanceNodeBERenderer extends LaserBlockEntityRenderer<ResonanceNodeBlockEntity, ResonanceNodeBERenderer.NodeRenderState> {
    private static final float SCALE = 0.09F;
    private static final float HOVER = 0.55F;

    public ResonanceNodeBERenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public NodeRenderState createRenderState() {
        return new NodeRenderState();
    }

    @Override
    public void extractRenderState(ResonanceNodeBlockEntity node, NodeRenderState state, float partialTick, Vec3 cameraPos) {
        super.extractRenderState(node, state, partialTick, cameraPos);
        state.facing = node.facing();
        state.online = node.isOnline();
        state.linked = node.getNetworkId() != null;
        state.ticks = node.getLevel() == null ? 0F : (float) (node.getLevel().getGameTime() % PrismarineCrystalRenderer.TICK_WRAP) + partialTick;
        state.seed = PrismarineCrystalRenderer.seed(node.getBlockPos());
    }

    @Override
    public void submit(NodeRenderState state, PoseStack poseStack, MultiBufferSource buffers, Vec3 cameraPos) {
        super.submit(state, poseStack, buffers, cameraPos);
        float speed = state.online ? 3F : state.linked ? 1.2F : 0.5F;
        float flash = state.online ? 0.45F + 0.25F * Mth.sin(state.ticks * 0.3F) : state.linked ? 0.1F : 0F;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(state.facing.getRotation());
        poseStack.translate(0F, HOVER - 0.5F + Mth.sin(state.ticks * 0.05F) * 0.02F, 0F);
        poseStack.scale(SCALE, SCALE, SCALE);
        PrismarineCrystalRenderer.submit(poseStack, buffers, state.ticks * speed, state.seed, flash, true);
        poseStack.popPose();
    }

    public static class NodeRenderState extends LaserRenderState {
        public Direction facing = Direction.UP;
        public boolean online;
        public boolean linked;
        public float ticks;
        public long seed;
    }
}
