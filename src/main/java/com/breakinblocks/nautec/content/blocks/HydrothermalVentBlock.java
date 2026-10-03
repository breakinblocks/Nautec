package com.breakinblocks.nautec.content.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.tags.FluidTags;

public class HydrothermalVentBlock extends Block {
    public static final MapCodec<HydrothermalVentBlock> CODEC = simpleCodec(HydrothermalVentBlock::new);

    public HydrothermalVentBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.isSteppingCarefully() && entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().hotFloor(), 1.0F);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        FluidState fluid = level.getFluidState(above);
        boolean underwater = fluid.is(FluidTags.WATER);
        if (!underwater && !level.getBlockState(above).isAir()) {
            return;
        }
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        for (int i = 0; i < 3; i++) {
            double ox = (random.nextDouble() - 0.5) * 0.4;
            double oz = (random.nextDouble() - 0.5) * 0.4;
            level.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, x + ox, y + random.nextDouble() * 0.3, z + oz,
                    0.0, 0.05 + random.nextDouble() * 0.04, 0.0);
        }
        if (underwater) {
            for (int i = 0; i < 2; i++) {
                level.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, x + (random.nextDouble() - 0.5) * 0.5, y, z + (random.nextDouble() - 0.5) * 0.5,
                        0.0, 0.04, 0.0);
            }
            if (random.nextInt(60) == 0) {
                level.playLocalSound(x, y, z, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS, 0.4F, 0.6F + random.nextFloat() * 0.2F, false);
            }
        }
    }
}
