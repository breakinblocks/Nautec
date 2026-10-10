package com.breakinblocks.nautec.worldgen;

import com.breakinblocks.nautec.datagen.DatapackRegistryProvider;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTParticles;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.world.entity.EntityType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class NTBiomes {
    private static final int DEFAULT_FOG_COLOR = 0xC0D8FF;

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);

        context.register(NTBiomeKeys.ABYSSAL_TRENCH, abyssalTrench(placedFeatures, carvers));
        context.register(NTBiomeKeys.BIOLUMINESCENT_GROVE, bioluminescentGrove(placedFeatures, carvers));
        context.register(NTBiomeKeys.HYDROTHERMAL_VENTS, hydrothermalVents(placedFeatures, carvers));
        context.register(NTBiomeKeys.PRISMARINE_REEF, prismarineReef(placedFeatures, carvers));
    }

    private static Biome abyssalTrench(HolderGetter<PlacedFeature> placedFeatures, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        MobSpawnSettings.Builder mobs = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.oceanSpawns(mobs, 3, 4, 4);
        mobs.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 20, 1, 3));
        mobs.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(NTEntities.ABYSSAL_MAW.get(), 25, 1, 2));
        mobs.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(NTEntities.SILT_SKIPPER.get(), 8, 3, 6));

        BiomeGenerationSettings.Builder generation = baseOceanGeneration(placedFeatures, carvers);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DatapackRegistryProvider.BUDDING_PRISMARINE_PLACE_KEY);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DatapackRegistryProvider.ABYSSAL_CORAL_PLACE_KEY);

        return baseOceanBiome(0.3F, effects(0.3F, 0x081A2B, 0x03080F, 0x0A1520)
                .ambientParticle(new AmbientParticleSettings(NTParticles.ABYSSAL_MOTE.get(), 0.004F)))
                .mobSpawnSettings(mobs.build())
                .generationSettings(generation.build())
                .build();
    }

    private static Biome bioluminescentGrove(HolderGetter<PlacedFeature> placedFeatures, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        MobSpawnSettings.Builder mobs = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.oceanSpawns(mobs, 6, 4, 12);
        mobs.addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GLOW_SQUID, 20, 2, 4));
        mobs.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(NTEntities.LANTERN_JELLY.get(), 12, 1, 3));
        mobs.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(NTEntities.SILT_SKIPPER.get(), 15, 4, 8));

        BiomeGenerationSettings.Builder generation = baseOceanGeneration(placedFeatures, carvers);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_DEEP);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DatapackRegistryProvider.DEEP_KELP_PLACE_KEY);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DatapackRegistryProvider.LUMINESCENT_ALGAE_PLACE_KEY);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEA_PICKLE);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DatapackRegistryProvider.GLOW_POLYP_PLACE_KEY);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DatapackRegistryProvider.BUDDING_PRISMARINE_PLACE_KEY);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DatapackRegistryProvider.GLOW_GROTTO_PLACE_KEY);

        return baseOceanBiome(0.5F, effects(0.5F, 0x1C8C81, 0x0B4F4A, DEFAULT_FOG_COLOR)
                .ambientParticle(new AmbientParticleSettings(NTParticles.GLOW_SPORE.get(), 0.0035F)))
                .mobSpawnSettings(mobs.build())
                .generationSettings(generation.build())
                .build();
    }

    private static Biome hydrothermalVents(HolderGetter<PlacedFeature> placedFeatures, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        MobSpawnSettings.Builder mobs = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.oceanSpawns(mobs, 4, 3, 6);
        mobs.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(NTEntities.VENT_CRAWLER.get(), 20, 1, 3));

        BiomeGenerationSettings.Builder generation = baseOceanGeneration(placedFeatures, carvers);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DatapackRegistryProvider.VENT_BASALT_PLACE_KEY);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DatapackRegistryProvider.VENT_MAGMA_PLACE_KEY);
        generation.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, DatapackRegistryProvider.VENT_FIELD_PLACE_KEY);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DatapackRegistryProvider.VENT_TUBEWORM_PLACE_KEY);

        return baseOceanBiome(0.85F, effects(0.85F, 0x4A3527, 0x2B1C12, DEFAULT_FOG_COLOR)
                .ambientParticle(new AmbientParticleSettings(NTParticles.VENT_BUBBLE.get(), 0.012F)))
                .mobSpawnSettings(mobs.build())
                .generationSettings(generation.build())
                .build();
    }

    private static Biome prismarineReef(HolderGetter<PlacedFeature> placedFeatures, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        MobSpawnSettings.Builder mobs = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.warmOceanSpawns(mobs, 10, 4);
        mobs.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8))
                .addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 3, 1, 2))
                .addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(NTEntities.SILT_SKIPPER.get(), 12, 4, 8));

        BiomeGenerationSettings.Builder generation = baseOceanGeneration(placedFeatures, carvers);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.WARM_OCEAN_VEGETATION);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_WARM);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEA_PICKLE);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DatapackRegistryProvider.PRISMARINE_FROND_PLACE_KEY);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DatapackRegistryProvider.REEF_PRISMARINE_PLACE_KEY);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DatapackRegistryProvider.REEF_BUDDING_PRISMARINE_PLACE_KEY);

        return baseOceanBiome(0.9F, effects(0.9F, 0x25C4B4, 0x1B9A90, DEFAULT_FOG_COLOR))
                .mobSpawnSettings(mobs.build())
                .generationSettings(generation.build())
                .build();
    }

    private static Biome.BiomeBuilder baseOceanBiome(float temperature, BiomeSpecialEffects.Builder effects) {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(temperature)
                .downfall(0.5F)
                .specialEffects(effects.build());
    }

    private static BiomeSpecialEffects.Builder effects(float temperature, int waterColor, int waterFogColor, int fogColor) {
        return new BiomeSpecialEffects.Builder()
                .waterColor(waterColor)
                .waterFogColor(waterFogColor)
                .fogColor(fogColor)
                .skyColor(calculateSkyColor(temperature))
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS);
    }

    private static int calculateSkyColor(float temperature) {
        float t = Mth.clamp(temperature / 3.0F, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.62222224F - t * 0.05F, 0.5F + t * 0.1F, 1.0F);
    }

    private static BiomeGenerationSettings.Builder baseOceanGeneration(HolderGetter<PlacedFeature> placedFeatures,
                                                                      HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        BiomeDefaultFeatures.addDefaultCarversAndLakes(generation);
        BiomeDefaultFeatures.addDefaultCrystalFormations(generation);
        BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(generation);
        BiomeDefaultFeatures.addDefaultSprings(generation);
        BiomeDefaultFeatures.addSurfaceFreezing(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
        BiomeDefaultFeatures.addWaterTrees(generation);
        BiomeDefaultFeatures.addDefaultFlowers(generation);
        BiomeDefaultFeatures.addDefaultGrass(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generation);
        return generation;
    }

    private NTBiomes() {
    }
}
