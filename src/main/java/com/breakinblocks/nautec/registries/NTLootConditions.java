package com.breakinblocks.nautec.registries;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.loot.InLuckyFishingZoneCondition;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NTLootConditions {
    public static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITIONS =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Nautec.MODID);

    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<InLuckyFishingZoneCondition>>
            IN_LUCKY_FISHING_ZONE = LOOT_CONDITIONS.register("in_lucky_fishing_zone",
            () -> InLuckyFishingZoneCondition.CODEC);

    private NTLootConditions() {
    }
}
