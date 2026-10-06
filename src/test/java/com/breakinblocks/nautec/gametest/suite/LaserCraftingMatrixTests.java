package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.content.blockentities.LaserCraftingMatrixBlockEntity;
import com.breakinblocks.nautec.content.recipes.ItemTransformationRecipe;
import com.breakinblocks.nautec.content.recipes.LaserCraftingRecipe;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;
import java.util.Set;

public final class LaserCraftingMatrixTests {
    private static final BlockPos MATRIX = new BlockPos(4, 2, 4);

    private LaserCraftingMatrixTests() {
    }

    private static LaserCraftingMatrixBlockEntity place(GameTestHelper helper) {
        helper.setBlock(MATRIX, NTBlocks.LASER_CRAFTING_MATRIX.get());
        return helper.getBlockEntity(MATRIX, LaserCraftingMatrixBlockEntity.class);
    }

    private static void run(GameTestHelper helper, LaserCraftingMatrixBlockEntity matrix, int power, float purity, int ticks) {
        BlockPos origin = helper.absolutePos(MATRIX).above();
        for (int i = 0; i < ticks; i++) {
            matrix.receivePower(power, Direction.UP, origin);
            matrix.receiveNewPurity(purity, Direction.UP, origin);
            matrix.commonTick();
        }
    }

    private static int outputCount(LaserCraftingMatrixBlockEntity matrix, ItemStack wanted) {
        int count = 0;
        for (int slot = LaserCraftingMatrixBlockEntity.OUTPUT_START; slot < LaserCraftingMatrixBlockEntity.SLOTS; slot++) {
            ItemStack stack = matrix.getItemStackHandler().getStackInSlot(slot);
            if (ItemStack.isSameItem(stack, wanted)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int insertItem(LaserCraftingMatrixBlockEntity matrix, int slot, ItemStack stack) {
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = matrix.getItemStackHandler().insert(slot, ItemResource.of(stack), stack.getCount(), tx);
            tx.commit();
            return inserted;
        }
    }

    public static void register(NTTestRegistrar r) {
        r.add("laser_crafting_matrix/takes_a_laser_from_the_top_only", 20, helper -> {
            LaserCraftingMatrixBlockEntity matrix = place(helper);
            helper.assertValueEqual(Set.copyOf(matrix.getLaserInputs()), Set.of(Direction.UP), "laser inputs");
            helper.assertTrue(matrix.getLaserOutputs().isEmpty(), "the matrix sends no beam");
            helper.succeed();
        });

        r.add("laser_crafting_matrix/makes_aquarine_steel_on_a_plain_beam", 20, helper -> {
            LaserCraftingMatrixBlockEntity matrix = place(helper);
            matrix.getItemStackHandler().setStackInSlot(0, new ItemStack(NTItems.AQUARINE_STEEL_COMPOUND.get(), 3));
            run(helper, matrix, 10, 0F, 25);
            helper.assertValueEqual(outputCount(matrix, NTItems.AQUARINE_STEEL_INGOT.toStack()), 1, "ingots after one craft");
            helper.assertValueEqual(matrix.getItemStackHandler().getStackInSlot(0).getCount(), 2, "compound left");
            helper.succeed();
        });

        r.add("laser_crafting_matrix/a_pure_beam_picks_the_better_recipe", 20, helper -> {
            LaserCraftingMatrixBlockEntity matrix = place(helper);
            matrix.getItemStackHandler().setStackInSlot(0, new ItemStack(NTItems.AQUARINE_STEEL_COMPOUND.get(), 1));
            run(helper, matrix, 20, 2F, 25);
            helper.assertValueEqual(outputCount(matrix, NTItems.AQUARINE_STEEL_INGOT.toStack()), 2, "ingots from one compound at purity 2");
            helper.succeed();
        });

        r.add("laser_crafting_matrix/low_purity_and_weak_beams_stall", 20, helper -> {
            LaserCraftingMatrixBlockEntity matrix = place(helper);
            matrix.getItemStackHandler().setStackInSlot(0, NTBlocks.CAST_IRON_BLOCK.toStack());
            run(helper, matrix, 100, 1F, 100);
            helper.assertValueEqual(matrix.status(), LaserCraftingMatrixBlockEntity.Status.LOW_PURITY, "status on a beam of purity 1");
            helper.assertValueEqual(matrix.getNeededPurity(), 2.5F, "purity the gear recipe needs");
            run(helper, matrix, 10, 3F, 100);
            helper.assertValueEqual(matrix.status(), LaserCraftingMatrixBlockEntity.Status.LOW_POWER, "status on a 10 AP beam");
            helper.assertValueEqual(matrix.getProgress(), 0, "progress on a weak beam");
            helper.assertValueEqual(outputCount(matrix, NTItems.GEAR.toStack()), 0, "gears while stalled");
            run(helper, matrix, 40, 3F, 90);
            helper.assertValueEqual(outputCount(matrix, NTItems.GEAR.toStack()), 4, "gears once the beam is strong and pure");
            helper.succeed();
        });

        r.add("laser_crafting_matrix/a_stronger_beam_runs_faster", 20, helper -> {
            LaserCraftingMatrixBlockEntity matrix = place(helper);
            matrix.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.PRISMARINE_CRYSTALS, 4));
            run(helper, matrix, 80, 2F, 22);
            helper.assertValueEqual(outputCount(matrix, NTItems.PRISMARINE_CRYSTAL_SHARD.toStack()), 2, "shards after 22 ticks on four times the beam");
            helper.succeed();
        });

        r.add("laser_crafting_matrix/two_fluids_and_items_in_two_of_each_out", 20, helper -> {
            LaserCraftingMatrixBlockEntity matrix = place(helper);
            matrix.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.STRUCTURE_VOID, 2));
            matrix.getItemStackHandler().setStackInSlot(2, new ItemStack(Items.BARRIER));
            matrix.inputTank(0).fill(new FluidStack(Fluids.LAVA, 1000));
            matrix.inputTank(1).fill(new FluidStack(Fluids.WATER, 1000));
            run(helper, matrix, 30, 1F, 12);
            helper.assertValueEqual(outputCount(matrix, new ItemStack(Items.BEDROCK)), 1, "bedrock out");
            helper.assertValueEqual(outputCount(matrix, new ItemStack(Items.COMMAND_BLOCK)), 2, "command blocks out");
            helper.assertValueEqual(matrix.inputTank(0).getFluidAmount(), 750, "lava left");
            helper.assertValueEqual(matrix.inputTank(1).getFluidAmount(), 500, "water left");
            helper.assertTrue(matrix.outputTank(0).getFluid().is(Fluids.LAVA) && matrix.outputTank(0).getFluidAmount() == 100, "first output tank holds 100 mB lava");
            helper.assertTrue(matrix.outputTank(1).getFluid().is(Fluids.WATER) && matrix.outputTank(1).getFluidAmount() == 200, "second output tank holds 200 mB water");
            helper.succeed();
        });

        r.add("laser_crafting_matrix/inputs_stay_unique", 20, helper -> {
            LaserCraftingMatrixBlockEntity matrix = place(helper);
            helper.assertValueEqual(insertItem(matrix, 0, new ItemStack(Items.DIAMOND, 4)), 4, "first stack of diamonds");
            helper.assertValueEqual(insertItem(matrix, 1, new ItemStack(Items.DIAMOND, 4)), 0, "diamonds into a second input slot");
            helper.assertValueEqual(insertItem(matrix, 1, new ItemStack(Items.EMERALD, 4)), 4, "a different item into the second slot");
            helper.assertValueEqual(insertItem(matrix, LaserCraftingMatrixBlockEntity.OUTPUT_START, new ItemStack(Items.EMERALD)), 0, "items into an output slot");
            helper.assertValueEqual(matrix.inputTank(0).fill(new FluidStack(Fluids.WATER, 1000)), 1000, "water into the first tank");
            helper.assertValueEqual(matrix.inputTank(1).fill(new FluidStack(Fluids.WATER, 1000)), 0, "water into the second tank too");
            helper.assertValueEqual(matrix.inputTank(1).fill(new FluidStack(Fluids.LAVA, 1000)), 1000, "lava into the second tank");
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(matrix.outputTank(0).insert(0, FluidResource.of(Fluids.WATER), 1000, tx), 0, "fluid into an output tank");
            }
            helper.succeed();
        });

        r.add("laser_crafting_matrix/a_full_output_stops_the_machine", 20, helper -> {
            LaserCraftingMatrixBlockEntity matrix = place(helper);
            matrix.getItemStackHandler().setStackInSlot(0, new ItemStack(NTItems.AQUARINE_STEEL_COMPOUND.get(), 8));
            for (int slot = LaserCraftingMatrixBlockEntity.OUTPUT_START; slot < LaserCraftingMatrixBlockEntity.SLOTS; slot++) {
                matrix.getItemStackHandler().setStackInSlot(slot, new ItemStack(Items.COBBLESTONE, 64));
            }
            run(helper, matrix, 10, 0F, 40);
            helper.assertValueEqual(matrix.status(), LaserCraftingMatrixBlockEntity.Status.OUTPUT_FULL, "status with every output slot full");
            helper.assertValueEqual(matrix.getItemStackHandler().getStackInSlot(0).getCount(), 8, "compound untouched");
            helper.succeed();
        });

        r.add("laser_crafting_matrix/covers_every_in_world_transformation", 20, helper -> {
            List<LaserCraftingRecipe> matrixRecipes = helper.getLevel().recipeAccess().recipeMap().byType(LaserCraftingRecipe.Type.INSTANCE)
                    .stream().map(RecipeHolder::value).toList();
            for (RecipeHolder<ItemTransformationRecipe> holder : helper.getLevel().recipeAccess().recipeMap().byType(ItemTransformationRecipe.Type.INSTANCE)) {
                ItemTransformationRecipe transformation = holder.value();
                ItemStack input = transformation.ingredient().ingredient().items().findFirst().orElseThrow().value().getDefaultInstance();
                boolean covered = matrixRecipes.stream().anyMatch(recipe -> recipe.ingredients().size() == 1
                        && recipe.ingredients().getFirst().test(input)
                        && recipe.purity() == transformation.purity()
                        && recipe.results().size() == 1
                        && ItemStack.isSameItemSameComponents(recipe.results().getFirst(), transformation.result())
                        && recipe.results().getFirst().getCount() == transformation.result().getCount());
                helper.assertTrue(covered, "no Laser Crafting Matrix recipe for " + holder.id().identifier());
            }
            helper.succeed();
        });

        r.add("laser_crafting_matrix/recipe_json_limits_hold", 20, helper -> {
            String tooMany = """
                    {"ingredients": [{"ingredient": "minecraft:stone"}, {"ingredient": "minecraft:dirt"},
                      {"ingredient": "minecraft:sand"}, {"ingredient": "minecraft:gravel"}],
                     "results": [{"id": "minecraft:stone"}], "power": 1, "duration": 1}""";
            String noOutput = """
                    {"ingredients": [{"ingredient": "minecraft:stone"}], "power": 1, "duration": 1}""";
            String valid = """
                    {"ingredients": [{"ingredient": "minecraft:stone"}], "results": [{"id": "minecraft:dirt"}], "power": 1, "duration": 1}""";
            helper.assertTrue(parse(helper, tooMany).isError(), "four item inputs are rejected");
            helper.assertTrue(parse(helper, noOutput).isError(), "a recipe with no output is rejected");
            helper.assertTrue(parse(helper, valid).isSuccess(), "a minimal recipe loads");
            helper.succeed();
        });
    }

    private static DataResult<LaserCraftingRecipe> parse(GameTestHelper helper, String json) {
        return LaserCraftingRecipe.Serializer.INSTANCE.codec().codec()
                .parse(helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE), JsonParser.parseString(json));
    }
}
