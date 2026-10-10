package com.breakinblocks.nautec.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.Nullable;

public final class ClientRecipes {
    private ClientRecipes() {
    }

    public static @Nullable RecipeManager get() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection == null ? null : connection.getRecipeManager();
    }
}
