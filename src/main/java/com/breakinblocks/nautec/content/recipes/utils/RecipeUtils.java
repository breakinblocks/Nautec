package com.breakinblocks.nautec.content.recipes.utils;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class RecipeUtils {
    public static List<IngredientWithCount> ingredientsToIWC(List<Ingredient> ingredients) {
        return ingredients.stream().map(ingredient -> new IngredientWithCount(ingredient, 1)).toList();
    }

    public static List<Ingredient> iWCToIngredients(List<IngredientWithCount> ingredientsWithCount) {
        return ingredientsWithCount.stream().map(IngredientWithCount::ingredient).toList();
    }

    public static List<Ingredient> iWCToIngredientsSaveCount(List<IngredientWithCount> ingredientsWithCount) {
        return ingredientsWithCount.stream().map(RecipeUtils::iWCToIngredientSaveCount).toList();
    }

    public static @NotNull Ingredient iWCToIngredientSaveCount(IngredientWithCount ingredientWithCount) {
        return ingredientWithCount.ingredient();
    }

    public static <T> NonNullList<T> listToNonNullList(List<T> list) {
        NonNullList<T> nnl = NonNullList.create();
        nnl.addAll(list);
        return nnl;
    }

    public static boolean compareItems(List<ItemStack> inputs, List<IngredientWithCount> ingredients) {
        return inputs.size() == ingredients.size() && consumptionPlan(inputs, ingredients) != null;
    }

    public static int[] consumptionPlan(List<ItemStack> inputs, List<IngredientWithCount> ingredients) {
        int[] slots = new int[ingredients.size()];
        return assign(inputs, ingredients, slots, new boolean[inputs.size()], 0) ? slots : null;
    }

    private static boolean assign(List<ItemStack> inputs, List<IngredientWithCount> ingredients,
                                  int[] slots, boolean[] used, int ingredient) {
        if (ingredient == ingredients.size()) return true;
        for (int slot = 0; slot < inputs.size(); slot++) {
            if (!used[slot] && ingredients.get(ingredient).test(inputs.get(slot))) {
                used[slot] = true;
                slots[ingredient] = slot;
                if (assign(inputs, ingredients, slots, used, ingredient + 1)) return true;
                used[slot] = false;
            }
        }
        return false;
    }
}
