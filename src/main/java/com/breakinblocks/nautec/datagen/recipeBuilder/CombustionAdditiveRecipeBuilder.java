package com.breakinblocks.nautec.datagen.recipeBuilder;

import com.breakinblocks.nautec.content.recipes.CombustionAdditiveRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CombustionAdditiveRecipeBuilder implements NTRecipeBuilder {
    private final Ingredient ingredient;
    private float outputMultiplier = 1F;
    private float fuelMultiplier = 1F;
    private int duration = 1;

    private CombustionAdditiveRecipeBuilder(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public static CombustionAdditiveRecipeBuilder newRecipe(Ingredient ingredient) {
        return new CombustionAdditiveRecipeBuilder(ingredient);
    }

    public CombustionAdditiveRecipeBuilder outputMultiplier(float outputMultiplier) {
        this.outputMultiplier = outputMultiplier;
        return this;
    }

    public CombustionAdditiveRecipeBuilder fuelMultiplier(float fuelMultiplier) {
        this.fuelMultiplier = fuelMultiplier;
        return this;
    }

    public CombustionAdditiveRecipeBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    @Override
    public @NotNull CombustionAdditiveRecipeBuilder unlockedBy(String s, Criterion<?> criterion) {
        return this;
    }

    @Override
    public @NotNull CombustionAdditiveRecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return Items.AIR;
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> key) {
        recipeOutput.accept(key, new CombustionAdditiveRecipe(ingredient, outputMultiplier, fuelMultiplier, duration), null);
    }

    @Override
    public List<Ingredient> getIngredients() {
        return List.of(ingredient);
    }

    @Override
    public String getName() {
        return CombustionAdditiveRecipe.NAME;
    }
}
