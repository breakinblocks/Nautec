package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.generators.ThermalVentTapBlockEntity;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.worldgen.feature.GlowGrottoFeature;
import com.breakinblocks.nautec.worldgen.feature.VentFieldFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class SeafloorFeatureTests {
    private static final ResourceLocation TALL = Nautec.rl("empty_19x11x19");
    private static final ResourceLocation WIDE = Nautec.rl("empty_34x16x14");

    private SeafloorFeatureTests() {
    }

    private static void fill(NTGameTestHelper helper, int sizeX, int sizeZ, int floorY, int topY) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int y = 0; y <= topY; y++) {
                    BlockState state = y < floorY ? Blocks.STONE.defaultBlockState() : Blocks.WATER.defaultBlockState();
                    level.setBlock(helper.absolutePos(new BlockPos(x, y, z)), state, Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    private static int count(NTGameTestHelper helper, BlockPos from, BlockPos to, Block block) {
        int found = 0;
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            if (helper.getBlockState(pos).is(block)) {
                found++;
            }
        }
        return found;
    }

    public static void register(NTTestRegistrar r) {
        r.add("worldgen/vent_chimney_builds_a_smoking_vent", TALL, 40, 0, helper -> {
            fill(helper, 19, 19, 2, 10);
            BlockPos floor = new BlockPos(9, 2, 9);
            helper.assertTrue(VentFieldFeature.findFloor(helper.getLevel(), helper.absolutePos(floor.above(3))) != null, "the floor is found from above");
            boolean placed = VentFieldFeature.chimney(helper.getLevel(), RandomSource.create(3), helper.absolutePos(floor), 6);
            helper.assertTrue(placed, "the chimney is built in open water");
            helper.assertTrue(helper.getBlockState(floor.above(5)).is(NTBlocks.HYDROTHERMAL_VENT.get()), "a vent caps the chimney");
            helper.assertTrue(helper.getBlockState(floor.above(5)).getValue(BlockStateProperties.WATERLOGGED), "the vent cap keeps its water");
            helper.assertTrue(helper.getBlockState(floor.below()).is(Blocks.MAGMA_BLOCK), "magma feeds it from below");
            helper.assertTrue(helper.getBlockState(floor.above(6)).is(Blocks.WATER), "water stays above the vent");
            helper.assertFalse(helper.getBlockState(floor.above(2)).is(Blocks.WATER), "the chimney is solid");
            helper.assertFalse(VentFieldFeature.chimney(helper.getLevel(), RandomSource.create(3), helper.absolutePos(floor), 6),
                    "a second chimney will not build into the first");
            helper.succeed();
        });

        r.add("worldgen/vent_block_heats_a_vent_tap", TALL, 40, 0, helper -> {
            fill(helper, 19, 19, 2, 10);
            helper.setBlock(new BlockPos(9, 2, 9), NTBlocks.HYDROTHERMAL_VENT.get());
            helper.setBlock(new BlockPos(9, 3, 9), NTBlocks.THERMAL_VENT_TAP.get());
            ThermalVentTapBlockEntity tap = helper.getBlockEntity(new BlockPos(9, 3, 9), ThermalVentTapBlockEntity.class);
            tap.scan(helper.getLevel());
            helper.assertValueEqual(tap.getHeat(), ThermalVentTapBlockEntity.VENT_BLOCK_HEAT, "one vent gives three heat");
            helper.succeed();
        });

        r.add("worldgen/glow_grotto_carves_a_lit_flooded_cave", WIDE, 40, 0, helper -> {
            fill(helper, 34, 14, 13, 15);
            BlockPos floor = new BlockPos(17, 13, 7);
            boolean placed = GlowGrottoFeature.carve(helper.getLevel(), RandomSource.create(5), helper.absolutePos(floor), 4, 3, 4);
            helper.assertTrue(placed, "the grotto carves into solid stone");
            BlockPos from = new BlockPos(12, 0, 2);
            BlockPos to = new BlockPos(22, 12, 12);
            int water = count(helper, from, to, Blocks.WATER);
            int polyps = count(helper, from, to, NTBlocks.GLOW_POLYP.get());
            int algae = count(helper, from, to, NTBlocks.LUMINESCENT_ALGAE.get());
            helper.assertTrue(water > 60, "a flooded chamber is carved, found " + water + " water");
            helper.assertTrue(polyps > 5, "glow polyps line the walls, found " + polyps);
            helper.assertTrue(algae + count(helper, from, to, Blocks.SEA_PICKLE) > 0, "the floor has light sources");
            helper.assertValueEqual(count(helper, from, to, NTBlocks.BUDDING_PRISMARINE.get()), 1, "one budding prismarine");
            helper.assertValueEqual(count(helper, from, to, Blocks.AIR), 0, "no air pockets are left");
            helper.succeed();
        });

        r.add("worldgen/glow_grotto_refuses_to_breach_air", WIDE, 40, 0, helper -> {
            fill(helper, 34, 14, 13, 15);
            for (int x = 15; x <= 19; x++) {
                helper.setBlock(new BlockPos(x, 3, 7), Blocks.AIR);
            }
            boolean placed = GlowGrottoFeature.carve(helper.getLevel(), RandomSource.create(5), helper.absolutePos(new BlockPos(17, 13, 7)), 4, 3, 4);
            helper.assertFalse(placed, "a grotto never opens into a dry cave");
            helper.assertTrue(helper.getBlockState(new BlockPos(17, 8, 7)).is(Blocks.STONE), "nothing is carved");
            helper.succeed();
        });
    }
}
