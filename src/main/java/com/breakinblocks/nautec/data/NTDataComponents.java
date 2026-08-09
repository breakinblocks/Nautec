package com.breakinblocks.nautec.data;

import com.mojang.serialization.Codec;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.data.components.ComponentBacteriaStorage;
import com.breakinblocks.nautec.data.components.ComponentPowerStorage;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class NTDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Nautec.MODID);

    public static final Supplier<DataComponentType<ComponentPowerStorage>> POWER = registerDataComponentType("power",
            () -> builder -> builder.persistent(ComponentPowerStorage.CODEC).networkSynchronized(ComponentPowerStorage.STREAM_CODEC));
    public static final Supplier<DataComponentType<SimpleFluidContent>> FLUID = registerDataComponentType("fluid",
            () -> builder -> builder.persistent(SimpleFluidContent.CODEC).networkSynchronized(SimpleFluidContent.STREAM_CODEC));
    public static final Supplier<DataComponentType<ComponentBacteriaStorage>> BACTERIA = registerDataComponentType("bacteria",
            () -> builder -> builder
                    .persistent(ComponentBacteriaStorage.CODEC)
                    .networkSynchronized(ComponentBacteriaStorage.STREAM_CODEC));

    public static final Supplier<DataComponentType<Boolean>> ABILITY_ENABLED = registerDataComponentType("ability_enabled",
            () -> builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final Supplier<DataComponentType<Boolean>> IS_INFUSED = registerDataComponentType("is_infused",
            () -> builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final Supplier<DataComponentType<Integer>> OXYGEN = registerDataComponentType("oxygen",
            () -> builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT));

    public static final Supplier<DataComponentType<EntityType<?>>> CATCH_ENTITY = registerDataComponentType("catch_entity",
            () -> builder -> builder
                    .persistent(BuiltInRegistries.ENTITY_TYPE.byNameCodec())
                    .networkSynchronized(ByteBufCodecs.registry(Registries.ENTITY_TYPE)));

    public static <T> Supplier<DataComponentType<T>> registerDataComponentType(
            String name, Supplier<UnaryOperator<DataComponentType.Builder<T>>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.get().apply(DataComponentType.builder()).build());
    }
}
