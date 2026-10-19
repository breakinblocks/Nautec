package com.breakinblocks.nautec.worldgen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class DeeperOceans {
    private static final List<String> VARIANTS = List.of("overworld", "overworld_amplified", "overworld_large_biomes");
    private static final List<String> FUNCTIONS = List.of("depth", "sloped_cheese");
    private static final ThreadLocal<Swap> ACTIVE = new ThreadLocal<>();

    private DeeperOceans() {
    }

    private static final class Swap {
        private final Map<ResourceKey<DensityFunction>, Holder<DensityFunction>> replacements;
        private boolean used;

        private Swap(Map<ResourceKey<DensityFunction>, Holder<DensityFunction>> replacements) {
            this.replacements = replacements;
        }
    }

    public static boolean enabledIn(ResourceKey<Level> dimension) {
        return !NTConfig.deeperOceanDimensionBlacklist.contains(dimension.location().toString());
    }

    public static <T> T applyIn(ResourceKey<Level> dimension, RegistryAccess registries, Supplier<T> action) {
        if (!enabledIn(dimension)) {
            return action.get();
        }

        Map<ResourceKey<DensityFunction>, Holder<DensityFunction>> replacements = replacements(registries);
        if (replacements.isEmpty()) {
            return action.get();
        }

        Swap previous = ACTIVE.get();
        Swap swap = new Swap(replacements);
        ACTIVE.set(swap);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                ACTIVE.remove();
            } else {
                ACTIVE.set(previous);
            }
            if (swap.used) {
                Nautec.LOGGER.info("Deeper oceans are active in {}", dimension.location());
            }
        }
    }

    public static Holder<DensityFunction> resolve(Holder<DensityFunction> holder) {
        Swap swap = ACTIVE.get();
        if (swap == null) {
            return holder;
        }

        Holder<DensityFunction> replacement = holder.unwrapKey().map(swap.replacements::get).orElse(null);
        if (replacement == null) {
            return holder;
        }
        swap.used = true;
        return replacement;
    }

    private static Map<ResourceKey<DensityFunction>, Holder<DensityFunction>> replacements(RegistryAccess registries) {
        Registry<DensityFunction> registry = registries.registryOrThrow(Registries.DENSITY_FUNCTION);
        Map<ResourceKey<DensityFunction>, Holder<DensityFunction>> replacements = new HashMap<>();
        for (String variant : VARIANTS) {
            for (String function : FUNCTIONS) {
                String path = variant + "/" + function;
                ResourceKey<DensityFunction> vanilla = ResourceKey.create(Registries.DENSITY_FUNCTION, ResourceLocation.withDefaultNamespace(path));
                if (!fromVanilla(registry, vanilla)) {
                    continue;
                }
                registry.getHolder(ResourceKey.create(Registries.DENSITY_FUNCTION, Nautec.rl(path)))
                        .ifPresent(ours -> replacements.put(vanilla, ours));
            }
        }
        return replacements;
    }

    private static boolean fromVanilla(Registry<DensityFunction> registry, ResourceKey<DensityFunction> key) {
        return registry.registrationInfo(key)
                .flatMap(RegistrationInfo::knownPackInfo)
                .map(KnownPack::isVanilla)
                .orElse(false);
    }
}
