package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.bubble.AirPocketBlockEntity;
import com.breakinblocks.nautec.content.bubble.BubbleAnchorBlockEntity;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class BubbleAnchorTests {
    private static final BlockPos ANCHOR = new BlockPos(4, 4, 4);
    private static final int RADIUS = 2;

    private BubbleAnchorTests() {
    }

    private static void flood(NTGameTestHelper helper) {
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    BlockPos pos = ANCHOR.offset(dx, dy, dz);
                    if (!pos.equals(ANCHOR)) {
                        helper.setBlock(pos, Blocks.WATER.defaultBlockState());
                    }
                }
            }
        }
    }

    private static int countIn(NTGameTestHelper helper, Block block) {
        int count = 0;
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    if (helper.getBlockState(ANCHOR.offset(dx, dy, dz)).is(block)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static BubbleAnchorBlockEntity anchor(NTGameTestHelper helper) {
        helper.setBlock(ANCHOR, NTBlocks.BUBBLE_ANCHOR.get());
        BubbleAnchorBlockEntity anchor = helper.getBlockEntity(ANCHOR, BubbleAnchorBlockEntity.class);
        anchor.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.DRIED_KELP_BLOCK, 4));
        return anchor;
    }

    private static void ticks(BubbleAnchorBlockEntity anchor, int count) {
        for (int i = 0; i < count; i++) {
            anchor.commonTick();
        }
    }

    private interface Body {
        void run();
    }

    private static void withSmallField(Body body) {
        int radius = NTConfig.bubbleAnchorRadius;
        int max = NTConfig.bubbleAnchorMaxRadius;
        try {
            NTConfig.bubbleAnchorRadius = RADIUS;
            NTConfig.bubbleAnchorMaxRadius = RADIUS + 1;
            body.run();
        } finally {
            NTConfig.bubbleAnchorRadius = radius;
            NTConfig.bubbleAnchorMaxRadius = max;
        }
    }

    public static void register(NTTestRegistrar r) {
        r.add("bubble_capsule/holds_then_lets_the_sea_back", 20, helper -> {
            int cube = (RADIUS * 2 + 1) * (RADIUS * 2 + 1) * (RADIUS * 2 + 1) - 1;
            flood(helper);
            helper.setBlock(ANCHOR, NTBlocks.AIR_POCKET.get());
            AirPocketBlockEntity pocket = helper.getBlockEntity(ANCHOR, AirPocketBlockEntity.class);
            pocket.start(helper.getLevel(), RADIUS, 3);
            helper.assertValueEqual(countIn(helper, NTBlocks.HELD_WATER.get()), cube, "every water block around the capsule is held");
            helper.setBlock(ANCHOR.east(), Blocks.STONE.defaultBlockState());
            for (int i = 0; i < 4; i++) {
                pocket.serverTick(helper.getLevel());
            }
            helper.assertValueEqual(countIn(helper, NTBlocks.HELD_WATER.get()), 0, "nothing is held once the pocket ends");
            helper.assertValueEqual(countIn(helper, Blocks.WATER), cube, "the sea comes back into every open space");
            helper.assertTrue(helper.getBlockState(ANCHOR.east()).is(Blocks.STONE), "blocks built inside the pocket stay");
            helper.succeed();
        });

        r.add("bubble_capsule/anchor_release_skips_a_live_pocket", 20, helper -> withSmallField(() -> {
            flood(helper);
            BubbleAnchorBlockEntity anchor = anchor(helper);
            ticks(anchor, 3);
            BlockPos center = ANCHOR.offset(1, 1, 1);
            helper.setBlock(center, NTBlocks.AIR_POCKET.get());
            helper.getBlockEntity(center, AirPocketBlockEntity.class).start(helper.getLevel(), 1, 600);
            anchor.setEnabled(false);
            ticks(anchor, 3);
            helper.assertTrue(helper.getBlockState(ANCHOR.offset(2, 2, 2)).is(NTBlocks.HELD_WATER.get()), "the pocket keeps its water held");
            helper.assertTrue(helper.getBlockState(ANCHOR.offset(-2, -2, -2)).is(Blocks.WATER), "the rest of the field refills");
            helper.setBlock(center, Blocks.STONE.defaultBlockState());
            helper.assertTrue(helper.getBlockState(ANCHOR.offset(2, 2, 2)).is(Blocks.WATER), "replacing the core lets the water back");
            helper.succeed();
        }));

        r.add("bubble_anchor/clears_holds_and_refills", 40, helper -> withSmallField(() -> {
            int cube = (RADIUS * 2 + 1) * (RADIUS * 2 + 1) * (RADIUS * 2 + 1) - 1;
            flood(helper);
            BubbleAnchorBlockEntity anchor = anchor(helper);
            ticks(anchor, 3);
            helper.assertValueEqual(countIn(helper, NTBlocks.HELD_WATER.get()), cube, "every water block is held");
            helper.assertValueEqual(anchor.getStatus(), BubbleAnchorBlockEntity.STATUS_FUEL, "running on fuel");
            helper.assertValueEqual(anchor.getItemStackHandler().getStackInSlot(0).getCount(), 3, "one fuel item burned");

            anchor.setEnabled(false);
            ticks(anchor, 3);
            helper.assertValueEqual(countIn(helper, NTBlocks.HELD_WATER.get()), 0, "nothing is held once the field ends");
            helper.assertValueEqual(countIn(helper, Blocks.WATER), cube, "water mode refills every open space");

            anchor.setFillWater(false);
            anchor.setEnabled(true);
            ticks(anchor, 3);
            helper.setBlock(ANCHOR.east(), Blocks.STONE.defaultBlockState());
            anchor.setEnabled(false);
            ticks(anchor, 3);
            helper.assertValueEqual(countIn(helper, Blocks.WATER), 0, "air mode leaves no water");
            helper.assertTrue(helper.getBlockState(ANCHOR.east()).is(Blocks.STONE), "blocks built inside the field stay");
            helper.succeed();
        }));

        r.add("bubble_anchor/outside_water_stays_out", 80, helper -> {
            int radius = NTConfig.bubbleAnchorRadius;
            NTConfig.bubbleAnchorRadius = RADIUS;
            flood(helper);
            anchor(helper);
            helper.runAfterDelay(10, () -> {
                helper.assertTrue(helper.getBlockState(ANCHOR.south(RADIUS)).is(NTBlocks.HELD_WATER.get()), "the edge is held");
                helper.setBlock(ANCHOR.south(RADIUS + 1), Blocks.WATER.defaultBlockState());
            });
            helper.runAfterDelay(50, () -> {
                NTConfig.bubbleAnchorRadius = radius;
                helper.assertTrue(helper.getBlockState(ANCHOR.south(RADIUS)).is(NTBlocks.HELD_WATER.get()), "water next to the field does not flow in");
                helper.assertTrue(helper.getBlockState(ANCHOR.south(RADIUS - 1)).is(NTBlocks.HELD_WATER.get()), "the inside stays dry");
                helper.succeed();
            });
        });

        r.add("bubble_anchor/laser_runs_it_and_purity_grows_it", 20, helper -> withSmallField(() -> {
            BubbleAnchorBlockEntity anchor = anchor(helper);
            anchor.getItemStackHandler().setStackInSlot(0, ItemStack.EMPTY);
            BlockPos origin = helper.absolutePos(ANCHOR.north());
            anchor.receivePower(NTConfig.bubbleAnchorLaserPower, Direction.SOUTH, origin);
            anchor.receiveNewPurity(1.0F, Direction.SOUTH, origin);
            anchor.commonTick();
            helper.assertValueEqual(anchor.getStatus(), BubbleAnchorBlockEntity.STATUS_LASER, "a strong enough beam runs it without fuel");
            helper.assertValueEqual(anchor.currentRadius(), RADIUS, "a weak purity keeps the base size");
            anchor.receivePower(NTConfig.bubbleAnchorLaserPower, Direction.SOUTH, origin);
            anchor.receiveNewPurity(3.0F, Direction.SOUTH, origin);
            anchor.commonTick();
            helper.assertValueEqual(anchor.currentRadius(), RADIUS + 1, "high purity grows the field up to the cap");
            helper.succeed();
        }));

        r.add("bubble_anchor/breaking_it_releases_the_water", 20, helper -> withSmallField(() -> {
            flood(helper);
            BubbleAnchorBlockEntity anchor = anchor(helper);
            ticks(anchor, 3);
            helper.assertTrue(countIn(helper, NTBlocks.HELD_WATER.get()) > 0, "water is held");
            helper.getLevel().destroyBlock(helper.absolutePos(ANCHOR), false);
            helper.assertValueEqual(countIn(helper, NTBlocks.HELD_WATER.get()), 0, "breaking the anchor releases everything");
            helper.assertTrue(helper.getBlockState(ANCHOR.above()).is(Blocks.WATER), "the water comes back");
            helper.succeed();
        }));

        r.add("bubble_anchor/fuel_values", 20, helper -> {
            helper.assertTrue(BubbleAnchorBlockEntity.fuelTicks(new ItemStack(Items.DRIED_KELP_BLOCK)) >= BubbleAnchorBlockEntity.fuelTicks(new ItemStack(Items.SEA_PICKLE)),
                    "a dried kelp block outlasts a sea pickle");
            helper.assertTrue(BubbleAnchorBlockEntity.fuelTicks(new ItemStack(Items.KELP)) > 0, "kelp is fuel");
            helper.assertValueEqual(BubbleAnchorBlockEntity.fuelTicks(new ItemStack(Items.DIRT)), 0, "dirt is not fuel");
            helper.succeed();
        });
    }
}
