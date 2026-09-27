package com.breakinblocks.nautec.client.sonar;

import com.breakinblocks.nautec.registries.NTParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** One source's contacts and wave; multiple submarines retain independent scan lifetimes. */
public final class SonarScan {
    private static final int FADE_TICKS = 100;
    private final Vec3 center;
    private final float range;
    private final float hostileRange;
    private final float waveRange;
    private final float waveDuration;
    private @Nullable SonarScanner scanner;
    private List<NautecSonarManager.Mark> marks = List.of();
    private List<NautecSonarManager.Mark> hostiles = List.of();
    private int ticksLeft;
    private int age;

    SonarScan(ClientLevel level, Vec3 center, float range, float hostileRange, int lifetime) {
        this.center = center;
        this.range = range;
        this.hostileRange = hostileRange;
        this.ticksLeft = lifetime;
        Minecraft minecraft = Minecraft.getInstance();
        waveRange = Math.max(Math.max(range, hostileRange), minecraft.options.getEffectiveRenderDistance() * 16F);
        waveDuration = SonarWave.durationTicks(minecraft.options.renderDistance().get());
        scanner = new SonarScanner(level, center, range);
    }

    public Vec3 center() { return center; }
    public float range() { return range; }
    public boolean active() { return ticksLeft > 0; }
    public boolean isPulseActive() { return active() && age <= waveDuration + 1F; }
    public float pulseRadius(float partialTick) {
        return SonarWave.radius(waveRange, Math.max(0F, age - 1F + partialTick), waveDuration);
    }
    public float fade() { return Math.min(1F, (float) ticksLeft / FADE_TICKS); }
    public List<NautecSonarManager.Mark> marks() { return marks; }
    public List<NautecSonarManager.Mark> hostiles() { return hostiles; }
    boolean needsScan() { return scanner != null; }

    void tick(ClientLevel level, int sectionBudget) {
        ticksLeft--;
        age++;
        if (!active()) return;
        if (scanner != null && sectionBudget > 0) {
            List<SonarScanner.Cluster> previous = scanner.collectClusters();
            scanner.tick(sectionBudget);
            List<SonarScanner.Cluster> discovered = scanner.collectClusters();
            if (discovered != previous) {
                marks = discovered.stream().map(cluster -> new NautecSonarManager.Mark(cluster.box().inflate(0.02D),
                        cluster.ore().color(), (float) cluster.box().getCenter().distanceTo(center))).toList();
            }
            if (scanner.isDone()) scanner = null;
        }
        List<NautecSonarManager.Mark> tracked = new ArrayList<>();
        AABB search = AABB.ofSize(center, hostileRange * 2D, hostileRange * 2D, hostileRange * 2D);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, search, entity -> entity instanceof Enemy && entity.isAlive())) {
            double distanceSquared = living.position().distanceToSqr(center);
            if (distanceSquared <= (double) hostileRange * hostileRange) {
                tracked.add(new NautecSonarManager.Mark(living.getBoundingBox().inflate(0.1D),
                        NautecSonarManager.HOSTILE_COLOR, (float) Math.sqrt(distanceSquared),
                        living.getPosition(0F).subtract(living.position())));
            }
        }
        hostiles = List.copyOf(tracked);
        if (isPulseActive()) {
            float previousRadius = SonarWave.radius(waveRange, age - 1F, waveDuration);
            float radius = SonarWave.radius(waveRange, age, waveDuration);
            for (NautecSonarManager.Mark mark : marks) {
                if (mark.revealAt() > previousRadius && mark.revealAt() <= radius) {
                    Vec3 at = mark.box().getCenter();
                    level.addParticle(NTParticles.SONAR_MOTE.get(), at.x, at.y, at.z, 0D, 0.01D, 0D);
                }
            }
        }
    }
}
