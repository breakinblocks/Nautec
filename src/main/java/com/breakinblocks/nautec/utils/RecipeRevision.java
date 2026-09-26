package com.breakinblocks.nautec.utils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeMap;

public final class RecipeRevision {
    private RecipeMap previous;

    public boolean changed(ServerLevel level) {
        return changed(level.getServer().getRecipeManager().recipeMap());
    }

    public boolean changed(RecipeMap current) {
        if (current == previous) return false;
        boolean changed = previous != null;
        previous = current;
        return changed;
    }
}
