package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import com.breakinblocks.nautec.data.generated.BacteriaBalance;
import com.breakinblocks.nautec.data.generated.BacteriaMaterials;
import com.breakinblocks.nautec.data.generated.BacteriaJsonWriter;
import com.breakinblocks.nautec.data.generated.GeneratedPackFinder;
import com.breakinblocks.nautec.data.generated.GeneratedPackPaths;
import com.breakinblocks.nautec.data.maps.BacteriaObtainValue;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import net.minecraft.core.registries.BuiltInRegistries;

public final class GeneratedPackTests {
    private static final String PROBE = "gametest_probe";
    private static final String BROKEN = "gametest_broken";
    private static final String FOREIGN_KEY = "minecraft:gravel";

    private GeneratedPackTests() {
    }

    public static void register(NTTestRegistrar r) {
        r.add("generated_pack/pack_mounted", 40, helper -> helper.runAfterDelay(1, () -> {
            MinecraftServer server = helper.getLevel().getServer();
            helper.assertTrue(server.getPackRepository().getSelectedIds().contains(GeneratedPackPaths.PACK_ID),
                    "The generated pack should be mounted, selected packs were " + server.getPackRepository().getSelectedIds());
            helper.succeed();
        }));

        r.add("generated_pack/scaffold_idempotent", 40, helper -> helper.runAfterDelay(1, () -> {
            GeneratedPackFinder.scaffold();
            GeneratedPackFinder.scaffold();

            helper.assertTrue(Files.isDirectory(GeneratedPackPaths.bacteriaDir()), "bacteria directory missing");
            helper.assertTrue(Files.isDirectory(GeneratedPackPaths.recipeDir()), "recipe directory missing");

            JsonObject meta = readJson(helper, GeneratedPackPaths.mcmeta()).getAsJsonObject();
            helper.assertTrue(meta.has("pack"), "pack.mcmeta has no pack section");
            helper.assertFalse(meta.getAsJsonObject("pack").get("description").isJsonNull(), "pack.mcmeta has no description");
            helper.succeed();
        }));

        r.add("generated_pack/balance_roundtrip", 40, helper -> helper.runAfterDelay(1, () -> {
            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());

            for (BacteriaBalance.Rarity rarity : BacteriaBalance.Rarity.values()) {
                Identifier id = GeneratedPackPaths.bacteriaId(PROBE + "_" + rarity.lowerName());
                Bacteria bacteria = BacteriaBalance.buildBacteria(Items.IRON_INGOT, rarity, id);
                JsonElement json = BacteriaJsonWriter.encodeBacteria(bacteria, helper.getLevel().registryAccess());

                JsonObject object = json.getAsJsonObject();
                helper.assertValueEqual("nautec:simple", object.get("type").getAsString(), "serializer type for " + rarity);
                helper.assertValueEqual("minecraft:iron_ingot", object.get("bacteria").getAsString(), "produced item for " + rarity);

                Bacteria decoded = Bacteria.CODEC.parse(ops, json).getOrThrow();
                helper.assertValueEqual(bacteria.initialSize().getMin(), decoded.initialSize().getMin(), "initial size min for " + rarity);
                helper.assertValueEqual(bacteria.initialSize().getMax(), decoded.initialSize().getMax(), "initial size max for " + rarity);
                helper.assertValueEqual(rarity, BacteriaBalance.inferRarity(decoded).orElse(null), "inferred rarity for " + rarity);
            }
            helper.succeed();
        }));

        r.add("generated_pack/recipe_json_roundtrip", 40, helper -> helper.runAfterDelay(1, () -> {
            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
            BacteriaBalance.Rarity rarity = BacteriaBalance.Rarity.RARE;

            JsonElement incubation = BacteriaJsonWriter.encodeRecipe(
                    BacteriaBalance.incubationRecipe(GeneratedPackPaths.bacteriaKey(PROBE), Ingredient.of(Items.IRON_INGOT), rarity),
                    helper.getLevel().registryAccess());
            helper.assertValueEqual("nautec:bacteria_incubation", incubation.getAsJsonObject().get("type").getAsString(),
                    "incubation recipe type");

            JsonElement mutation = BacteriaJsonWriter.encodeRecipe(
                    BacteriaBalance.mutationRecipe(GeneratedPackPaths.bacteriaKey(PROBE), Ingredient.of(Items.IRON_INGOT), rarity),
                    helper.getLevel().registryAccess());
            helper.assertValueEqual("nautec:bacteria_mutation", mutation.getAsJsonObject().get("type").getAsString(),
                    "mutation recipe type");

            JsonElement tagged = BacteriaJsonWriter.encodeRecipe(
                    BacteriaBalance.incubationRecipe(GeneratedPackPaths.bacteriaKey(PROBE),
                            Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(
                                    TagKey.create(Registries.ITEM, Identifier.parse("c:ingots/iron")))),
                            rarity),
                    helper.getLevel().registryAccess());
            helper.assertValueEqual("#c:ingots/iron", tagged.getAsJsonObject().get("nutrient").getAsString(),
                    "A tag nutrient must stay a tag so it follows whichever mod fills it");

            Recipe<?> decoded = Recipe.CODEC.parse(ops, mutation).getOrThrow();
            helper.assertTrue(decoded instanceof BacteriaMutationRecipe,
                    "decoded mutation recipe has the wrong class");
            helper.succeed();
        }));

        r.add("generated_pack/obtaining_read_modify_write", 40, helper -> helper.runAfterDelay(1, () -> {
            Path file = GeneratedPackPaths.obtainingFile();
            String backup = readOrNull(file);
            try {
                JsonObject foreign = new JsonObject();
                foreign.add(FOREIGN_KEY, BacteriaJsonWriter.encodeObtaining(
                        new BacteriaObtainValue(NTBacterias.LITHOPHILES, BiomeTags.IS_OCEAN, 0.25f)));
                JsonObject root = new JsonObject();
                root.add("values", foreign);
                BacteriaJsonWriter.writeAtomic(file, BacteriaJsonWriter.pretty(root));

                BacteriaJsonWriter.upsertObtaining(Blocks.DIRT,
                        new BacteriaObtainValue(GeneratedPackPaths.bacteriaKey(PROBE), BiomeTags.IS_OCEAN, 0.5f));
                JsonObject values = BacteriaJsonWriter.readObtaining();
                helper.assertTrue(values.has(FOREIGN_KEY), "upsert dropped a foreign entry");
                helper.assertTrue(values.has("minecraft:dirt"), "upsert did not add the new entry");

                List<String> removed = BacteriaJsonWriter.removeObtainingFor(GeneratedPackPaths.bacteriaId(PROBE));
                helper.assertValueEqual(1, removed.size(), "removed entry count");
                values = BacteriaJsonWriter.readObtaining();
                helper.assertTrue(values.has(FOREIGN_KEY), "remove dropped a foreign entry");
                helper.assertFalse(values.has("minecraft:dirt"), "remove kept the generated entry");

                BacteriaJsonWriter.removeObtainingFor(NTBacterias.LITHOPHILES.identifier());
                helper.assertFalse(Files.exists(file), "The obtaining file should be deleted once it is empty");
                helper.assertTrue(listTempFiles(file.getParent()).isEmpty(), "Atomic writes left .tmp files behind");
            } catch (IOException e) {
                throw helper.assertionException("Obtaining file io failed: " + e.getMessage());
            } finally {
                restore(file, backup);
            }
            helper.succeed();
        }));

        r.add("generated_pack/command_generate_and_delete", 60, helper -> helper.runAfterDelay(1, () -> {
            MinecraftServer server = helper.getLevel().getServer();
            CommandSourceStack source = server.createCommandSourceStack();
            deleteProbeFiles();

            server.getCommands().performPrefixedCommand(source, "nautec bacteria generate " + PROBE + " minecraft:iron_ingot rare");

            helper.assertTrue(Files.isRegularFile(GeneratedPackPaths.bacteriaFile(PROBE)), "generate did not write the bacteria file");
            helper.assertTrue(Files.isRegularFile(GeneratedPackPaths.incubationRecipeFile(PROBE)), "generate did not write the incubation recipe");
            helper.assertTrue(Files.isRegularFile(GeneratedPackPaths.mutationRecipeFile(PROBE)), "generate did not write the mutation recipe");

            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
            Bacteria written = Bacteria.CODEC.parse(ops, readJson(helper, GeneratedPackPaths.bacteriaFile(PROBE))).getOrThrow();
            helper.assertValueEqual(BacteriaBalance.Rarity.RARE, BacteriaBalance.inferRarity(written).orElse(null), "written rarity");
            helper.assertTrue(GeneratedPackPaths.listGeneratedNames().contains(PROBE), "listGeneratedNames missed the probe");

            server.getCommands().performPrefixedCommand(source, "nautec bacteria delete-generated " + PROBE);

            helper.assertFalse(Files.exists(GeneratedPackPaths.bacteriaFile(PROBE)), "delete left the bacteria file");
            helper.assertFalse(Files.exists(GeneratedPackPaths.incubationRecipeFile(PROBE)), "delete left the incubation recipe");
            helper.assertFalse(Files.exists(GeneratedPackPaths.mutationRecipeFile(PROBE)), "delete left the mutation recipe");
            helper.succeed();
        }));

        r.add("generated_pack/shipped_materials_are_gated", 40, helper -> helper.runAfterDelay(1, () -> {
            helper.assertValueEqual(78, BacteriaMaterials.SHIPPED_NAMES.size(), "shipped material bacteria");

            for (String name : BacteriaMaterials.SHIPPED_NAMES) {
                helper.assertTrue(GeneratedPackPaths.isValidName(name),
                        "Shipped bacteria name '" + name + "' cannot be a file name");
            }

            HolderLookup.RegistryLookup<Bacteria> registry =
                    helper.getLevel().registryAccess().lookupOrThrow(NTRegistries.BACTERIA_KEY);

            for (String name : BacteriaMaterials.SHIPPED_NAMES) {
                ResourceKey<Bacteria> key = ResourceKey.create(NTRegistries.BACTERIA_KEY, Nautec.rl(name));
                helper.assertTrue(registry.get(key).isEmpty(),
                        "Material bacteria '" + name + "' should be gated off when none of its mods are installed, "
                                + "otherwise every pack sees content it cannot use");
            }
            helper.succeed();
        }));


        r.add("generated_pack/quarantine_invalid_json", 40, helper -> helper.runAfterDelay(1, () -> {
            Path broken = GeneratedPackPaths.bacteriaFile(BROKEN);
            Path renamed = GeneratedPackPaths.bacteriaDir().resolve(BROKEN + GeneratedPackPaths.INVALID_SUFFIX);
            try {
                GeneratedPackFinder.scaffold();
                Files.deleteIfExists(renamed);
                BacteriaJsonWriter.writeAtomic(broken, "{ this is not bacteria json }");

                GeneratedPackFinder.quarantineInvalidBacteria();

                helper.assertFalse(Files.exists(broken), "A broken bacteria file should not stay in place");
                helper.assertTrue(Files.isRegularFile(renamed), "A broken bacteria file should be renamed to .json.invalid");
            } catch (IOException e) {
                throw helper.assertionException("Quarantine io failed: " + e.getMessage());
            } finally {
                try {
                    Files.deleteIfExists(broken);
                    Files.deleteIfExists(renamed);
                } catch (IOException ignored) {
                }
            }
            helper.succeed();
        }));
    }

    private static JsonElement readJson(GameTestHelper helper, Path file) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        } catch (IOException e) {
            throw helper.assertionException("Could not read " + file + ": " + e.getMessage());
        }
    }

    private static String readOrNull(Path file) {
        try {
            return Files.isRegularFile(file) ? Files.readString(file, StandardCharsets.UTF_8) : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static void restore(Path file, String backup) {
        try {
            if (backup == null) {
                Files.deleteIfExists(file);
            } else {
                BacteriaJsonWriter.writeAtomic(file, backup);
            }
        } catch (IOException ignored) {
        }
    }

    private static List<Path> listTempFiles(Path dir) {
        if (dir == null || !Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(path -> path.getFileName().toString().endsWith(".tmp")).toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private static void deleteProbeFiles() {
        try {
            Files.deleteIfExists(GeneratedPackPaths.bacteriaFile(PROBE));
            Files.deleteIfExists(GeneratedPackPaths.incubationRecipeFile(PROBE));
            Files.deleteIfExists(GeneratedPackPaths.mutationRecipeFile(PROBE));
        } catch (IOException ignored) {
        }
    }
}
