package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.transfer.TransferCapabilities;
import com.breakinblocks.nautec.api.sides.SideMode;
import com.breakinblocks.nautec.api.sides.SideKind;
import com.breakinblocks.nautec.api.sides.RelativeFace;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.content.bacteria.SimpleBacteriaStats;
import com.breakinblocks.nautec.content.bacteria.SimpleCollapsedStats;
import com.breakinblocks.nautec.content.blockentities.BacterialAnalyzerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.IncubatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MutatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.BioReactorBlockEntity;
import com.breakinblocks.nautec.content.items.GraftingToolItem;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ComponentBacteriaStorage;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import com.breakinblocks.nautec.utils.RNGUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import com.breakinblocks.nautec.transfer.transaction.Transaction;

import java.util.Set;

public final class BiologyFixTests {
    private static final float EPSILON = 1.0e-4f;
    private static final int SAMPLES = 400;
    private static final BlockPos REACTOR_C = new BlockPos(4, 2, 4);

    private BiologyFixTests() {
    }

    private static void placeShieldedSource(NTGameTestHelper helper, BlockPos pos, Direction... openDirections) {
        helper.setBlock(pos, NTBlocks.CREATIVE_POWER_SOURCE.get().defaultBlockState());
        Set<Direction> open = Set.of(openDirections);
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN || open.contains(direction)) {
                continue;
            }
            helper.setBlock(pos.relative(direction, 2), Blocks.STONE.defaultBlockState());
        }
    }

    private static SimpleCollapsedStats stats(float growthRate, float mutationResistance, float productionRate, int lifespan) {
        return new SimpleCollapsedStats(SimpleBacteriaStats.EMPTY, growthRate, mutationResistance, productionRate, lifespan, -1);
    }

    private static BacteriaInstance colony(ResourceKey<Bacteria> bacteria, long size, SimpleCollapsedStats stats, long age) {
        return new BacteriaInstance(bacteria, size, stats, true, age);
    }

    private static Item productOf(NTGameTestHelper helper, ResourceKey<Bacteria> bacteria) {
        Bacteria definition = BacteriaHelper.getBacteria(helper.getLevel().registryAccess(), bacteria);
        if (definition == null) {
            throw helper.assertionException("No bacteria definition for " + bacteria.location());
        }
        Item item = definition.resource().resolve();
        if (item == null || item == Items.AIR) {
            throw helper.assertionException("Bacteria " + bacteria.location() + " produces nothing");
        }
        return item;
    }

    private static ItemStack fullStack(Item item) {
        ItemStack stack = item.getDefaultInstance();
        stack.setCount(stack.getMaxStackSize());
        return stack;
    }

    private static ResourceHandler<ItemResource> itemsOn(NTGameTestHelper helper, BlockPos pos, Direction side) {
        return helper.getLevel().getCapability(TransferCapabilities.Item.BLOCK, helper.absolutePos(pos), side);
    }

    private static int insert(ResourceHandler<ItemResource> handler, int slot, ItemStack stack) {
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = handler.insert(slot, ItemResource.of(stack), stack.getCount(), tx);
            tx.commit();
            return inserted;
        }
    }

    private static int extract(ResourceHandler<ItemResource> handler, int slot, ItemStack stack) {
        try (Transaction tx = Transaction.openRoot()) {
            int extracted = handler.extract(slot, ItemResource.of(stack), stack.getCount(), tx);
            tx.commit();
            return extracted;
        }
    }

    private static void placeBioReactor(NTGameTestHelper helper) {
        for (int y = 0; y < 2; y++) {
            for (int z = 0; z < 3; z++) {
                for (int x = 0; x < 3; x++) {
                    Block block;
                    if (x == 1 && z == 1) {
                        block = y == 0 ? NTBlocks.POLISHED_PRISMARINE.get() : NTBlocks.BIO_REACTOR.get();
                    } else if (x != 1 && z != 1) {
                        block = NTBlocks.DARK_PRISMARINE_PILLAR.get();
                    } else {
                        block = NTBlocks.BACTERIAL_CONTAINMENT_SHIELD.get();
                    }
                    helper.setBlock(REACTOR_C.offset(x - 1, y - 1, z - 1), block);
                }
            }
        }
    }

    public static void register(NTTestRegistrar r) {
        r.add("biology_fix/bio_reactor_pauses_when_output_full", 260, helper -> {
            BlockPos reactorPos = new BlockPos(4, 1, 4);
            BlockPos sourcePos = new BlockPos(4, 1, 6);
            helper.setBlock(reactorPos, NTBlocks.BIO_REACTOR.get().defaultBlockState());
            placeShieldedSource(helper, sourcePos, Direction.NORTH);

            BioReactorBlockEntity reactor = helper.getBlockEntity(reactorPos, BioReactorBlockEntity.class);
            Item product = productOf(helper, NTBacterias.FERROPHILES);
            ItemStack full = fullStack(product);
            int fullCount = full.getCount();

            reactor.getItemStackHandler().setStackInSlot(0, full.copy());
            reactor.getItemStackHandler().setStackInSlot(1, full.copy());
            reactor.getBacteriaStorage().setBacteria(0, colony(NTBacterias.FERROPHILES, NTConfig.bacteriaColonySizeCap,
                    stats(1.0f, 0f, 2.0f, NTConfig.bacteriaLifespanCap), 0));
            reactor.getBacteriaStorage().setBacteria(1, colony(NTBacterias.FERROPHILES, 20,
                    stats(1.0f, 0f, 2.0f, 10), 500));

            long[] pausedAge = new long[1];
            long[] pausedSize = new long[1];
            helper.runAfterDelay(60, () -> {
                helper.assertTrue(reactor.getPower() >= reactor.getRequiredPower(),
                        "Reactor should be powered, had " + reactor.getPower() + " of " + reactor.getRequiredPower());
                pausedAge[0] = reactor.getBacteriaStorage().getBacteria(0).getAge();
                pausedSize[0] = reactor.getBacteriaStorage().getBacteria(1).getSize();
            });

            helper.runAfterDelay(120, () -> {
                helper.assertValueEqual(fullCount, reactor.getItemStackHandler().getStackInSlot(0).getCount(), "full output slot 0 count");
                helper.assertValueEqual(pausedAge[0], reactor.getBacteriaStorage().getBacteria(0).getAge(),
                        "colony age while its output slot is full");
                helper.assertValueEqual(pausedSize[0], reactor.getBacteriaStorage().getBacteria(1).getSize(),
                        "senescent colony size while its output slot is full");
                helper.assertTrue(reactor.getProgress(0) < 100f, "Paused progress should never pass 100, was " + reactor.getProgress(0));
                reactor.getItemStackHandler().setStackInSlot(0, full.copyWithCount(fullCount - 8));
            });

            helper.runAfterDelay(200, () -> {
                helper.assertTrue(reactor.getItemStackHandler().getStackInSlot(0).getCount() > fullCount - 8,
                        "Reactor should resume producing once the output has room");
                helper.assertTrue(reactor.getBacteriaStorage().getBacteria(0).getAge() > pausedAge[0],
                        "Colony should resume aging once the output has room");
                helper.succeed();
            });
        });

        r.add("biology_fix/bio_reactor_controller_output_extractable", 60, helper -> {
            BlockPos reactorPos = new BlockPos(4, 1, 4);
            helper.setBlock(reactorPos, NTBlocks.BIO_REACTOR.get().defaultBlockState());
            helper.runAfterDelay(2, () -> {
                BioReactorBlockEntity reactor = helper.getBlockEntity(reactorPos, BioReactorBlockEntity.class);
                reactor.getItemStackHandler().setStackInSlot(2, new ItemStack(Items.IRON_NUGGET, 5));

                ResourceHandler<ItemResource> up = itemsOn(helper, reactorPos, Direction.UP);
                helper.assertTrue(up != null, "Bio reactor should expose its outputs on top");
                helper.assertValueEqual(0, insert(up, 0, new ItemStack(Items.IRON_NUGGET, 1)), "inserted into an output-only side");
                helper.assertValueEqual(5, extract(up, 2, new ItemStack(Items.IRON_NUGGET, 5)), "extracted from output slot 2");
                helper.assertTrue(reactor.getItemStackHandler().getStackInSlot(2).isEmpty(), "Extraction should empty slot 2");
                helper.succeed();
            });
        });

        r.add("biology_fix/bio_reactor_base_forwards_output", 80, helper -> {
            placeBioReactor(helper);
            helper.runAfterDelay(1, () -> helper.assertTrue(
                    MultiblockHelper.form(NTMultiblocks.BIO_REACTOR.get(), helper.absolutePos(REACTOR_C), helper.getLevel()),
                    "Bio reactor should form"));
            helper.runAfterDelay(4, () -> {
                BioReactorBlockEntity reactor = helper.getBlockEntity(REACTOR_C, BioReactorBlockEntity.class);
                reactor.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 3));

                BlockPos base = REACTOR_C.below();
                ResourceHandler<ItemResource> down = itemsOn(helper, base, Direction.DOWN);
                helper.assertTrue(down != null, "Base part should expose the reactor outputs to a hopper below");
                helper.assertValueEqual(0, insert(down, 0, new ItemStack(Items.COBBLESTONE, 1)), "inserted through the base part");
                helper.assertValueEqual(3, extract(down, 0, new ItemStack(Items.COBBLESTONE, 3)), "extracted through the base part");
                helper.assertTrue(reactor.getItemStackHandler().getStackInSlot(0).isEmpty(), "Extraction should empty the controller slot");

                helper.assertTrue(itemsOn(helper, base, Direction.NORTH) == null, "Base part should expose nothing on its side");
                helper.succeed();
            });
        });

        r.add("biology_fix/incubator_and_mutator_accept_inputs", 60, helper -> {
            BlockPos incubatorPos = new BlockPos(2, 1, 4);
            BlockPos mutatorPos = new BlockPos(6, 1, 4);
            helper.setBlock(incubatorPos, NTBlocks.INCUBATOR.get().defaultBlockState());
            helper.setBlock(mutatorPos, NTBlocks.MUTATOR.get().defaultBlockState());
            helper.runAfterDelay(2, () -> {
                IncubatorBlockEntity incubator = helper.getBlockEntity(incubatorPos, IncubatorBlockEntity.class);
                MutatorBlockEntity mutator = helper.getBlockEntity(mutatorPos, MutatorBlockEntity.class);

                for (Direction side : Direction.values()) {
                    helper.assertTrue(itemsOn(helper, incubatorPos, side) != null, "Incubator should accept items on " + side);
                    helper.assertTrue(itemsOn(helper, mutatorPos, side) != null, "Mutator should accept items on " + side);
                }

                ResourceHandler<ItemResource> incubatorSide = itemsOn(helper, incubatorPos, Direction.NORTH);
                helper.assertValueEqual(64, insert(incubatorSide, 0, new ItemStack(Items.STONE, 64)), "nutrient inserted into the incubator");
                helper.assertTrue(incubator.getItemStackHandler().getStackInSlot(0).is(Items.STONE), "Incubator should hold the nutrient");
                helper.assertValueEqual(0, extract(incubatorSide, 0, new ItemStack(Items.STONE, 1)), "extracted from an input-only side");

                ResourceHandler<ItemResource> mutatorSide = itemsOn(helper, mutatorPos, Direction.EAST);
                helper.assertValueEqual(4, insert(mutatorSide, 0, new ItemStack(Items.BONE_MEAL, 4)), "catalyst inserted into the mutator");
                helper.assertValueEqual(4, mutator.getItemStackHandler().getStackInSlot(0).getCount(), "mutator catalyst count");
                helper.succeed();
            });
        });

        r.add("biology_fix/analyzer_sided_io", 60, helper -> {
            BlockPos analyzerPos = new BlockPos(4, 1, 4);
            helper.setBlock(analyzerPos, NTBlocks.BACTERIAL_ANALYZER.get().defaultBlockState());
            helper.runAfterDelay(2, () -> {
                BacterialAnalyzerBlockEntity analyzer = helper.getBlockEntity(analyzerPos, BacterialAnalyzerBlockEntity.class);
                Direction front = helper.getBlockState(analyzerPos).getValue(BlockStateProperties.HORIZONTAL_FACING);
                Direction back = front.getOpposite();
                ItemStack dish = DishPortTests.dish(DishPortTests.colony(helper, NTBacterias.LITHOPHILES, 100));

                ResourceHandler<ItemResource> frontSide = itemsOn(helper, analyzerPos, front);
                ResourceHandler<ItemResource> leftSide = itemsOn(helper, analyzerPos, front.getClockWise());
                ResourceHandler<ItemResource> backSide = itemsOn(helper, analyzerPos, back);
                helper.assertTrue(frontSide != null && leftSide != null && backSide != null, "Analyzer should expose its front, sides and back");
                analyzer.setSideMode(SideKind.ITEMS, RelativeFace.BACK, SideMode.OUTPUT);
                analyzer.setSideMode(SideKind.ITEMS, RelativeFace.BOTTOM, SideMode.NONE);
                backSide = itemsOn(helper, analyzerPos, back);
                helper.assertTrue(itemsOn(helper, analyzerPos, Direction.DOWN) == null, "An Off bottom exposes nothing");

                helper.assertValueEqual(0, insert(backSide, 0, dish.copy()), "inserted through an output face");
                helper.assertValueEqual(1, insert(leftSide, 0, dish.copy()), "dish inserted through a side");
                helper.assertTrue(analyzer.getItemStackHandler().getStackInSlot(0).is(NTItems.PETRI_DISH.get()), "Input slot should hold the dish");
                helper.assertValueEqual(0, extract(frontSide, 0, dish.copy()), "extracted the input through an input side");
                helper.assertValueEqual(0, extract(backSide, 0, dish.copy()), "extracted the input through the output side");

                analyzer.getItemStackHandler().setStackInSlot(1, dish.copy());
                helper.assertValueEqual(1, extract(frontSide, 1, dish.copy()), "extracted the result through an input side");
                helper.assertTrue(analyzer.getItemStackHandler().getStackInSlot(1).isEmpty(), "Result slot should be empty after a side extraction");
                analyzer.getItemStackHandler().setStackInSlot(1, dish.copy());
                helper.assertValueEqual(1, extract(backSide, 1, dish.copy()), "extracted the result through the back");
                helper.assertTrue(analyzer.getItemStackHandler().getStackInSlot(1).isEmpty(), "Result slot should be empty after extraction");
                helper.succeed();
            });
        });

        r.add("biology_fix/incubator_feeds_colony_at_cap", 260, helper -> {
            BlockPos incubatorPos = new BlockPos(4, 1, 4);
            BlockPos sourcePos = new BlockPos(4, 4, 4);
            helper.setBlock(incubatorPos, NTBlocks.INCUBATOR.get().defaultBlockState());
            placeShieldedSource(helper, sourcePos, Direction.DOWN);

            IncubatorBlockEntity incubator = helper.getBlockEntity(incubatorPos, IncubatorBlockEntity.class);
            incubator.getBacteriaStorage().setBacteria(0, colony(NTBacterias.LITHOPHILES, NTConfig.bacteriaColonySizeCap,
                    stats(2.0f, 0f, 1.0f, 2000), 700));
            incubator.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.STONE));

            helper.runAfterDelay(2, () -> helper.assertTrue(incubator.isActive(), "A colony at the size cap should still be fed"));

            helper.runAfterDelay(200, () -> {
                BacteriaInstance fed = incubator.getBacteriaStorage().getBacteria(0);
                helper.assertValueEqual(NTConfig.bacteriaColonySizeCap, fed.getSize(), "colony size after feeding at the cap");
                helper.assertValueEqual(0L, fed.getAge(), "colony age after feeding at the cap");
                helper.succeed();
            });
        });

        r.add("biology_fix/stat_rolls_go_both_ways", 40, helper -> helper.runAfterDelay(1, () -> {
            float rngMin = Float.MAX_VALUE;
            float rngMax = -Float.MAX_VALUE;
            for (int i = 0; i < SAMPLES; i++) {
                float value = RNGUtils.floatInRangeOf(1f);
                rngMin = Math.min(rngMin, value);
                rngMax = Math.max(rngMax, value);
            }
            helper.assertTrue(rngMin >= -1f - EPSILON && rngMax <= 1f + EPSILON,
                    "floatInRangeOf(1) should stay in [-1, 1], saw " + rngMin + " to " + rngMax);
            helper.assertTrue(rngMin < -0.5f && rngMax > 0.5f,
                    "floatInRangeOf(1) should spread both ways, saw " + rngMin + " to " + rngMax);

            SimpleCollapsedStats base = stats(2.0f, 0f, 1.0f, 2000);
            float prMin = Float.MAX_VALUE;
            float prMax = -Float.MAX_VALUE;
            float grMin = Float.MAX_VALUE;
            float grMax = -Float.MAX_VALUE;
            for (int i = 0; i < SAMPLES; i++) {
                float pr = base.rollProductionRate().productionRate();
                float gr = base.rollGrowthRate().growthRate();
                prMin = Math.min(prMin, pr);
                prMax = Math.max(prMax, pr);
                grMin = Math.min(grMin, gr);
                grMax = Math.max(grMax, gr);
            }
            helper.assertTrue(prMin >= 0.9f - EPSILON && prMax <= 1.1f + EPSILON,
                    "Production rate roll should stay in [0.9, 1.1], saw " + prMin + " to " + prMax);
            helper.assertTrue(prMin < 0.99f && prMax > 1.01f,
                    "Production rate roll should go both ways, saw " + prMin + " to " + prMax);
            helper.assertTrue(grMin >= 1.8f - EPSILON && grMax <= 2.2f + EPSILON,
                    "Growth rate roll should stay in [1.8, 2.2], saw " + grMin + " to " + grMax);
            helper.assertTrue(grMin < 1.98f && grMax > 2.02f,
                    "Growth rate roll should go both ways, saw " + grMin + " to " + grMax);
            helper.succeed();
        }));

        r.add("biology_fix/production_rate_roll_divides_by_resistance", 40, helper -> helper.runAfterDelay(1, () -> {
            SimpleCollapsedStats resistant = stats(2.0f, 1.0f, 1.0f, 2000);
            float maxDelta = 0f;
            for (int i = 0; i < SAMPLES; i++) {
                maxDelta = Math.max(maxDelta, Math.abs(resistant.rollProductionRate().productionRate() - 1.0f));
            }
            helper.assertTrue(maxDelta <= 0.05f + EPSILON,
                    "With resistance 1 a production rate of 1 should move at most 0.05, moved " + maxDelta);
            helper.assertTrue(maxDelta > 0.025f,
                    "Production rate roll should still vary with resistance 1, max move " + maxDelta);
            helper.succeed();
        }));

        r.add("biology_fix/grafting_keeps_occupied_dish", 40, helper -> helper.runAfterDelay(1, () -> {
            BacteriaInstance existing = colony(NTBacterias.LITHOPHILES, 123, stats(1f, 0f, 1f, 2000), 0);
            BacteriaInstance graft = colony(NTBacterias.CALCIOPHILES, 50, stats(1f, 0f, 1f, 2000), 0);

            ItemStack occupied = new ItemStack(NTItems.PETRI_DISH.get());
            occupied.set(NTDataComponents.BACTERIA, new ComponentBacteriaStorage(existing));
            IBacteriaStorage occupiedStorage = occupied.getCapability(NTCapabilities.BacteriaStorage.ITEM);
            helper.assertTrue(occupiedStorage != null, "Petri dish should expose a bacteria storage");
            helper.assertFalse(GraftingToolItem.canGraftInto(occupiedStorage), "An occupied dish should refuse a graft");
            helper.assertFalse(GraftingToolItem.graftInto(occupiedStorage, graft), "Grafting into an occupied dish should fail");
            BacteriaInstance kept = occupied.get(NTDataComponents.BACTERIA).bacteriaInstance();
            helper.assertValueEqual(NTBacterias.LITHOPHILES, kept.getBacteria(), "colony kept in the occupied dish");
            helper.assertValueEqual(123L, kept.getSize(), "colony size kept in the occupied dish");

            ItemStack empty = new ItemStack(NTItems.PETRI_DISH.get());
            IBacteriaStorage emptyStorage = empty.getCapability(NTCapabilities.BacteriaStorage.ITEM);
            helper.assertTrue(emptyStorage != null, "Empty petri dish should expose a bacteria storage");
            helper.assertTrue(GraftingToolItem.graftInto(emptyStorage, graft), "Grafting into an empty dish should succeed");
            BacteriaInstance grafted = empty.get(NTDataComponents.BACTERIA).bacteriaInstance();
            helper.assertValueEqual(NTBacterias.CALCIOPHILES, grafted.getBacteria(), "colony grafted into the empty dish");
            helper.succeed();
        }));
    }
}
