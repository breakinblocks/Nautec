package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.client.render.FluidTints;
import com.breakinblocks.nautec.content.reservoir.DeepSeaReservoirBlock;
import com.breakinblocks.nautec.content.reservoir.DeepSeaReservoirBlockEntity;
import com.breakinblocks.nautec.content.reservoir.ReservoirFluid;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class DeepSeaReservoirRenderer implements BlockEntityRenderer<DeepSeaReservoirBlockEntity, DeepSeaReservoirRenderer.ReservoirRenderState> {
    private static final float LEFT = 5f / 16f;
    private static final float RIGHT = 11f / 16f;
    private static final float BOTTOM = -8f / 16f;
    private static final float TOP = 24f / 16f;
    private static final float DEPTH = -12.05f / 16f;

    public DeepSeaReservoirRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public ReservoirRenderState createRenderState() {
        return new ReservoirRenderState();
    }

    @Override
    public void extractRenderState(DeepSeaReservoirBlockEntity reservoir, ReservoirRenderState state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(reservoir, state, crumbling);
        state.facing = reservoir.getBlockState().hasProperty(DeepSeaReservoirBlock.FACING)
                ? reservoir.getBlockState().getValue(DeepSeaReservoirBlock.FACING) : Direction.NORTH;
        state.sprite = null;
        ReservoirFluid contents = reservoir.contents();
        long capacity = reservoir.capacity();
        if (contents.isEmpty() || capacity <= 0) {
            return;
        }
        FluidStack type = contents.resource().toStack(1);
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(type.getFluid().defaultFluidState());
        state.sprite = model.stillMaterial().sprite();
        state.colour = FluidTints.colour(model, type) | 0xFF000000;
        state.fill = (float) Math.min(1.0, (double) contents.amount() / capacity);
        if (reservoir.getLevel() != null) {
            state.lightCoords = LevelRenderer.getLightCoords(reservoir.getLevel(), reservoir.getBlockPos().relative(state.facing, 2));
        }
        int emission = type.getFluidType().getLightLevel(type);
        if (emission > 0) {
            state.lightCoords = LightCoordsUtil.lightCoordsWithEmission(state.lightCoords, emission);
        }
    }

    @Override
    public void submit(ReservoirRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.sprite == null || state.fill <= 0) {
            return;
        }
        TextureAtlasSprite sprite = state.sprite;
        float top = BOTTOM + Math.max(1f / 32f, state.fill * (TOP - BOTTOM));
        poseStack.pushPose();
        poseStack.translate(0.5f, 0f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(-(state.facing.toYRot() + 180f)));
        poseStack.translate(-0.5f, 0f, -0.5f);
        collector.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(), (pose, consumer) ->
                column(pose, consumer, sprite, state.colour, state.lightCoords, top));
        poseStack.popPose();
    }

    private static void column(PoseStack.Pose pose, VertexConsumer consumer, TextureAtlasSprite sprite, int colour, int light, float top) {
        float u0 = sprite.getU(0f);
        float u1 = sprite.getU(RIGHT - LEFT);
        for (float y0 = BOTTOM; y0 < top; y0 += 1f) {
            float y1 = Math.min(top, y0 + 1f);
            float v0 = sprite.getV(1f - (y1 - y0));
            float v1 = sprite.getV(1f);
            vertex(consumer, pose, LEFT, y0, u0, v1, colour, light);
            vertex(consumer, pose, LEFT, y1, u0, v0, colour, light);
            vertex(consumer, pose, RIGHT, y1, u1, v0, colour, light);
            vertex(consumer, pose, RIGHT, y0, u1, v1, colour, light);
        }
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float u, float v, int colour, int light) {
        consumer.addVertex(pose, x, y, DEPTH).setColor(colour).setUv(u, v).setLight(light).setNormal(pose, 0f, 0f, -1f);
    }

    @Override
    public AABB getRenderBoundingBox(DeepSeaReservoirBlockEntity reservoir) {
        BlockPos pos = reservoir.getBlockPos();
        return new AABB(pos.getX() - 1.0, pos.getY() - 1.0, pos.getZ() - 1.0, pos.getX() + 2.0, pos.getY() + 2.0, pos.getZ() + 2.0);
    }

    public static class ReservoirRenderState extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        @Nullable TextureAtlasSprite sprite;
        int colour;
        float fill;
    }
}
