package com.breakinblocks.nautec.loot;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.nautec.content.fishing.LuckyZoneIndex;
import com.breakinblocks.nautec.registries.NTLootConditions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class InLuckyFishingZoneCondition implements LootItemCondition {
    public static final InLuckyFishingZoneCondition INSTANCE = new InLuckyFishingZoneCondition();
    public static final MapCodec<InLuckyFishingZoneCondition> CODEC = MapCodec.unit(INSTANCE);

    private InLuckyFishingZoneCondition() {
    }

    public static LootItemCondition.Builder builder() {
        return () -> INSTANCE;
    }

    @Override
    public @NotNull MapCodec<? extends LootItemCondition> codec() {
        return NTLootConditions.IN_LUCKY_FISHING_ZONE.get();
    }

    @Override
    public @NotNull Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }

    @Override
    public boolean test(LootContext context) {
        Vec3 origin = context.getOptionalParameter(LootContextParams.ORIGIN);
        if (origin == null) {
            return false;
        }
        ServerLevel level = context.getLevel();
        return LuckyZoneIndex.get(level).zoneAt(level, BlockPos.containing(origin)) != null;
    }
}
