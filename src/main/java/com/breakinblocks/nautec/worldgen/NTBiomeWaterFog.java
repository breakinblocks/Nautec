package com.breakinblocks.nautec.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public final class NTBiomeWaterFog {
    public static final float VANILLA_END_DISTANCE = 96.0F;

    public static float endDistance(Holder<Biome> biome) {
        if (biome.is(NTBiomeKeys.ABYSSAL_TRENCH)) {
            return 22.0F;
        }
        if (biome.is(NTBiomeKeys.BIOLUMINESCENT_GROVE)) {
            return 130.0F;
        }
        if (biome.is(NTBiomeKeys.HYDROTHERMAL_VENTS)) {
            return 48.0F;
        }
        if (biome.is(NTBiomeKeys.PRISMARINE_REEF)) {
            return 145.0F;
        }
        return -1.0F;
    }

    private NTBiomeWaterFog() {
    }
}
