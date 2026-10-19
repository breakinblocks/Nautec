package com.breakinblocks.nautec.content.recipes.inputs;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

public record LaserEnchantingRecipeInput(ItemStack target, ItemStack reagent) implements RecipeInput {
    @Override
    public @NotNull ItemStack getItem(int index) {
        return index == 0 ? target : reagent;
    }

    @Override
    public int size() {
        return 2;
    }
}
