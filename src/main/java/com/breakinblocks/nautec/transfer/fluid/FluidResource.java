package com.breakinblocks.nautec.transfer.fluid;

import com.breakinblocks.nautec.transfer.resource.DataComponentHolderResource;
import com.breakinblocks.nautec.utils.templates.FluidStackTemplate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class FluidResource implements DataComponentHolderResource<Fluid> {
    private static final Map<Fluid, FluidResource> PLAIN = new ConcurrentHashMap<>();

    public static final FluidResource EMPTY = new FluidResource(FluidStack.EMPTY);

    public static final Codec<FluidResource> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(i -> i.group(
            FluidStack.FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidResource::typeHolder),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidResource::getComponentsPatch)
    ).apply(i, FluidResource::of)));

    public static final Codec<FluidResource> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC).xmap(
            optional -> optional.orElse(EMPTY),
            resource -> resource.isEmpty() ? Optional.empty() : Optional.of(resource));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidResource> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.FLUID), FluidResource::typeHolder,
            DataComponentPatch.STREAM_CODEC, FluidResource::getComponentsPatch,
            FluidResource::of);

    private final FluidStack stack;

    private FluidResource(FluidStack stack) {
        this.stack = stack;
    }

    public static FluidResource of(FluidStack stack) {
        if (stack.isEmpty()) {
            return EMPTY;
        }
        if (stack.isComponentsPatchEmpty()) {
            return of(stack.getFluid());
        }
        return new FluidResource(stack.copyWithAmount(FluidType.BUCKET_VOLUME));
    }

    public static FluidResource of(@Nullable FluidStackTemplate template) {
        if (template == null) {
            return EMPTY;
        }
        return of(template.fluid(), template.components());
    }

    public static FluidResource of(Fluid fluid) {
        if (fluid == Fluids.EMPTY) {
            return EMPTY;
        }
        return PLAIN.computeIfAbsent(fluid, key -> new FluidResource(new FluidStack(key, FluidType.BUCKET_VOLUME)));
    }

    public static FluidResource of(Fluid fluid, DataComponentPatch patch) {
        return of(fluid.builtInRegistryHolder(), patch);
    }

    public static FluidResource of(Holder<Fluid> fluid) {
        return of(fluid.value());
    }

    public static FluidResource of(Holder<Fluid> holder, DataComponentPatch patch) {
        if (patch.isEmpty()) {
            return of(holder.value());
        }
        if (holder.value() == Fluids.EMPTY) {
            return EMPTY;
        }
        return new FluidResource(new FluidStack(holder, FluidType.BUCKET_VOLUME, patch));
    }

    @Override
    public Fluid value() {
        return stack.getFluid();
    }

    public Fluid getFluid() {
        return value();
    }

    @Override
    public Holder<Fluid> typeHolder() {
        return stack.getFluidHolder();
    }

    public FluidType getFluidType() {
        return stack.getFluidType();
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public FluidResource withMergedPatch(DataComponentPatch patch) {
        if (isEmpty() || patch.isEmpty()) {
            return this;
        }
        FluidStack copy = stack.copy();
        copy.applyComponents(patch);
        return of(copy);
    }

    @Override
    public <D> FluidResource with(DataComponentType<D> type, @Nullable D data) {
        if (isEmpty()) {
            return this;
        }
        FluidStack copy = stack.copy();
        copy.set(type, data);
        return of(copy);
    }

    @Override
    public <D> FluidResource with(Supplier<? extends DataComponentType<D>> type, @Nullable D data) {
        return with(type.get(), data);
    }

    @Override
    public FluidResource without(DataComponentType<?> type) {
        if (isEmpty()) {
            return this;
        }
        FluidStack copy = stack.copy();
        copy.remove(type);
        return of(copy);
    }

    @Override
    public FluidResource without(Supplier<? extends DataComponentType<?>> type) {
        return without(type.get());
    }

    @Override
    public DataComponentMap getComponents() {
        return stack.getComponents();
    }

    @Override
    public DataComponentPatch getComponentsPatch() {
        return stack.getComponentsPatch();
    }

    public FluidStack toStack(int amount) {
        return isEmpty() ? FluidStack.EMPTY : stack.copyWithAmount(amount);
    }

    @Override
    public boolean isComponentsPatchEmpty() {
        return stack.isComponentsPatchEmpty();
    }

    public boolean is(FluidType fluidType) {
        return stack.getFluidType() == fluidType;
    }

    public boolean is(Fluid fluid) {
        return stack.getFluid() == fluid;
    }

    public boolean matches(FluidStack other) {
        return FluidStack.isSameFluidSameComponents(stack, other);
    }

    public boolean matches(@Nullable FluidStackTemplate template) {
        return template == null ? isEmpty() : template.fluid().value() == value() && template.components().equals(getComponentsPatch());
    }

    public boolean test(Predicate<FluidStack> predicate) {
        return predicate.test(stack);
    }

    public Component getHoverName() {
        return stack.getHoverName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof FluidResource other && FluidStack.isSameFluidSameComponents(stack, other.stack);
    }

    @Override
    public int hashCode() {
        return FluidStack.hashFluidAndComponents(stack);
    }

    @Override
    public String toString() {
        return isEmpty() ? "FluidResource[EMPTY]" : "FluidResource[" + stack.getFluid() + ", " + stack.getComponentsPatch() + "]";
    }
}
