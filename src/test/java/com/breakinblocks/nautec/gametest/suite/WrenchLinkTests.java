package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.PrismarineLaserRelayBlockEntity;
import com.breakinblocks.nautec.content.blocks.LaserJunctionBlock;
import com.breakinblocks.nautec.content.blocks.PrismarineLaserRelayBlock;
import com.breakinblocks.nautec.content.items.tools.AquarineWrenchItem;
import com.breakinblocks.nautec.content.items.tools.WrenchLinking;
import com.breakinblocks.nautec.content.items.tools.WrenchMode;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class WrenchLinkTests {
    private WrenchLinkTests() {
    }

    private static ItemStack linkWrench() {
        ItemStack stack = new ItemStack(NTItems.AQUARINE_WRENCH.get());
        stack.set(NTDataComponents.WRENCH_MODE.get(), WrenchMode.LINK.ordinal());
        return stack;
    }

    private static void click(GameTestHelper helper, Player player, ItemStack stack, BlockPos relative) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos pos = helper.absolutePos(relative);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        stack.getItem().useOn(new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack, hit));
    }

    private static void relay(GameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(pos, NTBlocks.PRISMARINE_RELAY.get().defaultBlockState().setValue(PrismarineLaserRelayBlock.FACING, facing));
    }

    private static Direction facing(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockState(pos).getValue(PrismarineLaserRelayBlock.FACING);
    }

    public static void register(NTTestRegistrar r) {
        r.add("wrench_link/relay_to_relay_aims_both", 20, helper -> {
            BlockPos a = new BlockPos(1, 2, 4);
            BlockPos b = new BlockPos(6, 2, 4);
            relay(helper, a, Direction.UP);
            relay(helper, b, Direction.DOWN);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack wrench = linkWrench();
            click(helper, player, wrench, a);
            GlobalPos start = WrenchLinking.start(wrench);
            helper.assertTrue(start != null && start.pos().equals(helper.absolutePos(a)), "the first click saves the start");
            click(helper, player, wrench, b);
            helper.assertValueEqual(Direction.EAST, facing(helper, a), "the start relay now sends toward the second");
            helper.assertValueEqual(Direction.EAST, facing(helper, b), "the second relay now takes the beam from the start");
            helper.assertTrue(WrenchLinking.start(wrench) == null, "the saved position resets after linking");
            helper.succeed();
        });

        r.add("wrench_link/junction_source_opens_an_output_face", 20, helper -> {
            BlockPos junction = new BlockPos(2, 2, 1);
            BlockPos mixer = new BlockPos(2, 2, 6);
            helper.setBlock(junction, NTBlocks.LASER_JUNCTION.get());
            helper.setBlock(mixer, NTBlocks.MIXER.get());
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack wrench = linkWrench();
            click(helper, player, wrench, junction);
            click(helper, player, wrench, mixer);
            helper.assertValueEqual(LaserJunctionBlock.ConnectionType.OUTPUT,
                    helper.getBlockState(junction).getValue(LaserJunctionBlock.CONNECTION[Direction.SOUTH.get3DDataValue()]),
                    "the junction face toward the mixer outputs");
            helper.assertTrue(WrenchLinking.start(wrench) == null, "the saved position resets after linking");
            helper.succeed();
        });

        r.add("wrench_link/out_of_line_target_keeps_the_start", 20, helper -> {
            BlockPos a = new BlockPos(1, 2, 1);
            BlockPos b = new BlockPos(5, 2, 5);
            relay(helper, a, Direction.UP);
            relay(helper, b, Direction.UP);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack wrench = linkWrench();
            click(helper, player, wrench, a);
            click(helper, player, wrench, b);
            helper.assertValueEqual(Direction.UP, facing(helper, a), "a relay out of line is not turned");
            helper.assertValueEqual(Direction.UP, facing(helper, b), "the target is not turned either");
            helper.assertTrue(WrenchLinking.start(wrench) != null, "the start stays saved so another target can be tried");
            helper.succeed();
        });

        r.add("wrench_link/non_laser_block_clears_the_start", 20, helper -> {
            BlockPos a = new BlockPos(1, 2, 4);
            BlockPos stone = new BlockPos(5, 2, 4);
            relay(helper, a, Direction.UP);
            helper.setBlock(stone, Blocks.STONE);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack wrench = linkWrench();
            click(helper, player, wrench, a);
            click(helper, player, wrench, stone);
            helper.assertTrue(WrenchLinking.start(wrench) == null, "clicking a non-laser block clears the start");
            helper.assertValueEqual(Direction.UP, facing(helper, a), "the relay is left alone");
            helper.succeed();
        });

        r.add("wrench_link/receiver_only_block_is_not_a_start", 20, helper -> {
            BlockPos mixer = new BlockPos(2, 2, 2);
            helper.setBlock(mixer, NTBlocks.MIXER.get());
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack wrench = linkWrench();
            click(helper, player, wrench, mixer);
            helper.assertTrue(WrenchLinking.start(wrench) == null, "a block that cannot send a beam is not saved as the start");
            helper.succeed();
        });

        r.add("wrench_link/right_click_air_clears_the_start", 20, helper -> {
            BlockPos a = new BlockPos(1, 2, 4);
            relay(helper, a, Direction.UP);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack wrench = linkWrench();
            click(helper, player, wrench, a);
            helper.assertTrue(WrenchLinking.start(wrench) != null, "the start is saved");
            wrench.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            helper.assertTrue(WrenchLinking.start(wrench) == null, "right-clicking the air clears the start");
            helper.assertValueEqual(WrenchMode.LINK, AquarineWrenchItem.mode(wrench), "a plain right-click keeps Link mode");
            helper.succeed();
        });

        r.add("wrench_link/beyond_beam_range_is_refused", 20, helper -> {
            int range = NTConfig.relayLaserDistance;
            try {
                NTConfig.relayLaserDistance = 3;
                BlockPos a = new BlockPos(1, 2, 4);
                BlockPos b = new BlockPos(6, 2, 4);
                relay(helper, a, Direction.UP);
                relay(helper, b, Direction.UP);
                WrenchLinking.Result result = WrenchLinking.link(helper.getLevel(), helper.absolutePos(a), helper.absolutePos(b));
                helper.assertValueEqual(WrenchLinking.Result.TOO_FAR, result, "a target past the beam range");
                helper.assertValueEqual(Direction.UP, facing(helper, a), "nothing turns when the link fails");
            } finally {
                NTConfig.relayLaserDistance = range;
            }
            helper.succeed();
        });

        r.add("wrench_link/turning_a_fed_relay_warns", 20, helper -> {
            BlockPos a = new BlockPos(1, 2, 2);
            BlockPos b = new BlockPos(1, 2, 6);
            relay(helper, a, Direction.EAST);
            relay(helper, b, Direction.UP);
            PrismarineLaserRelayBlockEntity fed = helper.getBlockEntity(a, PrismarineLaserRelayBlockEntity.class);
            fed.setPowerPerSide(Direction.WEST, 20);
            fed.commonTick();
            helper.assertTrue(fed.getPower() > 0, "the relay is taking a beam");
            WrenchLinking.Result result = WrenchLinking.link(helper.getLevel(), helper.absolutePos(a), helper.absolutePos(b));
            helper.assertValueEqual(WrenchLinking.Result.LINKED_CUT_SOURCE, result, "turning a fed relay off its beam is reported");
            helper.assertValueEqual(Direction.SOUTH, facing(helper, a), "the relay still turns to the target");
            helper.succeed();
        });

        r.add("wrench_link/sneak_cycles_into_link_mode", 20, helper -> {
            ItemStack wrench = new ItemStack(NTItems.AQUARINE_WRENCH.get());
            wrench.set(NTDataComponents.WRENCH_MODE.get(), WrenchMode.FLUID_SIDES.ordinal());
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setShiftKeyDown(true);
            player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
            wrench.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            helper.assertValueEqual(WrenchMode.LINK, AquarineWrenchItem.mode(wrench), "Fluid Sides is followed by Link");
            wrench.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            helper.assertValueEqual(WrenchMode.ROTATE, AquarineWrenchItem.mode(wrench), "Link wraps back to Rotate");
            helper.succeed();
        });
    }
}
