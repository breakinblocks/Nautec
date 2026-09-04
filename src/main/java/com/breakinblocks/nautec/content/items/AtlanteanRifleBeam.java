package com.breakinblocks.nautec.content.items;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class AtlanteanRifleBeam {
    private static final double SWEEP_INFLATION = 0.5D;

    public record Hit(Vec3 origin, Vec3 end, @Nullable Entity entity) {
        public double length() {
            return origin.distanceTo(end);
        }
    }

    public static Hit trace(Level level, LivingEntity shooter, double range, float partialTick) {
        Vec3 origin = shooter.getEyePosition(partialTick);
        Vec3 look = shooter.getViewVector(partialTick);
        Vec3 end = origin.add(look.scale(range));

        BlockHitResult blockHit = level.clip(new ClipContext(origin, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        AABB sweep = new AABB(origin, end).inflate(SWEEP_INFLATION);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(shooter, origin, end, sweep,
                entity -> canHit(shooter, entity), range * range);
        if (entityHit != null) {
            return new Hit(origin, entityHit.getLocation(), entityHit.getEntity());
        }
        return new Hit(origin, end, null);
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
