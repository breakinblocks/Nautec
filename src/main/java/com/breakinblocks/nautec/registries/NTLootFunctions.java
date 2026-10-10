package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.loot.CatchAsEntityFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NTLootFunctions {
    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, Nautec.MODID);

    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<CatchAsEntityFunction>>
            CATCH_AS_ENTITY = LOOT_FUNCTIONS.register("catch_as_entity", () -> new LootItemFunctionType<>(CatchAsEntityFunction.CODEC));

    private NTLootFunctions() {
    }
}
