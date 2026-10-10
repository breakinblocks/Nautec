package com.breakinblocks.nautec.loot;

import com.breakinblocks.nautec.data.conditions.SkyblockOption;
import com.breakinblocks.nautec.registries.NTLootConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

public record SkyblockOptionLootCondition(SkyblockOption option) implements LootItemCondition {
    public static final MapCodec<SkyblockOptionLootCondition> CODEC = SkyblockOption.CODEC.fieldOf("option")
            .xmap(SkyblockOptionLootCondition::new, SkyblockOptionLootCondition::option);

    public static LootItemCondition.Builder builder(SkyblockOption option) {
        SkyblockOptionLootCondition condition = new SkyblockOptionLootCondition(option);
        return () -> condition;
    }

    @Override
    public @NotNull MapCodec<? extends LootItemCondition> codec() {
        return NTLootConditions.SKYBLOCK_OPTION.get();
    }

    @Override
    public boolean test(LootContext context) {
        return option.enabled();
    }
}
