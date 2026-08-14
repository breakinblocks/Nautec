package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.recipes.AquaticCatalystChannelingRecipe;
import com.breakinblocks.nautec.content.recipes.AugmentationRecipe;
import com.breakinblocks.nautec.content.recipes.BacteriaIncubationRecipe;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import com.breakinblocks.nautec.content.recipes.ItemEtchingRecipe;
import com.breakinblocks.nautec.content.recipes.ItemTransformationRecipe;
import com.breakinblocks.nautec.content.recipes.MixingRecipe;
import com.breakinblocks.nautec.content.recipes.PressureForgingRecipe;
import com.breakinblocks.nautec.content.recipes.ResonanceCraftingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NTRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Nautec.MODID);

    static {
        SERIALIZERS.register(AquaticCatalystChannelingRecipe.NAME, () -> AquaticCatalystChannelingRecipe.Serializer.INSTANCE);
        SERIALIZERS.register(ItemTransformationRecipe.NAME, () -> ItemTransformationRecipe.Serializer.INSTANCE);
        SERIALIZERS.register(ItemEtchingRecipe.NAME, () -> ItemEtchingRecipe.Serializer.INSTANCE);
        SERIALIZERS.register(MixingRecipe.NAME, () -> MixingRecipe.Serializer.INSTANCE);
        SERIALIZERS.register(AugmentationRecipe.NAME, () -> AugmentationRecipe.Serializer.INSTANCE);
        SERIALIZERS.register(BacteriaMutationRecipe.NAME, () -> BacteriaMutationRecipe.Serializer.INSTANCE);
        SERIALIZERS.register(BacteriaIncubationRecipe.NAME, () -> BacteriaIncubationRecipe.Serializer.INSTANCE);
        SERIALIZERS.register(ResonanceCraftingRecipe.NAME, () -> ResonanceCraftingRecipe.Serializer.INSTANCE);
        SERIALIZERS.register(PressureForgingRecipe.NAME, () -> PressureForgingRecipe.Serializer.INSTANCE);
    }

}
