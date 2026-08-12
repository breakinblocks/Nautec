package com.breakinblocks.nautec.content.recipes;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.recipes.inputs.PressureForgingRecipeInput;
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

public record PressureForgingRecipe(Ingredient ingredient, ItemStackTemplate resultTemplate, int minDepth,
                                    float purity, int duration) implements Recipe<PressureForgingRecipeInput> {
    public static final String NAME = "pressure_forging";

    public @NotNull ItemStack result() {
        return resultTemplate.create();
    }

    @Override
    public boolean matches(@NotNull PressureForgingRecipeInput recipeInput, @NotNull Level level) {
        return ingredient.test(recipeInput.item())
                && purity <= recipeInput.purity()
                && recipeInput.depth() <= minDepth;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull PressureForgingRecipeInput input) {
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
    public @NotNull RecipeSerializer<? extends Recipe<PressureForgingRecipeInput>> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<PressureForgingRecipeInput>> getType() {
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
        private static final MapCodec<PressureForgingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec((builder) -> builder.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(PressureForgingRecipe::ingredient),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(PressureForgingRecipe::resultTemplate),
                Codec.INT.fieldOf("min_depth").forGetter(PressureForgingRecipe::minDepth),
                Codec.FLOAT.fieldOf("purity").forGetter(PressureForgingRecipe::purity),
                Codec.INT.fieldOf("duration").forGetter(PressureForgingRecipe::duration)
        ).apply(builder, PressureForgingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, PressureForgingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, PressureForgingRecipe::ingredient,
                ItemStackTemplate.STREAM_CODEC, PressureForgingRecipe::resultTemplate,
                ByteBufCodecs.INT, PressureForgingRecipe::minDepth,
                ByteBufCodecs.FLOAT, PressureForgingRecipe::purity,
                ByteBufCodecs.INT, PressureForgingRecipe::duration,
                PressureForgingRecipe::new
        );

        public static final RecipeSerializer<PressureForgingRecipe> INSTANCE = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

        private Serializer() {
        }
    }

    public static class Type {
        public static final RecipeType<PressureForgingRecipe> INSTANCE = RecipeType.simple(Nautec.rl(NAME));

        private Type() {
        }
    }
}
