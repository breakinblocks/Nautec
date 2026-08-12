package com.breakinblocks.nautec.content.recipes;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.recipes.inputs.ResonanceRecipeInput;
import com.breakinblocks.nautec.content.recipes.utils.RecipeUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ResonanceCraftingRecipe(Ingredient ingredient, ItemStackTemplate resultTemplate,
                                      float purity) implements Recipe<ResonanceRecipeInput> {
    public static final String NAME = "resonance_crafting";

    public @NotNull ItemStack result() {
        return resultTemplate.create();
    }

    @Override
    public boolean matches(@NotNull ResonanceRecipeInput recipeInput, @NotNull Level level) {
        return ingredient.test(recipeInput.item()) && purity <= recipeInput.purity();
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull ResonanceRecipeInput input) {
        return resultTemplate.create();
    }

    public @NotNull ItemStack getResultItem(HolderLookup.@Nullable Provider registries) {
        return resultTemplate.create();
    }

    @Override
    public @NotNull String group() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<ResonanceRecipeInput>> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<ResonanceRecipeInput>> getType() {
        return Type.INSTANCE;
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public @NotNull NonNullList<Ingredient> getIngredients() {
        return RecipeUtils.listToNonNullList(List.of(ingredient));
    }

    public static class Serializer {
        private static final MapCodec<ResonanceCraftingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec((builder) -> builder.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(ResonanceCraftingRecipe::ingredient),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(ResonanceCraftingRecipe::resultTemplate),
                Codec.FLOAT.fieldOf("purity").forGetter(ResonanceCraftingRecipe::purity)
        ).apply(builder, ResonanceCraftingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ResonanceCraftingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC,
                ResonanceCraftingRecipe::ingredient,
                ItemStackTemplate.STREAM_CODEC,
                ResonanceCraftingRecipe::resultTemplate,
                ByteBufCodecs.FLOAT,
                ResonanceCraftingRecipe::purity,
                ResonanceCraftingRecipe::new
        );

        public static final RecipeSerializer<ResonanceCraftingRecipe> INSTANCE = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

        private Serializer() {
        }
    }

    public static class Type {
        public static final RecipeType<ResonanceCraftingRecipe> INSTANCE = RecipeType.simple(Nautec.rl(NAME));

        private Type() {
        }
    }
}
