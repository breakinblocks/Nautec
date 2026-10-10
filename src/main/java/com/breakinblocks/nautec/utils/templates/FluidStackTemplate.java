package com.breakinblocks.nautec.utils.templates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

public record FluidStackTemplate(Holder<Fluid> fluid, int amount, DataComponentPatch components) {
    private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Fluid>> FLUID_HOLDER_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.FLUID);
    public static final MapCodec<FluidStackTemplate> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            FluidStack.FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidStackTemplate::fluid),
            ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(FluidStackTemplate::amount),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidStackTemplate::components)
    ).apply(i, FluidStackTemplate::new));
    public static final Codec<FluidStackTemplate> CODEC = Codec.withAlternative(MAP_CODEC.codec(), FluidStack.FLUID_NON_EMPTY_CODEC,
            fluid -> new FluidStackTemplate(fluid.value(), FluidType.BUCKET_VOLUME));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackTemplate> STREAM_CODEC = StreamCodec.composite(
            FLUID_HOLDER_STREAM_CODEC, FluidStackTemplate::fluid,
            ByteBufCodecs.VAR_INT, FluidStackTemplate::amount,
            DataComponentPatch.STREAM_CODEC, FluidStackTemplate::components,
            FluidStackTemplate::new);

    public FluidStackTemplate {
        if (fluid.is(Fluids.EMPTY.builtInRegistryHolder()) || amount <= 0) {
            throw new IllegalStateException("Fluid must be non-empty");
        }
    }

    public FluidStackTemplate(Holder<Fluid> fluid, int amount) {
        this(fluid, amount, DataComponentPatch.EMPTY);
    }

    public FluidStackTemplate(Fluid fluid, int amount, DataComponentPatch components) {
        this(fluid.builtInRegistryHolder(), amount, components);
    }

    public FluidStackTemplate(Fluid fluid, int amount) {
        this(fluid, amount, DataComponentPatch.EMPTY);
    }

    public static FluidStackTemplate fromNonEmptyStack(FluidStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Stack must be non-empty");
        }
        return new FluidStackTemplate(stack.getFluidHolder(), stack.getAmount(), stack.getComponentsPatch());
    }

    public FluidStackTemplate withAmount(int amount) {
        return this.amount == amount ? this : new FluidStackTemplate(fluid, amount, components);
    }

    public FluidStack create() {
        return new FluidStack(fluid, amount, components);
    }

    public FluidStack apply(DataComponentPatch additionalPatch) {
        return apply(amount, additionalPatch);
    }

    public FluidStack apply(int amount, DataComponentPatch additionalPatch) {
        FluidStack stack = new FluidStack(fluid, amount, additionalPatch);
        stack.applyComponents(components);
        return stack;
    }

    public Holder<Fluid> typeHolder() {
        return fluid;
    }

    public Fluid getFluid() {
        return fluid.value();
    }

    public <T> @Nullable T get(DataComponentType<? extends T> type) {
        return create().get(type);
    }

    public static Codec<FluidStackTemplate> fixedAmountCodec(int amount) {
        return Codec.lazyInitialized(() -> RecordCodecBuilder.create(i -> i.group(
                FluidStack.FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidStackTemplate::fluid),
                DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidStackTemplate::components)
        ).apply(i, (holder, patch) -> new FluidStackTemplate(holder, amount, patch))));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, FluidStackTemplate> fixedAmountStreamCodec(int amount) {
        return StreamCodec.composite(
                FLUID_HOLDER_STREAM_CODEC, FluidStackTemplate::fluid,
                DataComponentPatch.STREAM_CODEC, FluidStackTemplate::components,
                (holder, patch) -> new FluidStackTemplate(holder, amount, patch));
    }
}
