package com.breakinblocks.nautec.content.recipes;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.bacteria.BacteriaSelector;
import com.breakinblocks.nautec.content.bacteria.ProductNutrients;
import com.breakinblocks.nautec.content.recipes.inputs.BacteriaRecipeInput;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public record ColonyFeedingRecipe(Optional<BacteriaSelector> bacteria, IngredientWithCount ingredient,
                                  int vitalityTicks) implements Recipe<BacteriaRecipeInput> {
    public static final String NAME = "colony_feeding";
    public static final RecipeType<ColonyFeedingRecipe> TYPE = RecipeType.simple(Nautec.rl(NAME));

    public boolean accepts(BacteriaInstance colony, Level level) {
        return !colony.isEmpty() && BacteriaSelector.matches(bacteria, colony.getBacteria(), level.registryAccess());
    }

    @Override
    public boolean matches(BacteriaRecipeInput input, Level level) {
        return accepts(input.input(), level) && ingredient.test(input.catalyst());
    }

    public static Optional<ColonyFeedingRecipe> find(Level level, BacteriaInstance colony, ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel) || stack.isEmpty() || colony.isEmpty()) {
            return Optional.empty();
        }
        ColonyFeedingRecipe best = null;
        for (RecipeHolder<ColonyFeedingRecipe> holder : serverLevel.recipeAccess().recipeMap().byType(TYPE)) {
            ColonyFeedingRecipe recipe = holder.value();
            if (!recipe.ingredient().test(stack) || !recipe.accepts(colony, level)) {
                continue;
            }
            if (best == null || BacteriaSelector.priority(recipe.bacteria()) > BacteriaSelector.priority(best.bacteria())) {
                best = recipe;
            }
        }
        if (best == null) {
            for (ColonyFeedingRecipe recipe : ProductNutrients.get(serverLevel).feeding()) {
                if (recipe.ingredient().test(stack) && recipe.accepts(colony, level)) {
                    return Optional.of(recipe);
                }
            }
        }
        return Optional.ofNullable(best);
    }

    public static boolean isNutrient(Level level, ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return false;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }
        for (RecipeHolder<ColonyFeedingRecipe> holder : serverLevel.recipeAccess().recipeMap().byType(TYPE)) {
            if (holder.value().ingredient().ingredient().test(stack)) {
                return true;
            }
        }
        for (ColonyFeedingRecipe recipe : ProductNutrients.get(serverLevel).feeding()) {
            if (recipe.ingredient().ingredient().test(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(BacteriaRecipeInput input) {
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
        public static final MapCodec<ColonyFeedingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                BacteriaSelector.CODEC.optionalFieldOf("bacteria").forGetter(ColonyFeedingRecipe::bacteria),
                IngredientWithCount.CODEC.fieldOf("ingredient").forGetter(ColonyFeedingRecipe::ingredient),
                ExtraCodecs.POSITIVE_INT.fieldOf("vitality_ticks").forGetter(ColonyFeedingRecipe::vitalityTicks)
        ).apply(inst, ColonyFeedingRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ColonyFeedingRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(BacteriaSelector.STREAM_CODEC),
                ColonyFeedingRecipe::bacteria,
                IngredientWithCount.STREAM_CODEC,
                ColonyFeedingRecipe::ingredient,
                ByteBufCodecs.VAR_INT,
                ColonyFeedingRecipe::vitalityTicks,
                ColonyFeedingRecipe::new
        );
        public static final RecipeSerializer<ColonyFeedingRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private Serializer() {
        }
    }
}
