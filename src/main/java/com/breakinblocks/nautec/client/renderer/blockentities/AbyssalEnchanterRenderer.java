package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.renderer.blockentities.BERenderState;
import com.breakinblocks.nautec.api.client.renderer.blockentities.NTBERenderer;
import com.breakinblocks.nautec.content.enchanter.AbyssalEnchanterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.breakinblocks.nautec.utils.ARGB;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AbyssalEnchanterRenderer extends NTBERenderer<AbyssalEnchanterBlockEntity, AbyssalEnchanterRenderer.EnchanterRenderState> {
    public static final ResourceLocation TEXTURE = Nautec.rl("textures/block/abyssal_enchanter.png");
    public static final float ATLAS = 128F;
    public static final int[] ROCK_TOP = {0, 112};
    public static final int[] ROCK_SIDE = {16, 112};
    public static final int[] GLOW = {32, 112};
    public static final int[] COLUMN = {48, 112};
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final int DEBRIS = 8;
    private static final float RING_RADIUS = 18F;
    private static final float PLATE_HEIGHT = 3F;
    private static final float[] PLATE_WIDTH = {12, 9, 12, 9, 12, 9, 12, 9};
    private static final float[] PLATE_DEPTH = {8, 9, 8, 9, 8, 9, 8, 9};
    private static final float[] PLATE_Y = {10, 13, 8, 12, 11, 7, 9, 14};
    private static final float[] PLATE_TILT = {-20, 14, -16, 22, -12, 18, -24, 10};
    private static final float ITEM_Y = 1.62F;
    private static final float COLUMN_BOTTOM = 24F / 16F;
    private static final float COLUMN_TOP = 2.6F;

    public AbyssalEnchanterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EnchanterRenderState createRenderState() {
        return new EnchanterRenderState();
    }

    @Override
    public void extractRenderState(AbyssalEnchanterBlockEntity enchanter, EnchanterRenderState state, float partialTick, Vec3 cameraPos) {
        BERenderState.extractBase(enchanter, state);
        state.spin = enchanter.spin(partialTick);
        state.speed = enchanter.spinSpeed();
        long gameTime = enchanter.getLevel() == null ? 0L : enchanter.getLevel().getGameTime();
        state.ticks = (gameTime % 24000L) + partialTick;
        ItemStack shown = enchanter.getItemStackHandler().getStackInSlot(AbyssalEnchanterBlockEntity.TARGET_SLOT);
        if (shown.isEmpty()) {
            shown = enchanter.getItemStackHandler().getStackInSlot(AbyssalEnchanterBlockEntity.OUTPUT_SLOT);
        }
        state.item.update(shown, ItemDisplayContext.GROUND, enchanter.getLevel(), 0);
    }

    @Override
    public void submit(EnchanterRenderState state, PoseStack poseStack, MultiBufferSource buffers, Vec3 cameraPos) {
        float speed = state.speed;
        int glowColor = ARGB.color(255, Mth.lerpInt(speed, 70, 255), Mth.lerpInt(speed, 90, 255), Mth.lerpInt(speed, 100, 255));
        int glowLight = speed > 0.5F ? FULL_BRIGHT : state.lightCoords;
        VertexConsumer solid = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        for (int i = 0; i < DEBRIS; i++) {
            float bob = Mth.sin(state.ticks * 0.08F + i * 1.7F) * 1.2F * speed;
            float wobble = Mth.sin(state.ticks * 0.05F + i * 2.3F) * 6F * speed;
            float w = PLATE_WIDTH[i];
            float d = PLATE_DEPTH[i];
            poseStack.pushPose();
            poseStack.translate(0.5F, (PLATE_Y[i] + bob) / 16F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(i * 45F + state.spin));
            poseStack.translate(RING_RADIUS / 16F, 0F, 0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(PLATE_TILT[i] + wobble));
            PoseStack.Pose pose = poseStack.last();
            box(solid, pose, -d / 2, 0, -w / 2, d / 2, PLATE_HEIGHT, w / 2, ROCK_TOP, ROCK_SIDE, ROCK_TOP, 0xFFFFFFFF, state.lightCoords);
            float inset = 1.5F;
            box(solid, pose, -d / 2 + inset, -1F, -w / 2 + inset, d / 2 - inset, 0F, w / 2 - inset, GLOW, GLOW, GLOW, glowColor, glowLight);
            poseStack.popPose();
        }

        if (!state.item.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, ITEM_Y + Mth.sin(state.ticks * 0.06F) * 0.05F, 0.5F);
            poseStack.scale(0.6F, 0.6F, 0.6F);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.ticks * (1.5F + 4F * speed) % 360F));
            state.item.submit(poseStack, buffers, speed > 0.5F ? FULL_BRIGHT : state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        if (speed > 0.02F) {
            int alpha = (int) (speed * (150 + 60 * Mth.sin(state.ticks * 0.2F)));
            int columnColor = ARGB.color(Math.clamp(alpha, 0, 255), 134, 231, 243);
            float half = 2F / 16F;
            box(buffers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE)), poseStack.last(), 0.5F * 16 - 2, COLUMN_BOTTOM * 16,
                    0.5F * 16 - 2, 0.5F * 16 + 2, COLUMN_TOP * 16, 0.5F * 16 + 2, COLUMN, COLUMN, COLUMN, columnColor, FULL_BRIGHT);
        }
    }

    private static void box(VertexConsumer buffer, PoseStack.Pose pose, float x0, float y0, float z0, float x1, float y1, float z1,
                            int[] top, int[] side, int[] bottom, int color, int light) {
        float ax = x0 / 16F;
        float ay = y0 / 16F;
        float az = z0 / 16F;
        float bx = x1 / 16F;
        float by = y1 / 16F;
        float bz = z1 / 16F;
        float w = x1 - x0;
        float h = y1 - y0;
        float d = z1 - z0;
        quad(buffer, pose, color, light, 0, 1, 0, top, w, d,
                ax, by, az, ax, by, bz, bx, by, bz, bx, by, az);
        quad(buffer, pose, color, light, 0, -1, 0, bottom, w, d,
                ax, ay, bz, ax, ay, az, bx, ay, az, bx, ay, bz);
        quad(buffer, pose, color, light, 0, 0, -1, side, w, h,
                bx, by, az, bx, ay, az, ax, ay, az, ax, by, az);
        quad(buffer, pose, color, light, 0, 0, 1, side, w, h,
                ax, by, bz, ax, ay, bz, bx, ay, bz, bx, by, bz);
        quad(buffer, pose, color, light, -1, 0, 0, side, d, h,
                ax, by, az, ax, ay, az, ax, ay, bz, ax, by, bz);
        quad(buffer, pose, color, light, 1, 0, 0, side, d, h,
                bx, by, bz, bx, ay, bz, bx, ay, az, bx, by, az);
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose, int color, int light, float nx, float ny, float nz, int[] region,
                             float uSpan, float vSpan, float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3) {
        float u0 = region[0] / ATLAS;
        float v0 = region[1] / ATLAS;
        float u1 = (region[0] + Math.min(16F, uSpan)) / ATLAS;
        float v1 = (region[1] + Math.min(16F, vSpan)) / ATLAS;
        vertex(buffer, pose, color, light, nx, ny, nz, x0, y0, z0, u0, v0);
        vertex(buffer, pose, color, light, nx, ny, nz, x1, y1, z1, u0, v1);
        vertex(buffer, pose, color, light, nx, ny, nz, x2, y2, z2, u1, v1);
        vertex(buffer, pose, color, light, nx, ny, nz, x3, y3, z3, u1, v0);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, int color, int light, float nx, float ny, float nz,
                               float x, float y, float z, float u, float v) {
        buffer.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    @Override
    public AABB getRenderBoundingBox(AbyssalEnchanterBlockEntity enchanter) {
        return new AABB(enchanter.getBlockPos()).inflate(1.5, 0, 1.5).expandTowards(0, 2, 0);
    }

    public static class EnchanterRenderState extends BERenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public float spin;
        public float speed;
        public float ticks;
    }
}
