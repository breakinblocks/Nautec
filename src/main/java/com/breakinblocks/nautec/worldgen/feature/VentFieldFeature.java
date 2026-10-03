package com.breakinblocks.nautec.worldgen.feature;

import com.breakinblocks.nautec.registries.NTBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VentFieldFeature extends Feature<NoneFeatureConfiguration> {
    private static final int SPREAD = 7;

    public VentFieldFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        if (findFloor(level, origin) == null) {
            return false;
        }
        int chimneys = 2 + random.nextInt(4);
        int placed = 0;
        for (int i = 0; i < chimneys * 3 && placed < chimneys; i++) {
            BlockPos column = i == 0 ? origin : origin.offset(random.nextInt(SPREAD * 2 + 1) - SPREAD, 0, random.nextInt(SPREAD * 2 + 1) - SPREAD);
            BlockPos floor = findFloor(level, column);
            if (floor != null && chimney(level, random, floor, placed == 0 ? 5 + random.nextInt(4) : 2 + random.nextInt(5))) {
                placed++;
            }
        }
        return placed > 0;
    }

    public static BlockPos findFloor(WorldGenLevel level, BlockPos column) {
        BlockPos.MutableBlockPos cursor = column.mutable().move(Direction.UP, 6);
        for (int i = 0; i < 20; i++) {
            if (level.isOutsideBuildHeight(cursor)) {
                return null;
            }
            BlockState below = level.getBlockState(cursor.below());
            if (isWater(level, cursor) && isWater(level, cursor.above()) && below.isSolidRender()) {
                return cursor.immutable();
            }
            cursor.move(Direction.DOWN);
        }
        return null;
    }

    private static boolean isWater(WorldGenLevel level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER);
    }

    private static boolean replaceable(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(Blocks.WATER) || state.is(Blocks.SEAGRASS) || state.is(Blocks.TALL_SEAGRASS) || state.is(Blocks.KELP)
                || state.is(Blocks.KELP_PLANT) || state.is(NTBlocks.VENT_TUBEWORM.get());
    }

    private static BlockState wall(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 3) {
            return Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState();
        }
        if (roll < 5) {
            return Blocks.DEEPSLATE_IRON_ORE.defaultBlockState();
        }
        if (roll < 6) {
            return Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState();
        }
        if (roll < 40) {
            return Blocks.BLACKSTONE.defaultBlockState();
        }
        if (roll < 55) {
            return Blocks.SMOOTH_BASALT.defaultBlockState();
        }
        return Blocks.BASALT.defaultBlockState();
    }

    public static boolean chimney(WorldGenLevel level, RandomSource random, BlockPos floor, int height) {
        if (!isWater(level, floor.above(height))) {
            return false;
        }
        for (int y = 0; y < height; y++) {
            if (!replaceable(level, floor.above(y))) {
                return false;
            }
        }
        level.setBlock(floor.below(), Blocks.MAGMA_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
        for (int y = 0; y < height; y++) {
            int radius = y == 0 && height > 3 ? 2 : (y < height / 2 ? 1 : 0);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (radius == 2 && Math.abs(dx) == 2 && Math.abs(dz) == 2) {
                        continue;
                    }
                    if (radius == 2 && (Math.abs(dx) == 2 || Math.abs(dz) == 2) && random.nextInt(3) == 0) {
                        continue;
                    }
                    BlockPos pos = floor.offset(dx, y, dz);
                    if (!replaceable(level, pos)) {
                        continue;
                    }
                    boolean core = dx == 0 && dz == 0;
                    BlockState state = core && radius > 0 ? Blocks.MAGMA_BLOCK.defaultBlockState() : wall(random);
                    level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                }
            }
        }
        level.setBlock(floor.above(height - 1), NTBlocks.HYDROTHERMAL_VENT.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        for (int i = 0; i < 12; i++) {
            BlockPos spot = findFloor(level, floor.offset(random.nextInt(7) - 3, 0, random.nextInt(7) - 3));
            if (spot == null) {
                continue;
            }
            if (random.nextInt(4) == 0) {
                level.setBlock(spot.below(), random.nextBoolean() ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.BASALT.defaultBlockState(),
                        Block.UPDATE_CLIENTS);
            }
            BlockState worm = NTBlocks.VENT_TUBEWORM.get().defaultBlockState();
            if (level.getBlockState(spot).is(Blocks.WATER) && worm.canSurvive(level, spot) && !level.getBlockState(spot.below()).is(Blocks.MAGMA_BLOCK)) {
                level.setBlock(spot, worm, Block.UPDATE_CLIENTS);
            }
        }
        return true;
    }
}
