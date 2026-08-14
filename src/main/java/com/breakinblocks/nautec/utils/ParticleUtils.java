package com.breakinblocks.nautec.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class ParticleUtils {

    private static final int PARTICLE_COUNT = 20;
    private static final double PARTICLE_RADIUS = 0.5;
    private static final int PARTICLE_DELAY = 5;

    public static void spawnParticlesAroundItem(ItemEntity itemEntity, Level level, ParticleOptions particlesTypes) {
        assert level.isClientSide();
        if (level.getGameTime() % PARTICLE_DELAY == 0) {
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                double theta = level.getRandom().nextDouble() * Math.PI * 2;
                double phi = level.getRandom().nextDouble() * Math.PI;

                double xOffset = PARTICLE_RADIUS * Math.sin(phi) * Math.cos(theta);
                double yOffset = PARTICLE_RADIUS * Math.cos(phi);
                double zOffset = PARTICLE_RADIUS * Math.sin(phi) * Math.sin(theta);

                level.addParticle(particlesTypes,
                        itemEntity.getX() + xOffset,
                        itemEntity.getY() + yOffset,
                        itemEntity.getZ() + zOffset,
                        0, 0, 0);
            }
        }
    }

    public static void spawnParticlesAroundBlock(BlockPos blockPos, Level level, ParticleOptions particlesTypes) {
        assert level.isClientSide();

        if (level.getGameTime() % PARTICLE_DELAY == 0) {
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                double theta = level.getRandom().nextDouble() * Math.PI * 2;
                double phi = level.getRandom().nextDouble() * Math.PI;

                double xOffset = PARTICLE_RADIUS * Math.sin(phi) * Math.cos(theta);
                double yOffset = PARTICLE_RADIUS * Math.cos(phi);
                double zOffset = PARTICLE_RADIUS * Math.sin(phi) * Math.sin(theta);

                level.addParticle(particlesTypes,
                        blockPos.getX() + 0.5 + xOffset,
                        blockPos.getY() + 0.5 + yOffset,
                        blockPos.getZ() + 0.5 + zOffset,
                        0, 0, 0);
            }
        }
    }

    public static void spawnBreakParticle(BlockPos pos, Block block, int count, Level level) {
        assert level.isClientSide();
        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, block.defaultBlockState());
        for (int i = 0; i < count; i++) {
            double spread = (double) i / 10;
            level.addParticle(particle,
                    pos.getX() + 0.5, pos.above().getY(), pos.getZ() + 0.5,
                    spread, spread, spread);
        }
    }
}
