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

    private static final int PULSE_TICKS = 70;
    private static final int FADE_TICKS = 100;

    private static @Nullable SonarScanner scanner;
    private static final List<Mark> marks = new ArrayList<>();
    private static List<Mark> hostiles = List.of();
    private static Vec3 center = Vec3.ZERO;
    private static float range;
    private static int ticksLeft;
    private static float pulseRadius;

    private NautecSonarManager() {
    }

    public record Mark(AABB box, int color, float revealAt) {
    }

    public static void begin(Vec3 origin, float pulseRange, int lifetime) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        center = origin;
        range = pulseRange;
        ticksLeft = lifetime;
        pulseRadius = 0F;
        marks.clear();
        hostiles = List.of();
        scanner = new SonarScanner(level, BlockPos.containing(origin), Mth.floor(pulseRange));
    }

    public static void clear() {
        scanner = null;
        marks.clear();
        hostiles = List.of();
        ticksLeft = 0;
        pulseRadius = 0F;
    }

    public static boolean isActive() {
        return ticksLeft > 0;
    }

    public static float pulseRadius() {
        return pulseRadius;
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
            tracked.add(new Mark(living.getBoundingBox().inflate(0.1D), HOSTILE_COLOR, 0F));
        }
        hostiles = List.copyOf(tracked);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            clear();
            return;
        }

        if (ticksLeft <= 0) {
            return;
        }

        ticksLeft--;
        pulseRadius = Math.min(range, pulseRadius + range / PULSE_TICKS);

        SonarScanner active = scanner;
        if (active != null) {
            active.tick();
            if (active.isDone()) {
                for (SonarScanner.Cluster cluster : active.collectClusters()) {
                    float distance = (float) cluster.box().getCenter().distanceTo(center);
                    marks.add(new Mark(cluster.box().inflate(0.02D), cluster.ore().color(), distance));
                }
                scanner = null;
            }
        }

        trackHostiles(minecraft.level);
        spawnRevealMotes(minecraft.level);
    }

    private static void spawnRevealMotes(ClientLevel level) {
        for (Mark mark : marks) {
            if (Math.abs(mark.revealAt() - pulseRadius) > range / PULSE_TICKS) {
                continue;
            }

            Vec3 at = mark.box().getCenter();
            level.addParticle(NTParticles.SONAR_MOTE.get(), at.x, at.y, at.z, 0D, 0.01D, 0D);
        }
    }
}
