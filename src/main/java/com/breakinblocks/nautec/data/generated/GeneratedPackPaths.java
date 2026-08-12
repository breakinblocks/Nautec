package com.breakinblocks.nautec.data.generated;

import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class GeneratedPackPaths {
    public static final String GENERATED_NAMESPACE = "nautec_generated";
    public static final String PACK_ID = "nautec/generated_pack";
    public static final String JSON_SUFFIX = ".json";
    public static final String INVALID_SUFFIX = ".json.invalid";

    private GeneratedPackPaths() {
    }

    public static Path root() {
        return FMLPaths.CONFIGDIR.get().resolve(Nautec.MODID).resolve("generated_pack");
    }

    public static Path mcmeta() {
        return root().resolve("pack.mcmeta");
    }

    public static Path dataRoot() {
        return root().resolve("data");
    }

    public static Path bacteriaDir() {
        return dataRoot().resolve(GENERATED_NAMESPACE).resolve(Nautec.MODID).resolve("bacteria");
    }

    public static Path bacteriaFile(String name) {
        return bacteriaDir().resolve(name + JSON_SUFFIX);
    }

    public static Path recipeDir() {
        return dataRoot().resolve(GENERATED_NAMESPACE).resolve("recipe");
    }

    public static Path incubationRecipeFile(String name) {
        return recipeDir().resolve(name + "_incubation" + JSON_SUFFIX);
    }

    public static Path mutationRecipeFile(String name) {
        return recipeDir().resolve(name + "_mutation" + JSON_SUFFIX);
    }

    public static Path obtainingFile() {
        return dataRoot().resolve(Nautec.MODID).resolve("data_maps").resolve("block").resolve("bacteria_obtaining" + JSON_SUFFIX);
    }

    public static Identifier bacteriaId(String name) {
        return Identifier.fromNamespaceAndPath(GENERATED_NAMESPACE, name);
    }

    public static ResourceKey<Bacteria> bacteriaKey(String name) {
        return ResourceKey.create(NTRegistries.BACTERIA_KEY, bacteriaId(name));
    }

    public static boolean isValidName(String name) {
        return !name.isEmpty() && Identifier.isValidPath(name) && name.indexOf('/') < 0;
    }

    public static List<String> listGeneratedNames() {
        Path dir = bacteriaDir();
        if (!Files.isDirectory(dir)) {
            return List.of();
        }

        List<String> names = new ArrayList<>();
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(fileName -> fileName.endsWith(JSON_SUFFIX))
                    .map(fileName -> fileName.substring(0, fileName.length() - JSON_SUFFIX.length()))
                    .sorted()
                    .forEach(names::add);
        } catch (IOException e) {
            Nautec.LOGGER.error("Could not list generated bacteria in {}", dir, e);
        }
        return names;
    }
}
