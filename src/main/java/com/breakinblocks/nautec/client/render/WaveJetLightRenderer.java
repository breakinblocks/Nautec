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
import com.breakinblocks.nautec.utils.ARGB;
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

    private static final float[] COS = new float[SEGMENTS + 1];
    private static final float[] SIN = new float[SEGMENTS + 1];

    static {
        for (int i = 0; i <= SEGMENTS; i++) {
            float angle = (float) (Math.PI * 2.0 * i / SEGMENTS);
            COS[i] = Mth.cos(angle);
            SIN[i] = Mth.sin(angle);
        }
    }

    private static @Nullable ByteBufferBuilder renderBuffer;

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        Vec3 cameraPos = event.getCamera().getPosition();
        float partialTick = minecraft.getTimer().getGameTimeDeltaPartialTick(false);
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

        float sx = (float) side.x;
        float sy = (float) side.y;
        float sz = (float) side.z;
        float ux = (float) up.x;
        float uy = (float) up.y;
        float uz = (float) up.z;
        float[] nearRing = new float[(SEGMENTS + 1) * 3];
        float[] farRing = new float[(SEGMENTS + 1) * 3];
        for (int shell = 0; shell < SHELL_SCALES.length; shell++) {
            float scale = SHELL_SCALES[shell];
            float peak = PEAK_ALPHA * SHELL_WEIGHTS[shell];
            fillRing(nearRing, origin, direction, cameraPos, 0.0F, scale, sx, sy, sz, ux, uy, uz);
            int nearColor = ARGB.color(alpha(peak, 0.0F, length), CORE_COLOR);
            for (int slice = 0; slice < SLICES; slice++) {
                float farDistance = length * (slice + 1) / SLICES;
                fillRing(farRing, origin, direction, cameraPos, farDistance, scale, sx, sy, sz, ux, uy, uz);
                int farColor = ARGB.color(alpha(peak, farDistance, length), CORE_COLOR);

                for (int segment = 0; segment < SEGMENTS; segment++) {
                    int a = segment * 3;
                    int b = a + 3;
                    buffer.addVertex(pose, nearRing[a], nearRing[a + 1], nearRing[a + 2]).setColor(nearColor);
                    buffer.addVertex(pose, nearRing[b], nearRing[b + 1], nearRing[b + 2]).setColor(nearColor);
                    buffer.addVertex(pose, farRing[b], farRing[b + 1], farRing[b + 2]).setColor(farColor);
                    buffer.addVertex(pose, farRing[a], farRing[a + 1], farRing[a + 2]).setColor(farColor);
                }

                float[] swap = nearRing;
                nearRing = farRing;
                farRing = swap;
                nearColor = farColor;
            }
        }
    }

    private static void fillRing(float[] ring, Vec3 origin, Vec3 direction, Vec3 cameraPos, float distance, float scale,
                                 float sx, float sy, float sz, float ux, float uy, float uz) {
        float cx = (float) (origin.x + direction.x * distance - cameraPos.x);
        float cy = (float) (origin.y + direction.y * distance - cameraPos.y);
        float cz = (float) (origin.z + direction.z * distance - cameraPos.z);
        float radius = (LENS_RADIUS + distance * SPREAD) * scale;
        for (int i = 0; i <= SEGMENTS; i++) {
            float c = COS[i] * radius;
            float sn = SIN[i] * radius;
            ring[i * 3] = cx + sx * c + ux * sn;
            ring[i * 3 + 1] = cy + sy * c + uy * sn;
            ring[i * 3 + 2] = cz + sz * c + uz * sn;
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
