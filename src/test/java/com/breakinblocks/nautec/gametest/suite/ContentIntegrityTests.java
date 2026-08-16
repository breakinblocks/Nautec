package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.Nautec;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.api.augments.AugmentType;
import com.breakinblocks.nautec.content.augments.VentCarapaceAugment;
import com.breakinblocks.nautec.registries.NTAugmentSlots;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public final class ContentIntegrityTests {
    private static JsonObject readJson(String path) {
        try (InputStream stream = ContentIntegrityTests.class.getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    private static void checkGeckolibArt(List<String> problems, String name, List<String> wantedAnimations) {
        int[] base = pngSize("/assets/nautec/textures/entity/" + name + ".png");
        int[] glow = pngSize("/assets/nautec/textures/entity/" + name + "_e.png");

        if (base == null) {
            problems.add(name + ".png is missing");
        }
        if (glow == null) {
            problems.add(name + "_e.png is missing");
        }
        if (base != null && glow != null && (base[0] != glow[0] || base[1] != glow[1])) {
            problems.add(name + "_e.png is " + glow[0] + "x" + glow[1]
                    + " but the base sheet is " + base[0] + "x" + base[1]);
        }

        JsonObject geo = readJson("/assets/nautec/geckolib/models/entity/" + name + ".geo.json");
        if (geo == null) {
            problems.add(name + ".geo.json is missing or does not parse");
        } else {
            JsonObject description = geo.getAsJsonArray("minecraft:geometry")
                    .get(0).getAsJsonObject().getAsJsonObject("description");
            String identifier = description.get("identifier").getAsString();
            if (!identifier.equals("geometry." + name)) {
                problems.add(name + ".geo.json declares " + identifier + ", not geometry." + name);
            }
            if (base != null) {
                int width = description.get("texture_width").getAsInt();
                int height = description.get("texture_height").getAsInt();
                if (width != base[0] || height != base[1]) {
                    problems.add(name + ".geo.json is unwrapped for " + width + "x" + height
                            + " but " + name + ".png is " + base[0] + "x" + base[1]);
                }
            }
        }

        JsonObject animationFile = readJson("/assets/nautec/geckolib/animations/entity/" + name + ".animation.json");
        if (animationFile == null) {
            problems.add(name + ".animation.json is missing or does not parse");
            return;
        }

        JsonObject declared = animationFile.getAsJsonObject("animations");
        for (String wanted : wantedAnimations) {
            if (!declared.has(wanted)) {
                problems.add(name + ".animation.json has no \"" + wanted + "\" animation");
            } else if (declared.getAsJsonObject(wanted).has("animation_length")
                    && declared.getAsJsonObject(wanted).get("animation_length").getAsDouble() <= 0.0) {
                problems.add(name + ".animation.json declares \"" + wanted + "\" with zero length");
            }
        }
    }

    private static int[] pngSize(String path) {
        try (InputStream stream = ContentIntegrityTests.class.getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            byte[] header = stream.readNBytes(24);
            if (header.length < 24) {
                return null;
            }
            return new int[]{readBigEndianInt(header, 16), readBigEndianInt(header, 20)};
        } catch (Exception e) {
            return null;
        }
    }

    private static int readBigEndianInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24)
                | ((bytes[offset + 1] & 0xFF) << 16)
                | ((bytes[offset + 2] & 0xFF) << 8)
                | (bytes[offset + 3] & 0xFF);
    }

    public static void register(NTTestRegistrar r) {
        r.add("content/every_structure_still_parses", 5, helper -> {
            HolderLookup.RegistryLookup<Structure> structures =
                    helper.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE);

            for (String name : List.of("ruins_1", "stone_crystal_geode", "deepslate_crystal_geode", "underwater_gateway")) {
                ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, Nautec.rl(name));
                if (structures.get(key).isEmpty()) {
                    helper.fail("Structure " + name + " did not load. The shared NTJigsawStructure codec must keep the "
                            + "same field names as the three original copies, or existing worlds lose their structures.");
                }
            }

            HolderLookup.RegistryLookup<StructureTemplatePool> pools =
                    helper.getLevel().registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
            if (pools.get(ResourceKey.create(Registries.TEMPLATE_POOL, Nautec.rl("underwater_gateway"))).isEmpty()) {
                helper.fail("The underwater gateway template pool did not load");
            }
            helper.succeed();
        });

        r.add("content/every_particle_has_a_definition", 5, helper -> {
            List<String> missing = new ArrayList<>();
            for (ParticleType<?> particle : BuiltInRegistries.PARTICLE_TYPE) {
                Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(particle);
                if (id == null || !id.getNamespace().equals(Nautec.MODID)) continue;

                String definition = "/assets/" + id.getNamespace() + "/particles/" + id.getPath() + ".json";
                String texture = "/assets/" + id.getNamespace() + "/textures/particle/" + id.getPath() + ".png";
                if (ContentIntegrityTests.class.getResource(definition) == null) {
                    missing.add(definition);
                }
                if (ContentIntegrityTests.class.getResource(texture) == null) {
                    missing.add(texture);
                }
            }
            if (!missing.isEmpty()) {
                helper.fail("Particles crash the client without these files: " + String.join(", ", missing));
            }
            helper.succeed();
        });

        r.add("content/every_sound_has_an_entry", 5, helper -> {
            JsonObject sounds = readJson("/assets/" + Nautec.MODID + "/sounds.json");
            JsonObject lang = readJson("/assets/" + Nautec.MODID + "/lang/en_us.json");
            if (sounds == null || lang == null) {
                helper.fail("Could not read sounds.json or en_us.json from the mod jar");
                return;
            }

            List<String> problems = new ArrayList<>();
            for (SoundEvent sound : BuiltInRegistries.SOUND_EVENT) {
                Identifier id = BuiltInRegistries.SOUND_EVENT.getKey(sound);
                if (id == null || !id.getNamespace().equals(Nautec.MODID)) continue;
                if (!sounds.has(id.getPath())) {
                    problems.add("sounds.json has no entry for " + id);
                }
            }

            for (String key : sounds.keySet()) {
                JsonObject entry = sounds.getAsJsonObject(key);
                if (entry.has("subtitle") && !lang.has(entry.get("subtitle").getAsString())) {
                    problems.add("missing translation " + entry.get("subtitle").getAsString());
                }

                for (JsonElement element : entry.getAsJsonArray("sounds")) {
                    JsonObject reference = element.getAsJsonObject();
                    if (!"event".equals(reference.has("type") ? reference.get("type").getAsString() : "file")) {
                        continue;
                    }

                    Identifier referenced = Identifier.tryParse(reference.get("name").getAsString());
                    if (referenced == null || !BuiltInRegistries.SOUND_EVENT.containsKey(referenced)) {
                        problems.add(key + " points at unknown sound event " + reference.get("name").getAsString());
                    }
                }
            }

            if (!problems.isEmpty()) {
                helper.fail("Sound problems: " + String.join(", ", problems));
            }
            helper.succeed();
        });

        r.add("content/every_item_has_a_model", 5, helper -> {
            List<String> missing = new ArrayList<>();
            for (Item item : BuiltInRegistries.ITEM) {
                Identifier id = BuiltInRegistries.ITEM.getKey(item);
                if (!id.getNamespace().equals(Nautec.MODID)) continue;
                if (item.getDefaultInstance().isEmpty()) {
                    missing.add(id.toString());
                }
            }
            if (!missing.isEmpty()) {
                helper.fail("Items that produce an empty stack: " + String.join(", ", missing));
            }
            helper.succeed();
        });

        r.add("content/geckolib_art_is_installed", 5, helper -> {
            List<String> problems = new ArrayList<>();
            checkGeckolibArt(problems, "submarine", List.of("deploy", "idle"));
            checkGeckolibArt(problems, "wave_jet", List.of("activated"));
            if (!problems.isEmpty()) {
                helper.fail("GeckoLib art problems: " + String.join("; ", problems));
            }
            helper.succeed();
        });

        r.add("content/mobs_can_spawn_in_water", 40, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos pool = helper.absolutePos(new BlockPos(4, 2, 4));
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    for (int y = 0; y <= 2; y++) {
                        level.setBlockAndUpdate(pool.offset(x, y, z), Blocks.WATER.defaultBlockState());
                    }
                }
            }

            List<EntityType<?>> types = List.of(
                    NTEntities.SILT_SKIPPER.get(), NTEntities.LANTERN_JELLY.get(),
                    NTEntities.VENT_CRAWLER.get(), NTEntities.ABYSSAL_MAW.get());

            for (EntityType<?> type : types) {
                Entity entity = type.spawn(level, pool, EntitySpawnReason.SPAWN_ITEM_USE);
                if (entity == null) {
                    helper.fail("Could not spawn " + BuiltInRegistries.ENTITY_TYPE.getKey(type));
                    return;
                }
                entity.discard();
            }
            helper.succeed();
        });

        r.add("content/budding_prismarine_grows_clusters", 60, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos budding = helper.absolutePos(new BlockPos(2, 2, 2));
            level.setBlockAndUpdate(budding, NTBlocks.BUDDING_PRISMARINE.get().defaultBlockState());
            for (Direction direction : Direction.values()) {
                level.setBlockAndUpdate(budding.relative(direction), Blocks.WATER.defaultBlockState());
            }

            BlockState budState = NTBlocks.SMALL_PRISMARINE_BUD.get().defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, Direction.UP);
            BlockPos budPos = budding.above();
            level.setBlockAndUpdate(budPos, budState);

            for (int attempt = 0; attempt < 400; attempt++) {
                NTBlocks.BUDDING_PRISMARINE.get().defaultBlockState()
                        .randomTick(level, budding, level.getRandom());
                if (level.getBlockState(budPos).is(NTBlocks.MEDIUM_PRISMARINE_BUD.get())) {
                    helper.succeed();
                    return;
                }
            }
            helper.fail("Budding Prismarine never advanced a Small Prismarine Bud to the next stage");
        });

        r.add("content/every_augment_is_installable", 5, helper -> {
            List<String> slotless = new ArrayList<>();
            for (AugmentType<?> type : NTRegistries.AUGMENT_TYPE) {
                if (type.getAugmentSlots().isEmpty()) {
                    slotless.add(String.valueOf(NTRegistries.AUGMENT_TYPE.getKey(type)));
                }
            }
            if (!slotless.isEmpty()) {
                helper.fail("Augments with no compatible slot cannot ever be applied: " + String.join(", ", slotless));
            }
            helper.succeed();
        });

        r.add("content/vent_carapace_modifiers_are_reversible", 5, helper -> {
            ServerLevel level = helper.getLevel();
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            AttributeInstance armor =
                    player.getAttribute(Attributes.ARMOR);
            if (armor == null) {
                helper.fail("Player has no armor attribute");
                return;
            }

            double before = armor.getValue();
            VentCarapaceAugment augment =
                    new VentCarapaceAugment(
                            NTAugmentSlots.BODY.get());

            augment.onAdded(player);
            double during = armor.getValue();
            augment.onAdded(player);
            if (armor.getValue() != during) {
                helper.fail("Applying Vent Carapace twice stacked its armour modifier");
            }
            if (during <= before) {
                helper.fail("Vent Carapace did not raise armour: " + before + " -> " + during);
            }

            augment.onRemoved(player);
            if (armor.getValue() != before) {
                helper.fail("Vent Carapace left its armour modifier behind after removal");
            }
            helper.succeed();
        });

        r.add("content/prismarine_cluster_drops_shards", 5, helper -> {
            Identifier lootTable = NTBlocks.PRISMARINE_CLUSTER.get().getLootTable()
                    .map(key -> key.identifier()).orElse(null);
            if (lootTable == null) {
                helper.fail("Prismarine Cluster has no loot table");
                return;
            }
            if (helper.getLevel().getServer().reloadableRegistries().getLootTable(
                    ResourceKey.create(Registries.LOOT_TABLE, lootTable)) == null) {
                helper.fail("Prismarine Cluster loot table " + lootTable + " does not resolve");
            }
            if (NTItems.PRISMARINE_CRYSTAL_SHARD.get().getDefaultInstance().isEmpty()) {
                helper.fail("Prismarine Crystal Shard is missing");
            }
            helper.succeed();
        });
    }

    private ContentIntegrityTests() {
    }
}
