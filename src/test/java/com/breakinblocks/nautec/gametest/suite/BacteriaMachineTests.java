package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.content.bacteria.SimpleBacteriaStats;
import com.breakinblocks.nautec.content.bacteria.SimpleCollapsedStats;
import com.breakinblocks.nautec.content.blockentities.BacterialAnalyzerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.BacterialFuelCellBlockEntity;
import com.breakinblocks.nautec.content.blockentities.IncubatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MutatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.BioReactorBlockEntity;
import com.breakinblocks.nautec.content.recipes.BacteriaIncubationRecipe;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ComponentBacteriaStorage;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Set;

public final class BacteriaMachineTests {
    private static final float EPSILON = 1.0e-4f;

    private BacteriaMachineTests() {
    }

    static void placeShieldedSource(GameTestHelper helper, BlockPos pos, Direction... openDirections) {
        helper.setBlock(pos, NTBlocks.CREATIVE_POWER_SOURCE.get().defaultBlockState());
        Set<Direction> open = Set.of(openDirections);
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN || open.contains(direction)) {
                continue;
            }
            helper.setBlock(pos.relative(direction, 2), Blocks.STONE.defaultBlockState());
        }
    }

    static SimpleCollapsedStats stats(float growthRate, float mutationResistance, float productionRate, int lifespan) {
        return new SimpleCollapsedStats(SimpleBacteriaStats.EMPTY, growthRate, mutationResistance, productionRate, lifespan, -1);
    }

    static BacteriaInstance colony(ResourceKey<Bacteria> bacteria, long size, SimpleCollapsedStats stats, long age) {
        return new BacteriaInstance(bacteria, size, stats, true, age);
    }

    private static BacteriaIncubationRecipe incubationRecipeFor(GameTestHelper helper, ResourceKey<Bacteria> bacteria) {
        for (RecipeHolder<BacteriaIncubationRecipe> holder : helper.getLevel().recipeAccess().recipeMap().byType(BacteriaIncubationRecipe.TYPE)) {
            if (holder.value().bacteria().equals(bacteria)) {
                return holder.value();
            }
        }
        throw helper.assertionException("No incubation recipe for " + bacteria.identifier());
    }

    public static void register(NTTestRegistrar r) {
        r.add("bacteria/bio_reactor_production_scales_with_rate", 420, helper -> {
            BlockPos reactorPos = new BlockPos(4, 1, 4);
            BlockPos sourcePos = new BlockPos(4, 1, 6);
            helper.setBlock(reactorPos, NTBlocks.BIO_REACTOR.get().defaultBlockState());
            placeShieldedSource(helper, sourcePos, Direction.NORTH);

            BioReactorBlockEntity reactor = helper.getBlockEntity(reactorPos, BioReactorBlockEntity.class);
            if (reactor == null) {
                helper.fail("Expected BioReactorBlockEntity");
                return;
            }
            reactor.getBacteriaStorage().setBacteria(0, colony(NTBacterias.FERROPHILES, 400,
                    stats(1.0f, 0f, 0.2f, NTConfig.bacteriaLifespanCap), 0));
            reactor.getBacteriaStorage().setBacteria(1, colony(NTBacterias.FERROPHILES, NTConfig.bacteriaColonySizeCap,
                    stats(1.0f, 0f, 2.0f, NTConfig.bacteriaLifespanCap), 0));

            int[] baseline = new int[2];
            helper.runAfterDelay(60, () -> {
                helper.assertTrue(reactor.getPower() >= reactor.getRequiredPower(),
                        "Reactor should be powered, had " + reactor.getPower() + " of " + reactor.getRequiredPower());
                baseline[0] = reactor.getItemStackHandler().getStackInSlot(0).getCount();
                baseline[1] = reactor.getItemStackHandler().getStackInSlot(1).getCount();
            });

            helper.runAfterDelay(360, () -> {
                int slow = reactor.getItemStackHandler().getStackInSlot(0).getCount() - baseline[0];
                int fast = reactor.getItemStackHandler().getStackInSlot(1).getCount() - baseline[1];
                helper.assertTrue(fast >= 30 && fast <= 36,
                        "High rate colony should make about 33 items in 300 ticks, made " + fast);
                helper.assertTrue(slow >= 0 && slow <= 2,
                        "Low rate colony should make at most 2 items in 300 ticks, made " + slow);
                helper.succeed();
            });
        });

        r.add("bacteria/bio_reactor_required_power_scales", 200, helper -> {
            BlockPos reactorPos = new BlockPos(4, 1, 4);
            BlockPos sourcePos = new BlockPos(4, 1, 6);
            helper.setBlock(reactorPos, NTBlocks.BIO_REACTOR.get().defaultBlockState());
            placeShieldedSource(helper, sourcePos, Direction.NORTH);

            BioReactorBlockEntity reactor = helper.getBlockEntity(reactorPos, BioReactorBlockEntity.class);
            if (reactor == null) {
                helper.fail("Expected BioReactorBlockEntity");
                return;
            }

            helper.assertValueEqual(25, reactor.getRequiredPower(), "required power with no colonies");
            for (int slot = 0; slot < 3; slot++) {
                reactor.getBacteriaStorage().setBacteria(slot, colony(NTBacterias.FERROPHILES, 400,
                        stats(1.0f, 0f, 1.0f, NTConfig.bacteriaLifespanCap), 0));
                helper.assertValueEqual(25 + 25 * (slot + 1), reactor.getRequiredPower(),
                        "required power with " + (slot + 1) + " colonies");
            }

            helper.runAfterDelay(120, () -> {
                helper.assertValueEqual(100, reactor.getRequiredPower(), "required power with three colonies");
                helper.assertValueEqual(100, reactor.getPower(), "power from a single creative source line");
                for (int slot = 0; slot < 3; slot++) {
                    helper.assertTrue(reactor.getItemStackHandler().getStackInSlot(slot).getCount() > 0,
                            "Slot " + slot + " should have produced at exactly the required power");
                }
                helper.succeed();
            });
        });

        r.add("bacteria/bio_reactor_senescent_colony_decays", 500, helper -> {
            BlockPos reactorPos = new BlockPos(4, 1, 4);
            BlockPos sourcePos = new BlockPos(4, 1, 6);
            helper.setBlock(reactorPos, NTBlocks.BIO_REACTOR.get().defaultBlockState());
            placeShieldedSource(helper, sourcePos, Direction.NORTH);

            BioReactorBlockEntity reactor = helper.getBlockEntity(reactorPos, BioReactorBlockEntity.class);
            if (reactor == null) {
                helper.fail("Expected BioReactorBlockEntity");
                return;
            }
            reactor.getBacteriaStorage().setBacteria(0, colony(NTBacterias.FERROPHILES, 20,
                    stats(1.0f, 0f, 2.0f, 10), 500));

            long[] samples = new long[2];
            helper.runAfterDelay(80, () -> {
                samples[0] = reactor.getBacteriaStorage().getBacteria(0).getSize();
                helper.assertTrue(samples[0] < 20, "Senescent colony should have started dying off, size " + samples[0]);
            });
            helper.runAfterDelay(200, () -> {
                samples[1] = reactor.getBacteriaStorage().getBacteria(0).getSize();
                helper.assertTrue(samples[1] < samples[0],
                        "Senescent colony should keep shrinking, " + samples[0] + " then " + samples[1]);
            });
            helper.runAfterDelay(450, () -> {
                helper.assertTrue(reactor.getBacteriaStorage().getBacteria(0).isEmpty(),
                        "Senescent colony should decay away completely");
                helper.succeed();
            });
        });

        r.add("bacteria/incubator_growth_clamps_at_cap_and_resets_age", 260, helper -> {
            BlockPos incubatorPos = new BlockPos(4, 1, 4);
            BlockPos sourcePos = new BlockPos(4, 4, 4);
            helper.setBlock(incubatorPos, NTBlocks.INCUBATOR.get().defaultBlockState());
            placeShieldedSource(helper, sourcePos, Direction.DOWN);

            IncubatorBlockEntity incubator = helper.getBlockEntity(incubatorPos, IncubatorBlockEntity.class);
            if (incubator == null) {
                helper.fail("Expected IncubatorBlockEntity");
                return;
            }
            long startSize = NTConfig.bacteriaColonySizeCap - 5;
            incubator.getBacteriaStorage().setBacteria(0, colony(NTBacterias.LITHOPHILES, startSize,
                    stats(2.0f, 0f, 1.0f, 2000), 700));
            incubator.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.STONE));

            helper.runAfterDelay(200, () -> {
                BacteriaInstance grown = incubator.getBacteriaStorage().getBacteria(0);
                helper.assertValueEqual(NTConfig.bacteriaColonySizeCap, grown.getSize(), "colony size after incubating past the cap");
                helper.assertValueEqual(0L, grown.getAge(), "colony age after incubating");
                helper.succeed();
            });
        });

        r.add("bacteria/incubator_growth_scaled_by_growth_rate", 220, helper -> {
            BlockPos slowPos = new BlockPos(2, 1, 4);
            BlockPos fastPos = new BlockPos(6, 1, 4);
            helper.setBlock(slowPos, NTBlocks.INCUBATOR.get().defaultBlockState());
            helper.setBlock(fastPos, NTBlocks.INCUBATOR.get().defaultBlockState());
            placeShieldedSource(helper, slowPos.above(3), Direction.DOWN);
            placeShieldedSource(helper, fastPos.above(3), Direction.DOWN);

            IncubatorBlockEntity slow = helper.getBlockEntity(slowPos, IncubatorBlockEntity.class);
            IncubatorBlockEntity fast = helper.getBlockEntity(fastPos, IncubatorBlockEntity.class);
            if (slow == null || fast == null) {
                helper.fail("Expected IncubatorBlockEntity at both positions");
                return;
            }
            long startSize = 1000;
            slow.getBacteriaStorage().setBacteria(0, colony(NTBacterias.LITHOPHILES, startSize, stats(0.5f, 0f, 1.0f, 2000), 0));
            fast.getBacteriaStorage().setBacteria(0, colony(NTBacterias.LITHOPHILES, startSize, stats(2.0f, 0f, 1.0f, 2000), 0));
            slow.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.STONE, 8));
            fast.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.STONE, 8));

            helper.runAfterDelay(150, () -> {
                BacteriaIncubationRecipe recipe = incubationRecipeFor(helper, NTBacterias.LITHOPHILES);
                long slowDelta = slow.getBacteriaStorage().getBacteria(0).getSize() - startSize;
                long fastDelta = fast.getBacteriaStorage().getBacteria(0).getSize() - startSize;

                helper.assertTrue(slowDelta >= Math.round(recipe.growth().getMin() * 0.5f)
                                && slowDelta <= Math.round(recipe.growth().getMax() * 0.5f),
                        "Slow colony grew " + slowDelta + ", outside the growth range scaled by 0.5");
                helper.assertTrue(fastDelta >= Math.round(recipe.growth().getMin() * 2.0f)
                                && fastDelta <= Math.round(recipe.growth().getMax() * 2.0f),
                        "Fast colony grew " + fastDelta + ", outside the growth range scaled by 2.0");
                helper.assertTrue(fastDelta > slowDelta, "A high growth rate should out-grow a low one");
                helper.succeed();
            });
        });

        r.add("bacteria/mutator_chance_formula", 40, helper -> helper.runAfterDelay(1, () -> {
            BacteriaMutationRecipe recipe = new BacteriaMutationRecipe(
                    NTBacterias.LITHOPHILES, NTBacterias.CALCIOPHILES, Ingredient.of(Items.BONE_MEAL), 20f);
            float cap = NTConfig.bacteriaMutationResistanceCap;
            long sizeCap = NTConfig.bacteriaColonySizeCap;

            assertNear(helper, 0.20f, MutatorBlockEntity.computeSuccessChance(recipe,
                    colony(NTBacterias.LITHOPHILES, 0, stats(1f, 0f, 1f, 2000), 0)), "fresh colony chance");
            assertNear(helper, 0.10f, MutatorBlockEntity.computeSuccessChance(recipe,
                    colony(NTBacterias.LITHOPHILES, 0, stats(1f, cap, 1f, 2000), 0)), "chance at max resistance");
            assertNear(helper, 0.15f, MutatorBlockEntity.computeSuccessChance(recipe,
                    colony(NTBacterias.LITHOPHILES, sizeCap, stats(1f, 0f, 1f, 2000), 0)), "chance at max size");
            assertNear(helper, 0.075f, MutatorBlockEntity.computeSuccessChance(recipe,
                    colony(NTBacterias.LITHOPHILES, sizeCap, stats(1f, cap, 1f, 2000), 0)), "chance at max size and resistance");

            helper.assertValueEqual(25L, MutatorBlockEntity.computeFailureShrink(
                    colony(NTBacterias.LITHOPHILES, 100, stats(1f, 0f, 1f, 2000), 0)), "failure shrink without resistance");
            helper.assertValueEqual(0L, MutatorBlockEntity.computeFailureShrink(
                    colony(NTBacterias.LITHOPHILES, 100, stats(1f, cap, 1f, 2000), 0)), "failure shrink at max resistance");
            helper.assertValueEqual(0L, MutatorBlockEntity.computeFailureShrink(
                    colony(NTBacterias.LITHOPHILES, 1, stats(1f, 0f, 1f, 2000), 0)), "failure shrink never empties a colony");
            helper.succeed();
        }));

        r.add("bacteria/mutator_outcome_invariant", 400, helper -> {
            BlockPos mutatorPos = new BlockPos(4, 1, 4);
            BlockPos sourcePos = new BlockPos(4, 4, 4);
            helper.setBlock(mutatorPos, NTBlocks.MUTATOR.get().defaultBlockState());
            placeShieldedSource(helper, sourcePos, Direction.DOWN);

            MutatorBlockEntity mutator = helper.getBlockEntity(mutatorPos, MutatorBlockEntity.class);
            if (mutator == null) {
                helper.fail("Expected MutatorBlockEntity");
                return;
            }
            long startSize = 1000;
            mutator.getBacteriaStorage().setBacteria(0, colony(NTBacterias.LITHOPHILES, startSize, stats(1f, 0f, 1f, 2000), 0));
            mutator.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.BONE_MEAL, 4));

            helper.runAfterDelay(320, () -> {
                BacteriaInstance input = mutator.getBacteriaStorage().getBacteria(0);
                BacteriaInstance output = mutator.getBacteriaStorage().getBacteria(1);

                helper.assertTrue(input.getSize() <= startSize, "A mutation attempt should never grow the input colony");

                if (!output.isEmpty()) {
                    helper.assertValueEqual(NTBacterias.CALCIOPHILES, output.getBacteria(), "converted colony");
                    helper.assertFalse(output.isAnalyzed(), "A converted colony should start unanalyzed");
                    helper.assertTrue(input.isEmpty(), "A successful conversion should consume the whole input colony");
                } else {
                    helper.assertFalse(input.isEmpty(), "A failed attempt should keep the input colony");
                    helper.assertTrue(input.getSize() < startSize, "A failed attempt should shrink the input colony");
                }
                helper.succeed();
            });
        });

        r.add("bacteria/fuel_cell_output_formulas", 40, helper -> helper.runAfterDelay(1, () -> {
            float cap = (float) NTConfig.bacteriaMutationResistanceCap;

            helper.assertValueEqual(24, BacterialFuelCellBlockEntity.powerOutput(
                    colony(NTBacterias.FERROPHILES, 1000, stats(1f, 0f, 1.0f, 2000), 0)), "power at production rate 1.0");
            helper.assertValueEqual(48, BacterialFuelCellBlockEntity.powerOutput(
                    colony(NTBacterias.FERROPHILES, 1000, stats(1f, 0f, 2.0f, 2000), 0)), "power at production rate 2.0");
            helper.assertValueEqual(0, BacterialFuelCellBlockEntity.powerOutput(BacteriaInstance.EMPTY), "power with no colony");

            assertNear(helper, 0f, BacterialFuelCellBlockEntity.purityOutput(
                    colony(NTBacterias.FERROPHILES, 1000, stats(1f, 0f, 1f, 2000), 0)), "purity without resistance");
            assertNear(helper, (float) NTConfig.fuelCellMaxPurity, BacterialFuelCellBlockEntity.purityOutput(
                    colony(NTBacterias.FERROPHILES, 1000, stats(1f, cap, 1f, 2000), 0)), "purity at max resistance");
            helper.assertTrue(BacterialFuelCellBlockEntity.purityOutput(
                            colony(NTBacterias.FERROPHILES, 1000, stats(1f, cap, 1f, 2000), 0)) < 3f,
                    "Fuel cell purity should stay under the prismarine crystal");

            assertNear(helper, 0.5f, BacterialFuelCellBlockEntity.burnPerTick(
                    colony(NTBacterias.FERROPHILES, 1000, stats(1f, 0f, 1.0f, 2000), 0)), "burn at production rate 1.0");
            assertNear(helper, 1.0f, BacterialFuelCellBlockEntity.burnPerTick(
                    colony(NTBacterias.FERROPHILES, 1000, stats(1f, 0f, 2.0f, 2000), 0)), "burn at production rate 2.0");
            helper.succeed();
        }));

        r.add("bacteria/fuel_cell_powers_a_machine", 200, helper -> {
            BlockPos cellPos = new BlockPos(4, 1, 4);
            BlockPos incubatorPos = new BlockPos(4, 4, 4);
            helper.setBlock(cellPos, NTBlocks.BACTERIAL_FUEL_CELL.get().defaultBlockState()
                    .setValue(BlockStateProperties.FACING, Direction.DOWN));
            helper.setBlock(incubatorPos, NTBlocks.INCUBATOR.get().defaultBlockState());

            BacterialFuelCellBlockEntity cell = helper.getBlockEntity(cellPos, BacterialFuelCellBlockEntity.class);
            IncubatorBlockEntity incubator = helper.getBlockEntity(incubatorPos, IncubatorBlockEntity.class);
            if (cell == null || incubator == null) {
                helper.fail("Expected a fuel cell and an incubator");
                return;
            }
            long startSize = 20_000;
            cell.getBacteriaStorage().setBacteria(0, colony(NTBacterias.FERROPHILES, startSize,
                    stats(1f, (float) NTConfig.bacteriaMutationResistanceCap, 1.0f, 2000), 0));
            incubator.getBacteriaStorage().setBacteria(0, colony(NTBacterias.LITHOPHILES, 1000, stats(1f, 0f, 1f, 2000), 0));
            incubator.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.STONE, 8));

            helper.runAfterDelay(150, () -> {
                helper.assertValueEqual(24, incubator.getPower(), "power delivered to the incubator");
                assertNear(helper, (float) NTConfig.fuelCellMaxPurity, incubator.getPurity(), "purity delivered to the incubator");

                long remaining = cell.getBacteriaStorage().getBacteria(0).getSize();
                long burned = startSize - remaining;
                helper.assertTrue(burned >= 50 && burned <= 90,
                        "Fuel cell should have burned about 70 colony over 140 ticks, burned " + burned);
                helper.assertTrue(incubator.getBacteriaStorage().getBacteria(0).getSize() > 1000,
                        "The incubator should have grown its colony on fuel cell power");
                helper.succeed();
            });
        });

        r.add("bacteria/fuel_cell_burns_out_and_stops", 220, helper -> {
            BlockPos cellPos = new BlockPos(4, 1, 4);
            BlockPos incubatorPos = new BlockPos(4, 4, 4);
            helper.setBlock(cellPos, NTBlocks.BACTERIAL_FUEL_CELL.get().defaultBlockState()
                    .setValue(BlockStateProperties.FACING, Direction.DOWN));
            helper.setBlock(incubatorPos, NTBlocks.INCUBATOR.get().defaultBlockState());

            BacterialFuelCellBlockEntity cell = helper.getBlockEntity(cellPos, BacterialFuelCellBlockEntity.class);
            IncubatorBlockEntity incubator = helper.getBlockEntity(incubatorPos, IncubatorBlockEntity.class);
            if (cell == null || incubator == null) {
                helper.fail("Expected a fuel cell and an incubator");
                return;
            }
            cell.getBacteriaStorage().setBacteria(0, colony(NTBacterias.FERROPHILES, 30, stats(1f, 0f, 1.0f, 2000), 0));

            helper.runAfterDelay(30, () -> helper.assertTrue(cell.getPowerToTransfer() > 0,
                    "Fuel cell should be emitting while it still has colony"));

            helper.runAfterDelay(200, () -> {
                helper.assertTrue(cell.getBacteriaStorage().getBacteria(0).isEmpty(), "Fuel cell should have burned its colony away");
                helper.assertTrue(cell.getLaserOutputs().isEmpty(), "A spent fuel cell should stop emitting");
                helper.assertValueEqual(0, incubator.getPower(), "power after the fuel cell burned out");
                helper.succeed();
            });
        });

        r.add("bacteria/fuel_cell_holds_fuel_without_a_target", 120, helper -> {
            BlockPos cellPos = new BlockPos(4, 1, 4);
            helper.setBlock(cellPos, NTBlocks.BACTERIAL_FUEL_CELL.get().defaultBlockState()
                    .setValue(BlockStateProperties.FACING, Direction.DOWN));

            BacterialFuelCellBlockEntity cell = helper.getBlockEntity(cellPos, BacterialFuelCellBlockEntity.class);
            if (cell == null) {
                helper.fail("Expected BacterialFuelCellBlockEntity");
                return;
            }
            cell.getBacteriaStorage().setBacteria(0, colony(NTBacterias.FERROPHILES, 500, stats(1f, 0f, 2.0f, 2000), 0));

            helper.runAfterDelay(100, () -> {
                helper.assertValueEqual(500L, cell.getBacteriaStorage().getBacteria(0).getSize(),
                        "colony size with nothing to power");
                helper.succeed();
            });
        });

        r.add("bacteria/analyzer_completes_and_resets", 200, helper -> {
            BlockPos analyzerPos = new BlockPos(4, 3, 4);
            BlockPos sourcePos = new BlockPos(4, 1, 4);
            helper.setBlock(analyzerPos, NTBlocks.BACTERIAL_ANALYZER.get().defaultBlockState());
            placeShieldedSource(helper, sourcePos, Direction.UP);

            BacterialAnalyzerBlockEntity analyzer = helper.getBlockEntity(analyzerPos, BacterialAnalyzerBlockEntity.class);
            if (analyzer == null) {
                helper.fail("Expected BacterialAnalyzerBlockEntity");
                return;
            }
            ItemStack dish = new ItemStack(NTItems.PETRI_DISH.get());
            dish.set(NTDataComponents.BACTERIA, new ComponentBacteriaStorage(
                    BacteriaInstance.roll(NTBacterias.LITHOPHILES, helper.getLevel().registryAccess())));
            analyzer.getItemStackHandler().setStackInSlot(0, dish);

            helper.runAfterDelay(150, () -> {
                helper.assertTrue(analyzer.getItemStackHandler().getStackInSlot(0).isEmpty(), "Input slot should be empty");
                ItemStack result = analyzer.getItemStackHandler().getStackInSlot(1);
                helper.assertTrue(result.is(NTItems.PETRI_DISH.get()), "Result slot should hold the petri dish");
                ComponentBacteriaStorage component = result.get(NTDataComponents.BACTERIA);
                helper.assertTrue(component != null, "Result dish lost its bacteria component");
                helper.assertTrue(component.bacteriaInstance().isAnalyzed(), "Result dish should be analyzed");
                helper.assertValueEqual(0, analyzer.getProgress(), "Analyzer progress after completing");
                helper.succeed();
            });
        });
    }

    private static void assertNear(GameTestHelper helper, float expected, float actual, String what) {
        helper.assertTrue(Math.abs(expected - actual) < EPSILON,
                what + ": expected " + expected + " but was " + actual);
    }
}
