package com.breakinblocks.nautec.worldgen.feature;

import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTLootTables;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlowGrottoFeature extends Feature<NoneFeatureConfiguration> {
    private static final int ATTEMPTS = 4;
    private static final int SPREAD = 4;

    public GlowGrottoFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    private static boolean carvable(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.DIRT) || state.is(Blocks.SAND) || state.is(Blocks.GRAVEL)
                || state.is(Blocks.CLAY) || state.is(NTBlocks.PRISMARINE_SAND.get()) || state.is(Blocks.SANDSTONE) || state.is(BlockTags.CORAL_BLOCKS)
                || state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            BlockPos column = origin;
            if (attempt > 0) {
                int x = origin.getX() + random.nextInt(SPREAD * 2 + 1) - SPREAD;
                int z = origin.getZ() + random.nextInt(SPREAD * 2 + 1) - SPREAD;
                column = new BlockPos(x, level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z), z);
            }
            BlockPos floor = VentFieldFeature.findFloor(level, column);
            if (floor != null && carve(level, random, floor, 4 + random.nextInt(3), 3 + random.nextInt(2), 4 + random.nextInt(3))) {
                return true;
            }
        }
        return false;
    }

    public static boolean carve(WorldGenLevel level, RandomSource random, BlockPos floor, int rx, int ry, int rz) {
        BlockPos center = floor.below(ry + 3 + random.nextInt(3));
        if (level.isOutsideBuildHeight(center.below(ry + 1)) || center.getY() - ry <= level.getMinBuildHeight() + 4) {
            return false;
        }

        Set<BlockPos> cavity = new HashSet<>();
        for (int dx = -rx - 1; dx <= rx + 1; dx++) {
            for (int dy = -ry - 1; dy <= ry + 1; dy++) {
                for (int dz = -rz - 1; dz <= rz + 1; dz++) {
                    double d = sq(dx / (double) rx) + sq(dy / (double) ry) + sq(dz / (double) rz);
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (d <= 1.0 + 0.15 * random.nextDouble()) {
                        if (state.isAir() || state.is(Blocks.BEDROCK) || state.is(Blocks.LAVA)) {
                            return false;
                        }
                        if (pos.getY() < floor.getY() && carvable(state)) {
                            cavity.add(pos);
                        }
                    } else if (d <= 1.6 && (state.isAir() || state.is(Blocks.LAVA))) {
                        return false;
                    }
                }
            }
        }
        if (cavity.size() < 60) {
            return false;
        }
        int shaftX = random.nextInt(3) - 1;
        int shaftZ = random.nextInt(3) - 1;
        for (int y = center.getY(); y < floor.getY(); y++) {
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) {
                    BlockPos pos = new BlockPos(center.getX() + shaftX + dx, y, center.getZ() + shaftZ + dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || state.is(Blocks.LAVA)) {
                        return false;
                    }
                    if (carvable(state)) {
                        cavity.add(pos);
                    }
                }
            }
        }

        BlockState water = Blocks.WATER.defaultBlockState();
        for (BlockPos pos : cavity) {
            level.setBlock(pos, water, Block.UPDATE_CLIENTS);
        }

        List<BlockPos> floorSpots = new ArrayList<>();
        BlockState polyp = NTBlocks.GLOW_POLYP.get().defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
        for (BlockPos pos : cavity) {
            BlockState below = level.getBlockState(pos.below());
            if (below.isSolidRender(level, pos.below()) && !cavity.contains(pos.below())) {
                floorSpots.add(pos);
                if (random.nextInt(3) == 0 && carvable(below)) {
                    level.setBlock(pos.below(), NTBlocks.PRISMARINE_SAND.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                }
                continue;
            }
            if (random.nextInt(3) != 0) {
                continue;
            }
            BlockState state = polyp;
            boolean any = false;
            for (Direction direction : Direction.values()) {
                if (direction == Direction.DOWN) {
                    continue;
                }
                BlockPos side = pos.relative(direction);
                if (!cavity.contains(side) && level.getBlockState(side).isSolidRender(level, side)) {
                    state = state.setValue(MultifaceBlock.getFaceProperty(direction), true);
                    any = true;
                }
            }
            if (any) {
                level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            }
        }

        BlockState algae = NTBlocks.LUMINESCENT_ALGAE.get().defaultBlockState();
        for (BlockPos pos : floorSpots) {
            int roll = random.nextInt(100);
            if (roll < 35 && algae.canSurvive(level, pos)) {
                level.setBlock(pos, algae, Block.UPDATE_CLIENTS);
            } else if (roll < 45) {
                level.setBlock(pos, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 1 + random.nextInt(4))
                        .setValue(BlockStateProperties.WATERLOGGED, true), Block.UPDATE_CLIENTS);
            } else if (roll < 49) {
                level.setBlock(pos, NTBlocks.PRISMARINE_CLUSTER.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP)
                        .setValue(AmethystClusterBlock.WATERLOGGED, true), Block.UPDATE_CLIENTS);
            }
        }

        if (!floorSpots.isEmpty()) {
            BlockPos budding = floorSpots.get(random.nextInt(floorSpots.size())).below();
            level.setBlock(budding, NTBlocks.BUDDING_PRISMARINE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            if (random.nextInt(3) == 0) {
                BlockPos crate = floorSpots.get(random.nextInt(floorSpots.size()));
                level.setBlock(crate, NTBlocks.RUSTY_CRATE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                RandomizableContainer.setBlockEntityLootTable(level, random, crate, NTLootTables.CRATE);
            }
        }
        return true;
    }

    private static double sq(double v) {
        return v * v;
    }
}
