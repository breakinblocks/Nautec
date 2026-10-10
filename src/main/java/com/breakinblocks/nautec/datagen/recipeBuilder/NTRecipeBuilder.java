package com.breakinblocks.nautec.datagen.recipeBuilder;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public interface NTRecipeBuilder extends RecipeBuilder {
    List<Ingredient> getIngredients();

    String getName();

    Item getResult();

    static String ingredientPathSuffix(Ingredient ingredient) {
        StringBuilder out = new StringBuilder();
        for (String path : ingredientPaths(ingredient)) {
            out.append('_').append(path.replace(':', '-'));
        }
        return out.toString();
    }

    static List<String> ingredientPaths(Ingredient ingredient) {
        List<String> paths = new ArrayList<>();
        if (ingredient.isCustom()) {
            for (ItemStack stack : ingredient.getItems()) {
                paths.add(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath());
            }
            return paths;
        }
        for (Ingredient.Value value : ingredient.getValues()) {
            if (value instanceof Ingredient.TagValue tagValue) {
                paths.add(tagValue.tag().location().getPath());
            } else if (value instanceof Ingredient.ItemValue itemValue) {
                paths.add(BuiltInRegistries.ITEM.getKey(itemValue.item().getItem()).getPath());
            }
        }
        return paths;
    }

    @Override
    default void save(RecipeOutput recipeOutput) {
        StringBuilder builder = new StringBuilder();
        for (Ingredient ingredient : getIngredients()) {
            for (String path : ingredientPaths(ingredient)) {
                builder.append(path).append("_");
            }
        }
        Item result = getResult();
        if (result != Items.AIR) {
            builder.append("to_").append(BuiltInRegistries.ITEM.getKey(result).getPath());
        } else {
            builder.deleteCharAt(builder.length() - 1);
        }
        save(recipeOutput, Nautec.rl(getName() + "/" + builder));
    }
}
