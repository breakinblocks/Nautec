package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.content.bacteria.BacteriaLineage;
import com.breakinblocks.nautec.content.bacteria.ProductNutrients;
import com.breakinblocks.nautec.content.blockentities.MutatorBlockEntity;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BacteriaGuidanceTests {
    private static final int OVERCLOCK_POWER = 100_000;

    private BacteriaGuidanceTests() {
    }

    private static List<BacteriaMutationRecipe> mutations(ServerLevel level) {
        List<BacteriaMutationRecipe> all = new ArrayList<>();
        for (RecipeHolder<BacteriaMutationRecipe> holder : level.getRecipeManager().getAllRecipesFor(BacteriaMutationRecipe.TYPE)) {
            all.add(holder.value());
        }
        all.addAll(ProductNutrients.get(level).mutation());
        return all;
    }

    private static float chance(NTGameTestHelper helper, ResourceKey<Bacteria> from, ResourceKey<Bacteria> to) {
        for (BacteriaMutationRecipe recipe : mutations(helper.getLevel())) {
            if (recipe.inputBacteria().equals(from) && recipe.resultBacteria().equals(to)) {
                return recipe.chance();
            }
        }
        throw helper.assertionException("No mutation from " + from.location() + " to " + to.location());
    }

    private static List<ResourceKey<Bacteria>> route(BacteriaLineage.Path path) {
        List<ResourceKey<Bacteria>> strains = new ArrayList<>();
        for (BacteriaMutationRecipe step : path.steps()) {
            strains.add(step.resultBacteria());
        }
        return strains;
    }

    public static void register(NTTestRegistrar r) {
        r.add("bacteria_guidance/early_mutations_are_three_times_as_likely", 20, helper -> {
            helper.assertValueEqual(15f, chance(helper, NTBacterias.LITHOPHILES, NTBacterias.CARBOPHAGES), "stone to coal");
            helper.assertValueEqual(15f, chance(helper, NTBacterias.METALLOPHILES, NTBacterias.FERROPHILES), "copper to iron");
            helper.assertValueEqual(75f, chance(helper, NTBacterias.HALOBACTERIA, NTBacterias.HALOTROPHS), "halobacteria to kelp");
            helper.assertValueEqual(30f, chance(helper, NTBacterias.RHIZOBACTERIA, NTBacterias.CAROTOPHYLES), "wheat to carrot");
            helper.assertValueEqual(5f, chance(helper, NTBacterias.FERROPHILES, NTBacterias.AURROPHILES), "gold stays rare");
            helper.assertValueEqual(5f, chance(helper, NTBacterias.AURROPHILES, NTBacterias.ADAMANTOPHILES), "diamond stays rare");
            helper.assertValueEqual(5f, chance(helper, NTBacterias.ADAMANTOPHILES, NTBacterias.SMARAGDOPHILES), "emerald stays rare");
            helper.succeed();
        });

        r.add("bacteria_guidance/lineage_finds_the_fastest_route_from_a_graft", 20, helper -> {
            Map<ResourceKey<Bacteria>, BacteriaLineage.Path> paths = BacteriaLineage.fastest(mutations(helper.getLevel()), BacteriaLineage.graftable());
            BacteriaLineage.Path iron = paths.get(NTBacterias.FERROPHILES);
            helper.assertTrue(iron != null, "iron has a route");
            helper.assertValueEqual(NTBacterias.LITHOPHILES, iron.root(), "iron starts from grafted Lithophiles");
            helper.assertValueEqual(List.of(NTBacterias.CARBOPHAGES, NTBacterias.METALLOPHILES, NTBacterias.FERROPHILES), route(iron), "iron route");
            helper.assertValueEqual(6, paths.get(NTBacterias.SMARAGDOPHILES).steps().size(), "emerald is six mutations from Lithophiles");
            helper.assertValueEqual(NTBacterias.HALOTROPHS, paths.get(NTBacterias.LIPOPHILES).root(), "lipids start from grafted Halotrophs");
            helper.assertFalse(paths.containsKey(NTBacterias.LITHOPHILES), "a graftable strain needs no route");
            for (BacteriaLineage.Path path : paths.values()) {
                for (BacteriaMutationRecipe step : path.steps()) {
                    helper.assertFalse(step.refines(), "a route never refines");
                }
            }
            helper.succeed();
        });

        r.add("bacteria_guidance/mutator_counts_failed_tries", 400, helper -> {
            BlockPos pos = new BlockPos(4, 1, 4);
            helper.setBlock(pos, NTBlocks.MUTATOR.get().defaultBlockState());
            MutatorBlockEntity mutator = helper.getBlockEntity(pos, MutatorBlockEntity.class);
            BlockPos abs = helper.absolutePos(pos);
            BacteriaInstance hardy = BacteriaMachineTests.colony(NTBacterias.THERMOPHILES, NTConfig.bacteriaColonySizeCap,
                    BacteriaMachineTests.stats(1f, NTConfig.bacteriaMutationResistanceCap, 1f, 2000), 0);
            mutator.getBacteriaStorage().setBacteria(0, hardy);
            mutator.getBacteriaStorage().onBacteriaChanged(0);
            mutator.getItemStackHandler().setStackInSlot(MutatorBlockEntity.CATALYST, new ItemStack(Items.STONE));
            helper.assertValueEqual(0, mutator.getFailedAttempts(), "no tries before it runs");
            int[] most = new int[1];
            helper.onEachTick(() -> {
                mutator.receivePower(OVERCLOCK_POWER, Direction.UP, abs.above());
                most[0] = Math.max(most[0], mutator.getFailedAttempts());
                if (!mutator.getBacteriaStorage().getBacteria(1).isEmpty()) {
                    helper.assertValueEqual(0, mutator.getFailedAttempts(), "a success clears the count");
                    mutator.getBacteriaStorage().setBacteria(1, BacteriaInstance.EMPTY);
                    mutator.getBacteriaStorage().onBacteriaChanged(1);
                    mutator.getBacteriaStorage().setBacteria(0, hardy);
                    mutator.getBacteriaStorage().onBacteriaChanged(0);
                }
            });
            helper.succeedWhen(() -> {
                helper.assertTrue(most[0] >= 3, "failed tries are counted, most seen " + most[0]);
                helper.assertTrue(mutator.getFailedAttempts() > 0, "waiting for a run of failed tries");
                mutator.getItemStackHandler().setStackInSlot(MutatorBlockEntity.CATALYST, ItemStack.EMPTY);
                helper.assertValueEqual(0, mutator.getFailedAttempts(), "taking the catalyst out clears the count");
            });
        });
    }
}
