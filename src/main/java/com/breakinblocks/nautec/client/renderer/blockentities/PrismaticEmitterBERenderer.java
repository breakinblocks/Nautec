package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.client.renderer.blockentities.NTBERenderer;
import com.breakinblocks.nautec.client.render.LaserBeamRenderer;
import com.breakinblocks.nautec.client.render.PrismarineCrystalRenderer;
import com.breakinblocks.nautec.content.resonance.PrismaticEmitterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class PrismaticEmitterBERenderer extends NTBERenderer<PrismaticEmitterBlockEntity, PrismaticEmitterBERenderer.EmitterRenderState> {
    private static final float CRYSTAL_SCALE = 0.09F;
    private static final float HOVER = 1.1F;
    private static final float TETHER_HALF_WIDTH = 0.055F;
    private static final float TARGET_FLARE = 0.18F;
    private static final int TETHER_COLOR = 0x9CF2FF;

    public PrismaticEmitterBERenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public EmitterRenderState createRenderState() {
        return new EmitterRenderState();
    }

    @Override
    public void extractRenderState(PrismaticEmitterBlockEntity emitter, EmitterRenderState state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(emitter, state, crumbling);
        state.active = emitter.isVisualActive();
        state.ticks = emitter.getLevel() == null ? 0F : (float) (emitter.getLevel().getGameTime() % PrismarineCrystalRenderer.TICK_WRAP) + partialTick;
        state.seed = PrismarineCrystalRenderer.seed(emitter.getBlockPos());
        state.targets.clear();
        BlockPos origin = emitter.getBlockPos();
        for (PrismaticEmitterBlockEntity.Link link : emitter.getLinks()) {
            BlockPos target = link.pos();
            state.targets.add(new Vector3f(target.getX() - origin.getX() + 0.5F, target.getY() - origin.getY() + 0.5F,
                    target.getZ() - origin.getZ() + 0.5F));
        }
    }

    @Override
    public void submit(EmitterRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float hover = HOVER + Mth.sin(state.ticks * 0.06F) * 0.04F;
        poseStack.pushPose();
        poseStack.translate(0.5F, hover, 0.5F);
        poseStack.scale(CRYSTAL_SCALE, CRYSTAL_SCALE, CRYSTAL_SCALE);
        PrismarineCrystalRenderer.submit(poseStack, collector, state.ticks * (state.active ? 2.5F : 1F), state.seed,
                state.active ? 0.4F : 0.05F, true);
        poseStack.popPose();

        if (state.targets.isEmpty()) {
            return;
        }
        float pulse = 0.5F + 0.5F * Mth.sin(state.ticks * 0.25F);
        int alpha = state.active ? Math.round(160F + 90F * pulse) : 100;
        int color = ARGB.color(alpha, TETHER_COLOR);
        Vector3f from = new Vector3f(0.5F, hover, 0.5F);
        for (Vector3f target : state.targets) {
            LaserBeamRenderer.submitBeam(poseStack, collector, from, target, TETHER_HALF_WIDTH, color, true);
            if (state.active) {
                LaserBeamRenderer.submitFlare(poseStack, collector, target, TARGET_FLARE * (0.7F + 0.3F * pulse), color, true);
            }
        }
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(PrismaticEmitterBlockEntity emitter) {
        return new AABB(emitter.getBlockPos()).inflate(NTConfig.emitterRange + 1);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    public static class EmitterRenderState extends BlockEntityRenderState {
        public boolean active;
        public float ticks;
        public long seed;
        public final List<Vector3f> targets = new ArrayList<>();
    }
}
