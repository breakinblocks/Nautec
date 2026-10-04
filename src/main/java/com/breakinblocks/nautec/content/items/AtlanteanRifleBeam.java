package com.breakinblocks.nautec.content.items;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class AtlanteanRifleBeam {
    private static final double SWEEP_INFLATION = 0.5D;
    private static final double BOUNCE_OFFSET = 0.01D;
    private static final double MIN_BOUNCE_RANGE = 0.5D;

    public record Hit(Vec3 origin, Vec3 end, List<Entity> entities, boolean blocked) {
        public double length() {
            return origin.distanceTo(end);
        }

        public @Nullable Entity entity() {
            return entities.isEmpty() ? null : entities.getFirst();
        }

        public boolean impact() {
            return blocked || !entities.isEmpty();
        }
    }

    private record Candidate(Entity entity, Vec3 location, double distance) {
    }

    public static Hit trace(Level level, LivingEntity shooter, double range, float partialTick) {
        return traceAll(level, shooter, range, partialTick).getFirst();
    }

    public static List<Hit> traceAll(Level level, LivingEntity shooter, double range, float partialTick) {
        int pierce = Math.max(0, AtlanteanRifleItem.pierceCount(shooter));
        int bounces = Math.max(0, AtlanteanRifleItem.ricochetCount(shooter));
        List<Hit> segments = new ArrayList<>();
        Set<Entity> struck = new HashSet<>();
        Vec3 origin = shooter.getEyePosition(partialTick);
        Vec3 direction = shooter.getViewVector(partialTick);
        double remaining = range;

        while (true) {
            Vec3 end = origin.add(direction.scale(remaining));
            BlockHitResult blockHit = level.clip(new ClipContext(origin, end,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
            boolean blocked = blockHit.getType() != HitResult.Type.MISS;
            if (blocked) {
                end = blockHit.getLocation();
            }

            List<Candidate> candidates = candidates(level, shooter, origin, end, struck);
            int limit = pierce + 1;
            if (candidates.size() >= limit) {
                List<Entity> entities = new ArrayList<>(limit);
                for (int i = 0; i < limit; i++) {
                    entities.add(candidates.get(i).entity());
                }
                segments.add(new Hit(origin, candidates.get(limit - 1).location(), entities, false));
                return segments;
            }

            List<Entity> entities = new ArrayList<>(candidates.size());
            for (Candidate candidate : candidates) {
                entities.add(candidate.entity());
            }
            struck.addAll(entities);
            Hit hit = new Hit(origin, end, entities, blocked);
            segments.add(hit);

            remaining -= hit.length();
            if (!blocked || bounces-- <= 0 || remaining < MIN_BOUNCE_RANGE) {
                return segments;
            }
            Direction face = blockHit.getDirection();
            Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
            direction = direction.subtract(normal.scale(2D * direction.dot(normal))).normalize();
            origin = end.add(normal.scale(BOUNCE_OFFSET));
        }
    }

    private static List<Candidate> candidates(Level level, LivingEntity shooter, Vec3 origin, Vec3 end, Set<Entity> excluded) {
        AABB sweep = new AABB(origin, end).inflate(SWEEP_INFLATION);
        List<Candidate> candidates = new ArrayList<>();
        for (Entity entity : level.getEntities(shooter, sweep, entity -> canHit(shooter, entity) && !excluded.contains(entity))) {
            AABB box = entity.getBoundingBox().inflate(entity.getPickRadius());
            if (box.contains(origin)) {
                candidates.add(new Candidate(entity, origin, 0D));
                continue;
            }
            Optional<Vec3> clip = box.clip(origin, end);
            clip.ifPresent(location -> candidates.add(new Candidate(entity, location, origin.distanceToSqr(location))));
        }
        candidates.sort(Comparator.comparingDouble(Candidate::distance));
        return candidates;
    }

    private static boolean canHit(LivingEntity shooter, Entity entity) {
        return entity != shooter
                && entity.isPickable()
                && !entity.isSpectator()
                && !entity.isPassengerOfSameVehicle(shooter);
    }

    private AtlanteanRifleBeam() {
    }
}
