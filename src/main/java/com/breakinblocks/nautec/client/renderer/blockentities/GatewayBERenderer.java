package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.renderer.blockentities.NTBERenderer;
import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.api.gateways.GatewayRing;
import com.breakinblocks.nautec.client.render.JsonMesh;
import com.breakinblocks.nautec.client.render.NTRenderTypes;
import com.breakinblocks.nautec.client.render.ShaderPackOverlay;
import com.breakinblocks.nautec.content.blockentities.GatewayBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GatewayBERenderer extends NTBERenderer<GatewayBlockEntity, GatewayBERenderer.GatewayRenderState> {
    public static final Identifier TEXTURE = Nautec.rl("textures/entity/gateway_ring.png");

    private static final float DIAL_TICKS = 26F;
    private static final float DIAL_SPIN = 200F;
    private static final float[] LOCK_AT = {3F, 7F, 11F, 15F};
    private static final float MASTER_LOCK_AT = 19F;
    private static final float OPEN_AT = 22F;
    private static final float OPEN_TICKS = 6F;
    private static final float CLOSE_TICKS = 12F;
    private static final int RIPPLE_TICKS = 40;
    private static final int IDLE_CHEVRON = 0xFF7FD9E8;

    public GatewayBERenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public GatewayRenderState createRenderState() {
        return new GatewayRenderState();
    }

    @Override
    public void extractRenderState(GatewayBlockEntity gateway, GatewayRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(gateway, state, crumbling);
        state.formed = gateway.isFormed();
        if (!state.formed || gateway.getLevel() == null) {
            return;
        }
        long gameTime = gateway.getLevel().getGameTime();
        state.ticks = (float) (gameTime % 2_400_000L) + partialTick;
        state.sinceChange = (float) Math.min(gameTime - gateway.getLinkChangedAt(), 100_000L) + partialTick;
        state.open = gateway.isOpen();
        state.front = gateway.getFront();
        state.address = gateway.getAddress();
        state.light = LevelRenderer.getLightCoords(gateway.getLevel(), gateway.getBlockPos().above(GatewayRing.CENTRE));
        state.ripples.clear();
        for (GatewayBlockEntity.Ripple ripple : gateway.getRipples()) {
            state.ripples.add(new float[]{ripple.x(), ripple.y(), (float) (gameTime - ripple.startedAt()) + partialTick});
        }
    }

    @Override
    public void submit(GatewayRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.formed) {
            return;
        }
        float energy = energy(state);
        float open = open(state);

        poseStack.pushPose();
        poseStack.translate(0.5, GatewayRing.CENTRE + 0.5, 0.5);
        Vec3 normal = GatewayRing.normal(state.front);
        poseStack.mulPose(Axis.YP.rotation((float) Math.atan2(normal.x, normal.z)));

        JsonMesh mesh = JsonMesh.GATEWAY_RING;
        int glowColor = ((int) (Mth.clamp(energy, 0F, 1F) * 255F) << 24) | 0x52E8FF;

        JsonMesh.submitLit(poseStack, collector, mesh.part("frame"), RenderTypes.entityCutout(TEXTURE), 0xFFFFFFFF, state.light, OverlayTexture.NO_OVERLAY);
        JsonMesh.submitTranslucent(poseStack, collector, mesh.part("frame"), NTRenderTypes.gatewayGlow(TEXTURE), glowColor, true);

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(trackAngle(state)));
        JsonMesh.submitLit(poseStack, collector, mesh.part("track"), RenderTypes.entityCutout(TEXTURE), 0xFFFFFFFF, state.light, OverlayTexture.NO_OVERLAY);
        JsonMesh.submitTranslucent(poseStack, collector, mesh.part("track"), NTRenderTypes.gatewayGlow(TEXTURE), glowColor, true);
        poseStack.popPose();

        for (int chevron = 0; chevron < GatewayRing.CHEVRONS; chevron++) {
            submitChevron(state, poseStack, collector, mesh, chevron);
        }

        if (open > 0F) {
            submitHorizon(poseStack, collector, open);
        }
        for (float[] ripple : state.ripples) {
            submitRipple(poseStack, collector, ripple[0], ripple[1], ripple[2]);
        }
        poseStack.popPose();
    }

    private static float energy(GatewayRenderState state) {
        if (state.open) {
            return Mth.clamp(0.25F + state.sinceChange / OPEN_AT * 0.75F, 0.25F, 1F);
        }
        return Mth.clamp(1F - state.sinceChange / 20F, 0.15F, 1F) * 0.3F;
    }

    private static float open(GatewayRenderState state) {
        if (state.open) {
            return Mth.clamp((state.sinceChange - OPEN_AT) / OPEN_TICKS, 0F, 1F);
        }
        return Mth.clamp(1F - state.sinceChange / CLOSE_TICKS, 0F, 1F);
    }

    private static float trackAngle(GatewayRenderState state) {
        float drift = state.open ? state.ticks * 0.06F : 0F;
        if (!state.open) {
            return drift;
        }
        float progress = Mth.clamp(state.sinceChange / DIAL_TICKS, 0F, 1F);
        float eased = 1F - (1F - progress) * (1F - progress) * (1F - progress);
        return drift + DIAL_SPIN * eased;
    }

    private static float chevronLit(GatewayRenderState state, int chevron) {
        int slot = GatewayRing.slotOfChevron(chevron);
        float lockAt = slot >= 0 ? LOCK_AT[slot] : MASTER_LOCK_AT;
        if (state.open) {
            return Mth.clamp((state.sinceChange - lockAt) / 4F, 0F, 1F);
        }
        return Mth.clamp(1F - state.sinceChange / 20F, 0F, 1F);
    }

    private static void submitChevron(GatewayRenderState state, PoseStack poseStack, SubmitNodeCollector collector, JsonMesh mesh, int chevron) {
        float lit = chevronLit(state, chevron);
        int slot = GatewayRing.slotOfChevron(chevron);
        int dye = slot >= 0 ? 0xFF000000 | dyeColour(state.address.slots().get(slot)) : IDLE_CHEVRON;

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(40F * chevron));
        JsonMesh.submitLit(poseStack, collector, mesh.part("chevron"), RenderTypes.entityCutout(TEXTURE), 0xFFFFFFFF, state.light, OverlayTexture.NO_OVERLAY);

        int glassAlpha = (int) Mth.lerp(lit, 140F, 255F);
        int glass = (glassAlpha << 24) | (slot >= 0 ? scale(dye, 0.55F + 0.45F * lit) : scale(IDLE_CHEVRON, 0.5F + 0.5F * lit)) & 0xFFFFFF;
        JsonMesh.submitTranslucent(poseStack, collector, mesh.part("chevron_glass"), NTRenderTypes.crystalShell(TEXTURE), glass, true);
        if (lit > 0F) {
            int glow = 0xFF000000 | scale(slot >= 0 ? dye : 0xFFC8FAFF, lit) & 0xFFFFFF;
            JsonMesh.submitTranslucent(poseStack, collector, mesh.part("chevron_glass"), NTRenderTypes.crystalCore(TEXTURE), glow, true);
        }
        poseStack.popPose();
    }

    private static int dyeColour(DyeColor colour) {
        return colour == DyeColor.BLACK ? 0x3A3F5A : colour.getTextureDiffuseColor() & 0xFFFFFF;
    }

    private static int scale(int rgb, float amount) {
        int r = (int) (((rgb >> 16) & 0xFF) * amount);
        int g = (int) (((rgb >> 8) & 0xFF) * amount);
        int b = (int) ((rgb & 0xFF) * amount);
        return Mth.clamp(r, 0, 255) << 16 | Mth.clamp(g, 0, 255) << 8 | Mth.clamp(b, 0, 255);
    }

    private static void submitHorizon(PoseStack poseStack, SubmitNodeCollector collector, float open) {
        float r = (float) GatewayRing.OPENING_RADIUS;
        int color = ((int) (open * 255F) << 24) | 0xFFFFFF;
        ShaderPackOverlay.submit(poseStack, collector, NTRenderTypes.gatewayHorizon(), (pose, buffer) -> {
            quadVertex(buffer, pose, -r, -r, 0F, 0F, 1F, color);
            quadVertex(buffer, pose, r, -r, 0F, 1F, 1F, color);
            quadVertex(buffer, pose, r, r, 0F, 1F, 0F, color);
            quadVertex(buffer, pose, -r, r, 0F, 0F, 0F, color);
        });
    }

    private static void submitRipple(PoseStack poseStack, SubmitNodeCollector collector, float x, float y, float age) {
        float life = Mth.clamp(age / RIPPLE_TICKS, 0F, 1F);
        float size = 0.6F + life * 3.2F;
        int color = ((int) ((1F - life) * 220F) << 24) | 0x9AF4FF;
        ShaderPackOverlay.submit(poseStack, collector, NTRenderTypes.crystalHalo(), (pose, buffer) -> {
            quadVertex(buffer, pose, x - size, y - size, 0.02F, 0F, 0F, color);
            quadVertex(buffer, pose, x + size, y - size, 0.02F, 1F, 0F, color);
            quadVertex(buffer, pose, x + size, y + size, 0.02F, 1F, 1F, color);
            quadVertex(buffer, pose, x - size, y + size, 0.02F, 0F, 1F, color);
        });
    }

    private static void quadVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setColor(color);
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(GatewayBlockEntity gateway) {
        return new AABB(gateway.getBlockPos()).inflate(7, 0, 7).expandTowards(0, GatewayRing.SIZE, 0);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    public static class GatewayRenderState extends BlockEntityRenderState {
        public boolean formed;
        public boolean open;
        public float ticks;
        public float sinceChange;
        public Direction front = Direction.SOUTH;
        public GatewayAddress address = GatewayAddress.DEFAULT;
        public int light;
        public final List<float[]> ripples = new ArrayList<>();
    }
}
