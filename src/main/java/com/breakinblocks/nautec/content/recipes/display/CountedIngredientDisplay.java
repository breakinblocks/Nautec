package com.breakinblocks.nautec.content.recipes.display;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.stream.Stream;

public record CountedIngredientDisplay(SlotDisplay ingredient, int count) implements SlotDisplay {
    public static final MapCodec<CountedIngredientDisplay> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            SlotDisplay.CODEC.fieldOf("ingredient").forGetter(CountedIngredientDisplay::ingredient),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("count").forGetter(CountedIngredientDisplay::count)
    ).apply(builder, CountedIngredientDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CountedIngredientDisplay> STREAM_CODEC = StreamCodec.composite(
            SlotDisplay.STREAM_CODEC, CountedIngredientDisplay::ingredient,
            ByteBufCodecs.VAR_INT, CountedIngredientDisplay::count, CountedIngredientDisplay::new);
    public static final Type<CountedIngredientDisplay> TYPE = new Type<>(CODEC, STREAM_CODEC);

    @Override
    public Type<CountedIngredientDisplay> type() {
        return TYPE;
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        if (factory instanceof DisplayContentsFactory.ForStacks<T> stacks) {
            return ingredient.resolveForStacks(context).stream().map(stack -> stacks.forStack(stack.copyWithCount(count)));
        }
        return Stream.empty();
    }
}
