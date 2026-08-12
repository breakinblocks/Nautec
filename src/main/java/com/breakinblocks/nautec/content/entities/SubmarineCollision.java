package com.breakinblocks.nautec.content.entities;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class SubmarineCollision {
    private static final float S = SubmarineEntity.MODEL_SCALE / 16F;
    private static final double Y_OFF = SubmarineEntity.MODEL_Y_OFFSET * SubmarineEntity.MODEL_SCALE;
    private static final double Z_OFF = SubmarineEntity.MODEL_Z_OFFSET * SubmarineEntity.MODEL_SCALE;

    private static final double MARGIN = 0.06;
    private static final int BISECTIONS = 5;

    private static final Vec3[] SHELL = buildShell();

    private static Vec3[] buildShell() {
        List<Vec3> points = new ArrayList<>();
        shellBox(points, -4, 4, -3, 8.5, -20, 15);
        shellBox(points, -9, -4, -3, 5.5, -13, 12);
        shellBox(points, 4, 9, -3, 5.5, -13, 12);
        return points.toArray(Vec3[]::new);
    }

    private static void shellBox(List<Vec3> points, double x0, double x1, double y0, double y1, double z0, double z1) {
        double[] xs = {x0, x1};
        double[] ys = {y0, y1};
        double[] zs = {z0, z1, (z0 + z1) / 2};
        for (double x : xs) {
            for (double y : ys) {
                for (double z : zs) {
                    points.add(toEntitySpace(x, y, z));
                }
            }
        }
        points.add(toEntitySpace((x0 + x1) / 2, y1, (z0 + z1) / 2));
        points.add(toEntitySpace((x0 + x1) / 2, y0, z0));
        points.add(toEntitySpace((x0 + x1) / 2, y0, z1));
        points.add(toEntitySpace((x0 + x1) / 2, (y0 + y1) / 2, z0));
        points.add(toEntitySpace((x0 + x1) / 2, (y0 + y1) / 2, z1));
    }

    private static Vec3 toEntitySpace(double x, double y, double z) {
        return new Vec3(x * S, y * S + Y_OFF, z * S + Z_OFF);
    }

    /**
     * Blocks only. Entities are shoved aside by the hull instead of stopping it, because testing
     * entity collisions here meant a single fish in any probe box clamped the throttle to nothing.
     */
    public static boolean blocked(Level level, Entity entity, Vec3 position, float yawDegrees, float pitchDegrees) {
        float pitchRad = pitchDegrees * Mth.DEG_TO_RAD;
        float yawRad = yawDegrees * Mth.DEG_TO_RAD;
        for (Vec3 offset : SHELL) {
            Vec3 world = position.add(offset.xRot(pitchRad).yRot(-yawRad));
            AABB probe = new AABB(world.x - MARGIN, world.y - MARGIN, world.z - MARGIN,
                    world.x + MARGIN, world.y + MARGIN, world.z + MARGIN);
            if (level.getBlockCollisions(entity, probe).iterator().hasNext()) {
                return true;
            }
        }
        return false;
    }

    public static Vec3 clampMotion(Level level, Entity entity, Vec3 position, Vec3 motion, float yawDegrees, float pitchDegrees) {
        double y = clampAxis(level, entity, position, new Vec3(0, motion.y, 0), yawDegrees, pitchDegrees).y;
        Vec3 afterY = position.add(0, y, 0);
        double x = clampAxis(level, entity, afterY, new Vec3(motion.x, 0, 0), yawDegrees, pitchDegrees).x;
        Vec3 afterX = afterY.add(x, 0, 0);
        double z = clampAxis(level, entity, afterX, new Vec3(0, 0, motion.z), yawDegrees, pitchDegrees).z;
        return new Vec3(x, y, z);
    }

    private static Vec3 clampAxis(Level level, Entity entity, Vec3 position, Vec3 motion, float yawDegrees, float pitchDegrees) {
        if (motion.lengthSqr() < 1.0E-8) {
            return motion;
        }

        if (!blocked(level, entity, position.add(motion), yawDegrees, pitchDegrees)) {
            return motion;
        }

        double allowed = 0.0;
        double step = 0.5;
        for (int i = 0; i < BISECTIONS; i++) {
            double trial = allowed + step;
            if (!blocked(level, entity, position.add(motion.scale(trial)), yawDegrees, pitchDegrees)) {
                allowed = trial;
            }
            step /= 2.0;
        }
        return motion.scale(allowed);
    }

    private SubmarineCollision() {
    }
}
