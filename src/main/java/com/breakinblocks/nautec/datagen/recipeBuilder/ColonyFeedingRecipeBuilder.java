package com.breakinblocks.nautec.datagen.recipeBuilder;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaSelector;
import com.breakinblocks.nautec.content.recipes.ColonyFeedingRecipe;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import com.breakinblocks.nautec.data.generated.BacteriaBalance;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record ColonyFeedingRecipeBuilder(ResourceKey<Bacteria> bacteria, IngredientWithCount ingredient, int vitalityTicks) implements NTRecipeBuilder {
    public ColonyFeedingRecipeBuilder(ResourceKey<Bacteria> bacteria, Ingredient ingredient) {
        this(bacteria, new IngredientWithCount(ingredient, 1), BacteriaBalance.DEFAULT_FEEDING_TICKS);
    }

    public ColonyFeedingRecipe build() {
        return new ColonyFeedingRecipe(Optional.of(BacteriaSelector.of(bacteria)), ingredient, vitalityTicks);
    }

    @Override
    public RecipeBuilder unlockedBy(String s, Criterion<?> criterion) {
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String s) {
        return this;
    }

    @Override
    public Item getResult() {
        return Items.AIR;
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> key) {
        recipeOutput.accept(key, build(), null);
    }

    @Override
    public void save(RecipeOutput output) {
        save(output, Nautec.rl(getName() + "/" + bacteria.identifier().getPath()));
    }

    @Override
    public List<Ingredient> getIngredients() {
        return Collections.singletonList(ingredient.ingredient());
    }

    @Override
    public String getName() {
        return ColonyFeedingRecipe.NAME;
    }
}
