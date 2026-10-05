package com.breakinblocks.nautec.content.recipes;

import com.breakinblocks.nautec.Nautec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record CombustionAdditiveRecipe(Ingredient ingredient, float outputMultiplier, float fuelMultiplier, int duration)
        implements Recipe<SingleRecipeInput> {
    public static final String NAME = "combustion_additive";

    @Override
    public boolean matches(@NotNull SingleRecipeInput input, @NotNull Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SingleRecipeInput input) {
        return ItemStack.EMPTY;
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
    public @NotNull RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public static class Serializer {
        private static final MapCodec<CombustionAdditiveRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(CombustionAdditiveRecipe::ingredient),
                Codec.floatRange(0F, 100F).fieldOf("output_multiplier").forGetter(CombustionAdditiveRecipe::outputMultiplier),
                Codec.floatRange(0F, 100F).optionalFieldOf("fuel_multiplier", 1F).forGetter(CombustionAdditiveRecipe::fuelMultiplier),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("duration").forGetter(CombustionAdditiveRecipe::duration)
        ).apply(builder, CombustionAdditiveRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, CombustionAdditiveRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC,
                CombustionAdditiveRecipe::ingredient,
                ByteBufCodecs.FLOAT,
                CombustionAdditiveRecipe::outputMultiplier,
                ByteBufCodecs.FLOAT,
                CombustionAdditiveRecipe::fuelMultiplier,
                ByteBufCodecs.VAR_INT,
                CombustionAdditiveRecipe::duration,
                CombustionAdditiveRecipe::new
        );
        public static final RecipeSerializer<CombustionAdditiveRecipe> INSTANCE = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

        private Serializer() {
        }
    }

    public static class Type {
        public static final RecipeType<CombustionAdditiveRecipe> INSTANCE = RecipeType.simple(Nautec.rl(NAME));

        private Type() {
        }
    }
}
