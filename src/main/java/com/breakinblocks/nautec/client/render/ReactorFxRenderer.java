package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.content.blockentities.multiblock.controller.AbstractBioReactorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ReactorFxRenderer<T extends AbstractBioReactorBlockEntity> implements BlockEntityRenderer<T, ReactorFxRenderer.State> {
    protected static final int CULTURE_CYAN = 0x38E4FF;
    protected static final float TINT_MIX = 0.7F;
    private static final long TIME_WRAP = 24000L;

    protected abstract boolean shouldDraw(ReactorCultureTracker tracker);

    protected abstract void draw(State state, PoseStack.Pose pose, VertexConsumer buffer);

    protected abstract AABB bounds(T blockEntity, ReactorCultureTracker tracker);

    @Override
    public State createRenderState() {
        return new State(this);
    }

    @Override
    public void extractRenderState(T blockEntity, State state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumbling);
        state.visible = false;
        Level level = blockEntity.getLevel();
        if (level == null || !blockEntity.isFormed()) {
            return;
        }
        ReactorCultureTracker tracker = ReactorCultureTracker.of(blockEntity);
        tracker.update(blockEntity, level, partialTick);
        if (!shouldDraw(tracker) && tracker.peakFlash() <= 0.0F) {
            return;
        }
        BlockPos pos = blockEntity.getBlockPos();
        state.cameraX = (float) (cameraPos.x - pos.getX());
        state.cameraY = (float) (cameraPos.y - pos.getY());
        state.cameraZ = (float) (cameraPos.z - pos.getZ());
        float dx = state.cameraX - 0.5F;
        float dy = state.cameraY - 0.5F;
        float dz = state.cameraZ - 0.5F;
        state.visible = true;
        state.tracker = tracker;
        state.time = (level.getGameTime() % TIME_WRAP) + partialTick;
        state.offset = ReactorFx.depthOffset(Math.sqrt(dx * dx + dy * dy + dz * dz));
        state.seed = (int) (pos.asLong() ^ (pos.asLong() >>> 32));
        state.front = blockEntity.front();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.visible && state.tracker != null) {
            ShaderPackOverlay.submit(poseStack, collector, NTRenderTypes.reactorGlow(), state);
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(T blockEntity) {
        return bounds(blockEntity, ReactorCultureTracker.of(blockEntity));
    }

    protected static int tint(ReactorCultureTracker tracker) {
        return ReactorFx.mix(CULTURE_CYAN, tracker.averageColor(), TINT_MIX);
    }

    public static final class State extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        private final ReactorFxRenderer<?> renderer;
        public @Nullable ReactorCultureTracker tracker;
        public boolean visible;
        public float time;
        public float offset;
        public float cameraX;
        public float cameraY;
        public float cameraZ;
        public int seed;
        public Direction front = Direction.NORTH;

        private State(ReactorFxRenderer<?> renderer) {
            this.renderer = renderer;
        }

        @Override
        public void render(PoseStack.Pose pose, VertexConsumer buffer) {
            if (this.tracker != null) {
                this.renderer.draw(this, pose, buffer);
            }
        }
    }
}
