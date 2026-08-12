package com.breakinblocks.nautec.datagen.recipeBuilder;

import com.breakinblocks.nautec.content.recipes.ResonanceCraftingRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResonanceCraftingRecipeBuilder implements NTRecipeBuilder {
    @NotNull
    private final ItemStackTemplate result;
    private Ingredient ingredient;
    private float purity;

    private ResonanceCraftingRecipeBuilder(ItemStackTemplate result) {
        this.result = result;
    }

    public static ResonanceCraftingRecipeBuilder newRecipe(ItemStackTemplate result) {
        return new ResonanceCraftingRecipeBuilder(result);
    }

    public ResonanceCraftingRecipeBuilder ingredient(ItemLike ingredient) {
        this.ingredient = Ingredient.of(ingredient);
        return this;
    }

    public ResonanceCraftingRecipeBuilder ingredient(Ingredient ingredient) {
        this.ingredient = ingredient;
        return this;
    }

    public ResonanceCraftingRecipeBuilder purity(float purity) {
        this.purity = purity;
        return this;
    }

    @Override
    public @NotNull ResonanceCraftingRecipeBuilder unlockedBy(String s, Criterion<?> criterion) {
        return this;
    }

    @Override
    public @NotNull ResonanceCraftingRecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return result.item().value();
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> key) {
        recipeOutput.accept(key, new ResonanceCraftingRecipe(this.ingredient, this.result, this.purity), null);
    }

    @Override
    public List<Ingredient> getIngredients() {
        return List.of(this.ingredient);
    }

    @Override
    public String getName() {
        return ResonanceCraftingRecipe.NAME;
    }
}
