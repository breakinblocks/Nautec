package com.breakinblocks.nautec.datagen.recipeBuilder;

import com.breakinblocks.nautec.content.recipes.LaserCraftingRecipe;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import com.breakinblocks.nautec.utils.templates.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import com.breakinblocks.nautec.utils.templates.FluidStackTemplate;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LaserCraftingRecipeBuilder implements NTRecipeBuilder {
    private final List<IngredientWithCount> ingredients = new ArrayList<>();
    private final List<SizedFluidIngredient> fluidIngredients = new ArrayList<>();
    private final List<ItemStackTemplate> results = new ArrayList<>();
    private final List<FluidStackTemplate> fluidResults = new ArrayList<>();
    private int power;
    private float purity;
    private int duration = 100;

    private LaserCraftingRecipeBuilder() {
    }

    public static LaserCraftingRecipeBuilder newRecipe() {
        return new LaserCraftingRecipeBuilder();
    }

    public LaserCraftingRecipeBuilder ingredient(IngredientWithCount ingredient) {
        ingredients.add(ingredient);
        return this;
    }

    public LaserCraftingRecipeBuilder fluidIngredient(SizedFluidIngredient ingredient) {
        fluidIngredients.add(ingredient);
        return this;
    }

    public LaserCraftingRecipeBuilder result(ItemStackTemplate result) {
        results.add(result);
        return this;
    }

    public LaserCraftingRecipeBuilder fluidResult(FluidStackTemplate result) {
        fluidResults.add(result);
        return this;
    }

    public LaserCraftingRecipeBuilder power(int power) {
        this.power = power;
        return this;
    }

    public LaserCraftingRecipeBuilder purity(float purity) {
        this.purity = purity;
        return this;
    }

    public LaserCraftingRecipeBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public Item getResult() {
        return results.isEmpty() ? Items.AIR : results.getFirst().item().value();
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation key) {
        recipeOutput.accept(key, new LaserCraftingRecipe(ingredients, fluidIngredients, results, fluidResults, power, purity, duration), null);
    }

    @Override
    public List<Ingredient> getIngredients() {
        return ingredients.stream().map(IngredientWithCount::ingredient).toList();
    }

    @Override
    public String getName() {
        return LaserCraftingRecipe.NAME;
    }
}
