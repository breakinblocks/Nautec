package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.items.AtlanteanRifleBeam;
import com.breakinblocks.nautec.content.items.AtlanteanRifleItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AtlanteanRifleBeamRenderer {
    private static final float HALF_WIDTH_COLD = 0.08F;
    private static final float HALF_WIDTH_HOT = 0.16F;
    private static final float INTENSITY_COLD = 0.6F;
    private static final float INTENSITY_HOT = 1.0F;
    private static final float MUZZLE_FLARE_COLD = 0.09F;
    private static final float MUZZLE_FLARE_HOT = 0.18F;
    private static final float IMPACT_FLARE_COLD = 0.28F;
    private static final float IMPACT_FLARE_HOT = 0.5F;
    private static final double MIN_LENGTH = 0.05D;

    private static final double FALLBACK_FORWARD = 0.7D;
    private static final double FALLBACK_SIDE = 0.28D;
    private static final double FALLBACK_DOWN = 0.36D;
    private static final long SAMPLE_LIFETIME_NANOS = 100_000_000L;

    private record MuzzleSample(Vec3 worldPos, long capturedAt) {
    }

    private record TraceSample(Level level, long gameTime, float partialTick, Vec3 eye, Vec3 view,
                               List<AtlanteanRifleBeam.Hit> hits) {
    }

    private static final Map<Integer, MuzzleSample> MUZZLES = new HashMap<>();
    private static final Map<Integer, TraceSample> TRACES = new HashMap<>();
    private static @Nullable Level trackedLevel;

    private AtlanteanRifleBeamRenderer() {
    }

    public static void trackMuzzle(int ownerId, Vec3 worldPos) {
        MUZZLES.put(ownerId, new MuzzleSample(worldPos, System.nanoTime()));
    }

    public static Vec3 muzzleWorld(Player player) {
        MuzzleSample sample = MUZZLES.get(player.getId());
        if (sample != null && System.nanoTime() - sample.capturedAt() <= SAMPLE_LIFETIME_NANOS) {
            return sample.worldPos();
        }
        Vec3 look = player.getLookAngle();
        Vec3 right = look.cross(new Vec3(0D, 1D, 0D)).normalize();
        Vec3 up = right.cross(look).normalize();
        double side = player.getMainArm() == HumanoidArm.LEFT ? -1D : 1D;
        return player.getEyePosition()
                .add(look.scale(FALLBACK_FORWARD))
                .add(right.scale(FALLBACK_SIDE * side))
                .subtract(up.scale(FALLBACK_DOWN));
    }

    public static void forget() {
        MUZZLES.clear();
        TRACES.clear();
        trackedLevel = null;
    }

    public static void tick(Level level) {
        if (level != trackedLevel) {
            forget();
            trackedLevel = level;
            return;
        }
        long now = System.nanoTime();
        MUZZLES.values().removeIf(sample -> now - sample.capturedAt() > SAMPLE_LIFETIME_NANOS);
        long gameTime = level.getGameTime();
        TRACES.values().removeIf(sample -> sample.gameTime() < gameTime - 1);
    }

    public static List<AtlanteanRifleBeam.Hit> trace(Level level, LivingEntity holder, float partialTick) {
        long gameTime = level.getGameTime();
        Vec3 eye = holder.getEyePosition(partialTick);
        Vec3 view = holder.getViewVector(partialTick);
        TraceSample sample = TRACES.get(holder.getId());
        if (sample != null && sample.level() == level && sample.gameTime() == gameTime
                && sample.partialTick() == partialTick && sample.eye().equals(eye) && sample.view().equals(view)) {
            return sample.hits();
        }
        List<AtlanteanRifleBeam.Hit> hits = AtlanteanRifleBeam.traceAll(level, holder, NTConfig.rifleRange, partialTick);
        TRACES.put(holder.getId(), new TraceSample(level, gameTime, partialTick, eye, view, hits));
        return hits;
    }

    public static void submitBeam(PoseStack poseStack, SubmitNodeCollector collector, Vec3 from, Vec3 to,
                                  float ramp, boolean impact) {
        submitBeam(poseStack, collector, from, to, ramp, impact, false);
    }

    public static void submitWorldBeam(Vec3 camera, SubmitNodeCollector collector, Vec3 from, Vec3 to, float ramp, boolean impact) {
        ShaderPackOverlay.anchored(camera, () -> submitBeam(new PoseStack(), collector, from.subtract(camera), to.subtract(camera), ramp, impact, true));
    }

    public static void submitBeam(PoseStack poseStack, SubmitNodeCollector collector, Vec3 from, Vec3 to,
                                  float ramp, boolean impact, boolean world) {
        if (to.distanceTo(from) < MIN_LENGTH) {
            return;
        }

        int color = ARGB.color(Math.round(Mth.lerp(ramp, INTENSITY_COLD, INTENSITY_HOT) * 255F), LaserBeamRenderer.CYAN);
        Vector3f start = from.toVector3f();
        Vector3f end = to.toVector3f();

        LaserBeamRenderer.submitBeam(poseStack, collector, start, end, Mth.lerp(ramp, HALF_WIDTH_COLD, HALF_WIDTH_HOT), color, world);
        LaserBeamRenderer.submitFlare(poseStack, collector, start, Mth.lerp(ramp, MUZZLE_FLARE_COLD, MUZZLE_FLARE_HOT), color, world);
        if (impact) {
            LaserBeamRenderer.submitFlare(poseStack, collector, end, Mth.lerp(ramp, IMPACT_FLARE_COLD, IMPACT_FLARE_HOT), color, world);
        }
    }
}
