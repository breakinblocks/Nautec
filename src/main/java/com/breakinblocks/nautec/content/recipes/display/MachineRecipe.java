package com.breakinblocks.nautec.content.recipes.display;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

import java.util.List;

public interface MachineRecipe<I extends RecipeInput> extends Recipe<I> {
    @Override
    default List<RecipeDisplay> display() {
        return List.of(MachineRecipeDisplay.of(this));
    }
}
