package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.worldgen.feature.GlowGrottoFeature;
import com.breakinblocks.nautec.worldgen.feature.VentFieldFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NTFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Nautec.MODID);

    public static final DeferredHolder<Feature<?>, VentFieldFeature> VENT_FIELD = FEATURES.register("vent_field",
            () -> new VentFieldFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, GlowGrottoFeature> GLOW_GROTTO = FEATURES.register("glow_grotto",
            () -> new GlowGrottoFeature(NoneFeatureConfiguration.CODEC));

    private NTFeatures() {
    }
}
