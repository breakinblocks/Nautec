package com.breakinblocks.nautec.worldgen.injection;

import com.mojang.datafixers.util.Pair;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public final class BiomeInjector {
    private static final String LITHOSTITCHED = "lithostitched";

    public static Climate.ParameterList<Holder<Biome>> inject(MultiNoiseBiomeSourceParameterList.Preset preset,
                                                              Climate.ParameterList<Holder<Biome>> vanilla,
                                                              HolderGetter<Biome> biomes) {
        if (!NTConfig.biomeInjectionEnabled() || !NTConfig.injectableWorldPresets().contains(preset.id().toString())) {
            return vanilla;
        }

        ModList modList = ModList.get();
        if (modList == null) {
            return vanilla;
        }

        if (modList.isLoaded(LITHOSTITCHED)) {
            Nautec.LOGGER.info("Lithostitched is present, leaving preset {} to the biome injectors in data/nautec/lithostitched/biome_injector", preset.id());
            return vanilla;
        }

        if (!(biomes instanceof HolderLookup.RegistryLookup<Biome> loadedBiomes)) {
            return vanilla;
        }

        List<Pair<Climate.ParameterPoint, Holder<Biome>>> additions = new ArrayList<>();
        NTOceanRegion.forEachPoint((point, key) -> loadedBiomes.get(key)
                .ifPresent(holder -> additions.add(Pair.of(point, holder))));

        if (additions.isEmpty()) {
            Nautec.LOGGER.warn("Biome injection is enabled but none of Nautec's ocean biomes are loaded, leaving preset {} alone", preset.id());
            return vanilla;
        }

        Climate.ParameterList<Holder<Biome>> merged = ParameterListMerger.carveAndAppend(vanilla, additions);
        Nautec.LOGGER.debug("Injected {} ocean climate points into preset {} ({} -> {} total)",
                additions.size(), preset.id(), vanilla.values().size(), merged.values().size());
        return merged;
    }

    private BiomeInjector() {
    }
}
