package com.breakinblocks.nautec.datagen.recipeBuilder;

import com.breakinblocks.nautec.content.recipes.PressureForgingRecipe;
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

public class PressureForgingRecipeBuilder implements NTRecipeBuilder {
    @NotNull
    private final ItemStackTemplate result;
    private Ingredient ingredient;
    private int minDepth;
    private float purity;
    private int duration;

    private PressureForgingRecipeBuilder(ItemStackTemplate result) {
        this.result = result;
    }

    public static PressureForgingRecipeBuilder newRecipe(ItemStackTemplate result) {
        return new PressureForgingRecipeBuilder(result);
    }

    public PressureForgingRecipeBuilder ingredient(ItemLike ingredient) {
        this.ingredient = Ingredient.of(ingredient);
        return this;
    }

    public PressureForgingRecipeBuilder minDepth(int minDepth) {
        this.minDepth = minDepth;
        return this;
    }

    public PressureForgingRecipeBuilder purity(float purity) {
        this.purity = purity;
        return this;
    }

    public PressureForgingRecipeBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    @Override
    public @NotNull PressureForgingRecipeBuilder unlockedBy(String s, Criterion<?> criterion) {
        return this;
    }

    @Override
    public @NotNull PressureForgingRecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return result.item().value();
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> key) {
        recipeOutput.accept(key, new PressureForgingRecipe(this.ingredient, this.result, this.minDepth, this.purity, this.duration), null);
    }

    @Override
    public List<Ingredient> getIngredients() {
        return List.of(this.ingredient);
    }

    @Override
    public String getName() {
        return PressureForgingRecipe.NAME;
    }
}
