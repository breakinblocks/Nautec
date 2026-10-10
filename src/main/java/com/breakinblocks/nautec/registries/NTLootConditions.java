package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.loot.InLuckyFishingZoneCondition;
import com.breakinblocks.nautec.loot.SkyblockOptionLootCondition;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NTLootConditions {
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Nautec.MODID);

    public static final DeferredHolder<LootItemConditionType, LootItemConditionType>
            IN_LUCKY_FISHING_ZONE = LOOT_CONDITIONS.register("in_lucky_fishing_zone",
            () -> new LootItemConditionType(InLuckyFishingZoneCondition.CODEC));

    public static final DeferredHolder<LootItemConditionType, LootItemConditionType>
            SKYBLOCK_OPTION = LOOT_CONDITIONS.register("skyblock_option",
            () -> new LootItemConditionType(SkyblockOptionLootCondition.CODEC));

    private NTLootConditions() {
    }
}
