package com.breakinblocks.nautec.api.gateways;

import com.breakinblocks.nautec.registries.NTParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class GatewayEffects {
    private static final int SWEEP_PERIOD = 90;
    private static final double SWEEP_RADIUS = 0.85;

    private GatewayEffects() {
    }

    public static void sweep(ServerLevel level, BlockPos pos) {
        Vec3 centre = Vec3.atCenterOf(pos).add(0.0, 0.2, 0.0);
        double phase = (double) Math.floorMod(level.getGameTime() + pos.asLong(), SWEEP_PERIOD) / SWEEP_PERIOD;
        double angle = phase * Math.PI * 2.0;

        level.sendParticles(NTParticles.GLOW_SPORE.get(),
                centre.x + Math.cos(angle) * SWEEP_RADIUS,
                centre.y,
                centre.z + Math.sin(angle) * SWEEP_RADIUS,
                1, 0.0, 0.02, 0.0, 0.0);
    }

    public static void travel(ServerLevel level, BlockPos pos) {
        Vec3 centre = Vec3.atCenterOf(pos.above());
        level.sendParticles(ParticleTypes.PORTAL, centre.x, centre.y, centre.z,
                48, 0.4, 0.7, 0.4, 0.45);
        level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, centre.x, centre.y - 0.4, centre.z,
                24, 0.4, 0.1, 0.4, 0.06);
        level.sendParticles(NTParticles.GLOW_SPORE.get(), centre.x, centre.y, centre.z,
                12, 0.5, 0.5, 0.5, 0.02);
    }

    public static void unlinked(ServerLevel level, BlockPos pos) {
        Vec3 centre = Vec3.atCenterOf(pos.above());
        level.sendParticles(ParticleTypes.SMOKE, centre.x, centre.y, centre.z,
                6, 0.3, 0.1, 0.3, 0.01);
    }

    public static void recoded(ServerLevel level, BlockPos pos) {
        Vec3 centre = Vec3.atCenterOf(pos).add(0.0, 0.3, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, centre.x, centre.y, centre.z,
                10, 0.5, 0.1, 0.5, 0.02);
    }
}
