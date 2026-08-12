package com.breakinblocks.nautec.data.generated;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.util.InclusiveRange;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class GeneratedPackFinder {
    private static final String TITLE = "Nautec Generated";
    private static final String DESCRIPTION =
            "Nautec generated content. pack.mcmeta is managed by the mod and gets rewritten; everything under data/ is yours to edit.";

    private GeneratedPackFinder() {
    }

    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }

        event.addRepositorySource(consumer -> {
            if (!Files.isDirectory(GeneratedPackPaths.root())) {
                return;
            }

            scaffold();
            quarantineInvalidBacteria();

            Pack pack = Pack.readMetaAndCreate(
                    new PackLocationInfo(GeneratedPackPaths.PACK_ID, Component.literal(TITLE), PackSource.BUILT_IN, Optional.empty()),
                    new PathPackResources.PathResourcesSupplier(GeneratedPackPaths.root()),
                    PackType.SERVER_DATA,
                    new PackSelectionConfig(true, Pack.Position.TOP, false)
            );

            if (pack == null) {
                Nautec.LOGGER.error("Could not read the generated pack at {}, generated content will not load", GeneratedPackPaths.root());
                return;
            }
            consumer.accept(pack);
        });
    }

    public static synchronized void scaffold() {
        try {
            Files.createDirectories(GeneratedPackPaths.bacteriaDir());
            Files.createDirectories(GeneratedPackPaths.recipeDir());

            if (!hasCurrentMeta()) {
                BacteriaJsonWriter.writeAtomic(GeneratedPackPaths.mcmeta(), BacteriaJsonWriter.pretty(buildMeta()));
            }
        } catch (Exception e) {
            Nautec.LOGGER.error("Could not prepare the generated pack at {}", GeneratedPackPaths.root(), e);
        }
    }

    public static int quarantineInvalidBacteria() {
        Path dataRoot = GeneratedPackPaths.dataRoot();
        if (!Files.isDirectory(dataRoot)) {
            return 0;
        }

        int quarantined = 0;
        for (Path file : listBacteriaFiles(dataRoot)) {
            DataResult<Bacteria> result = parseBacteria(file);
            if (result.isSuccess()) {
                continue;
            }

            String error = result.error().map(DataResult.Error::message).orElse("unknown error");
            Path renamed = file.resolveSibling(file.getFileName().toString().replace(
                    GeneratedPackPaths.JSON_SUFFIX, GeneratedPackPaths.INVALID_SUFFIX));
            try {
                Files.move(file, renamed, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                quarantined++;
                Nautec.LOGGER.error("Generated bacteria {} could not be read ({}), renamed it to {}",
                        file, error, renamed.getFileName());
            } catch (IOException e) {
                Nautec.LOGGER.error("Generated bacteria {} could not be read ({}) and could not be renamed either",
                        file, error, e);
            }
        }
        return quarantined;
    }

    private static List<Path> listBacteriaFiles(Path dataRoot) {
        try (Stream<Path> namespaces = Files.list(dataRoot)) {
            return namespaces
                    .filter(Files::isDirectory)
                    .map(namespace -> namespace.resolve(Nautec.MODID).resolve("bacteria"))
                    .filter(Files::isDirectory)
                    .flatMap(GeneratedPackFinder::listJsonFiles)
                    .toList();
        } catch (IOException e) {
            Nautec.LOGGER.error("Could not scan the generated pack at {}", dataRoot, e);
            return List.of();
        }
    }

    private static Stream<Path> listJsonFiles(Path dir) {
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(GeneratedPackPaths.JSON_SUFFIX))
                    .toList()
                    .stream();
        } catch (IOException e) {
            Nautec.LOGGER.error("Could not scan {}", dir, e);
            return Stream.of();
        }
    }

    private static DataResult<Bacteria> parseBacteria(Path file) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return Bacteria.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader));
        } catch (Exception e) {
            return DataResult.error(e::getMessage);
        }
    }

    private static PackFormat runtimeFormat() {
        return SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA);
    }

    private static MetadataSectionType<PackMetadataSection> metaType() {
        return PackMetadataSection.forPackType(PackType.SERVER_DATA);
    }

    private static JsonObject buildMeta() {
        PackFormat format = runtimeFormat();
        MetadataSectionType<PackMetadataSection> type = metaType();
        PackMetadataSection section = new PackMetadataSection(Component.literal(DESCRIPTION), new InclusiveRange<>(format, format));

        JsonObject root = new JsonObject();
        root.add(type.name(), type.codec().encodeStart(JsonOps.INSTANCE, section).getOrThrow());
        return root;
    }

    private static boolean hasCurrentMeta() {
        Path file = GeneratedPackPaths.mcmeta();
        if (!Files.isRegularFile(file)) {
            return false;
        }

        MetadataSectionType<PackMetadataSection> type = metaType();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) {
                return false;
            }
            JsonElement section = parsed.getAsJsonObject().get(type.name());
            if (section == null) {
                return false;
            }
            return type.codec().parse(JsonOps.INSTANCE, section)
                    .result()
                    .map(meta -> meta.supportedFormats().isValueInRange(runtimeFormat()))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
}
