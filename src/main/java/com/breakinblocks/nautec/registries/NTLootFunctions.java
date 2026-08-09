package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.loot.CatchAsEntityFunction;
import net.minecraft.core.registries.Registries;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NTLootFunctions {
    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, Nautec.MODID);

    public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<CatchAsEntityFunction>>
            CATCH_AS_ENTITY = LOOT_FUNCTIONS.register("catch_as_entity", () -> CatchAsEntityFunction.CODEC);

    private NTLootFunctions() {
    }
}
