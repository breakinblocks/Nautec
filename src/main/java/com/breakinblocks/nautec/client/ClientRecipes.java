package com.breakinblocks.nautec.client;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class ClientRecipes {
    private static RecipeMap recipes = RecipeMap.EMPTY;

    public static RecipeMap get() { return recipes; }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void receive(RecipesReceivedEvent event) { recipes = event.getRecipeMap(); }

    @SubscribeEvent
    public static void disconnect(ClientPlayerNetworkEvent.LoggingOut event) { recipes = RecipeMap.EMPTY; }
}
