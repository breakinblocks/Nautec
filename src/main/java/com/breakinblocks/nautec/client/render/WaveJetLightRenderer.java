package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.items.WaveJetSpotlight;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class WaveJetLightRenderer {
    private static final int SEGMENTS = 14;
    private static final int CORE_COLOR = 0xBFE8FF;

    private static final float NEAR_DISTANCE = 0.55F;
    private static final float NEAR_RADIUS = 0.12F;
    private static final float SPREAD = 0.22F;

    private static final int NEAR_ALPHA = 70;
    private static final int FAR_ALPHA = 0;

    private static @Nullable ByteBufferBuilder renderBuffer;

    @SubscribeEvent
    public static void render(RenderLevelStageEvent.AfterLevel event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        Vec3 cameraPos = event.getLevelRenderState().cameraRenderState.pos;
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().set(event.getModelViewMatrix());

        MultiBufferSource.BufferSource source = null;
        for (Player player : level.players()) {
            if (WaveJetSpotlight.litWaveJet(player) == null) {
                continue;
            }
            if (source == null) {
                if (renderBuffer == null) {
                    renderBuffer = new ByteBufferBuilder(4096);
                }
                source = MultiBufferSource.immediate(renderBuffer);
            }
            renderCone(level, player, poseStack.last(), source.getBuffer(NTRenderTypes.spotlightCone()), cameraPos, partialTick);
        }
        if (source != null) {
            source.endBatch();
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        close();
    }

    @SubscribeEvent
    public static void onShutdown(GameShuttingDownEvent event) {
        close();
    }

    private static void close() {
        if (renderBuffer != null) {
            renderBuffer.close();
            renderBuffer = null;
        }
    }

    private static void renderCone(ClientLevel level, Player player, PoseStack.Pose pose, VertexConsumer buffer,
                                   Vec3 cameraPos, float partialTick) {
        Vec3 origin = muzzle(player, partialTick);
        Vec3 direction = player.getViewVector(partialTick);
        float length = reach(level, player, origin, direction);
        if (length <= NEAR_DISTANCE) {
            return;
        }

        Vec3 side = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 1.0E-4D) {
            side = direction.cross(new Vec3(1.0D, 0.0D, 0.0D));
        }
        side = side.normalize();
        Vec3 up = side.cross(direction).normalize();

        Vec3 nearCentre = origin.add(direction.scale(NEAR_DISTANCE)).subtract(cameraPos);
        Vec3 farCentre = origin.add(direction.scale(length)).subtract(cameraPos);
        float farRadius = NEAR_RADIUS + (length - NEAR_DISTANCE) * SPREAD;

        int nearColor = ARGB.color(NEAR_ALPHA, CORE_COLOR);
        int farColor = ARGB.color(FAR_ALPHA, CORE_COLOR);

        for (int segment = 0; segment < SEGMENTS; segment++) {
            float from = (float) (Math.PI * 2.0 * segment / SEGMENTS);
            float to = (float) (Math.PI * 2.0 * (segment + 1) / SEGMENTS);

            Vec3 nearFrom = ring(nearCentre, side, up, from, NEAR_RADIUS);
            Vec3 nearTo = ring(nearCentre, side, up, to, NEAR_RADIUS);
            Vec3 farFrom = ring(farCentre, side, up, from, farRadius);
            Vec3 farTo = ring(farCentre, side, up, to, farRadius);

            vertex(pose, buffer, nearFrom, nearColor);
            vertex(pose, buffer, nearTo, nearColor);
            vertex(pose, buffer, farTo, farColor);
            vertex(pose, buffer, farFrom, farColor);
        }
    }

    private static Vec3 ring(Vec3 centre, Vec3 side, Vec3 up, float angle, float radius) {
        return centre
                .add(side.scale(Mth.cos(angle) * radius))
                .add(up.scale(Mth.sin(angle) * radius));
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, Vec3 position, int color) {
        buffer.addVertex(pose, (float) position.x, (float) position.y, (float) position.z).setColor(color);
    }

    private static Vec3 muzzle(Player player, float partialTick) {
        return player.getEyePosition(partialTick).subtract(0.0D, 0.2D, 0.0D);
    }

    private static float reach(ClientLevel level, Player player, Vec3 origin, Vec3 direction) {
        Vec3 end = origin.add(direction.scale(NTConfig.waveJetLightRange));
        BlockHitResult hit = level.clip(new ClipContext(origin, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.BLOCK
                ? (float) origin.distanceTo(hit.getLocation())
                : NTConfig.waveJetLightRange;
    }

    private WaveJetLightRenderer() {
    }
}
