package com.breakinblocks.nautec.client.renderer.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.breakinblocks.nautec.api.client.renderer.blockentities.LaserBlockEntityRenderer;
import com.breakinblocks.nautec.api.client.renderer.blockentities.LaserRenderState;
import com.breakinblocks.nautec.content.blockentities.LongDistanceLaserBlockEntity;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LongDistanceLaserBERenderer implements BlockEntityRenderer<LongDistanceLaserBlockEntity, LaserRenderState> {
    public LongDistanceLaserBERenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public LaserRenderState createRenderState() {
        return new LaserRenderState();
    }

    @Override
    public void extractRenderState(LongDistanceLaserBlockEntity blockEntity, LaserRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumbling);
        state.beams.clear();
        state.partialTick = partialTick;
        state.laserTime = blockEntity.getClientLaserTime() + (partialTick * 24);
        state.gameTime = blockEntity.getLevel() == null ? 0L : blockEntity.getLevel().getGameTime();

        Object2IntMap<Direction> laserDistances = blockEntity.getLaserDistances();
        for (Direction direction : blockEntity.getLaserOutputs()) {
            int laserDistance = laserDistances.getOrDefault(direction, 0);
            if (laserDistance != 0 && blockEntity.shouldRender(direction)) {
                state.beams.add(new LaserRenderState.Beam(direction, laserDistance, 1));
            }
        }
    }

    @Override
    public void submit(LaserRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        for (LaserRenderState.Beam beam : state.beams) {
            LaserBlockEntityRenderer.submitLaser(poseStack, collector, beam.direction(), beam.laserDistance() - 0.5F, true);
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(LongDistanceLaserBlockEntity blockEntity) {
        BlockPos blockPos = blockEntity.getBlockPos();
        AABB box = new AABB(blockPos);
        Object2IntMap<Direction> laserDistances = blockEntity.getLaserDistances();
        for (Direction direction : blockEntity.getLaserOutputs()) {
            int distance = laserDistances.getOrDefault(direction, 0);
            BlockPos pos = blockPos.relative(direction, distance);
            BlockPos relative = blockPos.subtract(pos);
            box = box.expandTowards(-relative.getX(), -relative.getY(), -relative.getZ());
        }
        return box;
    }
}
