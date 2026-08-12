package com.breakinblocks.nautec.gametest.suite;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.worldgen.OceanClimates;
import com.breakinblocks.nautec.worldgen.injection.NTOceanRegion;
import com.breakinblocks.nautec.worldgen.injection.ParameterListMerger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class WorldgenInjectionTests {
    private static final float OCEAN_CONTINENTALNESS_MAX = -0.19F;
    private static final ResourceKey<Biome> VANILLA = ResourceKey.create(Registries.BIOME, Identifier.withDefaultNamespace("deep_ocean"));

    private static List<String> featureNames(HolderSet<PlacedFeature> step) {
        List<String> names = new ArrayList<>();
        for (Holder<PlacedFeature> feature : step) {
            feature.unwrapKey().ifPresent(key -> names.add(key.identifier().toString()));
        }
        return names;
    }

    private static void assertParameter(GameTestHelper helper,
                                        JsonObject parameters, String key,
                                        NTOceanRegion.Slice slice, Climate.Parameter parameter, String biome) {
        if (!slice.constrains(parameter)) {
            if (parameters.has(key)) {
                helper.fail(biome + " injector constrains " + key + " but the slice leaves it unbounded");
            }
            return;
        }
        if (!parameters.has(key)) {
            helper.fail(biome + " injector is missing " + key + ", which the slice constrains");
            return;
        }
        JsonArray span = parameters.getAsJsonArray(key);
        float min = Climate.unquantizeCoord(parameter.min());
        float max = Climate.unquantizeCoord(parameter.max());
        if (Math.abs(span.get(0).getAsFloat() - min) > 1.0e-4f || Math.abs(span.get(1).getAsFloat() - max) > 1.0e-4f) {
            helper.fail(biome + " injector " + key + " is " + span + " but the slice is [" + min + ", " + max + "]");
        }
    }

    public static void register(NTTestRegistrar r) {
        r.add("worldgen/overworld_preset_contains_our_biomes", 20, helper -> {
            HolderLookup.RegistryLookup<Biome> biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);

            for (ResourceKey<Biome> key : NTOceanRegion.biomes()) {
                if (biomes.get(key).isEmpty()) {
                    helper.fail("Biome " + key.identifier() + " is not loaded, its datapack entry is missing");
                }
            }

            MultiNoiseBiomeSourceParameterList list =
                    new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD, biomes);

            Set<ResourceKey<Biome>> present = list.parameters().values().stream()
                    .map(Pair::getSecond)
                    .map(Holder::getKey)
                    .collect(Collectors.toSet());

            for (ResourceKey<Biome> key : NTOceanRegion.biomes()) {
                if (!present.contains(key)) {
                    helper.fail("The overworld preset does not contain " + key.identifier() + " after injection");
                }
            }

            if (!present.contains(Biomes.DEEP_OCEAN) || !present.contains(Biomes.WARM_OCEAN)) {
                helper.fail("Injection removed a vanilla ocean biome from the preset entirely");
            }
            helper.succeed();
        });

        r.add("worldgen/lithostitched_injectors_match_our_slices", 20, helper -> {
            for (NTOceanRegion.Slice slice : NTOceanRegion.slices()) {
                String name = slice.biome().identifier().getPath();
                Identifier id = Identifier.fromNamespaceAndPath(Nautec.MODID,
                        "lithostitched/biome_injector/" + name + ".json");

                var resource = helper.getLevel().getServer().getResourceManager().getResource(id);
                if (resource.isEmpty()) {
                    helper.fail("Missing Lithostitched biome injector for " + name
                            + ". Packs with Lithostitched rely on these instead of the parameter list mixin.");
                    return;
                }

                JsonObject json;
                try (var reader = resource.get().openAsReader()) {
                    json = JsonParser.parseReader(reader).getAsJsonObject();
                } catch (Exception e) {
                    helper.fail("Could not read the Lithostitched injector for " + name + ": " + e);
                    return;
                }

                helper.assertValueEqual(slice.biome().identifier().toString(),
                        json.get("replacement").getAsString(), "injector replacement for " + name);
                helper.assertValueEqual("minecraft:overworld",
                        json.get("dimension").getAsString(), "injector dimension for " + name);

                JsonObject parameters = json.getAsJsonObject("parameters");
                assertParameter(helper, parameters, "temperature", slice, slice.temperature(), name);
                assertParameter(helper, parameters, "humidity", slice, slice.humidity(), name);
                assertParameter(helper, parameters, "continentalness", slice, slice.continentalness(), name);
                assertParameter(helper, parameters, "erosion", slice, slice.erosion(), name);
                assertParameter(helper, parameters, "weirdness", slice, slice.weirdness(), name);
            }
            helper.succeed();
        });

        r.add("worldgen/biomes_invent_no_feature_ordering", 20, helper -> {
            HolderLookup.RegistryLookup<Biome> biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);

            Set<String> foreignAdjacencies = new HashSet<>();
            List<Holder.Reference<Biome>> all = biomes.listElements().toList();
            for (Holder.Reference<Biome> holder : all) {
                if (holder.key().identifier().getNamespace().equals(Nautec.MODID)) {
                    continue;
                }
                List<HolderSet<PlacedFeature>> steps =
                        holder.value().getGenerationSettings().features();
                for (int step = 0; step < steps.size(); step++) {
                    List<String> names = featureNames(steps.get(step));
                    for (int i = 0; i + 1 < names.size(); i++) {
                        foreignAdjacencies.add(step + "|" + names.get(i) + "|" + names.get(i + 1));
                    }
                }
            }

            for (ResourceKey<Biome> key : NTOceanRegion.biomes()) {
                Biome biome = biomes.getOrThrow(key).value();
                List<HolderSet<PlacedFeature>> steps =
                        biome.getGenerationSettings().features();
                for (int step = 0; step < steps.size(); step++) {
                    List<String> names = featureNames(steps.get(step));
                    for (int i = 0; i + 1 < names.size(); i++) {
                        String a = names.get(i);
                        String b = names.get(i + 1);
                        if (a.startsWith(Nautec.MODID + ":") || b.startsWith(Nautec.MODID + ":")) {
                            continue;
                        }
                        if (!foreignAdjacencies.contains(step + "|" + a + "|" + b)) {
                            helper.fail(key.identifier() + " orders " + a + " before " + b + " at generation step "
                                    + step + ", an ordering no other biome establishes. Two non-Nautec features must "
                                    + "only appear adjacent in an order vanilla already uses, otherwise a biome mod "
                                    + "that disagrees produces a feature order cycle and the world fails to generate.");
                        }
                    }
                }
            }
            helper.succeed();
        });

        r.add("worldgen/injected_biome_share_is_sane", 100, helper -> {
            Climate.ParameterList<ResourceKey<Biome>> vanilla =
                    MultiNoiseBiomeSourceParameterList.knownPresets().get(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD);

            List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> injected = new ArrayList<>();
            NTOceanRegion.forEachPoint((p, key) -> injected.add(Pair.of(p, key)));
            Climate.ParameterList<ResourceKey<Biome>> merged = ParameterListMerger.carveAndAppend(vanilla, injected);

            RandomSource random = RandomSource.create(20260808L);
            Map<ResourceKey<Biome>, Integer> before = new HashMap<>();
            Map<ResourceKey<Biome>, Integer> after = new HashMap<>();

            int samples = 60000;
            for (int i = 0; i < samples; i++) {
                float continentalness = -1.05F + random.nextFloat() * (OCEAN_CONTINENTALNESS_MAX - -1.05F);
                Climate.TargetPoint target = Climate.target(
                        random.nextFloat() * 2.0F - 1.0F,
                        random.nextFloat() * 2.0F - 1.0F,
                        continentalness,
                        random.nextFloat() * 2.0F - 1.0F,
                        0.0F,
                        random.nextFloat() * 2.0F - 1.0F);
                before.merge(vanilla.findValue(target), 1, Integer::sum);
                after.merge(merged.findValue(target), 1, Integer::sum);
            }

            Nautec.LOGGER.info("--- ocean surface climate share, {} samples (continentalness -1.05..{}, depth 0) ---", samples, OCEAN_CONTINENTALNESS_MAX);
            after.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .forEach(e -> Nautec.LOGGER.info(String.format("  %-42s %5.2f%%  (was %5.2f%%)",
                            e.getKey().identifier(),
                            100.0 * e.getValue() / samples,
                            100.0 * before.getOrDefault(e.getKey(), 0) / samples)));

            int injectedTotal = 0;
            for (ResourceKey<Biome> key : NTOceanRegion.biomes()) {
                double share = 100.0 * after.getOrDefault(key, 0) / samples;
                injectedTotal += after.getOrDefault(key, 0);
                if (share < 2.0) {
                    helper.fail(key.identifier() + " claims only " + String.format("%.2f", share)
                            + "% of ocean surface climate space, players will rarely find it (want 3-6%)");
                }
                if (share > 10.0) {
                    helper.fail(key.identifier() + " claims " + String.format("%.2f", share)
                            + "% of ocean surface climate space, it is crowding out vanilla oceans (want 3-6%)");
                }
            }

            double injectedShare = 100.0 * injectedTotal / samples;
            Nautec.LOGGER.info("  === Nautec total: {}% of ocean climate space ===", String.format("%.2f", injectedShare));
            if (injectedShare > 30.0) {
                helper.fail("Nautec biomes claim " + String.format("%.2f", injectedShare) + "% of all ocean climate space, leaving too little vanilla ocean");
            }
            helper.succeed();
        });

        r.add("worldgen/merger_leaves_disjoint_points_alone", 5, helper -> {
            Climate.ParameterPoint target = point(OceanClimates.FROZEN_TEMPERATURE, OceanClimates.DEEP_OCEAN_CONTINENTALNESS, OceanClimates.SURFACE_DEPTH);
            Climate.ParameterPoint cut = point(OceanClimates.WARM_TEMPERATURE, OceanClimates.DEEP_OCEAN_CONTINENTALNESS, OceanClimates.SURFACE_DEPTH);

            List<Climate.ParameterPoint> remainder = ParameterListMerger.subtract(target, cut);
            if (remainder.size() != 1 || !remainder.getFirst().equals(target)) {
                helper.fail("A non-overlapping cut must leave the point untouched, got " + remainder);
            }
            helper.succeed();
        });

        r.add("worldgen/merger_removes_fully_covered_points", 5, helper -> {
            Climate.ParameterPoint target = point(OceanClimates.NORMAL_TEMPERATURE, Climate.Parameter.span(-0.8F, -0.5F), OceanClimates.SURFACE_DEPTH);
            Climate.ParameterPoint cut = point(OceanClimates.FULL_RANGE, OceanClimates.FULL_RANGE, OceanClimates.SURFACE_DEPTH);

            List<Climate.ParameterPoint> remainder = ParameterListMerger.subtract(target, cut);
            if (!remainder.isEmpty()) {
                helper.fail("A cut covering the whole point must remove it, got " + remainder);
            }
            helper.succeed();
        });

        r.add("worldgen/merger_depth_variants_are_independent", 5, helper -> {
            Climate.ParameterPoint underground = point(OceanClimates.NORMAL_TEMPERATURE, OceanClimates.DEEP_OCEAN_CONTINENTALNESS, OceanClimates.UNDERGROUND_DEPTH);
            Climate.ParameterPoint surfaceCut = point(OceanClimates.FULL_RANGE, OceanClimates.FULL_RANGE, OceanClimates.SURFACE_DEPTH);

            List<Climate.ParameterPoint> remainder = ParameterListMerger.subtract(underground, surfaceCut);
            if (remainder.size() != 1) {
                helper.fail("A surface cut must not touch the underground copy of a biome, got " + remainder);
            }
            helper.succeed();
        });

        r.add("worldgen/merger_preserves_coverage", 5, helper -> {
            Climate.ParameterPoint vanilla = point(OceanClimates.NORMAL_TEMPERATURE, OceanClimates.DEEP_OCEAN_CONTINENTALNESS, OceanClimates.SURFACE_DEPTH);
            Climate.ParameterPoint cut = point(OceanClimates.NORMAL_TEMPERATURE, Climate.Parameter.span(-0.85F, -0.455F), OceanClimates.SURFACE_DEPTH);

            List<Climate.ParameterPoint> remainder = ParameterListMerger.subtract(vanilla, cut);
            long covered = remainder.stream().mapToLong(WorldgenInjectionTests::volume).sum() + volume(intersection(vanilla, cut));
            if (covered != volume(vanilla)) {
                helper.fail("Carving lost or duplicated climate volume: " + covered + " != " + volume(vanilla));
            }
            for (Climate.ParameterPoint piece : remainder) {
                if (overlaps(piece, cut)) {
                    helper.fail("A carved piece still overlaps the injected slice: " + piece);
                }
            }
            helper.succeed();
        });

        r.add("worldgen/injected_slices_resolve_to_our_biomes", 5, helper -> {
            List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> injected = new ArrayList<>();
            NTOceanRegion.forEachPoint((p, key) -> injected.add(Pair.of(p, key)));

            if (injected.size() != NTOceanRegion.biomes().size() * 2) {
                helper.fail("Every slice must be injected at both the surface and underground depth, got " + injected.size());
            }

            Climate.ParameterList<ResourceKey<Biome>> base = new Climate.ParameterList<>(List.of(
                    Pair.of(point(OceanClimates.FULL_RANGE, OceanClimates.DEEP_OCEAN_CONTINENTALNESS, OceanClimates.SURFACE_DEPTH), VANILLA),
                    Pair.of(point(OceanClimates.FULL_RANGE, OceanClimates.DEEP_OCEAN_CONTINENTALNESS, OceanClimates.UNDERGROUND_DEPTH), VANILLA),
                    Pair.of(point(OceanClimates.FULL_RANGE, OceanClimates.OCEAN_CONTINENTALNESS, OceanClimates.SURFACE_DEPTH), VANILLA),
                    Pair.of(point(OceanClimates.FULL_RANGE, OceanClimates.OCEAN_CONTINENTALNESS, OceanClimates.UNDERGROUND_DEPTH), VANILLA)
            ));

            Climate.ParameterList<ResourceKey<Biome>> merged = ParameterListMerger.carveAndAppend(base, injected);

            for (Pair<Climate.ParameterPoint, ResourceKey<Biome>> slice : injected) {
                ResourceKey<Biome> found = merged.findValue(centre(slice.getFirst()));
                if (!slice.getSecond().equals(found)) {
                    helper.fail("The centre of " + slice.getSecond().identifier() + "'s slice resolved to " + found.identifier());
                }
            }

            Climate.TargetPoint outside = Climate.target(0.0F, 0.0F, -0.25F, 0.0F, 0.0F, 0.9F);
            if (!VANILLA.equals(merged.findValue(outside))) {
                helper.fail("A climate outside every injected slice must still resolve to the vanilla biome");
            }
            helper.succeed();
        });
    }

    private static Climate.ParameterPoint point(Climate.Parameter temperature, Climate.Parameter continentalness, Climate.Parameter depth) {
        return new Climate.ParameterPoint(temperature, OceanClimates.FULL_RANGE, continentalness, OceanClimates.FULL_RANGE, depth, OceanClimates.FULL_RANGE, 0L);
    }

    private static Climate.TargetPoint centre(Climate.ParameterPoint point) {
        return new Climate.TargetPoint(
                mid(point.temperature()), mid(point.humidity()), mid(point.continentalness()),
                mid(point.erosion()), mid(point.depth()), mid(point.weirdness()));
    }

    private static long mid(Climate.Parameter parameter) {
        return parameter.min() + (parameter.max() - parameter.min()) / 2L;
    }

    private static long volume(Climate.ParameterPoint point) {
        return span(point.temperature()) * span(point.humidity()) * span(point.continentalness())
                * span(point.erosion()) * span(point.depth()) * span(point.weirdness());
    }

    private static long span(Climate.Parameter parameter) {
        return parameter.max() - parameter.min() + 1L;
    }

    private static Climate.ParameterPoint intersection(Climate.ParameterPoint a, Climate.ParameterPoint b) {
        return new Climate.ParameterPoint(
                overlap(a.temperature(), b.temperature()),
                overlap(a.humidity(), b.humidity()),
                overlap(a.continentalness(), b.continentalness()),
                overlap(a.erosion(), b.erosion()),
                overlap(a.depth(), b.depth()),
                overlap(a.weirdness(), b.weirdness()),
                0L);
    }

    private static Climate.Parameter overlap(Climate.Parameter a, Climate.Parameter b) {
        return new Climate.Parameter(Math.max(a.min(), b.min()), Math.min(a.max(), b.max()));
    }

    private static boolean overlaps(Climate.ParameterPoint a, Climate.ParameterPoint b) {
        return overlaps(a.temperature(), b.temperature()) && overlaps(a.humidity(), b.humidity())
                && overlaps(a.continentalness(), b.continentalness()) && overlaps(a.erosion(), b.erosion())
                && overlaps(a.depth(), b.depth()) && overlaps(a.weirdness(), b.weirdness());
    }

    private static boolean overlaps(Climate.Parameter a, Climate.Parameter b) {
        return a.min() <= b.max() && b.min() <= a.max();
    }

    private WorldgenInjectionTests() {
    }
}
