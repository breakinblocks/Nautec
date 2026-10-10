package com.breakinblocks.nautec.data.conditions;

import com.breakinblocks.nautec.NTConfig;
import net.minecraft.util.StringRepresentable;

import java.util.function.BooleanSupplier;

public enum SkyblockOption implements StringRepresentable {
    LUCKY_ZONES_ANY_BIOME("luckyZonesAnyBiome", () -> NTConfig.skyblockLuckyZonesAnyBiome),
    FISHING_LOOT("skyblockFishingLoot", () -> NTConfig.skyblockFishingLoot),
    GRAFTING_ANY_BIOME("graftingAnyBiome", () -> NTConfig.skyblockGraftingAnyBiome),
    PRISMARINE_SAND_MIXING("prismarineSandMixing", () -> NTConfig.skyblockPrismarineSandMixing),
    BUDDING_PRISMARINE_MIXING("buddingPrismarineMixing", () -> NTConfig.skyblockBuddingPrismarineMixing),
    FUEL_CELL_RESONANCE("fuelCellResonance", () -> NTConfig.skyblockFuelCellResonance),
    ELDRITCH_HEART_MIXING("eldritchHeartMixing", () -> NTConfig.skyblockEldritchHeartMixing);

    public static final StringRepresentable.EnumCodec<SkyblockOption> CODEC = StringRepresentable.fromEnum(SkyblockOption::values);

    private final String key;
    private final BooleanSupplier enabled;

    SkyblockOption(String key, BooleanSupplier enabled) {
        this.key = key;
        this.enabled = enabled;
    }

    public boolean enabled() {
        return enabled.getAsBoolean();
    }

    @Override
    public String getSerializedName() {
        return key;
    }
}
