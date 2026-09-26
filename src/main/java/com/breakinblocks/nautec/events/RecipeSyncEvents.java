package com.breakinblocks.nautec.events;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.recipes.ItemTransformationRecipe;
import com.breakinblocks.nautec.content.recipes.AquaticCatalystChannelingRecipe;
import com.breakinblocks.nautec.content.recipes.ItemEtchingRecipe;
import com.breakinblocks.nautec.content.recipes.MixingRecipe;
import com.breakinblocks.nautec.content.recipes.AugmentationRecipe;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import com.breakinblocks.nautec.content.recipes.BacteriaIncubationRecipe;
import com.breakinblocks.nautec.content.recipes.ResonanceCraftingRecipe;
import com.breakinblocks.nautec.content.recipes.PressureForgingRecipe;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@EventBusSubscriber(modid = Nautec.MODID)
public final class RecipeSyncEvents {
    @SubscribeEvent
    public static void sync(OnDatapackSyncEvent event) {
        event.sendRecipes(ItemTransformationRecipe.Type.INSTANCE,
                AquaticCatalystChannelingRecipe.Type.INSTANCE,
                ItemEtchingRecipe.Type.INSTANCE,
                MixingRecipe.Type.INSTANCE,
                AugmentationRecipe.Type.INSTANCE,
                BacteriaMutationRecipe.TYPE,
                BacteriaIncubationRecipe.TYPE,
                ResonanceCraftingRecipe.Type.INSTANCE,
                PressureForgingRecipe.Type.INSTANCE);
    }
}
