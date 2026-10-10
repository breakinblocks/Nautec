package com.breakinblocks.nautec.client.renderer.blockentities;

import com.mojang.math.Axis;
import com.breakinblocks.nautec.api.client.renderer.blockentities.BERenderState;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.renderer.blockentities.NTBERenderer;
import com.breakinblocks.nautec.client.render.NTRenderTypes;
import com.breakinblocks.nautec.client.render.ShaderPackOverlay;
import com.breakinblocks.nautec.content.blockentities.ConfinedSpawnerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ConfinedSpawnerBERenderer extends NTBERenderer<ConfinedSpawnerBlockEntity, ConfinedSpawnerBERenderer.ConfinedSpawnerRenderState> {
    public static final ResourceLocation RUNES = Nautec.rl("textures/entity/confined_spawner_runes.png");

    private static final float BAND_BOTTOM = 1.0F / 3.0F;
    private static final float BAND_TOP = 2.0F / 3.0F;
    private static final float OUTSET = 0.004F;
    private static final float SIDE_SPAN = 0.25F;
    private static final int BAND_RGB = 0x52E8FF;
    private static final float IDLE_ENERGY = 0.3F;
    private static final float DISPLAY_SCALE = 0.53125F;
    private static final double MOB_DISTANCE_SQR = 20.0 * 20.0;

    public ConfinedSpawnerBERenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ConfinedSpawnerRenderState createRenderState() {
        return new ConfinedSpawnerRenderState();
    }

    @Override
    public void extractRenderState(ConfinedSpawnerBlockEntity blockEntity, ConfinedSpawnerRenderState state, float partialTick, Vec3 cameraPos) {
        BERenderState.extractBase(blockEntity, state);
        state.energy = blockEntity.isActive() ? 1.0F : IDLE_ENERGY;
        BlockPos pos = blockEntity.getBlockPos();
        double dx = pos.getX() + 0.5 - cameraPos.x;
        double dy = pos.getY() + 0.5 - cameraPos.y;
        double dz = pos.getZ() + 0.5 - cameraPos.z;
        if (dx * dx + dy * dy + dz * dz > MOB_DISTANCE_SQR) {
            state.displayEntity = null;
            return;
        }
        Entity display = blockEntity.getOrCreateDisplayEntity();
        if (display == null) {
            state.displayEntity = null;
            return;
        }
        state.displayEntity = display;
        state.partialTick = partialTick;
        state.spin = blockEntity.getSpin(partialTick);
        float longest = Math.max(display.getBbWidth(), display.getBbHeight());
        state.scale = longest > 1.0F ? DISPLAY_SCALE / longest : DISPLAY_SCALE;
    }

    @Override
    public void submit(ConfinedSpawnerRenderState state, PoseStack poseStack, MultiBufferSource buffers, Vec3 cameraPos) {
        if (state.displayEntity != null) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.4F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.spin));
            poseStack.translate(0.0F, -0.2F, 0.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
            poseStack.scale(state.scale, state.scale, state.scale);
            this.context.getEntityRenderer().render(state.displayEntity, 0.0, 0.0, 0.0, 0.0F, state.partialTick, poseStack, buffers, state.lightCoords);
            poseStack.popPose();
        }
        int color = ((int) (state.energy * 255.0F) << 24) | BAND_RGB;
        ShaderPackOverlay.submit(poseStack, buffers, NTRenderTypes.gatewayGlow(RUNES), (pose, buffer) -> {
            float low = -OUTSET;
            float high = 1.0F + OUTSET;
            side(buffer, pose, 1, low, 0, low, 0.0F, 0.0F, -1.0F, color, 0);
            side(buffer, pose, high, 1, high, 0, 1.0F, 0.0F, 0.0F, color, 1);
            side(buffer, pose, 0, high, 1, high, 0.0F, 0.0F, 1.0F, color, 0);
            side(buffer, pose, low, 0, low, 1, -1.0F, 0.0F, 0.0F, color, 1);
        });
    }

    private static void side(VertexConsumer buffer, PoseStack.Pose pose, float x0, float z0, float x1, float z1,
                             float nx, float ny, float nz, int color, int segment) {
        float u0 = segment * SIDE_SPAN;
        float u1 = u0 + SIDE_SPAN;
        vertex(buffer, pose, x0, BAND_BOTTOM, z0, u0, 1.0F, color, nx, ny, nz);
        vertex(buffer, pose, x1, BAND_BOTTOM, z1, u1, 1.0F, color, nx, ny, nz);
        vertex(buffer, pose, x1, BAND_TOP, z1, u1, 0.0F, color, nx, ny, nz);
        vertex(buffer, pose, x0, BAND_TOP, z0, u0, 0.0F, color, nx, ny, nz);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color,
                               float nx, float ny, float nz) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setColor(color).setNormal(pose, nx, ny, nz);
    }

    @Override
    public AABB getRenderBoundingBox(ConfinedSpawnerBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 1.0, pos.getY() - 1.0, pos.getZ() - 1.0, pos.getX() + 2.0, pos.getY() + 2.0, pos.getZ() + 2.0);
    }

    public static class ConfinedSpawnerRenderState extends BERenderState {
        public @Nullable Entity displayEntity;
        public float partialTick;
        public float spin;
        public float scale;
        public float energy;
    }
}
