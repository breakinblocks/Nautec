package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.recipes.ItemTransformationRecipe;
import com.breakinblocks.nautec.content.recipes.MixingRecipe;
import com.breakinblocks.nautec.content.recipes.PressureForgingRecipe;
import com.breakinblocks.nautec.content.recipes.inputs.ItemTransformationRecipeInput;
import com.breakinblocks.nautec.content.recipes.inputs.MixingRecipeInput;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;

public final class GuaranteedPartTests {
    private GuaranteedPartTests() {
    }

    private static ItemStack craft(GameTestHelper helper, List<ItemStack> grid) {
        CraftingInput input = CraftingInput.of(3, 3, grid);
        Optional<RecipeHolder<CraftingRecipe>> holder = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        return holder.map(recipe -> recipe.value().assemble(input)).orElse(ItemStack.EMPTY);
    }

    private static ItemStack mix(GameTestHelper helper, List<ItemStack> items) {
        MixingRecipeInput input = new MixingRecipeInput(items, new FluidStack(NTFluids.SALT_WATER.getStillFluid(), 1000));
        for (RecipeHolder<MixingRecipe> holder : helper.getLevel().recipeAccess().recipeMap().byType(MixingRecipe.Type.INSTANCE)) {
            if (holder.value().matches(input, helper.getLevel())) {
                return holder.value().assemble(input);
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack of(Item item) {
        return new ItemStack(item);
    }

    public static void register(NTTestRegistrar r) {
        r.add("guaranteed_parts/gears_from_a_pure_beam", 20, helper -> {
            ItemStack block = new ItemStack(NTBlocks.CAST_IRON_BLOCK.get());
            Optional<ItemTransformationRecipe> pure = ItemTransformationRecipe.findBest(helper.getLevel(), new ItemTransformationRecipeInput(block, 2.5F, Integer.MAX_VALUE));
            helper.assertTrue(pure.isPresent(), "a purity 2.5 beam transforms a Cast Iron Block");
            helper.assertTrue(pure.get().result().is(NTItems.GEAR.get()) && pure.get().result().getCount() == 4, "it gives four Gears, got " + pure.get().result());
            Optional<ItemTransformationRecipe> weak = ItemTransformationRecipe.findBest(helper.getLevel(), new ItemTransformationRecipeInput(block, 2.0F, Integer.MAX_VALUE));
            helper.assertTrue(weak.isEmpty() || !weak.get().result().is(NTItems.GEAR.get()), "a purity 2.0 beam does not make Gears");
            helper.succeed();
        });

        r.add("guaranteed_parts/transformation_minimum_power", 5, helper -> {
            ItemTransformationRecipe recipe = new ItemTransformationRecipe(IngredientWithCount.fromItemLike(Items.STONE),
                    new ItemStackTemplate(Items.DIRT, 1), 20, 1.0F, 40);
            ItemStack stone = new ItemStack(Items.STONE);
            helper.assertFalse(recipe.matches(new ItemTransformationRecipeInput(stone, 2.0F, 39), helper.getLevel()), "a 39 AP beam ran a 40 AP recipe");
            helper.assertTrue(recipe.matches(new ItemTransformationRecipeInput(stone, 2.0F, 40), helper.getLevel()), "a 40 AP beam did not run a 40 AP recipe");
            Optional<ItemTransformationRecipe> copper = ItemTransformationRecipe.findBest(helper.getLevel(),
                    new ItemTransformationRecipeInput(new ItemStack(NTItems.AQUARINE_COPPER_COMPOUND.get()), 2.1F, 1));
            helper.assertTrue(copper.isPresent() && copper.get().result().is(NTItems.AQUARINE_COPPER_INGOT.get()) && copper.get().result().getCount() == 4,
                    "a purity 2.1 beam turns Aquarine Copper Compound into 4 ingots");
            helper.succeed();
        });

        r.add("guaranteed_parts/valve_and_whisk_at_the_crafting_table", 20, helper -> {
            ItemStack e = ItemStack.EMPTY;
            ItemStack rod = of(NTItems.CAST_IRON_ROD.get());
            ItemStack ingot = of(NTItems.CAST_IRON_INGOT.get());
            ItemStack shard = of(NTItems.PRISMARINE_CRYSTAL_SHARD.get());
            ItemStack valve = craft(helper, List.of(e, rod, e, ingot, of(NTItems.GEAR.get()), ingot, e, shard, e));
            helper.assertTrue(valve.is(NTItems.VALVE.get()), "Cast Iron, a Gear and a shard craft a Valve, got " + valve);
            ItemStack whisk = craft(helper, List.of(rod, e, rod, rod, shard, rod, e, of(NTItems.AQUARINE_STEEL_INGOT.get()), e));
            helper.assertTrue(whisk.is(NTItems.WHISK.get()), "Cast Iron Rods, a shard and Aquarine Steel craft a Whisk, got " + whisk);
            helper.succeed();
        });

        r.add("guaranteed_parts/mixer_makes_coils_and_chips", 20, helper -> {
            ItemStack coil = mix(helper, List.of(new ItemStack(Items.COPPER_INGOT, 4), new ItemStack(Items.REDSTONE, 2),
                    of(NTItems.AQUARINE_STEEL_INGOT.get()), of(NTItems.PRISMARINE_CRYSTAL_SHARD.get())));
            helper.assertTrue(coil.is(NTItems.BURNT_COIL.get()), "the Mixer makes a Burnt Coil, got " + coil);
            ItemStack chips = mix(helper, List.of(new ItemStack(Items.GOLD_INGOT, 2), new ItemStack(Items.REDSTONE, 4),
                    new ItemStack(NTItems.PRISMARINE_CRYSTAL_SHARD.get(), 2), of(NTItems.LASER_CHANNELING_COIL.get())));
            helper.assertTrue(chips.is(NTItems.AQUATIC_CHIP.get()) && chips.getCount() == 2, "the Mixer makes two Aquatic Chips, got " + chips);
            helper.succeed();
        });

        r.add("guaranteed_parts/pressure_forge_makes_atlantic_gold", 20, helper -> {
            RecipeHolder<?> holder = helper.getLevel().recipeAccess()
                    .byKey(ResourceKey.create(Registries.RECIPE, Nautec.rl("atlantic_gold_ingot_forging"))).orElse(null);
            helper.assertTrue(holder != null && holder.value() instanceof PressureForgingRecipe, "an Atlantic Gold forging recipe exists");
            PressureForgingRecipe recipe = (PressureForgingRecipe) holder.value();
            helper.assertTrue(recipe.ingredient().test(new ItemStack(Items.GOLD_BLOCK)), "it takes a Block of Gold");
            helper.assertTrue(recipe.result().is(NTItems.ATLANTIC_GOLD_INGOT.get()) && recipe.result().getCount() == 2, "it gives two Atlantic Gold Ingots");
            helper.succeed();
        });
    }
}
