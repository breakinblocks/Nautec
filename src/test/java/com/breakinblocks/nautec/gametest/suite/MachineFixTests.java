package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.api.gateways.GatewayIndex;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.fluid.DivingSuitAirHandler;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.content.augments.GuardianEyeAugment;
import com.breakinblocks.nautec.content.blockentities.ResonanceChamberBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.DrainBlockEntity;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import com.breakinblocks.nautec.registries.NTAugmentSlots;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

public final class MachineFixTests {
    private static final BlockPos CENTRE = new BlockPos(4, 1, 4);

    private MachineFixTests() {
    }

    private static ResonanceChamberBlockEntity chamber(GameTestHelper helper, BlockPos pos) {
        ResonanceChamberBlockEntity chamber = helper.getBlockEntity(pos, ResonanceChamberBlockEntity.class);
        if (chamber == null) {
            throw helper.assertionException("Expected ResonanceChamberBlockEntity at " + pos);
        }
        return chamber;
    }

    private static GatewayAddress address(DyeColor a, DyeColor b, DyeColor c, DyeColor d) {
        return new GatewayAddress(List.of(a, b, c, d));
    }

    private static IPowerStorage power(GameTestHelper helper, ItemStack stack) {
        IPowerStorage storage = stack.getCapability(NTCapabilities.PowerStorage.ITEM);
        if (storage == null) {
            throw helper.assertionException(stack + " has no power capability");
        }
        return storage;
    }

    private static ItemStack charged(GameTestHelper helper, ItemStack stack, int amount) {
        power(helper, stack).setPowerStored(amount);
        return stack;
    }

    private static void placeDrain(GameTestHelper helper) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block block = (dx == 0 && dz == 0) ? NTBlocks.DRAIN.get() : NTBlocks.DRAIN_WALL.get();
                helper.setBlock(CENTRE.offset(dx, 0, dz), block);
            }
        }
    }

    private static boolean formDrain(GameTestHelper helper) {
        return MultiblockHelper.form(NTMultiblocks.DRAIN.get(), helper.absolutePos(CENTRE), helper.getLevel());
    }

    private static ArmorStand armourStand(GameTestHelper helper, BlockPos pos, ItemStack head, ItemStack feet) {
        helper.setBlock(pos.below(), Blocks.STONE.defaultBlockState());
        ArmorStand stand = helper.spawn(EntityType.ARMOR_STAND, pos);
        head.inventoryTick(helper.getLevel(), stand, EquipmentSlot.HEAD);
        feet.inventoryTick(helper.getLevel(), stand, EquipmentSlot.FEET);
        stand.setItemSlot(EquipmentSlot.HEAD, head);
        stand.setItemSlot(EquipmentSlot.FEET, feet);
        return stand;
    }

    private static void assertNear(GameTestHelper helper, double expected, double actual, String what) {
        if (Math.abs(expected - actual) > 1.0e-3) {
            helper.fail(what + ": expected " + expected + " but was " + actual);
        }
    }

    public static void register(NTTestRegistrar r) {
        r.add("machinefix/resonance_rejects_non_recipe_items", 40, helper -> {
            helper.setBlock(CENTRE, NTBlocks.RESONANCE_CHAMBER.get().defaultBlockState());
            helper.runAfterDelay(2, () -> {
                ResonanceChamberBlockEntity chamber = chamber(helper, CENTRE);
                var handler = chamber.getItemStackHandler();
                helper.assertFalse(handler.isItemValid(0, new ItemStack(NTItems.PRISM_MONOCLE.get())),
                        "A monocle is not a resonance input");
                helper.assertFalse(handler.isItemValid(0, new ItemStack(NTItems.AQUARINE_WRENCH.get())),
                        "A wrench is not a resonance input");
                helper.assertTrue(handler.isItemValid(0, new ItemStack(NTItems.PRISMARINE_CRYSTAL_SHARD.get())),
                        "A crystal shard is a resonance input");
                helper.assertFalse(handler.isItemValid(1, new ItemStack(NTItems.PRISMARINE_CRYSTAL_SHARD.get())),
                        "Nothing may be inserted into the output slot");

                Player player = helper.makeMockPlayer(GameType.SURVIVAL);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.PRISM_MONOCLE.get()));
                helper.useBlock(CENTRE, player);
                helper.assertTrue(handler.getStackInSlot(0).isEmpty(),
                        "Right-clicking with a monocle put " + handler.getStackInSlot(0) + " into the chamber");
                helper.assertTrue(player.getMainHandItem().is(NTItems.PRISM_MONOCLE.get()),
                        "The monocle should still be in the player's hand");

                ResourceHandler<ItemResource> side = helper.getLevel().getCapability(Capabilities.Item.BLOCK,
                        helper.absolutePos(CENTRE), Direction.NORTH);
                helper.assertTrue(side != null, "The chamber should expose an item handler on its side");
                try (Transaction tx = Transaction.openRoot()) {
                    helper.assertValueEqual(0, side.insert(0, ItemResource.of(Items.STICK), 4, tx),
                            "sticks inserted by automation");
                    helper.assertValueEqual(1, side.insert(0, ItemResource.of(NTItems.PRISMARINE_CRYSTAL_SHARD.get()), 1, tx),
                            "crystal shards inserted by automation");
                    tx.commit();
                }

                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.PRISMARINE_CRYSTAL_SHARD.get()));
                helper.useBlock(CENTRE, player);
                helper.assertValueEqual(2, handler.getStackInSlot(0).getCount(), "shards in the input after a hand insert");
                helper.assertTrue(player.getMainHandItem().isEmpty(), "The shard should have left the player's hand");
                helper.succeed();
            });
        });

        r.add("machinefix/resonance_output_extractable_from_below", 40, helper -> {
            helper.setBlock(CENTRE, NTBlocks.RESONANCE_CHAMBER.get().defaultBlockState());
            helper.runAfterDelay(2, () -> {
                ResonanceChamberBlockEntity chamber = chamber(helper, CENTRE);
                chamber.getItemStackHandler().setStackInSlot(1, new ItemStack(NTItems.RESONANT_SHARD.get(), 3));
                BlockPos abs = helper.absolutePos(CENTRE);
                ItemResource shard = ItemResource.of(NTItems.RESONANT_SHARD.get());

                ResourceHandler<ItemResource> north = helper.getLevel().getCapability(Capabilities.Item.BLOCK, abs, Direction.NORTH);
                helper.assertTrue(north != null, "The chamber should expose an item handler on its side");
                try (Transaction tx = Transaction.openRoot()) {
                    helper.assertValueEqual(1, north.extract(1, shard, 1, tx), "output pulled from the side");
                    helper.assertValueEqual(0, north.extract(0, shard, 1, tx), "input pulled from the side");
                }

                ResourceHandler<ItemResource> down = helper.getLevel().getCapability(Capabilities.Item.BLOCK, abs, Direction.DOWN);
                helper.assertTrue(down != null, "The chamber should expose an item handler on its bottom");
                try (Transaction tx = Transaction.openRoot()) {
                    helper.assertValueEqual(2, down.extract(1, shard, 2, tx), "output pulled from the bottom");
                    helper.assertValueEqual(0, down.insert(0, ItemResource.of(NTItems.PRISMARINE_CRYSTAL_SHARD.get()), 1, tx),
                            "input pushed in from the bottom");
                    tx.commit();
                }
                helper.assertValueEqual(1, chamber.getItemStackHandler().getStackInSlot(1).getCount(), "output left after extraction");
                helper.succeed();
            });
        });

        r.add("machinefix/resonance_hopper_pulls_output", 100, helper -> {
            BlockPos hopperPos = CENTRE;
            BlockPos chamberPos = CENTRE.above();
            helper.setBlock(hopperPos, Blocks.HOPPER.defaultBlockState());
            helper.setBlock(chamberPos, NTBlocks.RESONANCE_CHAMBER.get().defaultBlockState());
            helper.runAfterDelay(2, () -> chamber(helper, chamberPos).getItemStackHandler()
                    .setStackInSlot(1, new ItemStack(NTItems.RESONANT_SHARD.get())));
            helper.succeedWhen(() -> {
                HopperBlockEntity hopper = helper.getBlockEntity(hopperPos, HopperBlockEntity.class);
                boolean pulled = false;
                for (int i = 0; i < hopper.getContainerSize(); i++) {
                    if (hopper.getItem(i).is(NTItems.RESONANT_SHARD.get())) {
                        pulled = true;
                    }
                }
                helper.assertTrue(pulled, "A hopper under the chamber should pull the Resonant Shard out");
            });
        });

        r.add("machinefix/resonance_strong_beam_lands_in_window", 40, helper -> {
            helper.setBlock(CENTRE, NTBlocks.RESONANCE_CHAMBER.get().defaultBlockState());
            helper.runAfterDelay(2, () -> {
                ResonanceChamberBlockEntity chamber = chamber(helper, CENTRE);
                chamber.setPurity(3.0f);
                chamber.getItemStackHandler().setStackInSlot(0, new ItemStack(NTItems.PRISMARINE_CRYSTAL_SHARD.get()));

                chamber.setPowerPerSide(Direction.UP, 5000);
                chamber.commonTick();
                assertNear(helper, 8000.0, chamber.getStabilityCeiling(), "ceiling at purity 3");
                assertNear(helper, 5000.0, chamber.getCharge(), "charge after one tick of a 5000 AP beam");

                chamber.setPowerPerSide(Direction.UP, 5000);
                chamber.commonTick();
                helper.assertFalse(chamber.isVenting(),
                        "A beam of 62% of the ceiling per tick jumped past the critical window and vented");
                helper.assertTrue(chamber.getItemStackHandler().getStackInSlot(1).is(NTItems.RESONANT_SHARD.get()),
                        "The charge should have stopped inside the window and crafted, output held "
                                + chamber.getItemStackHandler().getStackInSlot(1));
                helper.succeed();
            });
        });

        r.add("machinefix/gateway_index_sees_unloaded_partner", 20, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos from = helper.absolutePos(CENTRE);
            BlockPos far = from.offset(3072, 0, 0);
            helper.assertFalse(level.isLoaded(far), "The far position should start unloaded");
            GatewayAddress code = address(DyeColor.BLUE, DyeColor.LIME, DyeColor.BLUE, DyeColor.PURPLE);
            GatewayIndex index = GatewayIndex.get(level);
            index.put(far, code);
            BlockPos found = index.findNearest(level, from, code);
            index.remove(far);
            helper.assertValueEqual(far, found, "nearest partner for an address whose only match is unloaded");
            helper.assertFalse(level.isLoaded(far), "Searching the index should not load the partner's chunk");
            helper.succeed();
        });

        r.add("machinefix/guardian_eye_beam_stops_at_blocks", 40, helper -> {
            helper.setBlock(new BlockPos(5, 0, 4), Blocks.STONE.defaultBlockState());
            for (int y = 1; y <= 3; y++) {
                helper.setBlock(new BlockPos(3, y, 4), Blocks.STONE.defaultBlockState());
            }
            Cow cow = helper.spawn(EntityType.COW, new BlockPos(5, 1, 4));
            cow.setNoAi(true);

            helper.runAfterDelay(2, () -> {
                Player player = helper.makeMockPlayer(GameType.SURVIVAL);
                player.snapTo(helper.absoluteVec(new Vec3(1.5, 1.0, 4.5)), -90.0f, 0.0f);
                GuardianEyeAugment augment = new GuardianEyeAugment(NTAugmentSlots.EYES.get());
                augment.setPlayer(player);
                float health = cow.getHealth();

                augment.handleKeybindPress();
                helper.assertTrue(augment.getTargetEntity() == null, "The beam went through the stone wall and locked onto the cow");
                assertNear(helper, health, cow.getHealth(), "cow health behind a wall");

                for (int y = 1; y <= 3; y++) {
                    helper.setBlock(new BlockPos(3, y, 4), Blocks.AIR.defaultBlockState());
                }
                augment.handleKeybindPress();
                helper.assertTrue(augment.getTargetEntity() == cow, "With the wall gone the beam should reach the cow");
                helper.succeed();
            });
        });

        r.add("machinefix/aquarine_armor_bonus_stacks_per_slot", 60, helper -> {
            ArmorStand full = armourStand(helper, new BlockPos(1, 1, 4),
                    charged(helper, new ItemStack(NTItems.AQUARINE_HELMET.get()), 512),
                    charged(helper, new ItemStack(NTItems.AQUARINE_BOOTS.get()), 512));
            ArmorStand mixed = armourStand(helper, new BlockPos(4, 1, 4),
                    charged(helper, new ItemStack(NTItems.AQUARINE_HELMET.get()), 512),
                    new ItemStack(NTItems.AQUARINE_BOOTS.get()));
            ArmorStand empty = armourStand(helper, new BlockPos(7, 1, 4),
                    new ItemStack(NTItems.AQUARINE_HELMET.get()),
                    new ItemStack(NTItems.AQUARINE_BOOTS.get()));

            helper.runAfterDelay(20, () -> {
                double base = empty.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
                assertNear(helper, 10.0, full.getAttributeValue(Attributes.ARMOR_TOUGHNESS) - base,
                        "toughness bonus from two charged pieces");
                assertNear(helper, 5.0, mixed.getAttributeValue(Attributes.ARMOR_TOUGHNESS) - base,
                        "toughness bonus from one charged and one empty piece");
                helper.succeed();
            });
        });

        r.add("machinefix/aquarine_shovel_charges_ap_per_block", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack shovel = charged(helper, new ItemStack(NTItems.AQUARINE_SHOVEL.get()), 100);
            BlockState dirt = Blocks.DIRT.defaultBlockState();
            shovel.getItem().mineBlock(shovel, helper.getLevel(), dirt, helper.absolutePos(CENTRE), player);
            helper.assertValueEqual(99, power(helper, shovel).getPowerStored(), "shovel AP after mining one block with the ability off");
            helper.succeed();
        });

        r.add("machinefix/aquarine_shovel_area_mines_and_charges", 40, helper -> {
            helper.setBlock(CENTRE.below(), Blocks.STONE.defaultBlockState());
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    helper.setBlock(CENTRE.offset(dx, 0, dz), (dx == 0 && dz == 0) ? Blocks.AIR : Blocks.DIRT);
                }
            }
            helper.runAfterDelay(2, () -> {
                Player player = helper.makeMockPlayer(GameType.SURVIVAL);
                player.snapTo(helper.absoluteVec(new Vec3(4.5, 3.0, 4.5)), 0.0f, 90.0f);
                ItemStack shovel = charged(helper, new ItemStack(NTItems.AQUARINE_SHOVEL.get()), 100);
                NTDataComponentsUtils.setAbilityStatus(shovel, true);

                shovel.getItem().mineBlock(shovel, helper.getLevel(), Blocks.DIRT.defaultBlockState(), helper.absolutePos(CENTRE), player);

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos pos = CENTRE.offset(dx, 0, dz);
                        helper.assertTrue(helper.getBlockState(pos).isAir(), "The 3x3 ability should have dug " + pos);
                    }
                }
                helper.assertValueEqual(100 - 1 - 8 * 2, power(helper, shovel).getPowerStored(),
                        "shovel AP after a 3x3 dig (1 for the block, 2 for each extra block)");
                helper.succeed();
            });
        });

        r.add("machinefix/air_bottle_refills_non_full_tank", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack chest = new ItemStack(NTItems.DIVING_CHESTPLATE.get());
            player.setItemSlot(EquipmentSlot.CHEST, chest);

            NTDataComponentsUtils.setOxygenLevels(player.getItemBySlot(EquipmentSlot.CHEST), 300);
            ItemStack bottles = new ItemStack(NTItems.AIR_BOTTLE.get(), 2);
            bottles.getItem().finishUsingItem(bottles, helper.getLevel(), player);
            helper.assertValueEqual(420, NTDataComponentsUtils.getOxygenLevels(player.getItemBySlot(EquipmentSlot.CHEST)),
                    "oxygen after drinking a bottle with a half-full tank");
            helper.assertValueEqual(1, bottles.getCount(), "bottles left after drinking one of two");

            NTDataComponentsUtils.setOxygenLevels(player.getItemBySlot(EquipmentSlot.CHEST), 550);
            ItemStack another = new ItemStack(NTItems.AIR_BOTTLE.get());
            another.getItem().finishUsingItem(another, helper.getLevel(), player);
            helper.assertValueEqual(600, NTDataComponentsUtils.getOxygenLevels(player.getItemBySlot(EquipmentSlot.CHEST)),
                    "oxygen is capped at a full tank");
            helper.succeed();
        });

        r.add("machinefix/diving_chestplate_air_handler", 20, helper -> {
            ItemStack chest = new ItemStack(NTItems.DIVING_CHESTPLATE.get());
            NTDataComponentsUtils.setOxygenLevels(chest, 100);
            ResourceHandler<FluidResource> tank = ItemAccess.forStack(chest).getCapability(Capabilities.Fluid.ITEM);
            helper.assertTrue(tank != null, "The diving chestplate should expose a fluid handler");
            helper.assertValueEqual(600L, tank.getCapacityAsLong(0, FluidResource.EMPTY), "air tank capacity in mB");

            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(0, tank.insert(FluidResource.of(Fluids.WATER), 50, tx), "water accepted into the air tank");
                tx.commit();
            }
            helper.assertValueEqual(100, NTDataComponentsUtils.getOxygenLevels(chest), "oxygen after offering water");

            List<FluidResource> oxygenFluids = DivingSuitAirHandler.oxygenFluids();
            if (!oxygenFluids.isEmpty()) {
                try (Transaction tx = Transaction.openRoot()) {
                    helper.assertValueEqual(50, tank.insert(oxygenFluids.getFirst(), 50, tx), "oxygen accepted into the air tank");
                    tx.commit();
                }
                helper.assertValueEqual(150, NTDataComponentsUtils.getOxygenLevels(chest), "oxygen after filling 50 mB");
                try (Transaction tx = Transaction.openRoot()) {
                    helper.assertValueEqual(450, tank.insert(oxygenFluids.getFirst(), 1000, tx), "oxygen accepted up to a full tank");
                    tx.commit();
                }
                helper.assertValueEqual(600, NTDataComponentsUtils.getOxygenLevels(chest), "oxygen is capped at a full tank");
            }
            helper.succeed();
        });

        r.add("machinefix/prism_monocle_has_no_power", 20, helper -> {
            ItemStack monocle = new ItemStack(NTItems.PRISM_MONOCLE.get());
            helper.assertTrue(monocle.getCapability(NTCapabilities.PowerStorage.ITEM) == null,
                    "The monocle should not store power");
            helper.assertFalse(monocle.has(NTDataComponents.POWER.get()), "The monocle should not carry a power component");
            helper.assertFalse(monocle.isBarVisible(), "The monocle should not show a power bar");
            helper.succeed();
        });

        r.add("machinefix/drain_bucket_needs_a_full_bucket", 60, helper -> {
            placeDrain(helper);
            helper.runAfterDelay(2, () -> {
                helper.assertTrue(formDrain(helper), "Drain multiblock should form");
                DrainBlockEntity drain = helper.getBlockEntity(CENTRE, DrainBlockEntity.class);
                Player player = helper.makeMockPlayer(GameType.SURVIVAL);

                drain.getFluidTank().setFluid(FluidStack.EMPTY);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
                helper.useBlock(CENTRE, player);
                helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "An empty drain should leave the bucket empty");
                helper.assertFalse(player.getInventory().contains(stack -> stack.is(Items.WATER_BUCKET)),
                        "An empty drain handed out a filled bucket");

                drain.getFluidTank().setFluid(new FluidStack(Fluids.WATER, 500));
                helper.useBlock(CENTRE, player);
                helper.assertValueEqual(500, drain.getFluidTank().getFluidAmount(), "tank after a bucket click with 500 mB");
                helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "A half-bucket tank should leave the bucket empty");
                helper.assertFalse(player.getInventory().contains(stack -> stack.is(Items.WATER_BUCKET)),
                        "A 500 mB tank handed out a full bucket");

                drain.getFluidTank().setFluid(new FluidStack(Fluids.WATER, 1500));
                helper.useBlock(CENTRE, player);
                helper.assertValueEqual(500, drain.getFluidTank().getFluidAmount(), "tank after filling one bucket from 1500 mB");
                helper.assertTrue(player.getInventory().contains(stack -> stack.is(Items.WATER_BUCKET)),
                        "A tank with a full bucket of water should fill the bucket");
                helper.succeed();
            });
        });

        r.add("machinefix/drain_break_keeps_block_above", 60, helper -> {
            placeDrain(helper);
            helper.runAfterDelay(2, () -> {
                helper.assertTrue(formDrain(helper), "Drain multiblock should form");
                helper.setBlock(CENTRE.above(), Blocks.STONE.defaultBlockState());
                helper.setBlock(CENTRE.offset(1, 1, 0), Blocks.STONE.defaultBlockState());
                helper.getLevel().destroyBlock(helper.absolutePos(CENTRE), false);
            });
            helper.runAfterDelay(6, () -> {
                helper.assertTrue(helper.getBlockState(CENTRE.above()).is(Blocks.STONE),
                        "Breaking the drain removed the block above it");
                helper.assertTrue(helper.getBlockState(CENTRE.offset(1, 1, 0)).is(Blocks.STONE),
                        "Breaking the drain removed a block above its wall");
                helper.assertTrue(helper.getBlockState(CENTRE.offset(1, 0, 0)).is(NTBlocks.DRAIN_WALL.get()),
                        "The drain walls should be restored when the controller breaks");
                helper.succeed();
            });
        });
    }
}
