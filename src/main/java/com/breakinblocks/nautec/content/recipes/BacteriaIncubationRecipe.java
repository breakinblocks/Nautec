package com.breakinblocks.nautec.content.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.content.bacteria.ProductNutrients;
import com.breakinblocks.nautec.content.recipes.inputs.BacteriaRecipeInput;
import com.breakinblocks.nautec.utils.ranges.IntRange;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public record BacteriaIncubationRecipe(ResourceKey<Bacteria> bacteria, Ingredient nutrient, IntRange growth, float consumeChance) implements Recipe<BacteriaRecipeInput> {
    public static final String NAME = "bacteria_incubation";
    public static final RecipeType<BacteriaIncubationRecipe> TYPE = RecipeType.simple(Nautec.rl(NAME));

    @Override
    public boolean matches(BacteriaRecipeInput input, Level level) {
        return input.input().is(bacteria) && nutrient.test(input.catalyst());
    }

    public static Optional<BacteriaIncubationRecipe> find(ServerLevel level, BacteriaRecipeInput input) {
        Optional<BacteriaIncubationRecipe> recipe = level.recipeAccess().getRecipeFor(TYPE, input, level).map(RecipeHolder::value);
        if (recipe.isPresent()) {
            return recipe;
        }
        for (BacteriaIncubationRecipe derived : ProductNutrients.get(level).incubation()) {
            if (derived.matches(input, level)) {
                return Optional.of(derived);
            }
        }
        return Optional.empty();
    }

    @Override
    public ItemStack assemble(BacteriaRecipeInput input) {
        return ItemStack.EMPTY;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public RecipeSerializer<? extends Recipe<BacteriaRecipeInput>> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<? extends Recipe<BacteriaRecipeInput>> getType() {
        return TYPE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public static final class Serializer {
        public static final MapCodec<BacteriaIncubationRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Bacteria.BACTERIA_TYPE_CODEC.fieldOf("bacteria").forGetter(BacteriaIncubationRecipe::bacteria),
                Ingredient.CODEC.fieldOf("nutrient").forGetter(BacteriaIncubationRecipe::nutrient),
                IntRange.MAP_CODEC.fieldOf("growth").forGetter(BacteriaIncubationRecipe::growth),
                Codec.FLOAT.fieldOf("consume_chance").forGetter(BacteriaIncubationRecipe::consumeChance)
        ).apply(inst, BacteriaIncubationRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, BacteriaIncubationRecipe> STREAM_CODEC = StreamCodec.composite(
                Bacteria.BACTERIA_TYPE_STREAM_CODEC,
                BacteriaIncubationRecipe::bacteria,
                Ingredient.CONTENTS_STREAM_CODEC,
                BacteriaIncubationRecipe::nutrient,
                IntRange.STREAM_CODEC,
                BacteriaIncubationRecipe::growth,
                ByteBufCodecs.FLOAT,
                BacteriaIncubationRecipe::consumeChance,
                BacteriaIncubationRecipe::new
        );
        public static final RecipeSerializer<BacteriaIncubationRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private Serializer() {
        }
    }
}
