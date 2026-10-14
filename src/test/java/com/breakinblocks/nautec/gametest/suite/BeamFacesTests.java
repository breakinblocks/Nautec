package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.content.blockentities.LaserJunctionBlockEntity;
import com.breakinblocks.nautec.content.blocks.BeamFaces;
import com.breakinblocks.nautec.content.blocks.PrismarineLaserRelayBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

public final class BeamFacesTests {
    private static final BlockPos POS = new BlockPos(4, 2, 4);

    private BeamFacesTests() {
    }

    public static void register(NTTestRegistrar r) {
        r.add("beam_faces/relay_faces_set_from_the_screen_even_when_boxed_in", 5, helper -> {
            helper.setBlock(POS, NTBlocks.PRISMARINE_RELAY.get().defaultBlockState().setValue(PrismarineLaserRelayBlock.FACING, Direction.NORTH));
            for (Direction direction : Direction.values()) {
                helper.setBlock(POS.relative(direction), Blocks.STONE);
            }
            BlockPos abs = helper.absolutePos(POS);
            helper.assertValueEqual(BeamFaces.Mode.OUTPUT, BeamFaces.mode(helper.getBlockState(POS), Direction.NORTH), "north starts as the output");
            helper.assertValueEqual(BeamFaces.Mode.INPUT, BeamFaces.mode(helper.getBlockState(POS), Direction.SOUTH), "south starts as the input");
            helper.assertTrue(BeamFaces.apply(helper.getLevel(), abs, Direction.EAST, true), "clicking east changes the relay");
            helper.assertValueEqual(Direction.EAST, helper.getBlockState(POS).getValue(PrismarineLaserRelayBlock.FACING), "clicking a face fires out of it");
            helper.assertValueEqual(BeamFaces.Mode.INPUT, BeamFaces.mode(helper.getBlockState(POS), Direction.WEST), "the opposite face takes the beam in");
            helper.assertTrue(BeamFaces.apply(helper.getLevel(), abs, Direction.UP, false), "right-clicking up changes the relay");
            helper.assertValueEqual(Direction.DOWN, helper.getBlockState(POS).getValue(PrismarineLaserRelayBlock.FACING), "right-clicking a face takes the beam in there");
            helper.assertFalse(BeamFaces.apply(helper.getLevel(), abs, Direction.DOWN, true), "picking the current output changes nothing");
            helper.succeed();
        });

        r.add("beam_faces/junction_faces_cycle_and_stay_in_sync", 5, helper -> {
            helper.setBlock(POS, NTBlocks.LASER_JUNCTION.get());
            BlockPos abs = helper.absolutePos(POS);
            LaserJunctionBlockEntity junction = helper.getBlockEntity(POS, LaserJunctionBlockEntity.class);
            BeamFaces.apply(helper.getLevel(), abs, Direction.WEST, true);
            helper.assertValueEqual(BeamFaces.Mode.INPUT, BeamFaces.mode(helper.getBlockState(POS), Direction.WEST), "one click opens an input");
            helper.assertTrue(junction.getLaserInputs().contains(Direction.WEST), "the beam sets follow the input");
            BeamFaces.apply(helper.getLevel(), abs, Direction.WEST, true);
            helper.assertValueEqual(BeamFaces.Mode.OUTPUT, BeamFaces.mode(helper.getBlockState(POS), Direction.WEST), "a second click makes it an output");
            helper.assertTrue(junction.getLaserOutputs().contains(Direction.WEST) && !junction.getLaserInputs().contains(Direction.WEST),
                    "the beam sets follow the output");
            BeamFaces.apply(helper.getLevel(), abs, Direction.WEST, true);
            helper.assertValueEqual(BeamFaces.Mode.OFF, BeamFaces.mode(helper.getBlockState(POS), Direction.WEST), "a third click closes it");
            helper.assertTrue(junction.getLaserOutputs().isEmpty() && junction.getLaserInputs().isEmpty(), "a closed face leaves both sets");
            BeamFaces.apply(helper.getLevel(), abs, Direction.UP, false);
            helper.assertValueEqual(BeamFaces.Mode.OUTPUT, BeamFaces.mode(helper.getBlockState(POS), Direction.UP), "right-click steps back from off to output");
            helper.succeed();
        });

        r.add("beam_faces/empty_hand_opens_the_screen", 5, helper -> {
            helper.setBlock(POS, NTBlocks.LASER_JUNCTION.get());
            helper.setBlock(POS.east(2), NTBlocks.PRISMARINE_RELAY.get());
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            for (BlockPos pos : new BlockPos[]{POS, POS.east(2)}) {
                BlockPos abs = helper.absolutePos(pos);
                InteractionResult result = helper.getBlockState(pos).useWithoutItem(helper.getLevel(), player,
                        new BlockHitResult(abs.getCenter(), Direction.UP, abs, false));
                helper.assertTrue(result.consumesAction(), "an empty hand is taken by " + helper.getBlockState(pos).getBlock().getName().getString());
            }
            helper.succeed();
        });

        r.add("beam_faces/junction_is_dry_by_default", 5, helper -> {
            helper.assertTrue(!NTBlocks.LASER_JUNCTION.get().defaultBlockState().getValue(BlockStateProperties.WATERLOGGED),
                    "a junction placed without water around it is not waterlogged");
            helper.setBlock(POS, NTBlocks.LASER_JUNCTION.get());
            helper.assertTrue(helper.getBlockState(POS).getFluidState().isEmpty(), "a placed junction holds no water");
            helper.succeed();
        });
    }
}
