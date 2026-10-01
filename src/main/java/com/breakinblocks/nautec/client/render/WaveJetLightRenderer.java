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
    private static final int SEGMENTS = 20;
    private static final int SLICES = 10;
    private static final int CORE_COLOR = 0xBFE8FF;

    private static final float LENS_RADIUS = 0.06F;
    private static final float SPREAD = 0.24F;
    private static final float FADE_IN_DISTANCE = 1.2F;
    private static final float FALLOFF_POWER = 1.6F;
    private static final float PEAK_ALPHA = 26.0F;
    private static final float[] SHELL_SCALES = {0.3F, 0.55F, 0.8F, 1.0F};
    private static final float[] SHELL_WEIGHTS = {1.0F, 0.6F, 0.35F, 0.18F};

    private static final double FIRST_PERSON_FORWARD = 0.75D;
    private static final double FIRST_PERSON_DROP = 0.28D;
    private static final double THIRD_PERSON_FORWARD = 0.6D;
    private static final double THIRD_PERSON_DROP = 0.45D;

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
        Vec3 direction = player.getViewVector(partialTick);
        Vec3 side = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 1.0E-4D) {
            side = direction.cross(new Vec3(1.0D, 0.0D, 0.0D));
        }
        side = side.normalize();
        Vec3 up = side.cross(direction).normalize();

        Vec3 origin = lens(player, direction, up, partialTick);
        float length = reach(level, player, origin, direction);
        if (length <= FADE_IN_DISTANCE * 0.25F) {
            return;
        }

        for (int shell = 0; shell < SHELL_SCALES.length; shell++) {
            float scale = SHELL_SCALES[shell];
            float peak = PEAK_ALPHA * SHELL_WEIGHTS[shell];
            for (int slice = 0; slice < SLICES; slice++) {
                float nearDistance = length * slice / SLICES;
                float farDistance = length * (slice + 1) / SLICES;
                Vec3 nearCentre = origin.add(direction.scale(nearDistance)).subtract(cameraPos);
                Vec3 farCentre = origin.add(direction.scale(farDistance)).subtract(cameraPos);
                float nearRadius = (LENS_RADIUS + nearDistance * SPREAD) * scale;
                float farRadius = (LENS_RADIUS + farDistance * SPREAD) * scale;
                int nearColor = ARGB.color(alpha(peak, nearDistance, length), CORE_COLOR);
                int farColor = ARGB.color(alpha(peak, farDistance, length), CORE_COLOR);

                for (int segment = 0; segment < SEGMENTS; segment++) {
                    float from = (float) (Math.PI * 2.0 * segment / SEGMENTS);
                    float to = (float) (Math.PI * 2.0 * (segment + 1) / SEGMENTS);

                    vertex(pose, buffer, ring(nearCentre, side, up, from, nearRadius), nearColor);
                    vertex(pose, buffer, ring(nearCentre, side, up, to, nearRadius), nearColor);
                    vertex(pose, buffer, ring(farCentre, side, up, to, farRadius), farColor);
                    vertex(pose, buffer, ring(farCentre, side, up, from, farRadius), farColor);
                }
            }
        }
    }

    private static int alpha(float peak, float distance, float length) {
        float fadeIn = Mth.clamp(distance / FADE_IN_DISTANCE, 0.0F, 1.0F);
        float falloff = (float) Math.pow(1.0F - Mth.clamp(distance / length, 0.0F, 1.0F), FALLOFF_POWER);
        return Mth.clamp(Math.round(peak * fadeIn * fadeIn * falloff), 0, 255);
    }

    private static Vec3 lens(Player player, Vec3 direction, Vec3 up, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Vec3 eyes = player.getEyePosition(partialTick);
        if (player == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
            return eyes.add(direction.scale(FIRST_PERSON_FORWARD)).subtract(up.scale(FIRST_PERSON_DROP));
        }
        return eyes.add(direction.scale(THIRD_PERSON_FORWARD)).subtract(0.0D, THIRD_PERSON_DROP, 0.0D);
    }

    private static Vec3 ring(Vec3 centre, Vec3 side, Vec3 up, float angle, float radius) {
        return centre
                .add(side.scale(Mth.cos(angle) * radius))
                .add(up.scale(Mth.sin(angle) * radius));
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, Vec3 position, int color) {
        buffer.addVertex(pose, (float) position.x, (float) position.y, (float) position.z).setColor(color);
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
