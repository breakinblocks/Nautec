package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.content.items.WaveJetHands;
import com.breakinblocks.nautec.content.items.WaveJetItem;
import com.breakinblocks.nautec.content.items.WaveJetSpotlight;
import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class WaveJetTests {
    public static void register(NTTestRegistrar r) {
        r.add("wave_jet/spotlight_lights_the_block_it_lands_on", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos floor = shaft(helper, Blocks.AIR.defaultBlockState());
            Player holder = aiming(helper, floor);

            WaveJetSpotlight.aim(level, holder);

            BlockPos lit = floor.above();
            if (!level.getBlockState(lit).is(Blocks.LIGHT)) {
                helper.fail("Nothing lit above the floor, found " + level.getBlockState(lit));
                return;
            }
            if (level.getBlockState(lit).getValue(LightBlock.WATERLOGGED)) {
                helper.fail("The light placed in air came out waterlogged");
                return;
            }

            WaveJetSpotlight.extinguish(holder);
            if (!level.getBlockState(lit).isAir()) {
                helper.fail("Extinguishing left " + level.getBlockState(lit) + " behind instead of air");
            }
            helper.succeed();
        });

        r.add("wave_jet/spotlight_gives_water_back_when_it_moves_on", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos floor = shaft(helper, Blocks.WATER.defaultBlockState());
            Player holder = aiming(helper, floor);

            WaveJetSpotlight.aim(level, holder);

            BlockPos lit = floor.above();
            BlockState state = level.getBlockState(lit);
            if (!state.is(Blocks.LIGHT)) {
                helper.fail("Nothing lit under water, found " + state);
                return;
            }
            if (!state.getValue(LightBlock.WATERLOGGED)) {
                helper.fail("The light placed in water is not waterlogged, so putting it out would drain the block");
                return;
            }

            WaveJetSpotlight.extinguish(holder);
            if (!level.getBlockState(lit).is(Blocks.WATER)) {
                helper.fail("Extinguishing under water left " + level.getBlockState(lit) + " instead of water");
            }
            helper.succeed();
        });

        r.add("wave_jet/spotlight_never_eats_a_real_block", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos floor = shaft(helper, Blocks.AIR.defaultBlockState());
            BlockPos lit = floor.above();
            level.setBlockAndUpdate(lit, Blocks.GOLD_BLOCK.defaultBlockState());

            Player holder = aiming(helper, floor);
            WaveJetSpotlight.aim(level, holder);

            if (!level.getBlockState(lit).is(Blocks.GOLD_BLOCK)) {
                helper.fail("The spotlight replaced a solid block with " + level.getBlockState(lit));
                return;
            }
            WaveJetSpotlight.extinguish(holder);
            helper.succeed();
        });

        registerHands(r);
        registerBreath(r);
    }

    private static void registerBreath(NTTestRegistrar r) {
        r.add("wave_jet/thrusting_holds_the_air_you_have", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            int max = player.getMaxAirSupply();

            player.setAirSupply(max / 2);
            int before = player.getAirSupply();
            player.setAirSupply(before - 1);
            WaveJetItem.holdBreath(player);

            if (player.getAirSupply() != before) {
                helper.fail("A tick of air loss was not given back, went from " + before
                        + " to " + player.getAirSupply());
                return;
            }
            helper.succeed();
        });

        r.add("wave_jet/thrusting_does_not_refill_past_full", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            int max = player.getMaxAirSupply();

            player.setAirSupply(max);
            WaveJetItem.holdBreath(player);
            if (player.getAirSupply() != max) {
                helper.fail("A full bar was pushed past full, to " + player.getAirSupply());
                return;
            }
            helper.succeed();
        });

        r.add("wave_jet/thrusting_does_not_rescue_you_from_empty", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);

            player.setAirSupply(0);
            WaveJetItem.holdBreath(player);
            if (player.getAirSupply() != 0) {
                helper.fail("An empty bar was topped up to " + player.getAirSupply()
                        + ", which would stop you drowning for free");
                return;
            }
            helper.succeed();
        });
    }

    private static void registerHands(NTTestRegistrar r) {
        r.add("wave_jet/an_occupied_offhand_unequips_it", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.WAVE_JET.get()));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.TORCH));

            if (!WaveJetHands.enforce(player)) {
                helper.fail("A torch in the offhand did not displace the Wave Jet");
                return;
            }
            if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                helper.fail("The Wave Jet stayed in the main hand");
                return;
            }
            if (!player.getItemInHand(InteractionHand.OFF_HAND).is(Items.TORCH)) {
                helper.fail("The torch was taken instead of the Wave Jet");
                return;
            }
            if (!player.getInventory().contains(stack -> stack.is(NTItems.WAVE_JET.get()))) {
                helper.fail("The Wave Jet was not put back into the inventory");
                return;
            }
            helper.succeed();
        });

        r.add("wave_jet/an_occupied_main_hand_unequips_it", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(NTItems.WAVE_JET.get()));

            if (!WaveJetHands.enforce(player)) {
                helper.fail("A sword in the main hand did not displace the Wave Jet");
                return;
            }
            if (!player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
                helper.fail("The Wave Jet stayed in the offhand");
                return;
            }
            if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.IRON_SWORD)) {
                helper.fail("The sword was taken instead of the Wave Jet");
                return;
            }
            helper.succeed();
        });

        r.add("wave_jet/one_free_hand_is_left_alone", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.WAVE_JET.get()));
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);

            if (WaveJetHands.enforce(player)) {
                helper.fail("The Wave Jet was displaced even though the offhand was empty");
                return;
            }
            if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(NTItems.WAVE_JET.get())) {
                helper.fail("The Wave Jet left a hand it was entitled to");
                return;
            }
            helper.succeed();
        });

        r.add("wave_jet/two_of_them_leaves_the_main_hand_holding_one", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.WAVE_JET.get()));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(NTItems.WAVE_JET.get()));

            if (!WaveJetHands.enforce(player)) {
                helper.fail("Holding two Wave Jets was allowed to stand");
                return;
            }
            if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(NTItems.WAVE_JET.get())) {
                helper.fail("The main hand lost its Wave Jet instead of the offhand");
                return;
            }
            if (!player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
                helper.fail("The offhand kept its Wave Jet");
                return;
            }
            helper.succeed();
        });
    }

    private static BlockPos shaft(GameTestHelper helper, BlockState fill) {
        ServerLevel level = helper.getLevel();
        BlockPos floor = helper.absolutePos(new BlockPos(4, 1, 4));
        level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
        for (int y = 1; y <= 6; y++) {
            level.setBlockAndUpdate(floor.above(y), fill);
        }
        return floor;
    }

    private static Player aiming(GameTestHelper helper, BlockPos floor) {
        Player holder = helper.makeMockPlayer(GameType.SURVIVAL);
        holder.setPos(floor.getX() + 0.5, floor.getY() + 3, floor.getZ() + 0.5);
        holder.setYRot(0.0F);
        holder.setXRot(90.0F);

        ItemStack stack = new ItemStack(NTItems.WAVE_JET.get());
        NTDataComponentsUtils.setAbilityStatus(stack, true);
        holder.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return holder;
    }

    private WaveJetTests() {
    }
}
