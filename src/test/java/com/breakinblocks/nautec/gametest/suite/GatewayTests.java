package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.api.gateways.GatewayIndex;
import com.breakinblocks.nautec.content.blockentities.GatewayBlockEntity;
import com.breakinblocks.nautec.content.blocks.GatewayBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class GatewayTests {
    private GatewayTests() {
    }

    private static GatewayBlockEntity place(GameTestHelper helper, BlockPos pos, GatewayAddress address) {
        helper.setBlock(pos, NTBlocks.GATEWAY.get().defaultBlockState());
        GatewayBlockEntity gateway = helper.getBlockEntity(pos, GatewayBlockEntity.class);
        if (gateway == null) {
            throw helper.assertionException("Expected GatewayBlockEntity at " + pos);
        }
        gateway.setAddress(address);
        return gateway;
    }

    public static void register(NTTestRegistrar r) {
        r.add("gateway/address_packs_and_unpacks", 20, helper -> {
            helper.assertValueEqual(4096, GatewayAddress.addressCount(), "total addressable combinations");

            for (int packed : new int[]{0, 1, 7, 8, 63, 512, 4095}) {
                GatewayAddress address = GatewayAddress.unpack(packed);
                helper.assertValueEqual(packed, address.pack(), "round trip of packed address " + packed);
            }

            GatewayAddress cyan = GatewayAddress.DEFAULT;
            helper.assertValueEqual(DyeColor.CYAN, cyan.slots().get(0), "default address colour");

            GatewayAddress changed = cyan.withSlot(2, DyeColor.BLACK);
            helper.assertValueEqual(DyeColor.BLACK, changed.slots().get(2), "recoloured slot");
            helper.assertValueEqual(DyeColor.CYAN, changed.slots().get(0), "untouched slot");
            helper.assertFalse(changed.equals(cyan), "Recolouring should produce a different address");
            helper.succeed();
        });

        r.add("gateway/dye_slot_from_hit_quadrant", 20, helper -> {
            BlockPos pos = new BlockPos(4, 1, 4);
            helper.assertValueEqual(0, GatewayBlock.slotFor(pos, new Vec3(4.2, 1.25, 4.2)), "north west quadrant");
            helper.assertValueEqual(1, GatewayBlock.slotFor(pos, new Vec3(4.8, 1.25, 4.2)), "north east quadrant");
            helper.assertValueEqual(2, GatewayBlock.slotFor(pos, new Vec3(4.2, 1.25, 4.8)), "south west quadrant");
            helper.assertValueEqual(3, GatewayBlock.slotFor(pos, new Vec3(4.8, 1.25, 4.8)), "south east quadrant");
            helper.succeed();
        });

        r.add("gateway/index_registers_and_forgets", 40, helper -> {
            BlockPos pos = new BlockPos(4, 1, 4);
            place(helper, pos, GatewayAddress.uniform(DyeColor.BLUE));

            helper.runAfterDelay(5, () -> {
                GatewayIndex index = GatewayIndex.get(helper.getLevel());
                BlockPos absolute = helper.absolutePos(pos);
                helper.assertTrue(index.addressAt(absolute) != null, "A placed gateway should be in the index");

                helper.setBlock(pos, Blocks.AIR.defaultBlockState());
                helper.runAfterDelay(5, () -> {
                    helper.assertTrue(GatewayIndex.get(helper.getLevel()).addressAt(absolute) == null,
                            "A broken gateway should be dropped from the index");
                    helper.succeed();
                });
            });
        });

        r.add("gateway/moves_an_entity_to_the_matching_pair", 120, helper -> {
            BlockPos from = new BlockPos(1, 1, 4);
            BlockPos to = new BlockPos(7, 1, 4);
            GatewayAddress address = GatewayAddress.uniform(DyeColor.WHITE);
            place(helper, from, address);
            place(helper, to, address);

            Cow cow = helper.spawn(EntityType.COW, from.above());

            helper.succeedWhen(() -> {
                BlockPos target = helper.absolutePos(to);
                helper.assertTrue(cow.blockPosition().closerThan(target, 2.0),
                        "The cow should be standing on the far gateway at " + target
                                + ", but was at " + cow.blockPosition());
            });
        });

        r.add("gateway/mismatched_address_does_not_travel", 100, helper -> {
            BlockPos from = new BlockPos(1, 1, 4);
            BlockPos to = new BlockPos(7, 1, 4);
            place(helper, from, GatewayAddress.uniform(DyeColor.PURPLE));
            place(helper, to, GatewayAddress.uniform(DyeColor.MAGENTA));

            Cow cow = helper.spawn(EntityType.COW, from.above());

            helper.runAfterDelay(80, () -> {
                helper.assertFalse(cow.isOnPortalCooldown(),
                        "A gateway with no matching partner should never have moved the cow");
                helper.succeed();
            });
        });

        r.add("gateway/cooldown_stops_it_bouncing_back", 200, helper -> {
            BlockPos from = new BlockPos(1, 1, 4);
            BlockPos to = new BlockPos(7, 1, 4);
            GatewayAddress address = GatewayAddress.uniform(DyeColor.LIME);
            place(helper, from, address);
            place(helper, to, address);

            Cow cow = helper.spawn(EntityType.COW, from.above());

            helper.runAfterDelay(40, () -> helper.assertTrue(cow.isOnPortalCooldown(),
                    "An entity that just travelled should be on cooldown"));

            helper.runAfterDelay(60, () -> {
                BlockPos origin = helper.absolutePos(from);
                helper.assertFalse(cow.blockPosition().closerThan(origin, 3.0),
                        "The cow bounced straight back to the gateway it came from, so the cooldown is not holding");
                helper.succeed();
            });
        });
    }
}
