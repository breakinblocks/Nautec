package com.breakinblocks.nautec.utils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeManager;

public final class RecipeRevision {
    private RecipeManager previous;

    public boolean changed(ServerLevel level) {
        return changed(level.getServer().getRecipeManager());
    }

    public boolean changed(RecipeManager current) {
        if (current == previous) return false;
        boolean changed = previous != null;
        previous = current;
        return changed;
    }
}
