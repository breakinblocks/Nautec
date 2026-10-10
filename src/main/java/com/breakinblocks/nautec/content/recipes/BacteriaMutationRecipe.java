package com.breakinblocks.nautec.content.recipes;


import com.breakinblocks.nautec.content.recipes.utils.SimpleRecipeSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.content.bacteria.ProductNutrients;
import com.breakinblocks.nautec.content.recipes.inputs.BacteriaRecipeInput;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.NotNull;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public record BacteriaMutationRecipe(ResourceKey<Bacteria> inputBacteria, ResourceKey<Bacteria> resultBacteria,
                                     Ingredient catalyst, float chance) implements Recipe<BacteriaRecipeInput> {
    public static final String NAME = "bacteria_mutation";
    public static final RecipeType<BacteriaMutationRecipe> TYPE = RecipeType.simple(Nautec.rl("bacteria_mutation"));

    @Override
    public boolean matches(BacteriaRecipeInput input, Level level) {
        return input.input().is(inputBacteria) && catalyst.test(input.catalyst());
    }

    public boolean refines() {
        return inputBacteria.equals(resultBacteria);
    }

    public static Optional<BacteriaMutationRecipe> find(ServerLevel level, BacteriaRecipeInput input) {
        Optional<BacteriaMutationRecipe> recipe = level.getRecipeManager().getRecipeFor(TYPE, input, level).map(RecipeHolder::value);
        if (recipe.isPresent()) {
            return recipe;
        }
        for (BacteriaMutationRecipe derived : ProductNutrients.get(level).mutation()) {
            if (derived.matches(input, level)) {
                return Optional.of(derived);
            }
        }
        return Optional.empty();
    }

    @Override
    public ItemStack assemble(BacteriaRecipeInput input, HolderLookup.@NotNull Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public String getGroup() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
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

    public static final class Serializer {
        public static final MapCodec<BacteriaMutationRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Bacteria.BACTERIA_TYPE_CODEC.fieldOf("input_bacteria").forGetter(BacteriaMutationRecipe::inputBacteria),
                Bacteria.BACTERIA_TYPE_CODEC.fieldOf("result_bacteria").forGetter(BacteriaMutationRecipe::resultBacteria),
                Ingredient.CODEC.fieldOf("catalyst").forGetter(BacteriaMutationRecipe::catalyst),
                Codec.FLOAT.fieldOf("chance").forGetter(BacteriaMutationRecipe::chance)
        ).apply(inst, BacteriaMutationRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, BacteriaMutationRecipe> STREAM_CODEC = StreamCodec.composite(
                Bacteria.BACTERIA_TYPE_STREAM_CODEC,
                BacteriaMutationRecipe::inputBacteria,
                Bacteria.BACTERIA_TYPE_STREAM_CODEC,
                BacteriaMutationRecipe::resultBacteria,
                Ingredient.CONTENTS_STREAM_CODEC,
                BacteriaMutationRecipe::catalyst,
                ByteBufCodecs.FLOAT,
                BacteriaMutationRecipe::chance,
                BacteriaMutationRecipe::new
        );
        public static final RecipeSerializer<BacteriaMutationRecipe> INSTANCE = new SimpleRecipeSerializer<>(CODEC, STREAM_CODEC);

        private Serializer() {
        }
    }
}
