package com.breakinblocks.nautec.client.sonar;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.registries.NTParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class NautecSonarManager {
    public static final int HOSTILE_COLOR = 0xFFFF5A3C;

    private static final int FADE_TICKS = 100;

    private static @Nullable SonarScanner scanner;
    private static @Nullable ClientLevel scanLevel;
    private static List<Mark> marks = List.of();
    private static List<Mark> hostiles = List.of();
    private static Vec3 center = Vec3.ZERO;
    private static float range;
    private static float waveRange;
    private static float waveDuration;
    private static int ticksLeft;
    private static int age;

    private NautecSonarManager() {
    }

    public record Mark(AABB box, int color, float revealAt) {
    }

    public static void begin(Vec3 origin, float pulseRange, int lifetime) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        center = origin;
        range = pulseRange;
        // Like Scannable, sweep the visible terrain; detection still uses the server's scan range.
        waveRange = Math.max(pulseRange, minecraft.options.getEffectiveRenderDistance() * 16F);
        waveDuration = SonarWave.durationTicks(minecraft.options.renderDistance().get());
        ticksLeft = lifetime;
        age = 0;
        scanLevel = level;
        marks = List.of();
        hostiles = List.of();
        scanner = new SonarScanner(level, BlockPos.containing(origin), Mth.floor(pulseRange));
    }

    public static void clear() {
        scanner = null;
        scanLevel = null;
        marks = List.of();
        hostiles = List.of();
        ticksLeft = 0;
        age = 0;
    }

    public static boolean isActive() {
        return ticksLeft > 0 && scanLevel == Minecraft.getInstance().level;
    }

    public static boolean isPulseActive() {
        return isActive() && age <= waveDuration + 1F;
    }

    public static float pulseRadius(float partialTick) {
        return SonarWave.radius(waveRange, Math.max(0F, age - 1F + partialTick), waveDuration);
    }

    public static Vec3 center() {
        return center;
    }

    public static float range() {
        return range;
    }

    public static float fade() {
        return ticksLeft >= FADE_TICKS ? 1F : (float) ticksLeft / FADE_TICKS;
    }

    public static List<Mark> marks() {
        return marks;
    }

    public static List<Mark> hostiles() {
        return hostiles;
    }

    private static void trackHostiles(ClientLevel level) {
        List<Mark> tracked = new ArrayList<>();
        AABB search = AABB.ofSize(center, range * 2D, range * 2D, range * 2D);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, search, entity -> entity instanceof Enemy)) {
            float distance = (float) living.position().distanceTo(center);
            if (distance <= range) {
                tracked.add(new Mark(living.getBoundingBox().inflate(0.1D), HOSTILE_COLOR, distance));
            }
        }
        hostiles = List.copyOf(tracked);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level != scanLevel) {
            clear();
            return;
        }

        if (ticksLeft <= 0) {
            return;
        }

        ticksLeft--;
        age++;

        SonarScanner active = scanner;
        if (active != null) {
            active.tick();
            List<Mark> discovered = new ArrayList<>();
            for (SonarScanner.Cluster cluster : active.collectClusters()) {
                float distance = (float) cluster.box().getCenter().distanceTo(center);
                discovered.add(new Mark(cluster.box().inflate(0.02D), cluster.ore().color(), distance));
            }
            marks = List.copyOf(discovered);
            if (active.isDone()) scanner = null;
        }

        trackHostiles(minecraft.level);
        spawnRevealMotes(minecraft.level);
    }

    private static void spawnRevealMotes(ClientLevel level) {
        if (age > waveDuration + 1F) return;
        float previousRadius = SonarWave.radius(waveRange, age - 1F, waveDuration);
        float radius = SonarWave.radius(waveRange, age, waveDuration);
        for (Mark mark : marks) {
            if (mark.revealAt() <= previousRadius || mark.revealAt() > radius) {
                continue;
            }

            Vec3 at = mark.box().getCenter();
            level.addParticle(NTParticles.SONAR_MOTE.get(), at.x, at.y, at.z, 0D, 0.01D, 0D);
        }
    }
}
