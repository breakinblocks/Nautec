package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.content.menus.DeepSeaReservoirMenu;
import com.breakinblocks.nautec.content.reservoir.DeepSeaReservoirBlock;
import com.breakinblocks.nautec.content.reservoir.DeepSeaReservoirBlockEntity;
import com.breakinblocks.nautec.content.reservoir.DeepSeaReservoirItem;
import com.breakinblocks.nautec.content.reservoir.DeepSeaReservoirPartBlock;
import com.breakinblocks.nautec.content.reservoir.ReservoirFluid;
import com.breakinblocks.nautec.content.reservoir.ReservoirStructure;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import com.breakinblocks.nautec.transfer.TransferCapabilities;
import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.fluid.FluidResource;
import com.breakinblocks.nautec.transfer.transaction.Transaction;

import java.util.List;

public final class DeepSeaReservoirTests {
    private static final BlockPos BASE = new BlockPos(4, 1, 4);
    private static final BlockPos CORE = BASE.above();
    private static FluidResource water() {
        return FluidResource.of(Fluids.WATER);
    }

    private DeepSeaReservoirTests() {
    }

    private static InteractionResult place(NTGameTestHelper helper, Player player, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos clicked = helper.absolutePos(BASE);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(clicked), Direction.UP, clicked, false);
        BlockPlaceContext context = new BlockPlaceContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack, hit);
        return ((DeepSeaReservoirItem) stack.getItem()).place(context);
    }

    private static DeepSeaReservoirBlockEntity placed(NTGameTestHelper helper, ItemStack stack) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        InteractionResult result = place(helper, player, stack);
        helper.assertTrue(result.consumesAction(), "placing the reservoir works, got " + result);
        return helper.getBlockEntity(CORE, DeepSeaReservoirBlockEntity.class);
    }

    private static int insert(ResourceHandler<FluidResource> handler, int amount) {
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = handler.insert(water(), amount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    private static int extract(ResourceHandler<FluidResource> handler, int amount) {
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = handler.extract(water(), amount, transaction);
            transaction.commit();
            return extracted;
        }
    }

    public static void register(NTTestRegistrar r) {
        r.add("reservoir/places_a_full_3x3x3", 20, helper -> {
            DeepSeaReservoirBlockEntity reservoir = placed(helper, new ItemStack(NTBlocks.DEEP_SEA_RESERVOIR.get()));
            helper.assertValueEqual(1, reservoir.tier(), "a fresh reservoir is tier 1");
            BlockPos core = helper.absolutePos(CORE);
            for (int cell = 0; cell < ReservoirStructure.CELLS; cell++) {
                BlockPos pos = ReservoirStructure.cellPos(core, cell);
                if (cell == ReservoirStructure.CORE_CELL) {
                    helper.assertTrue(helper.getLevel().getBlockState(pos).is(NTBlocks.DEEP_SEA_RESERVOIR.get()), "the core sits in the middle");
                    continue;
                }
                var state = helper.getLevel().getBlockState(pos);
                helper.assertTrue(state.is(NTBlocks.DEEP_SEA_RESERVOIR_PART.get()), "cell " + cell + " holds a part");
                helper.assertValueEqual(cell, state.getValue(DeepSeaReservoirPartBlock.CELL), "part cell index");
                helper.assertValueEqual(core, DeepSeaReservoirPartBlock.corePos(state, pos), "part finds its core");
            }
            helper.succeed();
        });

        r.add("reservoir/needs_a_clear_space", 20, helper -> {
            helper.setBlock(BASE.offset(1, 2, -1), Blocks.STONE);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack stack = new ItemStack(NTBlocks.DEEP_SEA_RESERVOIR.get());
            InteractionResult result = place(helper, player, stack);
            helper.assertFalse(result.consumesAction(), "a blocked space refuses the reservoir");
            helper.assertBlockNotPresent(NTBlocks.DEEP_SEA_RESERVOIR.get(), CORE);
            helper.assertBlockNotPresent(NTBlocks.DEEP_SEA_RESERVOIR_PART.get(), BASE);
            helper.assertValueEqual(1, stack.getCount(), "the item is kept");
            helper.succeed();
        });

        r.add("reservoir/every_part_reaches_the_tank", 20, helper -> {
            DeepSeaReservoirBlockEntity reservoir = placed(helper, new ItemStack(NTBlocks.DEEP_SEA_RESERVOIR.get()));
            ServerLevel level = helper.getLevel();
            BlockPos corner = helper.absolutePos(BASE.offset(-1, 0, -1));
            BlockPos top = helper.absolutePos(BASE.offset(1, 2, 1));
            ResourceHandler<FluidResource> low = level.getCapability(TransferCapabilities.Fluid.BLOCK, corner, Direction.DOWN);
            ResourceHandler<FluidResource> high = level.getCapability(TransferCapabilities.Fluid.BLOCK, top, Direction.UP);
            helper.assertTrue(low != null && high != null, "parts expose the fluid tank");
            helper.assertValueEqual(8_000, insert(low, 8_000), "water poured into a bottom corner");
            helper.assertValueEqual(3_000, extract(high, 3_000), "water drawn from the top corner");
            helper.assertValueEqual(5_000L, reservoir.amount(), "the core holds what is left");
            helper.succeed();
        });

        r.add("reservoir/holds_past_the_int_limit", 20, helper -> {
            DeepSeaReservoirBlockEntity reservoir = placed(helper, new ItemStack(NTBlocks.DEEP_SEA_RESERVOIR.get()));
            ResourceHandler<FluidResource> tank = reservoir.tank();
            helper.assertValueEqual(500_000_000_000L, DeepSeaReservoirBlockEntity.capacityFor(1), "tier 1 holds 500M buckets");
            helper.assertValueEqual(2_000_000_000_000L, DeepSeaReservoirBlockEntity.capacityFor(2), "tier 2 holds 2000M buckets");
            helper.assertValueEqual(10_000_000_000_000L, DeepSeaReservoirBlockEntity.capacityFor(3), "tier 3 holds 10000M buckets");
            helper.assertValueEqual(50_000_000_000_000L, DeepSeaReservoirBlockEntity.capacityFor(4), "tier 4 holds 50000M buckets");
            for (int i = 0; i < 3; i++) {
                helper.assertValueEqual(Integer.MAX_VALUE, insert(tank, Integer.MAX_VALUE), "a full int of water fits");
            }
            long expected = 3L * Integer.MAX_VALUE;
            helper.assertValueEqual(expected, reservoir.amount(), "the amount passes the int limit");
            helper.assertValueEqual(expected, tank.getAmountAsLong(0), "the handler reports the long amount");
            helper.assertValueEqual(Integer.MAX_VALUE, tank.getAmountAsInt(0), "the int view clamps");
            helper.assertValueEqual(Integer.MAX_VALUE, extract(tank, Integer.MAX_VALUE), "a full int comes back out");
            helper.assertValueEqual(expected - Integer.MAX_VALUE, reservoir.amount(), "the rest stays");
            helper.succeed();
        });

        r.add("reservoir/upgrades_in_order_and_downgrade_voids_excess", 20, helper -> {
            DeepSeaReservoirBlockEntity reservoir = placed(helper, new ItemStack(NTBlocks.DEEP_SEA_RESERVOIR.get()));
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            BlockPos core = helper.absolutePos(CORE);
            ItemStack wrong = new ItemStack(NTItems.RESERVOIR_UPGRADE_3.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, wrong);
            DeepSeaReservoirBlock.useOn(wrong, helper.getLevel(), core, player, InteractionHand.MAIN_HAND);
            helper.assertValueEqual(1, reservoir.tier(), "a tier III upgrade does not fit tier I");
            helper.assertValueEqual(1, wrong.getCount(), "the wrong upgrade is kept");
            ItemStack right = new ItemStack(NTItems.RESERVOIR_UPGRADE_2.get(), 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, right);
            ItemInteractionResult result = DeepSeaReservoirBlock.useOn(right, helper.getLevel(), core, player, InteractionHand.MAIN_HAND);
            helper.assertTrue(result.consumesAction(), "the tier II upgrade applies");
            helper.assertValueEqual(2, reservoir.tier(), "the reservoir is tier II");
            helper.assertValueEqual(1, right.getCount(), "one upgrade is used");
            helper.assertValueEqual(2, helper.getBlockState(CORE).getValue(DeepSeaReservoirBlock.TIER), "the block state shows tier II");

            long overflow = 12_345L;
            reservoir.tank().load(new ReservoirFluid(water(), DeepSeaReservoirBlockEntity.capacityFor(1) + overflow));
            helper.assertValueEqual(overflow, reservoir.excessIfRemoved(), "the screen can warn about the excess");
            long voided = reservoir.downgrade();
            helper.assertValueEqual(overflow, voided, "only the excess is voided");
            helper.assertValueEqual(1, reservoir.tier(), "back to tier I");
            helper.assertValueEqual(DeepSeaReservoirBlockEntity.capacityFor(1), reservoir.amount(), "the reservoir stays full");
            helper.assertValueEqual(0L, reservoir.downgrade(), "tier I cannot drop further");
            helper.succeed();
        });

        r.add("reservoir/upgrade_slot_applies_the_next_tier", 20, helper -> {
            DeepSeaReservoirBlockEntity reservoir = placed(helper, new ItemStack(NTBlocks.DEEP_SEA_RESERVOIR.get()));
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            DeepSeaReservoirMenu menu = new DeepSeaReservoirMenu(0, player.getInventory(), reservoir);
            helper.assertFalse(menu.getSlot(0).mayPlace(new ItemStack(NTItems.RESERVOIR_UPGRADE_3.get())), "the slot refuses a skipped tier");
            menu.getSlot(0).set(new ItemStack(NTItems.RESERVOIR_UPGRADE_2.get()));
            helper.assertValueEqual(2, reservoir.tier(), "the slot applies the upgrade");
            helper.assertTrue(menu.getSlot(0).getItem().isEmpty(), "the upgrade is used up");
            helper.assertTrue(menu.getSlot(0).mayPlace(new ItemStack(NTItems.RESERVOIR_UPGRADE_3.get())), "tier III fits next");
            helper.succeed();
        });

        r.add("reservoir/waterlogs_underwater_and_leaves_water_behind", 40, helper -> {
            ServerLevel level = helper.getLevel();
            for (int x = 1; x <= 7; x++) {
                for (int y = 1; y <= 6; y++) {
                    for (int z = 1; z <= 7; z++) {
                        helper.setBlock(new BlockPos(x, y, z), Blocks.WATER);
                    }
                }
            }
            DeepSeaReservoirBlockEntity reservoir = placed(helper, new ItemStack(NTBlocks.DEEP_SEA_RESERVOIR.get()));
            BlockPos core = helper.absolutePos(CORE);
            for (int cell = 0; cell < ReservoirStructure.CELLS; cell++) {
                BlockPos pos = ReservoirStructure.cellPos(core, cell);
                var state = level.getBlockState(pos);
                helper.assertTrue(state.getValue(BlockStateProperties.WATERLOGGED), "cell " + cell + " is waterlogged underwater");
                helper.assertTrue(state.getFluidState().isSource() && state.getFluidState().is(Fluids.WATER), "cell " + cell + " holds a water source");
            }
            insert(reservoir.tank(), 4_000);

            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            BlockPos part = helper.absolutePos(BASE.offset(1, 1, -1));
            var partState = level.getBlockState(part);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.NETHERITE_PICKAXE));
            partState.onDestroyedByPlayer(level, part, player, true, level.getFluidState(part));
            for (int cell = 0; cell < ReservoirStructure.CELLS; cell++) {
                BlockPos pos = ReservoirStructure.cellPos(core, cell);
                var state = level.getBlockState(pos);
                helper.assertTrue(state.is(Blocks.WATER) && state.getFluidState().isSource(), "cell " + cell + " turns back into a water source, got " + state);
            }
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(core).inflate(4));
            helper.assertValueEqual(1, drops.size(), "breaking it underwater drops one item");
            ReservoirFluid fluid = drops.getFirst().getItem().get(NTDataComponents.RESERVOIR_FLUID.get());
            helper.assertTrue(fluid != null && fluid.amount() == 4_000L, "the drop keeps its water, got " + fluid);
            drops.getFirst().discard();
            helper.succeed();
        });

        r.add("reservoir/floods_when_water_reaches_its_side", 40, helper -> {
            DeepSeaReservoirBlockEntity reservoir = placed(helper, new ItemStack(NTBlocks.DEEP_SEA_RESERVOIR.get()));
            ServerLevel level = helper.getLevel();
            BlockPos core = helper.absolutePos(CORE);
            for (int cell = 0; cell < ReservoirStructure.CELLS; cell++) {
                helper.assertFalse(level.getBlockState(ReservoirStructure.cellPos(core, cell)).getValue(BlockStateProperties.WATERLOGGED),
                        "cell " + cell + " starts dry");
            }
            helper.setBlock(BASE.offset(-2, 2, 0), Blocks.WATER);
            helper.succeedWhen(() -> {
                for (int cell = 0; cell < ReservoirStructure.CELLS; cell++) {
                    var state = level.getBlockState(ReservoirStructure.cellPos(core, cell));
                    helper.assertTrue(state.getValue(BlockStateProperties.WATERLOGGED), "cell " + cell + " floods from the top layer down");
                }
                helper.assertValueEqual(1, reservoir.tier(), "flooding keeps the reservoir");
            });
        });

        r.add("reservoir/water_below_does_not_flood_it", 40, helper -> {
            placed(helper, new ItemStack(NTBlocks.DEEP_SEA_RESERVOIR.get()));
            ServerLevel level = helper.getLevel();
            BlockPos core = helper.absolutePos(CORE);
            helper.setBlock(BASE.below(), Blocks.WATER);
            helper.runAfterDelay(10, () -> {
                for (int cell = 0; cell < ReservoirStructure.CELLS; cell++) {
                    helper.assertFalse(level.getBlockState(ReservoirStructure.cellPos(core, cell)).getValue(BlockStateProperties.WATERLOGGED),
                            "cell " + cell + " stays dry, water does not rise into it");
                }
                helper.succeed();
            });
        });

        r.add("reservoir/every_block_outlines_the_whole_structure", 20, helper -> {
            placed(helper, new ItemStack(NTBlocks.DEEP_SEA_RESERVOIR.get()));
            ServerLevel level = helper.getLevel();
            BlockPos core = helper.absolutePos(CORE);
            AABB whole = new AABB(core.offset(-1, -1, -1).getCenter(), core.offset(1, 1, 1).getCenter()).inflate(0.5);
            for (int cell = 0; cell < ReservoirStructure.CELLS; cell++) {
                BlockPos pos = ReservoirStructure.cellPos(core, cell);
                var state = level.getBlockState(pos);
                AABB outline = state.getShape(level, pos).bounds().move(pos);
                helper.assertTrue(outline.equals(whole), "cell " + cell + " outlines " + outline + ", want " + whole);
                AABB collision = state.getCollisionShape(level, pos).bounds();
                helper.assertTrue(collision.equals(new AABB(0, 0, 0, 1, 1, 1)), "cell " + cell + " collides as one block, got " + collision);
            }
            helper.succeed();
        });

        r.add("reservoir/breaks_into_one_item_that_keeps_everything", 40, helper -> {
            DeepSeaReservoirBlockEntity reservoir = placed(helper, new ItemStack(NTBlocks.DEEP_SEA_RESERVOIR.get()));
            reservoir.upgradeTo(3);
            insert(reservoir.tank(), 64_000);
            ServerLevel level = helper.getLevel();
            BlockPos core = helper.absolutePos(CORE);
            level.removeBlock(helper.absolutePos(BASE.offset(1, 0, 1)), false);
            for (int cell = 0; cell < ReservoirStructure.CELLS; cell++) {
                BlockPos pos = ReservoirStructure.cellPos(core, cell);
                helper.assertTrue(level.getBlockState(pos).isAir(), "cell " + cell + " is cleared");
            }
            AABB area = new AABB(core).inflate(3);
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, area);
            helper.assertValueEqual(1, drops.size(), "exactly one item drops");
            ItemStack dropped = drops.getFirst().getItem();
            helper.assertTrue(dropped.is(NTBlocks.DEEP_SEA_RESERVOIR.asItem()), "the drop is the reservoir");
            helper.assertValueEqual(3, dropped.get(NTDataComponents.RESERVOIR_TIER.get()), "the drop keeps its tier");
            ReservoirFluid fluid = dropped.get(NTDataComponents.RESERVOIR_FLUID.get());
            helper.assertTrue(fluid != null && fluid.amount() == 64_000L && fluid.resource().equals(water()), "the drop keeps its water, got " + fluid);
            drops.getFirst().discard();

            DeepSeaReservoirBlockEntity again = placed(helper, dropped.copy());
            helper.assertValueEqual(3, again.tier(), "placing it again restores the tier");
            helper.assertValueEqual(64_000L, again.amount(), "placing it again restores the water");
            helper.succeed();
        });
    }
}
