package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.content.blocks.PressureHatchBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public final class PressureHatchTests {
    private static final BlockPos HATCH = new BlockPos(4, 1, 4);

    private PressureHatchTests() {
    }

    private static void build(NTGameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
                for (int y = 1; y <= 5; y++) {
                    helper.setBlock(new BlockPos(x, y, z), z < 4 ? Blocks.WATER : Blocks.STONE);
                }
            }
        }
        BlockState lower = NTBlocks.PRESSURE_HATCH.get().defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH);
        helper.setBlock(HATCH, lower.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        helper.setBlock(HATCH.above(), lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        helper.setBlock(HATCH.south(), Blocks.WATER);
        helper.setBlock(HATCH.south().above(), Blocks.WATER);
        helper.setBlock(HATCH.south(2), Blocks.WATER);
    }

    private static void setOpen(NTGameTestHelper helper, boolean open) {
        BlockPos pos = helper.absolutePos(HATCH);
        BlockState state = helper.getLevel().getBlockState(pos);
        ((PressureHatchBlock) state.getBlock()).setOpen(null, helper.getLevel(), state, pos, open);
    }

    public static void register(NTTestRegistrar r) {
        r.add("pressure_hatch/closing_drains_the_inside_and_spares_the_sea", 20, helper -> {
            build(helper);
            setOpen(helper, true);
            setOpen(helper, false);
            helper.assertTrue(helper.getBlockState(HATCH.south()).isAir(), "the cell inside the hatch is drained");
            helper.assertTrue(helper.getBlockState(HATCH.south().above()).isAir(), "the upper cell is drained");
            helper.assertTrue(helper.getBlockState(HATCH.south(2)).isAir(), "water joined to the inside cell is drained too");
            helper.assertTrue(helper.getBlockState(HATCH.north()).is(Blocks.WATER), "the sea outside is left alone");
            helper.succeed();
        });

        r.add("pressure_hatch/open_hatch_holds_the_sea_back", 60, helper -> {
            build(helper);
            setOpen(helper, false);
            setOpen(helper, true);
            helper.runAfterDelay(40, () -> {
                helper.assertTrue(helper.getBlockState(HATCH).getBlock() instanceof PressureHatchBlock, "the hatch survives the water");
                helper.assertTrue(helper.getBlockState(HATCH).getValue(DoorBlock.OPEN), "the hatch is open");
                helper.assertTrue(helper.getBlockState(HATCH.south()).isAir(), "no water comes through an open hatch");
                helper.succeed();
            });
        });

        r.add("pressure_hatch/large_flooded_room_is_not_drained", 20, helper -> {
            build(helper);
            for (int x = 1; x < 8; x++) {
                for (int z = 5; z < 9; z++) {
                    for (int y = 1; y <= 4; y++) {
                        helper.getLevel().setBlock(helper.absolutePos(new BlockPos(x, y, z)), Blocks.WATER.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
            int drained = PressureHatchBlock.drainIfClosed(helper.getLevel(), helper.absolutePos(HATCH));
            helper.assertValueEqual(drained, 0, "a flooded room bigger than the pocket limit is left alone");
            helper.assertTrue(helper.getBlockState(HATCH.south()).is(Blocks.WATER), "the room stays flooded");
            helper.succeed();
        });
    }
}
