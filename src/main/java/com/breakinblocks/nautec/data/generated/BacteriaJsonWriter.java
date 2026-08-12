package com.breakinblocks.nautec.data.generated;

import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.data.maps.BacteriaObtainValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BacteriaJsonWriter {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String VALUES = "values";

    private BacteriaJsonWriter() {
    }

    public static String pretty(JsonElement json) {
        return GSON.toJson(json);
    }

    public static void writeAtomic(Path target, String content) throws IOException {
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path temp = target.resolveSibling(target.getFileName() + ".tmp");
        Files.writeString(temp, content, StandardCharsets.UTF_8);
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    public static JsonElement encodeBacteria(Bacteria bacteria, HolderLookup.Provider registries) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registries);
        JsonElement encoded = Bacteria.CODEC.encodeStart(ops, bacteria).getOrThrow();
        Bacteria.CODEC.parse(ops, encoded).getOrThrow();
        return encoded;
    }

    public static JsonElement encodeRecipe(Recipe<?> recipe, HolderLookup.Provider registries) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registries);
        JsonElement encoded = Recipe.CODEC.encodeStart(ops, recipe).getOrThrow();
        Recipe.CODEC.parse(ops, encoded).getOrThrow();
        return encoded;
    }

    public static JsonElement encodeObtaining(BacteriaObtainValue value) {
        JsonElement encoded = BacteriaObtainValue.CODEC.encodeStart(JsonOps.INSTANCE, value).getOrThrow();
        BacteriaObtainValue.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        return encoded;
    }

    public static JsonObject readObtaining() throws IOException {
        Path file = GeneratedPackPaths.obtainingFile();
        if (!Files.isRegularFile(file)) {
            return new JsonObject();
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) {
                return new JsonObject();
            }
            JsonElement values = parsed.getAsJsonObject().get(VALUES);
            return values != null && values.isJsonObject() ? values.getAsJsonObject() : new JsonObject();
        }
    }

    public static void upsertObtaining(Block block, BacteriaObtainValue value) throws IOException {
        JsonObject values = readObtaining();
        values.add(blockKey(block), encodeObtaining(value));
        writeObtaining(values);
    }

    public static List<String> removeObtainingFor(Identifier bacteriaId) throws IOException {
        JsonObject values = readObtaining();
        List<String> removed = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
            if (!entry.getValue().isJsonObject()) {
                continue;
            }
            JsonElement bacteria = entry.getValue().getAsJsonObject().get("bacteria");
            if (bacteria != null && bacteria.isJsonPrimitive() && bacteria.getAsString().equals(bacteriaId.toString())) {
                removed.add(entry.getKey());
            }
        }

        for (String key : removed) {
            values.remove(key);
        }
        if (!removed.isEmpty()) {
            writeObtaining(values);
        }
        return removed;
    }

    public static String blockKey(Block block) {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    private static void writeObtaining(JsonObject values) throws IOException {
        Path file = GeneratedPackPaths.obtainingFile();
        if (values.isEmpty()) {
            Files.deleteIfExists(file);
            return;
        }

        JsonObject root = new JsonObject();
        root.add(VALUES, values);
        writeAtomic(file, pretty(root));
    }
}
