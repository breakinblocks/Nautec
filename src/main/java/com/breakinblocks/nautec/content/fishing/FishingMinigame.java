package com.breakinblocks.nautec.content.fishing;

import com.breakinblocks.nautec.registries.NTLootTables;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public final class FishingMinigame {
    public static final int DURATION_TICKS = 60;
    public static final int TREASURE_CHANCE = 15;

    public static List<ResourceKey<LootTable>> rewardTables(boolean minigameSucceeded, boolean baseWasTreasure) {
        if (!minigameSucceeded) {
            return List.of(baseWasTreasure ? NTLootTables.LUCKY_ZONE_TREASURE : NTLootTables.LUCKY_ZONE_CATCH);
        }
        if (baseWasTreasure) {
            return List.of(NTLootTables.LUCKY_ZONE_TREASURE, NTLootTables.LUCKY_ZONE_TREASURE, NTLootTables.LUCKY_ZONE_CATCH);
        }
        return List.of(NTLootTables.LUCKY_ZONE_CATCH, NTLootTables.LUCKY_ZONE_TREASURE);
    }

    private FishingMinigame() {
    }
}
