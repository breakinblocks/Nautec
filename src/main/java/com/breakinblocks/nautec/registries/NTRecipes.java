package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.recipes.AquaticCatalystChannelingRecipe;
import com.breakinblocks.nautec.content.recipes.AugmentationRecipe;
import com.breakinblocks.nautec.content.recipes.BacteriaIncubationRecipe;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import com.breakinblocks.nautec.content.recipes.ColonyFeedingRecipe;
import com.breakinblocks.nautec.content.recipes.CombustionAdditiveRecipe;
import com.breakinblocks.nautec.content.recipes.ItemEtchingRecipe;
import com.breakinblocks.nautec.content.recipes.ItemTransformationRecipe;
import com.breakinblocks.nautec.content.recipes.LaserCraftingRecipe;
import com.breakinblocks.nautec.content.recipes.MixingRecipe;
import com.breakinblocks.nautec.content.recipes.PressureForgingRecipe;
import com.breakinblocks.nautec.content.recipes.ResonanceCraftingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NTRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Nautec.MODID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, Nautec.MODID);

    static {
        register(AquaticCatalystChannelingRecipe.NAME, AquaticCatalystChannelingRecipe.Serializer.INSTANCE, AquaticCatalystChannelingRecipe.Type.INSTANCE);
        register(CombustionAdditiveRecipe.NAME, CombustionAdditiveRecipe.Serializer.INSTANCE, CombustionAdditiveRecipe.Type.INSTANCE);
        register(ItemTransformationRecipe.NAME, ItemTransformationRecipe.Serializer.INSTANCE, ItemTransformationRecipe.Type.INSTANCE);
        register(ItemEtchingRecipe.NAME, ItemEtchingRecipe.Serializer.INSTANCE, ItemEtchingRecipe.Type.INSTANCE);
        register(MixingRecipe.NAME, MixingRecipe.Serializer.INSTANCE, MixingRecipe.Type.INSTANCE);
        register(AugmentationRecipe.NAME, AugmentationRecipe.Serializer.INSTANCE, AugmentationRecipe.Type.INSTANCE);
        register(BacteriaMutationRecipe.NAME, BacteriaMutationRecipe.Serializer.INSTANCE, BacteriaMutationRecipe.TYPE);
        register(BacteriaIncubationRecipe.NAME, BacteriaIncubationRecipe.Serializer.INSTANCE, BacteriaIncubationRecipe.TYPE);
        register(ResonanceCraftingRecipe.NAME, ResonanceCraftingRecipe.Serializer.INSTANCE, ResonanceCraftingRecipe.Type.INSTANCE);
        register(PressureForgingRecipe.NAME, PressureForgingRecipe.Serializer.INSTANCE, PressureForgingRecipe.Type.INSTANCE);
        register(LaserCraftingRecipe.NAME, LaserCraftingRecipe.Serializer.INSTANCE, LaserCraftingRecipe.Type.INSTANCE);
        register(ColonyFeedingRecipe.NAME, ColonyFeedingRecipe.Serializer.INSTANCE, ColonyFeedingRecipe.TYPE);
    }

    private static void register(String name, RecipeSerializer<?> serializer, RecipeType<?> type) {
        SERIALIZERS.register(name, () -> serializer);
        TYPES.register(name, () -> type);
    }
}
