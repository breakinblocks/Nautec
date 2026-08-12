package com.breakinblocks.nautec.data.generated;

import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.content.bacteria.SimpleBacteria;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.google.gson.JsonElement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class BacteriaPresets {
    private BacteriaPresets() {
    }

    public record Result(List<String> written, List<String> skipped) {
    }

    public static Result applyAll(HolderLookup.Provider registries) {
        List<String> written = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        GeneratedPackFinder.scaffold();

        for (BacteriaPreset preset : BacteriaPresetManager.all()) {
            if (!preset.enabled()) {
                continue;
            }
            if (!GeneratedPackPaths.isValidName(preset.name())) {
                Nautec.LOGGER.warn("Bacteria preset '{}' has a name that cannot be a file, skipping it", preset.name());
                continue;
            }
            if (Files.exists(GeneratedPackPaths.bacteriaFile(preset.name()))) {
                continue;
            }
            if (NTBacterias.BACTERIAS.contains(ResourceKey.create(NTRegistries.BACTERIA_KEY, Nautec.rl(preset.name())))) {
                Nautec.LOGGER.warn("Bacteria preset '{}' collides with a bacteria Nautec ships, skipping it", preset.name());
                continue;
            }

            Optional<HolderSet<Item>> nutrient = firstResolved(preset.nutrient());
            Item resource = resolveResource(preset.resource());
            if (nutrient.isEmpty() || resource == null) {
                skipped.add(preset.name());
                continue;
            }

            if (write(preset, nutrient.get(), resource, registries)) {
                written.add(preset.name());
            }
        }

        return new Result(written, skipped);
    }

    private static boolean write(BacteriaPreset preset, HolderSet<Item> nutrient, Item resource, HolderLookup.Provider registries) {
        Identifier id = GeneratedPackPaths.bacteriaId(preset.name());
        ResourceKey<Bacteria> key = GeneratedPackPaths.bacteriaKey(preset.name());
        SimpleBacteria bacteria = BacteriaBalance.buildBacteria(resource, preset.rarity(), id);

        try {
            JsonElement bacteriaJson = BacteriaJsonWriter.encodeBacteria(bacteria, registries);
            JsonElement incubationJson = BacteriaJsonWriter.encodeRecipe(
                    BacteriaBalance.incubationRecipe(key, Ingredient.of(nutrient), preset.rarity()), registries);
            JsonElement mutationJson = BacteriaJsonWriter.encodeRecipe(
                    BacteriaBalance.mutationRecipe(key, Ingredient.of(resource), preset.rarity()), registries);

            BacteriaJsonWriter.writeAtomic(GeneratedPackPaths.bacteriaFile(preset.name()), BacteriaJsonWriter.pretty(bacteriaJson));
            BacteriaJsonWriter.writeAtomic(GeneratedPackPaths.incubationRecipeFile(preset.name()), BacteriaJsonWriter.pretty(incubationJson));
            BacteriaJsonWriter.writeAtomic(GeneratedPackPaths.mutationRecipeFile(preset.name()), BacteriaJsonWriter.pretty(mutationJson));
            return true;
        } catch (Exception e) {
            Nautec.LOGGER.error("Could not write the files for bacteria preset '{}'", preset.name(), e);
            return false;
        }
    }

    public static Optional<HolderSet<Item>> firstResolved(List<MaterialRef> candidates) {
        for (MaterialRef candidate : candidates) {
            Optional<HolderSet<Item>> holders = candidate.resolve();
            if (holders.isPresent()) {
                return holders;
            }
        }
        return Optional.empty();
    }

    public static Item resolveResource(List<MaterialRef> candidates) {
        return firstResolved(candidates)
                .flatMap(holders -> holders.stream()
                        .filter(holder -> holder.value() != Items.AIR)
                        .min(Comparator.comparing(holder -> holder.getKey().identifier().toString()))
                        .map(Holder::value))
                .orElse(null);
    }
}
