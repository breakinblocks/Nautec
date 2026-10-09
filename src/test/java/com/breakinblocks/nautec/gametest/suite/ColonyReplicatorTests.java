package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.content.bacteria.ProductNutrients;
import com.breakinblocks.nautec.content.bacteria.SimpleCollapsedStats;
import com.breakinblocks.nautec.content.blockentities.ColonyReplicatorBlockEntity;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ColonyReplicatorTests {
    private static final BlockPos REPLICATOR = new BlockPos(4, 1, 4);

    private ColonyReplicatorTests() {
    }

    private static BacteriaInstance colony(GameTestHelper helper, ResourceKey<Bacteria> strain, float resistance, long size, boolean analyzed) {
        BacteriaInstance rolled = BacteriaInstance.roll(strain, helper.getLevel().registryAccess());
        SimpleCollapsedStats base = (SimpleCollapsedStats) rolled.getStats();
        SimpleCollapsedStats stats = new SimpleCollapsedStats(base.baseStats(), base.growthRate(), resistance, base.productionRate(), base.lifespan(), base.color());
        return new BacteriaInstance(strain, size, stats, analyzed, 0);
    }

    private static SimpleCollapsedStats stats(float growth, float resistance, float production, int lifespan, SimpleCollapsedStats like) {
        return new SimpleCollapsedStats(like.baseStats(), growth, resistance, production, lifespan, like.color());
    }

    private static ColonyReplicatorBlockEntity place(GameTestHelper helper) {
        helper.setBlock(REPLICATOR, NTBlocks.COLONY_REPLICATOR.get());
        return helper.getBlockEntity(REPLICATOR, ColonyReplicatorBlockEntity.class);
    }

    private static void run(GameTestHelper helper, ColonyReplicatorBlockEntity replicator, int power, float purity, int ticks) {
        BlockPos origin = helper.absolutePos(REPLICATOR.above());
        for (int tick = 0; tick < ticks; tick++) {
            replicator.receivePower(power, Direction.UP, origin);
            replicator.receiveNewPurity(purity, Direction.UP, origin);
            replicator.commonTick();
        }
    }

    private static void feed(GameTestHelper helper, ColonyReplicatorBlockEntity replicator, ResourceKey<Bacteria> strain, long size) {
        IBacteriaStorage storage = replicator.getBacteriaStorage();
        storage.setBacteria(ColonyReplicatorBlockEntity.FODDER, colony(helper, strain, 0F, size, false));
        replicator.commonTick();
    }

    public static void register(NTTestRegistrar r) {
        r.add("replicator/product_items_become_biomass_and_copies_start_large", 20, helper -> {
            ColonyReplicatorBlockEntity replicator = place(helper);
            IBacteriaStorage storage = replicator.getBacteriaStorage();
            storage.setBacteria(ColonyReplicatorBlockEntity.TEMPLATE,
                    colony(helper, NTBacterias.FERROPHILES, NTConfig.bacteriaMutationResistanceCap, 5_000, true));
            Bacteria strain = BacteriaHelper.getBacteria(helper.getLevel().registryAccess(), NTBacterias.FERROPHILES);
            long ingot = ProductNutrients.biomass(strain, new ItemStack(Items.IRON_INGOT));
            long block = ProductNutrients.biomass(strain, new ItemStack(Items.IRON_BLOCK));
            helper.assertTrue(ingot > NTConfig.replicatorBiomassPerItem, "iron is rarer than stone, so an ingot gives more than the base amount");
            helper.assertValueEqual(block, ingot * 9, "a block gives nine ingots' worth");
            helper.assertValueEqual(ProductNutrients.biomass(strain, new ItemStack(Items.STONE)), 0L, "other items are not fodder");

            replicator.getItemStackHandler().setStackInSlot(ColonyReplicatorBlockEntity.FODDER_ITEM, new ItemStack(Items.IRON_INGOT, 3));
            replicator.commonTick();
            helper.assertValueEqual(replicator.getBiomass(), ingot * 3, "three ingots become biomass");
            helper.assertTrue(replicator.getItemStackHandler().getStackInSlot(ColonyReplicatorBlockEntity.FODDER_ITEM).isEmpty(), "the ingots are used up");

            int blocks = (int) ((NTConfig.replicatorBiomassCost + block - 1) / block);
            replicator.getItemStackHandler().setStackInSlot(ColonyReplicatorBlockEntity.FODDER_ITEM, new ItemStack(Items.IRON_BLOCK, blocks));
            run(helper, replicator, NTConfig.replicatorPowerUsage, 3F, NTConfig.replicatorDuration + 2);
            BacteriaInstance copy = storage.getBacteria(ColonyReplicatorBlockEntity.RESULT);
            helper.assertFalse(copy.isEmpty(), "iron blocks alone pay for a copy");
            helper.assertValueEqual(copy.getSize(), Math.round(NTConfig.replicatorBiomassCost * NTConfig.replicatorCopySize), "the copy starts large");
            helper.succeed();
        });

        r.add("replicator/perfect_copy_at_resistance_cap", 20, helper -> {
            ColonyReplicatorBlockEntity replicator = place(helper);
            IBacteriaStorage storage = replicator.getBacteriaStorage();
            BacteriaInstance template = colony(helper, NTBacterias.LITHOPHILES, NTConfig.bacteriaMutationResistanceCap, 30_000, true);
            storage.setBacteria(ColonyReplicatorBlockEntity.TEMPLATE, template.copy());
            long half = NTConfig.replicatorBiomassCost / 2;
            feed(helper, replicator, NTBacterias.LITHOPHILES, half);
            feed(helper, replicator, NTBacterias.LITHOPHILES, NTConfig.replicatorBiomassCost - half);
            helper.assertTrue(storage.getBacteria(ColonyReplicatorBlockEntity.FODDER).isEmpty(), "fodder is broken down into biomass");
            helper.assertValueEqual(replicator.getBiomass(), NTConfig.replicatorBiomassCost, "biomass adds up by size");
            run(helper, replicator, NTConfig.replicatorPowerUsage, (float) NTConfig.replicatorPurity, NTConfig.replicatorDuration + 1);
            BacteriaInstance result = storage.getBacteria(ColonyReplicatorBlockEntity.RESULT);
            helper.assertTrue(result.is(NTBacterias.LITHOPHILES), "a copy is made");
            helper.assertTrue(result.getStats().equals(template.getStats()), "a capped resistance template copies perfectly");
            helper.assertTrue(result.isAnalyzed(), "the copy is analyzed");
            helper.assertTrue(result.getSize() < template.getSize(), "the copy starts small");
            helper.assertTrue(storage.getBacteria(ColonyReplicatorBlockEntity.TEMPLATE).getStats().equals(template.getStats()),
                    "the template is kept");
            helper.assertValueEqual(replicator.getBiomass(), 0L, "the copy uses the biomass");
            helper.succeed();
        });

        r.add("replicator/copy_errors_only_lower_and_shrink_with_resistance", 20, helper -> {
            SimpleCollapsedStats like = (SimpleCollapsedStats) colony(helper, NTBacterias.LITHOPHILES, 0F, 100, true).getStats();
            RandomSource random = RandomSource.create(42);
            int weakErrors = 0;
            int strongErrors = 0;
            SimpleCollapsedStats weak = stats(4F, 0F, 1.5F, 20_000, like);
            SimpleCollapsedStats strong = stats(4F, NTConfig.bacteriaMutationResistanceCap * 0.9F, 1.5F, 20_000, like);
            for (int i = 0; i < 500; i++) {
                SimpleCollapsedStats a = ColonyReplicatorBlockEntity.copy(weak, random);
                SimpleCollapsedStats b = ColonyReplicatorBlockEntity.copy(strong, random);
                helper.assertTrue(a.growthRate() <= weak.growthRate() && a.productionRate() <= weak.productionRate()
                        && a.lifespan() <= weak.lifespan() && a.mutationResistance() <= weak.mutationResistance(), "a copy is never better");
                helper.assertTrue(a.productionRate() >= weak.productionRate() * 0.9F - 0.0001F, "an error costs at most a tenth");
                if (!a.equals(weak)) {
                    weakErrors++;
                }
                if (!b.equals(strong)) {
                    strongErrors++;
                }
            }
            helper.assertTrue(weakErrors > strongErrors * 3, "resistance makes errors rarer: " + weakErrors + " vs " + strongErrors);
            helper.succeed();
        });

        r.add("replicator/splice_takes_from_both_parents", 20, helper -> {
            SimpleCollapsedStats like = (SimpleCollapsedStats) colony(helper, NTBacterias.LITHOPHILES, 0F, 100, true).getStats();
            SimpleCollapsedStats fast = stats(4.5F, 0.2F, 0.5F, 2_000, like);
            SimpleCollapsedStats rich = stats(1F, 0.8F, 1.8F, 20_000, like);
            double chance = NTConfig.replicatorSpliceChance;
            try {
                NTConfig.replicatorSpliceChance = 1.0;
                SimpleCollapsedStats child = ColonyReplicatorBlockEntity.splice(fast, rich, RandomSource.create(1));
                helper.assertTrue(child.equals(stats(4.5F, 0.8F, 1.8F, 20_000, like)), "a certain splice takes the best of each stat");
                NTConfig.replicatorSpliceChance = 0.5;
                RandomSource random = RandomSource.create(7);
                for (int i = 0; i < 200; i++) {
                    SimpleCollapsedStats mixed = ColonyReplicatorBlockEntity.splice(fast, rich, random);
                    helper.assertTrue((mixed.growthRate() == 4.5F || mixed.growthRate() == 1F) && (mixed.lifespan() == 2_000 || mixed.lifespan() == 20_000),
                            "every stat comes from one parent");
                }
            } finally {
                NTConfig.replicatorSpliceChance = chance;
            }

            ColonyReplicatorBlockEntity replicator = place(helper);
            IBacteriaStorage storage = replicator.getBacteriaStorage();
            storage.setBacteria(ColonyReplicatorBlockEntity.TEMPLATE, new BacteriaInstance(NTBacterias.LITHOPHILES, 5_000, fast, true, 0));
            replicator.setSplice(true);
            feed(helper, replicator, NTBacterias.LITHOPHILES, NTConfig.replicatorBiomassCost);
            run(helper, replicator, NTConfig.replicatorPowerUsage, (float) NTConfig.replicatorPurity, 3);
            helper.assertValueEqual(replicator.getStatus(), ColonyReplicatorBlockEntity.STATUS_NO_PARTNER, "splice needs a partner");
            storage.setBacteria(ColonyReplicatorBlockEntity.PARTNER, new BacteriaInstance(NTBacterias.LITHOPHILES, 5_000, rich, true, 0));
            run(helper, replicator, NTConfig.replicatorPowerUsage, (float) NTConfig.replicatorPurity, NTConfig.replicatorDuration + 1);
            BacteriaInstance result = storage.getBacteria(ColonyReplicatorBlockEntity.RESULT);
            helper.assertTrue(result.is(NTBacterias.LITHOPHILES), "splice makes a child");
            helper.assertFalse(storage.getBacteria(ColonyReplicatorBlockEntity.PARTNER).isEmpty(), "the partner is kept");
            helper.succeed();
        });

        r.add("replicator/refuses_wrong_strain_unanalyzed_and_weak_beams", 20, helper -> {
            ColonyReplicatorBlockEntity replicator = place(helper);
            IBacteriaStorage storage = replicator.getBacteriaStorage();
            run(helper, replicator, NTConfig.replicatorPowerUsage, 3F, 1);
            helper.assertValueEqual(replicator.getStatus(), ColonyReplicatorBlockEntity.STATUS_NO_TEMPLATE, "empty");
            storage.setBacteria(ColonyReplicatorBlockEntity.TEMPLATE, colony(helper, NTBacterias.LITHOPHILES, 0.5F, 5_000, false));
            run(helper, replicator, NTConfig.replicatorPowerUsage, 3F, 1);
            helper.assertValueEqual(replicator.getStatus(), ColonyReplicatorBlockEntity.STATUS_NOT_ANALYZED, "unanalyzed template");
            storage.setBacteria(ColonyReplicatorBlockEntity.TEMPLATE, colony(helper, NTBacterias.LITHOPHILES, 0.5F, 5_000, true));
            storage.setBacteria(ColonyReplicatorBlockEntity.FODDER, colony(helper, NTBacterias.CYANOBACTERIA, 0F, 30_000, false));
            run(helper, replicator, NTConfig.replicatorPowerUsage, 3F, 1);
            helper.assertValueEqual(replicator.getStatus(), ColonyReplicatorBlockEntity.STATUS_WRONG_STRAIN, "other strains are not fodder");
            helper.assertValueEqual(replicator.getBiomass(), 0L, "wrong strain fodder is left alone");
            storage.setBacteria(ColonyReplicatorBlockEntity.FODDER, BacteriaInstance.EMPTY);
            run(helper, replicator, NTConfig.replicatorPowerUsage, 3F, 1);
            helper.assertValueEqual(replicator.getStatus(), ColonyReplicatorBlockEntity.STATUS_NO_BIOMASS, "no fodder");
            feed(helper, replicator, NTBacterias.LITHOPHILES, NTConfig.replicatorBiomassCost);
            run(helper, replicator, NTConfig.replicatorPowerUsage - 1, 3F, 3);
            helper.assertValueEqual(replicator.getStatus(), ColonyReplicatorBlockEntity.STATUS_LOW_POWER, "weak beam");
            run(helper, replicator, NTConfig.replicatorPowerUsage, (float) NTConfig.replicatorPurity - 0.1F, 3);
            helper.assertValueEqual(replicator.getStatus(), ColonyReplicatorBlockEntity.STATUS_LOW_PURITY, "impure beam");
            helper.assertTrue(storage.getBacteria(ColonyReplicatorBlockEntity.RESULT).isEmpty(), "nothing is made");
            storage.setBacteria(ColonyReplicatorBlockEntity.TEMPLATE, colony(helper, NTBacterias.CYANOBACTERIA, 0.5F, 5_000, true));
            replicator.commonTick();
            helper.assertValueEqual(replicator.getBiomass(), 0L, "biomass belongs to one strain and is lost when the template changes");
            helper.succeed();
        });
    }
}
