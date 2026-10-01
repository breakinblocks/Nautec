package com.breakinblocks.nautec.api.gateways;

import com.breakinblocks.nautec.registries.NTParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class GatewayEffects {
    private GatewayEffects() {
    }

    public static void travel(ServerLevel level, Vec3 point) {
        level.sendParticles(NTParticles.CRYSTAL_MOTE.get(), point.x, point.y, point.z,
                40, 0.6, 0.6, 0.6, 0.04);
        level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, point.x, point.y - 0.4, point.z,
                24, 0.5, 0.3, 0.5, 0.06);
        level.sendParticles(NTParticles.GLOW_SPORE.get(), point.x, point.y, point.z,
                12, 0.5, 0.5, 0.5, 0.02);
    }

    public static void opened(ServerLevel level, Vec3 centre) {
        level.sendParticles(NTParticles.CRYSTAL_MOTE.get(), centre.x, centre.y, centre.z,
                80, 2.4, 2.4, 2.4, 0.05);
        level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, centre.x, centre.y - 2.0, centre.z,
                40, 2.0, 1.0, 2.0, 0.08);
    }

    public static void recoded(ServerLevel level, BlockPos pos) {
        Vec3 centre = Vec3.atCenterOf(pos).add(0.0, 0.3, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, centre.x, centre.y, centre.z,
                10, 0.5, 0.1, 0.5, 0.02);
    }
}
