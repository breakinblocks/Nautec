package com.breakinblocks.nautec.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTLootFunctions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CatchAsEntityFunction extends LootItemConditionalFunction {
    public static final MapCodec<CatchAsEntityFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            commonFields(instance).and(
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(function -> function.entity)
            ).apply(instance, CatchAsEntityFunction::new));

    private final EntityType<?> entity;

    private CatchAsEntityFunction(List<LootItemCondition> conditions, EntityType<?> entity) {
        super(conditions);
        this.entity = entity;
    }

    public static LootItemConditionalFunction.Builder<?> catchAsEntity(EntityType<?> entity) {
        return simpleBuilder(conditions -> new CatchAsEntityFunction(conditions, entity));
    }

    @Override
    protected @NotNull ItemStack run(@NotNull ItemStack stack, @NotNull LootContext context) {
        stack.set(NTDataComponents.CATCH_ENTITY.get(), this.entity);
        return stack;
    }

    @Override
    public @NotNull MapCodec<? extends LootItemConditionalFunction> codec() {
        return NTLootFunctions.CATCH_AS_ENTITY.get();
    }
}
