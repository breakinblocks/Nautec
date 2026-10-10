package com.breakinblocks.nautec.gametest.suite;


import net.minecraft.nbt.Tag;
import com.breakinblocks.nautec.transfer.TransferCapabilities;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.api.sides.RelativeFace;
import com.breakinblocks.nautec.api.sides.SideKind;
import com.breakinblocks.nautec.api.sides.SideMode;
import com.breakinblocks.nautec.api.utils.HorizontalDirection;
import com.breakinblocks.nautec.content.bacteria.SimpleCollapsedStats;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.AbstractBioReactorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.BioReactorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.IndustrialBioReactorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.BioReactorPartBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.IndustrialBioReactorPartBlockEntity;
import com.breakinblocks.nautec.content.items.ReactorUpgradeItem;
import com.breakinblocks.nautec.content.multiblocks.BioReactorMultiblock;
import com.breakinblocks.nautec.content.multiblocks.IndustrialBioReactorMultiblock;
import com.breakinblocks.nautec.content.recipes.BacteriaIncubationRecipe;
import com.breakinblocks.nautec.content.recipes.ColonyFeedingRecipe;
import com.breakinblocks.nautec.content.recipes.inputs.BacteriaRecipeInput;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import com.breakinblocks.nautec.utils.valueio.ProblemReporter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.TagValueInput;

import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import com.breakinblocks.nautec.transfer.transaction.Transaction;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BioReactorOverhaulTests {
    private static final float EPSILON = 1.0e-4f;
    private static final BlockPos REACTOR = new BlockPos(4, 1, 4);
    private static final BlockPos SOURCE = new BlockPos(4, 1, 6);
    private static final BlockPos FORMED_BIO_CONTROLLER = new BlockPos(4, 2, 4);
    private static final int[] HATCH_CELLS = {6, 7, 8, 11, 12, 13};

    private BioReactorOverhaulTests() {
    }

    private static SimpleCollapsedStats stats(float productionRate, int lifespan) {
        return BacteriaMachineTests.stats(1.0f, 0f, productionRate, lifespan);
    }

    private static BioReactorBlockEntity loneReactor(NTGameTestHelper helper, BlockPos reactorPos, BlockPos sourcePos) {
        helper.setBlock(reactorPos, NTBlocks.BIO_REACTOR.get().defaultBlockState());
        BacteriaMachineTests.feedExact(helper, reactorPos);
        return helper.getBlockEntity(reactorPos, BioReactorBlockEntity.class);
    }

    private static BlockPos cellPos(BlockPos origin, int layer, int cell) {
        return origin.offset(cell % IndustrialBioReactorMultiblock.SIZE, layer, cell / IndustrialBioReactorMultiblock.SIZE);
    }

    private static BlockPos industrialController(BlockPos origin) {
        return cellPos(origin, IndustrialBioReactorMultiblock.CONTROLLER_LAYER, IndustrialBioReactorMultiblock.CONTROLLER_CELL);
    }

    private static void buildIndustrial(NTGameTestHelper helper, BlockPos origin) {
        Map<Integer, Block> definition = NTMultiblocks.INDUSTRIAL_BIO_REACTOR.get().getDefinition();
        for (int layer = 0; layer < IndustrialBioReactorMultiblock.HEIGHT; layer++) {
            for (int cell = 0; cell < IndustrialBioReactorMultiblock.SIZE * IndustrialBioReactorMultiblock.SIZE; cell++) {
                helper.setBlock(cellPos(origin, layer, cell), definition.get(IndustrialBioReactorMultiblock.keyAt(layer, cell)));
            }
        }
    }

    private static boolean formIndustrial(NTGameTestHelper helper, BlockPos origin) {
        return MultiblockHelper.form(NTMultiblocks.INDUSTRIAL_BIO_REACTOR.get(), helper.absolutePos(industrialController(origin)), helper.getLevel());
    }

    private static void powerIndustrialThroughHatches(NTGameTestHelper helper, BlockPos origin) {
        for (int cell : HATCH_CELLS) {
            BlockPos roof = cellPos(origin, IndustrialBioReactorMultiblock.HEIGHT - 1, cell);
            BlockState state = helper.getBlockState(roof);
            helper.getLevel().setBlock(helper.absolutePos(roof), state.setValue(BioReactorMultiblock.HATCH, true), 3);
            helper.setBlock(roof.above(), NTBlocks.CREATIVE_POWER_SOURCE.get().defaultBlockState());
        }
    }

    private static void placeBioReactor(NTGameTestHelper helper, BlockPos controller) {
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
                    helper.setBlock(controller.offset(x - 1, y - 1, z - 1), block);
                }
            }
        }
    }

    private static int count(AbstractBioReactorBlockEntity reactor, Item item) {
        int total = 0;
        for (int i = 0; i < reactor.getItemStackHandler().getSlots(); i++) {
            ItemStack stack = reactor.getItemStackHandler().getStackInSlot(i);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static int count(HopperBlockEntity hopper, Item item) {
        int total = 0;
        for (int i = 0; i < hopper.getContainerSize(); i++) {
            ItemStack stack = hopper.getItem(i);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static boolean active(BlockState state) {
        return state.hasProperty(BioReactorMultiblock.ACTIVE) && state.getValue(BioReactorMultiblock.ACTIVE);
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

    private static ResourceHandler<ItemResource> items(NTGameTestHelper helper, BlockPos pos, Direction side) {
        return helper.getLevel().getCapability(TransferCapabilities.Item.BLOCK, helper.absolutePos(pos), side);
    }

    private static int window() {
        return (int) Math.round(300 * 5.6 / NTConfig.bioReactorBaseSpeed);
    }

    public static void register(NTTestRegistrar r) {
        r.add("bio_overhaul/feeding_stops_aging_and_pulls_nutrients", 200, helper -> {
            BioReactorBlockEntity reactor = loneReactor(helper, REACTOR, SOURCE);
            reactor.getBacteriaStorage().setBacteria(0, BacteriaMachineTests.colony(NTBacterias.FERROPHILES, 400,
                    stats(2.0f, NTConfig.bacteriaLifespanCap), 0));
            reactor.getItemStackHandler().setStackInSlot(reactor.nutrientSlot(0), new ItemStack(Items.IRON_ORE, 3));

            helper.runAfterDelay(80, () -> {
                helper.assertTrue(reactor.getPower() >= reactor.getRequiredPower(),
                        "Reactor should be powered, had " + reactor.getPower() + " of " + reactor.getRequiredPower());
                helper.assertValueEqual(0L, reactor.getBacteriaStorage().getBacteria(0).getAge(), "age of a fed colony");
                helper.assertValueEqual(2, reactor.getItemStackHandler().getStackInSlot(reactor.nutrientSlot(0)).getCount(),
                        "iron ore left after one feeding");
                float vitality = reactor.getVitality(0);
                helper.assertTrue(vitality > 0 && vitality < 1200, "Vitality should be spending down from 1200, was " + vitality);
                helper.assertTrue(count(reactor, Items.IRON_INGOT) > 0, "A fed colony should still produce");
                helper.succeed();
            });
        });

        r.add("bio_overhaul/fed_senescent_colony_does_not_decay", 220, helper -> {
            BioReactorBlockEntity reactor = loneReactor(helper, REACTOR, SOURCE);
            reactor.getBacteriaStorage().setBacteria(0, BacteriaMachineTests.colony(NTBacterias.FERROPHILES, 20,
                    stats(2.0f, 10), 500));
            reactor.getItemStackHandler().setStackInSlot(reactor.nutrientSlot(0), new ItemStack(Items.IRON_ORE, 2));

            helper.runAfterDelay(140, () -> {
                BacteriaInstance colony = reactor.getBacteriaStorage().getBacteria(0);
                helper.assertTrue(colony.isSenescent(), "The colony should stay senescent in age");
                helper.assertValueEqual(20L, colony.getSize(), "size of a fed senescent colony");
                helper.assertValueEqual(500L, colony.getAge(), "age of a fed senescent colony");
                helper.assertTrue(count(reactor, Items.IRON_INGOT) > 0, "A fed senescent colony should still produce");
                helper.succeed();
            });
        });

        r.add("bio_overhaul/starving_colony_decays_per_second", 300, helper -> {
            BioReactorBlockEntity reactor = loneReactor(helper, REACTOR, SOURCE);
            reactor.getBacteriaStorage().setBacteria(0, BacteriaMachineTests.colony(NTBacterias.FERROPHILES, 1000,
                    stats(2.0f, 10), 500));

            long[] start = new long[1];
            helper.runAfterDelay(40, () -> {
                helper.assertTrue(reactor.getPower() >= reactor.getRequiredPower(), "Reactor should be powered");
                start[0] = reactor.getBacteriaStorage().getBacteria(0).getSize();
            });
            helper.runAfterDelay(140, () -> {
                long lost = start[0] - reactor.getBacteriaStorage().getBacteria(0).getSize();
                helper.assertTrue(lost >= 85 && lost <= 105,
                        "100 working ticks at 2% per second should cost five decay steps of about 20, lost " + lost);
                helper.succeed();
            });
        });

        r.add("bio_overhaul/multiplier_slows_gems", 420, helper -> {
            int window = window();
            BioReactorBlockEntity reactor = loneReactor(helper, REACTOR, SOURCE);
            reactor.getBacteriaStorage().setBacteria(0, BacteriaMachineTests.colony(NTBacterias.LITHOPHILES, NTConfig.bacteriaColonySizeCap,
                    stats(2.0f, NTConfig.bacteriaLifespanCap), 0));
            reactor.getBacteriaStorage().setBacteria(1, BacteriaMachineTests.colony(NTBacterias.ADAMANTOPHILES, NTConfig.bacteriaColonySizeCap,
                    stats(2.0f, NTConfig.bacteriaLifespanCap), 0));

            assertMultiplier(helper, reactor, NTBacterias.LITHOPHILES, 1.0f);
            assertMultiplier(helper, reactor, NTBacterias.FERROPHILES, 0.6f);
            assertMultiplier(helper, reactor, NTBacterias.ADAMANTOPHILES, 0.12f);
            assertMultiplier(helper, reactor, NTBacterias.SMARAGDOPHILES, 0.1f);
            assertMultiplier(helper, reactor, NTBacterias.CYANOBACTERIA, 1.0f);

            int[] baseline = new int[2];
            helper.runAfterDelay(60, () -> {
                baseline[0] = count(reactor, Items.STONE);
                baseline[1] = count(reactor, Items.DIAMOND);
            });
            helper.runAfterDelay(60 + window, () -> {
                int stone = count(reactor, Items.STONE) - baseline[0];
                int diamonds = count(reactor, Items.DIAMOND) - baseline[1];
                helper.assertTrue(stone >= 30 && stone <= 36, "Stone strain should make about 33 items, made " + stone);
                helper.assertTrue(diamonds >= 3 && diamonds <= 5, "Diamond strain at x0.12 should make about 4 items, made " + diamonds);
                helper.succeed();
            });
        });

        r.add("bio_overhaul/fast_colony_finishes_several_cycles_a_tick", 120, helper -> {
            BioReactorBlockEntity reactor = loneReactor(helper, REACTOR, SOURCE);
            reactor.getBacteriaStorage().setBacteria(0, BacteriaMachineTests.colony(NTBacterias.LITHOPHILES, NTConfig.bacteriaColonySizeCap,
                    stats(2.0f, NTConfig.bacteriaLifespanCap), 0));
            helper.runAfterDelay(60, () -> {
                double speed = NTConfig.bioReactorBaseSpeed;
                int before = count(reactor, Items.STONE);
                try {
                    NTConfig.bioReactorBaseSpeed = 250.0;
                    BacteriaMachineTests.feedNow(reactor);
                    reactor.commonTick();
                } finally {
                    NTConfig.bioReactorBaseSpeed = speed;
                }
                int made = count(reactor, Items.STONE) - before;
                helper.assertTrue(made >= 5 && made <= 6, "500 progress in one tick should finish five cycles, made " + made);
            });
            helper.runAfterDelay(70, () -> {
                double speed = NTConfig.bioReactorBaseSpeed;
                reactor.getItemStackHandler().setStackInSlot(reactor.outputSlot(0), new ItemStack(Items.STONE, 63));
                try {
                    NTConfig.bioReactorBaseSpeed = 250.0;
                    BacteriaMachineTests.feedNow(reactor);
                    reactor.commonTick();
                } finally {
                    NTConfig.bioReactorBaseSpeed = speed;
                }
                helper.assertValueEqual(64, reactor.getItemStackHandler().getStackInSlot(reactor.outputSlot(0)).getCount(),
                        "a full output stops the extra cycles");
                helper.assertTrue(reactor.getProgress(0) <= 100.0f, "progress does not pile up behind a full output");
                helper.succeed();
            });
        });

        r.add("bio_overhaul/speed_upgrade_power_and_rate", 420, helper -> {
            int window = window();
            BioReactorBlockEntity fast = loneReactor(helper, new BlockPos(2, 1, 4), new BlockPos(2, 1, 6));
            BioReactorBlockEntity slow = loneReactor(helper, new BlockPos(6, 1, 4), new BlockPos(6, 1, 6));
            for (BioReactorBlockEntity reactor : List.of(fast, slow)) {
                reactor.getBacteriaStorage().setBacteria(0, BacteriaMachineTests.colony(NTBacterias.LITHOPHILES, 400,
                        stats(1.0f, NTConfig.bacteriaLifespanCap), 0));
            }
            fast.getItemStackHandler().setStackInSlot(fast.upgradeSlot(0), new ItemStack(NTItems.REACTOR_SPEED_UPGRADE.get()));

            helper.assertValueEqual(80, fast.getRequiredPower(), "AP with one colony and one speed upgrade");
            helper.assertValueEqual(50, slow.getRequiredPower(), "AP with one colony and no upgrades");
            helper.assertTrue(Math.abs(fast.getSpeedMultiplier() - 1.5f) < EPSILON, "speed multiplier with one upgrade");

            int[] baseline = new int[2];
            helper.runAfterDelay(60, () -> {
                baseline[0] = count(fast, Items.STONE);
                baseline[1] = count(slow, Items.STONE);
            });
            helper.runAfterDelay(60 + window, () -> {
                int boosted = count(fast, Items.STONE) - baseline[0];
                int plain = count(slow, Items.STONE) - baseline[1];
                helper.assertTrue(boosted >= 11 && boosted <= 14, "Speed upgraded colony should make about 12.7 items, made " + boosted);
                helper.assertTrue(plain >= 7 && plain <= 10, "Plain colony should make about 8.5 items, made " + plain);
                helper.succeed();
            });
        });

        r.add("bio_overhaul/yield_upgrade_power_and_items", 300, helper -> {
            BioReactorBlockEntity reactor = loneReactor(helper, REACTOR, SOURCE);
            reactor.getBacteriaStorage().setBacteria(0, BacteriaMachineTests.colony(NTBacterias.LITHOPHILES, NTConfig.bacteriaColonySizeCap,
                    stats(2.0f, NTConfig.bacteriaLifespanCap), 0));
            reactor.getItemStackHandler().setStackInSlot(reactor.upgradeSlot(0), new ItemStack(NTItems.REACTOR_YIELD_UPGRADE.get()));

            helper.assertValueEqual(90, reactor.getRequiredPower(), "AP with one colony and one yield upgrade");
            helper.assertValueEqual(2, reactor.getItemsPerCycle(), "items per cycle with one yield upgrade");

            helper.runAfterDelay(200, () -> {
                int made = count(reactor, Items.STONE);
                helper.assertTrue(made >= 30, "Yield upgraded colony should have made at least 30 items, made " + made);
                helper.assertValueEqual(0, made % 2, "every cycle should add two items");
                helper.succeed();
            });
        });

        r.add("bio_overhaul/efficiency_upgrade_power_and_vitality", 200, helper -> {
            BioReactorBlockEntity reactor = loneReactor(helper, REACTOR, SOURCE);
            reactor.getBacteriaStorage().setBacteria(0, BacteriaMachineTests.colony(NTBacterias.LITHOPHILES, 400,
                    stats(1.0f, NTConfig.bacteriaLifespanCap), 0));
            reactor.getItemStackHandler().setStackInSlot(reactor.upgradeSlot(0), new ItemStack(NTItems.REACTOR_EFFICIENCY_UPGRADE.get()));
            reactor.setVitality(0, 500);

            helper.assertValueEqual(60, reactor.getRequiredPower(), "AP with one colony and one efficiency upgrade");
            helper.assertTrue(Math.abs(reactor.getVitalityCost() - 0.75f) < EPSILON, "vitality per tick with one efficiency upgrade");

            float[] start = new float[1];
            helper.runAfterDelay(40, () -> start[0] = reactor.getVitality(0));
            helper.runAfterDelay(80, () -> {
                float spent = start[0] - reactor.getVitality(0);
                helper.assertTrue(spent >= 29 && spent <= 31, "40 working ticks at 0.75 should spend 30 vitality, spent " + spent);
                helper.assertValueEqual(0L, reactor.getBacteriaStorage().getBacteria(0).getAge(), "age while the buffer lasts");

                reactor.getItemStackHandler().setStackInSlot(reactor.upgradeSlot(1), new ItemStack(NTItems.REACTOR_EFFICIENCY_UPGRADE.get()));
                helper.assertTrue(Math.abs(reactor.getVitalityCost() - 0.5625f) < EPSILON, "vitality per tick with two efficiency upgrades");
                helper.assertValueEqual(72, reactor.getRequiredPower(), "AP with one colony and two efficiency upgrades");
                helper.succeed();
            });
        });

        r.add("bio_overhaul/combined_upgrade_power_math", 40, helper -> {
            BlockPos controller = new BlockPos(4, 1, 4);
            helper.setBlock(controller, NTBlocks.INDUSTRIAL_BIO_REACTOR.get().defaultBlockState());
            IndustrialBioReactorBlockEntity reactor = helper.getBlockEntity(controller, IndustrialBioReactorBlockEntity.class);

            helper.assertValueEqual(100, reactor.getRequiredPower(), "industrial AP with no colonies");
            for (int slot = 0; slot < IndustrialBioReactorBlockEntity.COLONIES; slot++) {
                reactor.getBacteriaStorage().setBacteria(slot, BacteriaMachineTests.colony(NTBacterias.LITHOPHILES, 400,
                        stats(1.0f, NTConfig.bacteriaLifespanCap), 0));
            }
            helper.assertValueEqual(550, reactor.getRequiredPower(), "industrial AP with nine colonies");

            reactor.getItemStackHandler().setStackInSlot(reactor.upgradeSlot(0), new ItemStack(NTItems.REACTOR_SPEED_UPGRADE.get()));
            reactor.getItemStackHandler().setStackInSlot(reactor.upgradeSlot(1), new ItemStack(NTItems.REACTOR_SPEED_UPGRADE.get()));
            reactor.getItemStackHandler().setStackInSlot(reactor.upgradeSlot(2), new ItemStack(NTItems.REACTOR_YIELD_UPGRADE.get()));
            reactor.getItemStackHandler().setStackInSlot(reactor.upgradeSlot(3), new ItemStack(NTItems.REACTOR_EFFICIENCY_UPGRADE.get()));
            helper.assertValueEqual(3042, reactor.getRequiredPower(), "550 x 1.6 x 1.6 x 1.8 x 1.2 rounded up");
            helper.assertTrue(Math.abs(reactor.getSpeedMultiplier() - 2.0f) < EPSILON, "speed with two speed upgrades");
            helper.assertValueEqual(2, reactor.getItemsPerCycle(), "items per cycle with one yield upgrade");

            for (int slot = 0; slot < IndustrialBioReactorBlockEntity.UPGRADE_SLOTS; slot++) {
                reactor.getItemStackHandler().setStackInSlot(reactor.upgradeSlot(slot), new ItemStack(NTItems.REACTOR_EFFICIENCY_UPGRADE.get()));
            }
            helper.assertTrue(Math.abs(reactor.getVitalityCost() - 0.31640625f) < EPSILON, "vitality per tick with four efficiency upgrades");
            helper.assertValueEqual(1141, reactor.getRequiredPower(), "550 x 1.2^4 rounded up");
            helper.succeed();
        });

        r.add("bio_overhaul/fusion_upgrade_counts_as_all_three", 40, helper -> {
            BlockPos controller = new BlockPos(4, 1, 4);
            helper.setBlock(controller, NTBlocks.INDUSTRIAL_BIO_REACTOR.get().defaultBlockState());
            IndustrialBioReactorBlockEntity reactor = helper.getBlockEntity(controller, IndustrialBioReactorBlockEntity.class);
            for (int slot = 0; slot < IndustrialBioReactorBlockEntity.COLONIES; slot++) {
                reactor.getBacteriaStorage().setBacteria(slot, BacteriaMachineTests.colony(NTBacterias.LITHOPHILES, 400,
                        stats(1.0f, NTConfig.bacteriaLifespanCap), 0));
            }
            helper.assertTrue(!reactor.hasUpgrades(), "no upgrades before inserting the fusion upgrade");

            reactor.getItemStackHandler().setStackInSlot(reactor.upgradeSlot(0), new ItemStack(NTItems.REACTOR_FUSION_UPGRADE.get()));
            helper.assertTrue(reactor.hasUpgrades(), "fusion counts as an upgrade");
            helper.assertValueEqual(1375, reactor.getRequiredPower(), "550 x 2.5 for one fusion upgrade");
            helper.assertTrue(Math.abs(reactor.getSpeedMultiplier() - 1.5f) < EPSILON, "speed with one fusion upgrade");
            helper.assertValueEqual(2, reactor.getItemsPerCycle(), "items per cycle with one fusion upgrade");
            helper.assertTrue(Math.abs(reactor.getVitalityCost() - 0.75f) < EPSILON, "vitality per tick with one fusion upgrade");

            reactor.getItemStackHandler().setStackInSlot(reactor.upgradeSlot(1), new ItemStack(NTItems.REACTOR_SPEED_UPGRADE.get()));
            helper.assertValueEqual(2200, reactor.getRequiredPower(), "550 x 2.5 x 1.6 for a fusion upgrade and a speed upgrade");
            helper.assertTrue(Math.abs(reactor.getSpeedMultiplier() - 2.0f) < EPSILON, "speed with a fusion upgrade and a speed upgrade");

            for (int slot = 0; slot < IndustrialBioReactorBlockEntity.UPGRADE_SLOTS; slot++) {
                reactor.getItemStackHandler().setStackInSlot(reactor.upgradeSlot(slot), new ItemStack(NTItems.REACTOR_FUSION_UPGRADE.get()));
            }
            helper.assertValueEqual(21485, reactor.getRequiredPower(), "550 x 2.5^4 rounded up for four fusion upgrades");
            helper.assertTrue(Math.abs(reactor.getSpeedMultiplier() - 3.0f) < EPSILON, "speed with four fusion upgrades");
            helper.assertValueEqual(5, reactor.getItemsPerCycle(), "items per cycle with four fusion upgrades");
            helper.assertTrue(Math.abs(reactor.getVitalityCost() - 0.31640625f) < EPSILON, "vitality per tick with four fusion upgrades");
            helper.succeed();
        });

        r.add("bio_overhaul/industrial_forms_from_exact_shape", 60, helper -> {
            BlockPos origin = new BlockPos(2, 1, 2);
            buildIndustrial(helper, origin);
            helper.runAfterDelay(1, () -> {
                helper.assertTrue(formIndustrial(helper, origin), "Industrial Bio Reactor should form from the exact shape");
                BlockState controller = helper.getBlockState(industrialController(origin));
                helper.assertTrue(controller.is(NTBlocks.INDUSTRIAL_BIO_REACTOR.get()), "Controller should stay the controller block");
                helper.assertTrue(controller.getValue(Multiblock.FORMED), "Controller should be formed");

                for (int layer = 0; layer < IndustrialBioReactorMultiblock.HEIGHT; layer++) {
                    for (int cell = 0; cell < IndustrialBioReactorMultiblock.SIZE * IndustrialBioReactorMultiblock.SIZE; cell++) {
                        int key = IndustrialBioReactorMultiblock.keyAt(layer, cell);
                        BlockState state = helper.getBlockState(cellPos(origin, layer, cell));
                        if (key == IndustrialBioReactorMultiblock.CHAMBER) {
                            helper.assertTrue(state.isAir(), "Chamber cell " + layer + "/" + cell + " should stay air");
                        } else if (key != IndustrialBioReactorMultiblock.CONTROLLER) {
                            helper.assertTrue(state.is(NTBlocks.INDUSTRIAL_BIO_REACTOR_PART.get()), "Cell " + layer + "/" + cell + " should be a formed part, was " + state.getBlock());
                            helper.assertValueEqual(layer, state.getValue(IndustrialBioReactorMultiblock.LAYER), "layer property at " + layer + "/" + cell);
                            helper.assertValueEqual(cell, state.getValue(IndustrialBioReactorMultiblock.CELL), "cell property at " + layer + "/" + cell);
                            helper.assertTrue(state.getValue(Multiblock.FORMED), "Part " + layer + "/" + cell + " should be formed");
                        }
                    }
                }
                IndustrialBioReactorBlockEntity reactor = helper.getBlockEntity(industrialController(origin), IndustrialBioReactorBlockEntity.class);
                helper.assertTrue(reactor.getMultiblockData().valid(), "Controller multiblock data should be valid");
                helper.succeed();
            });
        });

        for (HorizontalDirection facing : new HorizontalDirection[]{HorizontalDirection.EAST, HorizontalDirection.SOUTH, HorizontalDirection.WEST}) {
            r.add("bio_overhaul/industrial_forms_facing_" + facing.getSerializedName(), 60, helper -> {
                BlockPos controller = new BlockPos(4, 2, 4);
                BlockPos first = MultiblockHelper.getFirstBlockPos(facing, controller,
                        MultiblockHelper.getRelativeControllerPos(NTMultiblocks.INDUSTRIAL_BIO_REACTOR.get()));
                Map<Integer, Block> definition = NTMultiblocks.INDUSTRIAL_BIO_REACTOR.get().getDefinition();
                for (int layer = 0; layer < IndustrialBioReactorMultiblock.HEIGHT; layer++) {
                    for (int cell = 0; cell < IndustrialBioReactorMultiblock.SIZE * IndustrialBioReactorMultiblock.SIZE; cell++) {
                        BlockPos pos = MultiblockHelper.getCurPos(first, new Vec3i(cell % IndustrialBioReactorMultiblock.SIZE, layer,
                                cell / IndustrialBioReactorMultiblock.SIZE), facing);
                        helper.setBlock(pos, definition.get(IndustrialBioReactorMultiblock.keyAt(layer, cell)));
                    }
                }
                helper.runAfterDelay(1, () -> {
                    helper.assertTrue(MultiblockHelper.form(NTMultiblocks.INDUSTRIAL_BIO_REACTOR.get(), helper.absolutePos(controller), helper.getLevel()),
                            "A reactor built with its controller in the " + facing.getSerializedName() + " wall should form");
                    BlockState state = helper.getBlockState(controller);
                    helper.assertTrue(state.getValue(Multiblock.FORMED), "Controller should be formed");
                    helper.assertValueEqual(facing, state.getValue(IndustrialBioReactorMultiblock.ORIENTATION), "controller orientation");
                    IndustrialBioReactorBlockEntity reactor = helper.getBlockEntity(controller, IndustrialBioReactorBlockEntity.class);
                    helper.assertValueEqual(facing.toRegularDirection(), reactor.front(), "front of the reactor");

                    BlockPos roofEdge = MultiblockHelper.getCurPos(first, new Vec3i(1, IndustrialBioReactorMultiblock.HEIGHT - 1, 0), facing);
                    BlockState roof = helper.getBlockState(roofEdge);
                    helper.assertValueEqual(facing, roof.getValue(IndustrialBioReactorMultiblock.ORIENTATION), "part orientation");
                    helper.getLevel().setBlock(helper.absolutePos(roofEdge), roof.setValue(BioReactorMultiblock.HATCH, true), 3);
                    IndustrialBioReactorPartBlockEntity hatch = helper.getBlockEntity(roofEdge, IndustrialBioReactorPartBlockEntity.class);
                    helper.assertTrue(hatch.getLaserInputs().contains(Direction.UP), "a hatch takes a beam from above");
                    helper.assertTrue(hatch.getLaserInputs().contains(facing.toRegularDirection()), "a front edge hatch takes a beam on its outer side");
                    helper.assertFalse(hatch.getLaserInputs().contains(facing.toRegularDirection().getOpposite()), "but not from the inside");
                    helper.assertTrue(hatch.getItemHandlerOnSide(facing.toRegularDirection()) != null, "the outer face of the front wall takes items");
                    helper.assertTrue(hatch.getItemHandlerOnSide(facing.toRegularDirection().getOpposite()) == null, "the inner face does not");
                    helper.succeed();
                });
            });
        }

        r.add("bio_overhaul/industrial_refuses_wrong_shape", 60, helper -> {
            BlockPos origin = new BlockPos(2, 1, 2);
            buildIndustrial(helper, origin);
            BlockPos shield = cellPos(origin, 2, 1);
            BlockPos chamber = cellPos(origin, 1, 12);
            helper.setBlock(shield, Blocks.STONE);
            helper.runAfterDelay(1, () -> {
                helper.assertFalse(formIndustrial(helper, origin), "A stone in the wall should stop the reactor forming");
                helper.assertFalse(helper.getBlockState(industrialController(origin)).getValue(Multiblock.FORMED), "Controller should stay unformed");
                helper.assertFalse(helper.getBlockState(cellPos(origin, 0, 0)).is(NTBlocks.INDUSTRIAL_BIO_REACTOR_PART.get()), "No part should exist");

                helper.setBlock(shield, NTBlocks.BACTERIAL_CONTAINMENT_SHIELD.get());
                helper.setBlock(chamber, Blocks.STONE);
                helper.assertFalse(formIndustrial(helper, origin), "A block inside the culture chamber should stop the reactor forming");

                helper.setBlock(chamber, Blocks.AIR);
                helper.assertTrue(formIndustrial(helper, origin), "The repaired shape should form");
                helper.succeed();
            });
        });

        r.add("bio_overhaul/industrial_produces_with_nine_colonies", 300, helper -> {
            BlockPos origin = new BlockPos(2, 1, 2);
            buildIndustrial(helper, origin);
            helper.runAfterDelay(1, () -> {
                helper.assertTrue(formIndustrial(helper, origin), "Industrial Bio Reactor should form");
                powerIndustrialThroughHatches(helper, origin);
                IndustrialBioReactorBlockEntity reactor = helper.getBlockEntity(industrialController(origin), IndustrialBioReactorBlockEntity.class);
                for (int slot = 0; slot < IndustrialBioReactorBlockEntity.COLONIES; slot++) {
                    reactor.getBacteriaStorage().setBacteria(slot, BacteriaMachineTests.colony(NTBacterias.LITHOPHILES, NTConfig.bacteriaColonySizeCap,
                            stats(1.0f, NTConfig.bacteriaLifespanCap), 0));
                }
            });
            helper.runAfterDelay(160, () -> {
                IndustrialBioReactorBlockEntity reactor = helper.getBlockEntity(industrialController(origin), IndustrialBioReactorBlockEntity.class);
                helper.assertValueEqual(550, reactor.getRequiredPower(), "AP for nine colonies");
                helper.assertTrue(reactor.getPower() >= reactor.getRequiredPower(),
                        "Six roof hatches should supply the reactor, had " + reactor.getPower() + " of " + reactor.getRequiredPower());
                for (int slot = 0; slot < IndustrialBioReactorBlockEntity.COLONIES; slot++) {
                    helper.assertTrue(reactor.getItemStackHandler().getStackInSlot(reactor.outputSlot(slot)).is(Items.STONE),
                            "Colony " + slot + " should have produced stone");
                }
                helper.assertTrue(active(helper.getBlockState(industrialController(origin))), "Controller should be active while producing");
                helper.assertTrue(active(helper.getBlockState(cellPos(origin, 0, 0))), "Base corner should be active while producing");
                helper.assertTrue(active(helper.getBlockState(cellPos(origin, 3, 24))), "Roof corner should be active while producing");
                helper.succeed();
            });
        });

        r.add("bio_overhaul/industrial_hopper_automation", 400, helper -> {
            BlockPos origin = new BlockPos(2, 2, 2);
            BlockPos topHopper = cellPos(origin, 3, 1).above();
            BlockPos bottomHopper = cellPos(origin, 0, 6).below();
            buildIndustrial(helper, origin);
            helper.setBlock(topHopper, Blocks.HOPPER.defaultBlockState());
            helper.setBlock(bottomHopper, Blocks.HOPPER.defaultBlockState());

            helper.runAfterDelay(1, () -> {
                helper.assertTrue(formIndustrial(helper, origin), "Industrial Bio Reactor should form");
                powerIndustrialThroughHatches(helper, origin);
                IndustrialBioReactorBlockEntity reactor = helper.getBlockEntity(industrialController(origin), IndustrialBioReactorBlockEntity.class);
                reactor.getBacteriaStorage().setBacteria(0, BacteriaMachineTests.colony(NTBacterias.FERROPHILES, NTConfig.bacteriaColonySizeCap,
                        stats(2.0f, NTConfig.bacteriaLifespanCap), 0));
                HopperBlockEntity hopper = helper.getBlockEntity(topHopper, HopperBlockEntity.class);
                hopper.setItem(0, new ItemStack(Items.DIRT, 2));
                hopper.setItem(1, new ItemStack(Items.IRON_ORE, 8));
                hopper.setItem(2, new ItemStack(NTItems.REACTOR_SPEED_UPGRADE.get()));
            });

            long[] age = new long[1];
            helper.runAfterDelay(120, () -> {
                IndustrialBioReactorBlockEntity reactor = helper.getBlockEntity(industrialController(origin), IndustrialBioReactorBlockEntity.class);
                age[0] = reactor.getBacteriaStorage().getBacteria(0).getAge();
            });
            helper.runAfterDelay(260, () -> {
                IndustrialBioReactorBlockEntity reactor = helper.getBlockEntity(industrialController(origin), IndustrialBioReactorBlockEntity.class);
                HopperBlockEntity top = helper.getBlockEntity(topHopper, HopperBlockEntity.class);
                HopperBlockEntity bottom = helper.getBlockEntity(bottomHopper, HopperBlockEntity.class);

                helper.assertValueEqual(1, reactor.getUpgradeCount(ReactorUpgradeItem.Type.SPEED), "speed upgrades inserted by the hopper");
                helper.assertValueEqual(0, count(reactor, Items.DIRT), "dirt inside the reactor");
                helper.assertValueEqual(2, count(top, Items.DIRT), "dirt refused and left in the top hopper");
                helper.assertValueEqual(0, count(top, Items.IRON_ORE), "iron ore left in the top hopper");
                helper.assertTrue(count(reactor, Items.IRON_ORE) >= 6, "The nutrient slots should hold the delivered ore, had " + count(reactor, Items.IRON_ORE));
                helper.assertTrue(reactor.getVitality(0) > 0, "The colony should be fed from the hopper's ore");
                helper.assertValueEqual(age[0], reactor.getBacteriaStorage().getBacteria(0).getAge(), "age of a colony fed by automation");
                helper.assertTrue(count(bottom, Items.IRON_INGOT) > 0, "The bottom hopper should pull iron ingots out of the base");
                helper.assertValueEqual(0, count(bottom, Items.IRON_ORE), "nutrients pulled out through the base");
                helper.assertValueEqual(0, count(bottom, NTItems.REACTOR_SPEED_UPGRADE.get()), "upgrades pulled out through the base");
                helper.succeed();
            });
        });

        r.add("bio_overhaul/bio_reactor_hopper_feeds_and_drains", 300, helper -> {
            BlockPos reactorPos = new BlockPos(4, 2, 4);
            BlockPos topHopper = reactorPos.above();
            BlockPos bottomHopper = reactorPos.below();
            BioReactorBlockEntity reactor = loneReactor(helper, reactorPos, new BlockPos(4, 2, 6));
            helper.setBlock(topHopper, Blocks.HOPPER.defaultBlockState());
            helper.setBlock(bottomHopper, Blocks.HOPPER.defaultBlockState());
            reactor.getBacteriaStorage().setBacteria(0, BacteriaMachineTests.colony(NTBacterias.FERROPHILES, NTConfig.bacteriaColonySizeCap,
                    stats(2.0f, NTConfig.bacteriaLifespanCap), 0));

            helper.runAfterDelay(1, () -> {
                HopperBlockEntity hopper = helper.getBlockEntity(topHopper, HopperBlockEntity.class);
                hopper.setItem(0, new ItemStack(Items.DIRT, 1));
                hopper.setItem(1, new ItemStack(Items.IRON_ORE, 4));
                hopper.setItem(2, new ItemStack(NTItems.REACTOR_YIELD_UPGRADE.get()));
            });

            long[] age = new long[1];
            helper.runAfterDelay(100, () -> age[0] = reactor.getBacteriaStorage().getBacteria(0).getAge());
            helper.runAfterDelay(220, () -> {
                HopperBlockEntity top = helper.getBlockEntity(topHopper, HopperBlockEntity.class);
                HopperBlockEntity bottom = helper.getBlockEntity(bottomHopper, HopperBlockEntity.class);
                helper.assertValueEqual(1, reactor.getUpgradeCount(ReactorUpgradeItem.Type.YIELD), "yield upgrades inserted by the hopper");
                helper.assertValueEqual(1, count(top, Items.DIRT), "dirt refused and left in the top hopper");
                helper.assertValueEqual(0, count(reactor, Items.DIRT), "dirt inside the reactor");
                helper.assertValueEqual(age[0], reactor.getBacteriaStorage().getBacteria(0).getAge(), "age of a colony fed by automation");
                helper.assertTrue(count(bottom, Items.IRON_INGOT) > 0, "The bottom hopper should drain iron ingots");
                helper.assertValueEqual(0, count(bottom, Items.IRON_ORE), "nutrients drained by the bottom hopper");
                helper.succeed();
            });
        });

        r.add("bio_overhaul/bio_reactor_active_follows_production", 260, helper -> {
            placeBioReactor(helper, FORMED_BIO_CONTROLLER);
            BlockPos hatch = FORMED_BIO_CONTROLLER.north();
            helper.runAfterDelay(1, () -> {
                helper.assertTrue(MultiblockHelper.form(NTMultiblocks.BIO_REACTOR.get(), helper.absolutePos(FORMED_BIO_CONTROLLER), helper.getLevel()),
                        "Bio reactor should form");
                BlockState hatchState = helper.getBlockState(hatch);
                helper.getLevel().setBlock(helper.absolutePos(hatch), hatchState.setValue(BioReactorMultiblock.HATCH, true), 3);
                helper.getBlockEntity(hatch, BioReactorPartBlockEntity.class).setLaserInput(true);
                helper.setBlock(hatch.above(), NTBlocks.CREATIVE_POWER_SOURCE.get().defaultBlockState());
                BioReactorBlockEntity reactor = helper.getBlockEntity(FORMED_BIO_CONTROLLER, BioReactorBlockEntity.class);
                reactor.getBacteriaStorage().setBacteria(0, BacteriaMachineTests.colony(NTBacterias.LITHOPHILES, NTConfig.bacteriaColonySizeCap,
                        stats(1.0f, NTConfig.bacteriaLifespanCap), 0));
            });
            helper.runAfterDelay(90, () -> {
                BioReactorBlockEntity reactor = helper.getBlockEntity(FORMED_BIO_CONTROLLER, BioReactorBlockEntity.class);
                helper.assertTrue(reactor.getPower() >= reactor.getRequiredPower(), "Hatch should power the reactor, had " + reactor.getPower());
                helper.assertTrue(active(helper.getBlockState(FORMED_BIO_CONTROLLER)), "Controller should be active");
                helper.assertTrue(active(helper.getBlockState(FORMED_BIO_CONTROLLER.below())), "Base part should be active");
                helper.assertTrue(active(helper.getBlockState(FORMED_BIO_CONTROLLER.offset(-1, 0, -1))), "Top corner should be active");
                reactor.getBacteriaStorage().setBacteria(0, BacteriaInstance.EMPTY);
            });
            helper.runAfterDelay(160, () -> {
                helper.assertFalse(active(helper.getBlockState(FORMED_BIO_CONTROLLER)), "Controller should go idle without colonies");
                helper.assertFalse(active(helper.getBlockState(FORMED_BIO_CONTROLLER.below())), "Base part should go idle");
                helper.assertFalse(active(helper.getBlockState(FORMED_BIO_CONTROLLER.offset(-1, 0, -1))), "Top corner should go idle");
                helper.succeed();
            });
        });

        r.add("bio_overhaul/legacy_three_slot_inventory_loads", 40, helper -> {
            BlockPos sourcePos = new BlockPos(2, 1, 2);
            BlockPos targetPos = new BlockPos(6, 1, 6);
            helper.setBlock(sourcePos, NTBlocks.BIO_REACTOR.get().defaultBlockState());
            helper.setBlock(targetPos, NTBlocks.BIO_REACTOR.get().defaultBlockState());
            helper.runAfterDelay(1, () -> {
                BioReactorBlockEntity source = helper.getBlockEntity(sourcePos, BioReactorBlockEntity.class);
                BioReactorBlockEntity target = helper.getBlockEntity(targetPos, BioReactorBlockEntity.class);
                source.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 5));

                CompoundTag tag = source.saveWithoutMetadata(helper.getLevel().registryAccess());
                ListTag stacks = tag.getCompound("itemhandler").getList("stacks", Tag.TAG_COMPOUND);
                while (stacks.size() > 3) {
                    stacks.remove(stacks.size() - 1);
                }
                helper.assertValueEqual(3, tag.getCompound("itemhandler").getList("stacks", Tag.TAG_COMPOUND).size(), "trimmed legacy item list");
                tag.remove("vitality0");
                tag.remove("vitalityCapacity0");

                target.loadWithComponents(tag, helper.getLevel().registryAccess());
                helper.assertValueEqual(source.totalItemSlots(), target.getItemStackHandler().getSlots(), "slot count after loading a three slot save");
                helper.assertValueEqual(5, target.getItemStackHandler().getStackInSlot(0).getCount(), "legacy output slot 0");
                helper.assertTrue(target.getItemStackHandler().getStackInSlot(target.nutrientSlot(1)).isEmpty(), "new nutrient slot should start empty");
                target.getItemStackHandler().setStackInSlot(target.upgradeSlot(1), new ItemStack(NTItems.REACTOR_YIELD_UPGRADE.get()));
                helper.assertValueEqual(1, target.getUpgradeCount(ReactorUpgradeItem.Type.YIELD), "upgrade placed in a slot that did not exist in the old save");
                helper.assertValueEqual(0.0f, target.getVitality(0), "vitality of a legacy save");
                helper.succeed();
            });
        });

        r.add("bio_overhaul/ore_strains_eat_their_product_and_its_block", 20, helper -> {
            BacteriaInstance colony = BacteriaMachineTests.colony(NTBacterias.FERROPHILES, 400, stats(1.0f, 2000), 0);
            float[] chances = new float[3];
            int[] ticks = new int[3];
            ItemStack[] nutrients = {new ItemStack(Items.IRON_ORE), new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_BLOCK)};
            for (int i = 0; i < nutrients.length; i++) {
                Optional<BacteriaIncubationRecipe> incubation = BacteriaIncubationRecipe.find(helper.getLevel(), new BacteriaRecipeInput(colony, nutrients[i]));
                Optional<ColonyFeedingRecipe> feeding = ColonyFeedingRecipe.find(helper.getLevel(), colony, nutrients[i]);
                helper.assertTrue(incubation.isPresent(), "Ferrophiles incubate on " + nutrients[i].getHoverName().getString());
                helper.assertTrue(feeding.isPresent(), "Ferrophiles are fed by " + nutrients[i].getHoverName().getString());
                chances[i] = incubation.get().consumeChance();
                ticks[i] = feeding.get().vitalityTicks();
            }
            helper.assertValueEqual(chances[0], 0.1f, "ore consume chance");
            helper.assertTrue(chances[1] > chances[0], "an ingot is used up more often than ore");
            helper.assertTrue(chances[2] < chances[1] / 8, "a block lasts about nine times as long as an ingot");
            helper.assertValueEqual(ticks[2], ticks[1] * 9, "a block feeds a reactor nine times as long as an ingot");
            helper.succeed();
        });

        r.add("bio_overhaul/every_strain_eats_its_product_block", 20, helper -> {
            BacteriaInstance wheat = BacteriaMachineTests.colony(NTBacterias.RHIZOBACTERIA, 400, stats(1.0f, 2000), 0);
            ItemStack hay = new ItemStack(Items.HAY_BLOCK);
            Optional<BacteriaIncubationRecipe> incubation = BacteriaIncubationRecipe.find(helper.getLevel(), new BacteriaRecipeInput(wheat, hay));
            Optional<ColonyFeedingRecipe> feeding = ColonyFeedingRecipe.find(helper.getLevel(), wheat, hay);
            Optional<ColonyFeedingRecipe> base = ColonyFeedingRecipe.find(helper.getLevel(), wheat, new ItemStack(Items.WHEAT));
            helper.assertTrue(incubation.isPresent(), "Rhizobacteria incubate on a Hay Bale through the storage block data map");
            helper.assertTrue(feeding.isPresent() && base.isPresent(), "Rhizobacteria are fed by wheat and by a Hay Bale");
            helper.assertValueEqual(feeding.get().vitalityTicks(), base.get().vitalityTicks() * 9, "a Hay Bale feeds nine times as long as wheat");
            helper.assertTrue(ColonyFeedingRecipe.isNutrient(helper.getLevel(), hay), "a reactor's nutrient slot takes a Hay Bale");

            BacteriaInstance bone = BacteriaMachineTests.colony(NTBacterias.CALCIOPHILES, 400, stats(1.0f, 2000), 0);
            helper.assertTrue(BacteriaIncubationRecipe.find(helper.getLevel(), new BacteriaRecipeInput(bone, new ItemStack(Items.BONE_MEAL))).isPresent(),
                    "Calciophiles also eat the Bone Meal they make");

            BacteriaInstance cyano = BacteriaMachineTests.colony(NTBacterias.CYANOBACTERIA, 400, stats(1.0f, 2000), 0);
            helper.assertTrue(BacteriaIncubationRecipe.find(helper.getLevel(), new BacteriaRecipeInput(cyano, new ItemStack(Items.STONE))).isEmpty(),
                    "a strain with no nutrient of its own gets none");
            helper.succeed();
        });

        r.add("bio_overhaul/feeding_recipes_cover_incubated_strains", 20, helper -> {
            StringBuilder missing = new StringBuilder();
            for (RecipeHolder<BacteriaIncubationRecipe> holder : helper.getLevel().getRecipeManager().getAllRecipesFor(BacteriaIncubationRecipe.TYPE)) {
                BacteriaIncubationRecipe incubation = holder.value();
                ItemStack[] nutrient = incubation.nutrient().getItems();
                if (nutrient.length == 0) {
                    continue;
                }
                BacteriaInstance colony = BacteriaMachineTests.colony(incubation.bacteria(), 400, stats(1.0f, 2000), 0);
                if (ColonyFeedingRecipe.find(helper.getLevel(), colony, new ItemStack(nutrient[0].getItem())).isEmpty()) {
                    missing.append(' ').append(incubation.bacteria().location());
                }
            }
            helper.assertTrue(missing.isEmpty(), "Strains whose incubation nutrient does not feed them in a reactor:" + missing);
            helper.succeed();
        });

        r.add("bio_overhaul/automation_slots_are_filtered", 40, helper -> {
            helper.setBlock(REACTOR, NTBlocks.BIO_REACTOR.get().defaultBlockState());
            helper.runAfterDelay(2, () -> {
                BioReactorBlockEntity reactor = helper.getBlockEntity(REACTOR, BioReactorBlockEntity.class);
                ResourceHandler<ItemResource> side = helper.getLevel().getCapability(TransferCapabilities.Item.BLOCK, helper.absolutePos(REACTOR), Direction.UP);
                helper.assertTrue(side != null, "Bio reactor should expose items on top");
                ItemStack upgrade = new ItemStack(NTItems.REACTOR_SPEED_UPGRADE.get());
                helper.assertValueEqual(0, insert(side, reactor.outputSlot(0), upgrade), "upgrade into an output slot");
                helper.assertValueEqual(0, insert(side, reactor.nutrientSlot(0), upgrade), "upgrade into a nutrient slot");
                helper.assertValueEqual(1, insert(side, reactor.upgradeSlot(0), upgrade), "upgrade into an upgrade slot");
                helper.assertValueEqual(0, insert(side, reactor.upgradeSlot(1), new ItemStack(Items.IRON_ORE)), "nutrient into an upgrade slot");
                helper.assertValueEqual(0, insert(side, reactor.nutrientSlot(0), new ItemStack(Items.DIRT)), "junk into a nutrient slot");
                helper.assertValueEqual(4, insert(side, reactor.nutrientSlot(0), new ItemStack(Items.IRON_ORE, 4)), "nutrient into a nutrient slot");
                helper.assertValueEqual(0, insert(side, reactor.outputSlot(1), new ItemStack(Items.IRON_ORE)), "nutrient into an output slot");
                helper.assertValueEqual(1, reactor.getItemStackHandler().getStackInSlot(reactor.upgradeSlot(0)).getCount(), "upgrade slots hold one item");
                helper.succeed();
            });
        });
        r.add("bio_overhaul/bio_reactor_side_config_applies_to_parts", 60, helper -> {
            placeBioReactor(helper, FORMED_BIO_CONTROLLER);
            helper.runAfterDelay(1, () -> helper.assertTrue(
                    MultiblockHelper.form(NTMultiblocks.BIO_REACTOR.get(), helper.absolutePos(FORMED_BIO_CONTROLLER), helper.getLevel()),
                    "Bio reactor should form"));
            helper.runAfterDelay(3, () -> {
                BioReactorBlockEntity reactor = helper.getBlockEntity(FORMED_BIO_CONTROLLER, BioReactorBlockEntity.class);
                BlockPos base = FORMED_BIO_CONTROLLER.below();
                BlockPos northWall = base.north();
                reactor.getItemStackHandler().setStackInSlot(reactor.outputSlot(0), new ItemStack(Items.COBBLESTONE, 4));

                ResourceHandler<ItemResource> wall = items(helper, northWall, Direction.NORTH);
                helper.assertTrue(wall != null, "An outer wall face should expose the reactor");
                helper.assertValueEqual(1, extract(wall, reactor.outputSlot(0), new ItemStack(Items.COBBLESTONE, 1)), "extracted through a wall set to Both");
                helper.assertTrue(items(helper, northWall, Direction.EAST) == null, "A face against another part should expose nothing");

                reactor.setSideMode(SideKind.ITEMS, RelativeFace.BOTTOM, SideMode.INPUT);
                ResourceHandler<ItemResource> bottom = items(helper, base, Direction.DOWN);
                helper.assertTrue(bottom != null, "An Input bottom should still expose the reactor");
                helper.assertValueEqual(0, extract(bottom, reactor.outputSlot(0), new ItemStack(Items.COBBLESTONE, 1)), "extracted through an Input bottom");
                helper.assertValueEqual(2, insert(bottom, reactor.nutrientSlot(0), new ItemStack(Items.IRON_ORE, 2)), "nutrients through an Input bottom");

                reactor.setSideMode(SideKind.ITEMS, RelativeFace.FRONT, SideMode.NONE);
                helper.assertTrue(items(helper, northWall, Direction.NORTH) == null, "A front set to None should expose nothing on the north wall");
                helper.succeed();
            });
        });

        r.add("bio_overhaul/industrial_side_config_applies_to_walls", 60, helper -> {
            BlockPos origin = new BlockPos(2, 1, 2);
            buildIndustrial(helper, origin);
            helper.runAfterDelay(1, () -> helper.assertTrue(formIndustrial(helper, origin), "Industrial Bio Reactor should form"));
            helper.runAfterDelay(3, () -> {
                IndustrialBioReactorBlockEntity reactor = helper.getBlockEntity(industrialController(origin), IndustrialBioReactorBlockEntity.class);
                BlockPos westWall = cellPos(origin, 1, 5);
                reactor.getItemStackHandler().setStackInSlot(reactor.outputSlot(4), new ItemStack(Items.STONE, 6));

                ResourceHandler<ItemResource> wall = items(helper, westWall, Direction.WEST);
                helper.assertTrue(wall != null, "The west wall should expose the reactor");
                helper.assertValueEqual(2, extract(wall, reactor.outputSlot(4), new ItemStack(Items.STONE, 2)), "extracted through a wall set to Both");
                helper.assertTrue(items(helper, westWall, Direction.EAST) == null, "A face into the culture chamber should expose nothing");

                RelativeFace west = RelativeFace.of(reactor.front(), Direction.WEST);
                reactor.setSideMode(SideKind.ITEMS, west, SideMode.OUTPUT);
                ResourceHandler<ItemResource> output = items(helper, westWall, Direction.WEST);
                helper.assertValueEqual(0, insert(output, reactor.nutrientSlot(0), new ItemStack(Items.IRON_ORE, 2)), "nutrients through an Output wall");
                helper.assertValueEqual(2, extract(output, reactor.outputSlot(4), new ItemStack(Items.STONE, 2)), "extracted through an Output wall");

                ResourceHandler<ItemResource> floor = items(helper, cellPos(origin, 0, 12), Direction.DOWN);
                helper.assertValueEqual(2, insert(floor, reactor.nutrientSlot(0), new ItemStack(Items.IRON_ORE, 2)), "nutrients through the floor, which is still Both");
                helper.succeed();
            });
        });
    }

    private static void assertMultiplier(NTGameTestHelper helper, AbstractBioReactorBlockEntity reactor, ResourceKey<Bacteria> key, float expected) {
        Bacteria definition = reactor.definitionOf(key);
        helper.assertTrue(definition != null, "Missing bacteria definition " + key.location());
        helper.assertTrue(Math.abs(definition.productionMultiplier() - expected) < EPSILON,
                key.location() + " multiplier should be " + expected + ", was " + definition.productionMultiplier());
    }
}
