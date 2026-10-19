package com.breakinblocks.nautec.content.recipes;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.enchanter.EnchantingRules;
import com.breakinblocks.nautec.content.recipes.inputs.LaserEnchantingRecipeInput;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import com.breakinblocks.nautec.content.recipes.utils.SimpleRecipeSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record LaserEnchantingRecipe(ResourceKey<Enchantment> enchantment, IngredientWithCount input, float costMultiplier)
        implements Recipe<LaserEnchantingRecipeInput> {
    public static final String NAME = "laser_enchanting";

    public Optional<Holder.Reference<Enchantment>> enchantment(HolderLookup.Provider registries) {
        return registries.lookup(Registries.ENCHANTMENT).flatMap(lookup -> lookup.get(enchantment));
    }

    public int reagentCount(int level) {
        return input.count() * Math.max(1, level);
    }

    @Override
    public boolean matches(@NotNull LaserEnchantingRecipeInput input, @NotNull Level level) {
        if (!this.input.ingredient().test(input.reagent())) {
            return false;
        }
        return input.target().isEmpty() || enchantment(level.registryAccess())
                .map(holder -> EnchantingRules.accepts(input.target(), holder))
                .orElse(false);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull LaserEnchantingRecipeInput input, HolderLookup.@NotNull Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull String getGroup() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<LaserEnchantingRecipeInput>> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<LaserEnchantingRecipeInput>> getType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer {
        private static final MapCodec<LaserEnchantingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ResourceKey.codec(Registries.ENCHANTMENT).fieldOf("enchantment").forGetter(LaserEnchantingRecipe::enchantment),
                IngredientWithCount.CODEC.fieldOf("input").forGetter(LaserEnchantingRecipe::input),
                Codec.floatRange(0, 1_000F).optionalFieldOf("cost_multiplier", 1F).forGetter(LaserEnchantingRecipe::costMultiplier)
        ).apply(builder, LaserEnchantingRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, LaserEnchantingRecipe> STREAM_CODEC = StreamCodec.composite(
                ResourceKey.streamCodec(Registries.ENCHANTMENT),
                LaserEnchantingRecipe::enchantment,
                IngredientWithCount.STREAM_CODEC,
                LaserEnchantingRecipe::input,
                ByteBufCodecs.FLOAT,
                LaserEnchantingRecipe::costMultiplier,
                LaserEnchantingRecipe::new
        );
        public static final RecipeSerializer<LaserEnchantingRecipe> INSTANCE = new SimpleRecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

        private Serializer() {
        }
    }

    public static class Type {
        public static final RecipeType<LaserEnchantingRecipe> INSTANCE = RecipeType.simple(Nautec.rl(NAME));

        private Type() {
        }
    }
}
