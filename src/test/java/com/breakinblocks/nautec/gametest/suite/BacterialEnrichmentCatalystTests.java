package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.DishPort;
import com.breakinblocks.nautec.content.blockentities.BacterialEnrichmentCatalystBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.CrystalPurity;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalBlockEntity;
import com.breakinblocks.nautec.content.blocks.BacterialEnrichmentCatalystBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalBlock;
import com.breakinblocks.nautec.content.enrichment.EnrichmentRules;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public final class BacterialEnrichmentCatalystTests {
    private static final BlockPos TARGET_CORE = new BlockPos(3, 4, 4);
    private static final BlockPos MACHINE = new BlockPos(4, 1, 4);
    private static final BlockPos SOURCE_CORE = new BlockPos(4, 5, 4);

    private BacterialEnrichmentCatalystTests() {
    }

    private static PrismarineCrystalBlockEntity crystal(NTGameTestHelper helper, BlockPos core, boolean cultivated, float purity) {
        PrismarineCrystalBlock.build(helper.getLevel(), helper.absolutePos(core), cultivated);
        PrismarineCrystalBlockEntity crystal = helper.getBlockEntity(core, PrismarineCrystalBlockEntity.class);
        crystal.setCrystalPurity(purity);
        return crystal;
    }

    private static BacterialEnrichmentCatalystBlockEntity machine(NTGameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(pos, NTBlocks.BACTERIAL_ENRICHMENT_CATALYST.get().defaultBlockState()
                .setValue(BacterialEnrichmentCatalystBlock.FACING, facing));
        return helper.getBlockEntity(pos, BacterialEnrichmentCatalystBlockEntity.class);
    }

    private static BacterialEnrichmentCatalystBlockEntity machine(NTGameTestHelper helper) {
        return machine(helper, MACHINE, Direction.EAST);
    }

    private static BacteriaInstance colony(NTGameTestHelper helper, long size, boolean maxed) {
        BacteriaInstance colony = maxed
                ? BacteriaInstance.withMaxStats(NTBacterias.PRISMATOPHILES, helper.getLevel().registryAccess())
                : BacteriaInstance.roll(NTBacterias.PRISMATOPHILES, helper.getLevel().registryAccess());
        colony.setSize(size);
        return colony;
    }

    private static void load(NTGameTestHelper helper, BacterialEnrichmentCatalystBlockEntity machine, long size) {
        machine.getBacteriaStorage().setBacteria(0, colony(helper, size, false));
    }

    private static void feed(BacterialEnrichmentCatalystBlockEntity machine, int power, float purity) {
        machine.setPowerPerSide(Direction.DOWN, power);
        machine.setPurityPerSide(Direction.DOWN, purity);
        machine.commonTick();
    }

    private static void near(NTGameTestHelper helper, double actual, double expected, double tolerance, String what) {
        helper.assertTrue(Math.abs(actual - expected) <= tolerance, what + ": expected " + expected + ", got " + actual);
    }

    public static void register(NTTestRegistrar r) {
        r.add("enrichment/cycle_ap_grows_with_purity", 20, h -> {
            h.assertValueEqual(EnrichmentRules.cycleAp(6f), (long) NTConfig.enrichmentBaseAp, "base cost at purity 6");
            near(h, EnrichmentRules.cycleAp(7f), NTConfig.enrichmentBaseAp * NTConfig.enrichmentApGrowth, 1, "one point higher");
            near(h, EnrichmentRules.cycleAp(12f), NTConfig.enrichmentBaseAp * Math.pow(NTConfig.enrichmentApGrowth, 6), 1, "purity 12");
            h.assertTrue(EnrichmentRules.cycleAp(4f) < EnrichmentRules.cycleAp(6f), "cheaper below 6");
            h.succeed();
        });
        r.add("enrichment/colony_cost_scales", 20, h -> {
            long base = EnrichmentRules.colonyCost(6f, 6f, 0f);
            h.assertValueEqual(base, (long) NTConfig.enrichmentColonyBase, "base colony cost");
            h.assertTrue(EnrichmentRules.colonyCost(8f, 8f, 0f) > base, "rises with purity");
            near(h, EnrichmentRules.colonyCost(7f, 6f, 0f), EnrichmentRules.colonyCost(7f, 7f, 0f) * (1 + NTConfig.enrichmentOverreachColony), 1, "overreach costs more");
            near(h, EnrichmentRules.colonyCost(6f, 6f, 1f), base * (1 - NTConfig.enrichmentResistanceSaving), 1, "resistance saves colony");
            BacteriaInstance maxed = colony(h, 1000, true);
            near(h, EnrichmentRules.resistanceRatio(maxed), 1.0, 0.0001, "max stats reach the resistance cap");
            h.succeed();
        });
        r.add("enrichment/regression_chance_curve", 20, h -> {
            near(h, EnrichmentRules.regressionChance(6f, 8f), NTConfig.enrichmentMinChance, 0.0001, "source well above the crystal");
            near(h, EnrichmentRules.regressionChance(6f, 6f), NTConfig.enrichmentParityChance, 0.0001, "parity");
            near(h, EnrichmentRules.regressionChance(7.5f, 6f), NTConfig.enrichmentMaxChance, 0.0001, "at the overreach limit");
            float previous = 0f;
            for (int step = -20; step <= 15; step++) {
                float chance = EnrichmentRules.regressionChance(6f + step / 10f, 6f);
                h.assertTrue(chance >= previous - 0.00001f, "chance never falls as overreach rises, step " + step);
                previous = chance;
            }
            h.succeed();
        });
        r.add("enrichment/success_and_regress_clamp", 20, h -> {
            near(h, EnrichmentRules.success(6f, 6f, 12f), 6f + NTConfig.enrichmentStep, 0.0001, "one step up");
            near(h, EnrichmentRules.success(7.45f, 6f, 12f), 6f + NTConfig.enrichmentMaxOverreach, 0.0001, "stops at the overreach limit");
            near(h, EnrichmentRules.success(6.95f, 9f, 7f), 7f, 0.0001, "stops at the target");
            near(h, EnrichmentRules.success(11.95f, 12f, 12f), EnrichmentRules.maxPurity(), 0.0001, "stops at the cap");
            near(h, EnrichmentRules.regress(6.5f), 6.5f - NTConfig.enrichmentRegression, 0.0001, "regression");
            near(h, EnrichmentRules.regress(4.1f), EnrichmentRules.minPurity(), 0.0001, "never below the enrichable floor");
            h.assertTrue(EnrichmentRules.overreached(7.5f, 6f), "refuses at the limit");
            h.assertFalse(EnrichmentRules.overreached(7.4f, 6f), "works under the limit");
            h.succeed();
        });
        r.add("enrichment/finds_crystal_from_any_block", 20, h -> {
            PrismarineCrystalBlockEntity target = crystal(h, TARGET_CORE, true, 5f);
            for (int offset = -3; offset <= 2; offset++) {
                BlockPos pos = TARGET_CORE.above(offset).east();
                BacterialEnrichmentCatalystBlockEntity machine = machine(h, pos, Direction.EAST);
                h.assertTrue(machine.targetCrystal() == target, "resolves the core from column offset " + offset);
                h.setBlock(pos, Blocks.AIR);
            }
            h.succeed();
        });
        r.add("enrichment/status_explains_problems", 40, h -> {
            BacterialEnrichmentCatalystBlockEntity machine = machine(h);
            feed(machine, 0, 0f);
            h.assertValueEqual(machine.getStatus(), BacterialEnrichmentCatalystBlockEntity.STATUS_NO_CRYSTAL, "nothing behind it");
            PrismarineCrystalBlockEntity target = crystal(h, TARGET_CORE, false, 3f);
            feed(machine, 0, 0f);
            h.assertValueEqual(machine.getStatus(), BacterialEnrichmentCatalystBlockEntity.STATUS_WILD_CRYSTAL, "wild crystal");
            target.setCultivated(true);
            target.setCrystalPurity(3.5f);
            feed(machine, 0, 0f);
            h.assertValueEqual(machine.getStatus(), BacterialEnrichmentCatalystBlockEntity.STATUS_TOO_IMPURE, "below the enrichable floor");
            target.setCrystalPurity(6f);
            feed(machine, 0, 0f);
            h.assertValueEqual(machine.getStatus(), BacterialEnrichmentCatalystBlockEntity.STATUS_NO_SOURCE, "no beam");
            feed(machine, NTConfig.enrichmentMinPower - 1, 6f);
            h.assertValueEqual(machine.getStatus(), BacterialEnrichmentCatalystBlockEntity.STATUS_LOW_POWER, "weak beam");
            h.assertValueEqual(machine.getProgress(), 0L, "a weak beam adds nothing");
            feed(machine, NTConfig.enrichmentMinPower, 6f);
            h.assertValueEqual(machine.getStatus(), BacterialEnrichmentCatalystBlockEntity.STATUS_NO_COLONY, "no colony");
            load(h, machine, 1);
            feed(machine, NTConfig.enrichmentMinPower, 6f);
            h.assertValueEqual(machine.getStatus(), BacterialEnrichmentCatalystBlockEntity.STATUS_SMALL_COLONY, "colony smaller than one cycle");
            h.assertValueEqual(machine.getProgress(), 0L, "no progress without enough colony");
            load(h, machine, 10_000);
            feed(machine, NTConfig.enrichmentMinPower, 4.4f);
            h.assertValueEqual(machine.getStatus(), BacterialEnrichmentCatalystBlockEntity.STATUS_OVERREACH, "crystal too far above the source");
            machine.setTargetPurity(6f);
            feed(machine, NTConfig.enrichmentMinPower, 6f);
            h.assertValueEqual(machine.getStatus(), BacterialEnrichmentCatalystBlockEntity.STATUS_AT_TARGET, "target reached");
            machine.setTargetPurity(12f);
            target.setCrystalPurity(12f);
            feed(machine, NTConfig.enrichmentMinPower, 12f);
            h.assertValueEqual(machine.getStatus(), BacterialEnrichmentCatalystBlockEntity.STATUS_AT_CAP, "cap reached");
            target.setCrystalPurity(6f);
            machine.setPaused(true);
            feed(machine, NTConfig.enrichmentMinPower, 6f);
            h.assertValueEqual(machine.getStatus(), BacterialEnrichmentCatalystBlockEntity.STATUS_PAUSED, "redstone pause");
            h.assertValueEqual(machine.getProgress(), 0L, "paused machine makes no progress");
            machine.setPaused(false);
            feed(machine, NTConfig.enrichmentMinPower, 6f);
            h.assertValueEqual(machine.getStatus(), BacterialEnrichmentCatalystBlockEntity.STATUS_RUNNING, "everything in place");
            h.assertTrue(h.getBlockState(MACHINE).getValue(BacterialEnrichmentCatalystBlock.ACTIVE), "active state follows running");
            h.succeed();
        });
        r.add("enrichment/progress_counts_every_ap", 20, h -> {
            crystal(h, TARGET_CORE, true, 6f);
            BacterialEnrichmentCatalystBlockEntity machine = machine(h);
            load(h, machine, 10_000);
            for (int tick = 0; tick < 5; tick++) {
                feed(machine, 1_000, 6f);
            }
            h.assertValueEqual(machine.getProgress(), 5_000L, "progress is the sum of beam power");
            h.assertTrue(machine.beamSpeed() == 1_000f / NTConfig.enrichmentMinPower, "speed readout is linear in power");
            h.succeed();
        });
        r.add("enrichment/cycle_completes_from_beam_power", 20, h -> {
            int savedBase = NTConfig.enrichmentBaseAp;
            double savedMax = NTConfig.enrichmentMaxChance;
            double savedParity = NTConfig.enrichmentParityChance;
            double savedMin = NTConfig.enrichmentMinChance;
            NTConfig.enrichmentBaseAp = 3_000;
            NTConfig.enrichmentMaxChance = 0;
            NTConfig.enrichmentParityChance = 0;
            NTConfig.enrichmentMinChance = 0;
            try {
                PrismarineCrystalBlockEntity target = crystal(h, TARGET_CORE, true, 6f);
                BacterialEnrichmentCatalystBlockEntity machine = machine(h);
                load(h, machine, 10_000);
                long cost = EnrichmentRules.colonyCost(6f, 6f, EnrichmentRules.resistanceRatio(machine.getBacteriaStorage().getBacteria(0)));
                feed(machine, 1_000, 6f);
                feed(machine, 1_000, 6f);
                near(h, target.getCrystalPurity(), 6f, 0.0001, "no change before the cycle finishes");
                feed(machine, 1_000, 6f);
                near(h, target.getCrystalPurity(), 6f + NTConfig.enrichmentStep, 0.0001, "a finished cycle raises purity");
                h.assertValueEqual(machine.getBacteriaStorage().getBacteria(0).getSize(), 10_000L - cost, "the cycle spends colony");
                h.assertValueEqual(machine.getProgress(), 0L, "progress starts over");
                h.assertValueEqual(machine.getLastOutcome(), BacterialEnrichmentCatalystBlockEntity.OUTCOME_SUCCESS, "outcome recorded");
            } finally {
                NTConfig.enrichmentBaseAp = savedBase;
                NTConfig.enrichmentMaxChance = savedMax;
                NTConfig.enrichmentParityChance = savedParity;
                NTConfig.enrichmentMinChance = savedMin;
            }
            h.succeed();
        });
        r.add("enrichment/forced_rolls_raise_and_regress", 20, h -> {
            PrismarineCrystalBlockEntity target = crystal(h, TARGET_CORE, true, 6.5f);
            BacterialEnrichmentCatalystBlockEntity machine = machine(h);
            load(h, machine, 20_000);
            feed(machine, 1_000, 6.5f);
            h.assertTrue(machine.completeCycle(h.getLevel(), target, 0.999f), "a high roll succeeds");
            near(h, target.getCrystalPurity(), 6.5f + NTConfig.enrichmentStep, 0.0001, "success adds a step");
            machine.setPowerPerSide(Direction.DOWN, 1_000);
            machine.setPurityPerSide(Direction.DOWN, 6.5f);
            machine.commonTick();
            h.assertFalse(machine.completeCycle(h.getLevel(), target, 0f), "a zero roll regresses");
            near(h, target.getCrystalPurity(), 6.5f + NTConfig.enrichmentStep - NTConfig.enrichmentRegression, 0.0001, "regression removes purity");
            h.assertValueEqual(machine.getLastOutcome(), BacterialEnrichmentCatalystBlockEntity.OUTCOME_REGRESSION, "outcome recorded");
            target.setCrystalPurity(4.05f);
            feed(machine, 1_000, 6f);
            machine.completeCycle(h.getLevel(), target, 0f);
            near(h, target.getCrystalPurity(), EnrichmentRules.minPurity(), 0.0001, "regression stops at the enrichable floor");
            h.succeed();
        });
        r.add("enrichment/regression_rate_matches_chance", 40, h -> {
            PrismarineCrystalBlockEntity target = crystal(h, TARGET_CORE, true, 6f);
            BacterialEnrichmentCatalystBlockEntity machine = machine(h);
            feed(machine, 1_000, 6f);
            int regressions = 0;
            int safer = 0;
            int rolls = 2_000;
            for (int i = 0; i < rolls; i++) {
                float roll = h.getLevel().getRandom().nextFloat();
                if (roll < EnrichmentRules.regressionChance(6f, 6f)) {
                    regressions++;
                }
                if (roll < EnrichmentRules.regressionChance(6f, 7f)) {
                    safer++;
                }
            }
            near(h, regressions / (double) rolls, NTConfig.enrichmentParityChance, 0.03, "regression rate at parity");
            h.assertTrue(safer < regressions, "a purer source regresses less often");
            target.setCrystalPurity(6f);
            h.succeed();
        });
        r.add("enrichment/only_foreign_crystal_tips_feed_it", 40, h -> {
            crystal(h, TARGET_CORE, true, 6f);
            PrismarineCrystalBlockEntity source = crystal(h, SOURCE_CORE, true, 5.5f);
            BacterialEnrichmentCatalystBlockEntity machine = machine(h);
            machine.commonTick();
            BlockPos sourceTip = source.getBlockPos().below(3);
            BlockPos ownPart = h.absolutePos(TARGET_CORE).below(3);
            BlockPos plain = h.absolutePos(MACHINE).below();
            h.assertTrue(machine.acceptsSource(sourceTip), "another crystal's tip is accepted");
            h.assertFalse(machine.acceptsSource(ownPart), "the target's own crystal is refused");
            h.assertFalse(machine.acceptsSource(plain), "a non-crystal sender is refused");
            machine.acceptBeam(900, 9f, Direction.UP, plain, LongSets.EMPTY_SET);
            machine.acceptBeam(900, 9f, Direction.UP, ownPart, LongSets.EMPTY_SET);
            machine.commonTick();
            h.assertValueEqual(machine.getPower(), 0, "refused beams add no power");
            machine.acceptBeam(900, 5.5f, Direction.DOWN, sourceTip, LongSets.EMPTY_SET);
            machine.commonTick();
            h.assertValueEqual(machine.getPower(), 900, "the source crystal's beam counts");
            near(h, machine.getPurity(), 5.5f, 0.0001, "the reference purity is the source crystal's");
            h.assertTrue(machine.getLaserInputs().contains(Direction.UP) && machine.getLaserInputs().contains(Direction.DOWN)
                    && machine.getLaserInputs().size() == 2, "inputs are top and bottom only");
            h.assertTrue(machine.getLaserOutputs().isEmpty(), "it emits nothing");
            h.succeed();
        });
        r.add("enrichment/real_source_crystal_drives_it", 200, h -> {
            crystal(h, TARGET_CORE, true, 6f);
            PrismarineCrystalBlockEntity source = crystal(h, SOURCE_CORE, true, 5f);
            BacterialEnrichmentCatalystBlockEntity machine = machine(h);
            load(h, machine, 10_000);
            h.onEachTick(() -> source.receivePower(1_500, Direction.EAST, source.getBlockPos().west(3)));
            h.succeedWhen(() -> {
                h.assertTrue(machine.getPower() > 0, "the crystal's tip beam reaches the machine");
                near(h, machine.getPurity(), 5f, 0.0001, "at the source crystal's purity");
                h.assertTrue(machine.getProgress() > 0, "and it makes progress");
            });
        });
        r.add("enrichment/only_one_machine_controls", 40, h -> {
            crystal(h, TARGET_CORE, true, 6f);
            BacterialEnrichmentCatalystBlockEntity first = machine(h, TARGET_CORE.below(3).east(), Direction.EAST);
            BacterialEnrichmentCatalystBlockEntity second = machine(h, TARGET_CORE.east(), Direction.EAST);
            load(h, first, 10_000);
            load(h, second, 10_000);
            for (int tick = 0; tick < 25; tick++) {
                feed(first, 1_000, 6f);
                feed(second, 1_000, 6f);
            }
            boolean firstOwns = first.getBlockPos().asLong() < second.getBlockPos().asLong();
            BacterialEnrichmentCatalystBlockEntity owner = firstOwns ? first : second;
            BacterialEnrichmentCatalystBlockEntity other = firstOwns ? second : first;
            h.assertValueEqual(owner.getStatus(), BacterialEnrichmentCatalystBlockEntity.STATUS_RUNNING, "the lower position runs");
            h.assertValueEqual(other.getStatus(), BacterialEnrichmentCatalystBlockEntity.STATUS_SHARED, "the other reports the crystal is taken");
            h.succeed();
        });
        r.add("enrichment/only_prismatophiles_load", 40, h -> {
            BacterialEnrichmentCatalystBlockEntity machine = machine(h);
            ItemStack wrong = NTItems.PETRI_DISH.toStack();
            wrong.getCapability(NTCapabilities.BacteriaStorage.ITEM)
                    .setBacteria(0, BacteriaInstance.roll(NTBacterias.SMARAGDOPHILES, h.getLevel().registryAccess()));
            ItemStack right = NTItems.PETRI_DISH.toStack();
            right.getCapability(NTCapabilities.BacteriaStorage.ITEM)
                    .setBacteria(0, colony(h, 500, false));
            h.assertTrue(machine.getItemStackHandler().insertItem(BacterialEnrichmentCatalystBlockEntity.DISH_IN, wrong, true).getCount() == 1,
                    "another strain's dish is refused");
            h.assertTrue(machine.getItemStackHandler().insertItem(BacterialEnrichmentCatalystBlockEntity.DISH_IN, right, false).isEmpty(),
                    "a Prismatophiles dish is taken");
            DishPort.tick(machine, BacterialEnrichmentCatalystBlockEntity.DISH_IN, BacterialEnrichmentCatalystBlockEntity.DISH_EMPTY_OUT,
                    BacterialEnrichmentCatalystBlockEntity.DISH_OUT, new int[]{0}, () -> 0, slot -> {
                    });
            h.assertValueEqual(machine.getBacteriaStorage().getBacteria(0).getSize(), 500L, "the colony is loaded");
            h.assertFalse(machine.getItemStackHandler().getStackInSlot(BacterialEnrichmentCatalystBlockEntity.DISH_EMPTY_OUT).isEmpty(),
                    "the emptied dish moves to the empty slot");
            ItemStack empty = NTItems.PETRI_DISH.toStack();
            h.assertTrue(machine.getItemStackHandler().insertItem(BacterialEnrichmentCatalystBlockEntity.DISH_IN, empty, false).isEmpty(),
                    "an empty dish is taken to unload");
            DishPort.tick(machine, BacterialEnrichmentCatalystBlockEntity.DISH_IN, BacterialEnrichmentCatalystBlockEntity.DISH_EMPTY_OUT,
                    BacterialEnrichmentCatalystBlockEntity.DISH_OUT, new int[]{0}, () -> 0, slot -> {
                    });
            h.assertTrue(machine.getBacteriaStorage().getBacteria(0).isEmpty(), "the colony is unloaded");
            h.assertValueEqual(DishPort.colonyOf(machine.getItemStackHandler().getStackInSlot(BacterialEnrichmentCatalystBlockEntity.DISH_OUT)).getSize(),
                    500L, "into the dish in the colony-out slot");
            h.succeed();
        });
        r.add("enrichment/target_buttons_clamp", 20, h -> {
            BacterialEnrichmentCatalystBlockEntity machine = machine(h);
            near(h, machine.getTargetPurity(), CrystalPurity.MAX, 0.0001, "default target is the cap");
            machine.pressButton(BacterialEnrichmentCatalystBlockEntity.BUTTON_TARGET_UP);
            near(h, machine.getTargetPurity(), EnrichmentRules.maxPurity(), 0.0001, "cannot go above the cap");
            machine.pressButton(BacterialEnrichmentCatalystBlockEntity.BUTTON_TARGET_DOWN);
            machine.pressButton(BacterialEnrichmentCatalystBlockEntity.BUTTON_TARGET_DOWN_FINE);
            near(h, machine.getTargetPurity(), EnrichmentRules.maxPurity() - 0.6f, 0.0001, "coarse and fine steps");
            machine.setTargetPurity(1f);
            near(h, machine.getTargetPurity(), EnrichmentRules.minPurity(), 0.0001, "cannot go below the enrichable floor");
            h.succeed();
        });
        r.add("enrichment/state_survives_reload", 20, h -> {
            crystal(h, TARGET_CORE, true, 6f);
            BacterialEnrichmentCatalystBlockEntity machine = machine(h);
            load(h, machine, 4_321);
            machine.setTargetPurity(8.5f);
            feed(machine, 1_234, 6f);
            CompoundTag tag = machine.saveWithoutMetadata(h.getLevel().registryAccess());
            BacterialEnrichmentCatalystBlockEntity copy = new BacterialEnrichmentCatalystBlockEntity(machine.getBlockPos(), machine.getBlockState());
            copy.loadWithComponents(tag, h.getLevel().registryAccess());
            h.assertValueEqual(copy.getProgress(), 1_234L, "progress");
            near(h, copy.getTargetPurity(), 8.5f, 0.0001, "target");
            h.assertValueEqual(copy.getBacteriaStorage().getBacteria(0).getSize(), 4_321L, "colony");
            h.succeed();
        });
        r.add("crystal/purity_clamps_to_twelve", 20, h -> {
            PrismarineCrystalBlockEntity crystal = crystal(h, TARGET_CORE, true, 13f);
            near(h, crystal.getCrystalPurity(), CrystalPurity.MAX, 0.0001, "clamped to the cap");
            crystal.setCrystalPurity(9.5f);
            CompoundTag tag = crystal.saveWithoutMetadata(h.getLevel().registryAccess());
            PrismarineCrystalBlockEntity copy = new PrismarineCrystalBlockEntity(crystal.getBlockPos(), crystal.getBlockState());
            copy.loadWithComponents(tag, h.getLevel().registryAccess());
            near(h, copy.getCrystalPurity(), 9.5f, 0.0001, "purity above 6 survives a save and load");
            ItemStack stack = PrismarineCrystalBlock.cultivatedItem();
            stack.set(NTDataComponents.CRYSTAL_PURITY, 9.5f);
            CompoundTag saved = (CompoundTag) stack.save(h.getLevel().registryAccess());
            ItemStack parsed = ItemStack.parse(h.getLevel().registryAccess(), saved).orElseThrow();
            near(h, parsed.getOrDefault(NTDataComponents.CRYSTAL_PURITY, 0f), 9.5f, 0.0001, "the item keeps purity above 6");
            h.succeed();
        });
    }
}
