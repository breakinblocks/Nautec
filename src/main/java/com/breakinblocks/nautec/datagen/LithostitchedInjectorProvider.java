package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.worldgen.injection.NTOceanRegion;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.biome.Climate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LithostitchedInjectorProvider implements DataProvider {
    public static final String DIRECTORY = "lithostitched/biome_injector";
    private static final String OCEAN_TARGET = "#minecraft:is_ocean";

    private final PackOutput.PathProvider path;

    public LithostitchedInjectorProvider(PackOutput output) {
        this.path = output.createPathProvider(PackOutput.Target.DATA_PACK, DIRECTORY);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        int priority = 10;
        for (NTOceanRegion.Slice slice : NTOceanRegion.slices()) {
            String name = slice.biome().identifier().getPath();
            Path file = path.json(Nautec.rl(name));
            futures.add(DataProvider.saveStable(cache, injector(slice, priority), file));
            priority += 10;
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private static JsonObject injector(NTOceanRegion.Slice slice, int priority) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "lithostitched:replace_partially");
        json.addProperty("dimension", "minecraft:overworld");
        json.addProperty("priority", priority);
        json.addProperty("targets", OCEAN_TARGET);
        json.addProperty("replacement", slice.biome().identifier().toString());

        JsonObject parameters = new JsonObject();
        put(parameters, "temperature", slice, slice.temperature());
        put(parameters, "humidity", slice, slice.humidity());
        put(parameters, "continentalness", slice, slice.continentalness());
        put(parameters, "erosion", slice, slice.erosion());
        put(parameters, "weirdness", slice, slice.weirdness());
        parameters.add("depth", span(0.0F, 1.0F));
        json.add("parameters", parameters);
        return json;
    }

    private static void put(JsonObject parameters, String key, NTOceanRegion.Slice slice, Climate.Parameter parameter) {
        if (slice.constrains(parameter)) {
            parameters.add(key, span(Climate.unquantizeCoord(parameter.min()), Climate.unquantizeCoord(parameter.max())));
        }
    }

    private static JsonArray span(float min, float max) {
        JsonArray array = new JsonArray();
        array.add(min);
        array.add(max);
        return array;
    }

    @Override
    public String getName() {
        return "Lithostitched Biome Injectors";
    }
}
