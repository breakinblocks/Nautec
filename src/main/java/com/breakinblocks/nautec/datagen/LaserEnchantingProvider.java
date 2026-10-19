package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.recipes.LaserEnchantingRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LaserEnchantingProvider implements DataProvider {
    private record Entry(String enchantment, String reagent, int count) {
        String namespace() {
            return enchantment.substring(0, enchantment.indexOf(':'));
        }

        String path() {
            return enchantment.substring(enchantment.indexOf(':') + 1);
        }
    }

    private static final List<Entry> ENTRIES = List.of(
            new Entry("minecraft:aqua_affinity", "minecraft:lily_pad", 1),
            new Entry("minecraft:bane_of_arthropods", "minecraft:spider_eye", 12),
            new Entry("minecraft:blast_protection", "minecraft:gunpowder", 16),
            new Entry("minecraft:breach", "minecraft:pointed_dripstone", 4),
            new Entry("minecraft:channeling", "minecraft:lightning_rod", 1),
            new Entry("minecraft:density", "minecraft:anvil", 1),
            new Entry("minecraft:depth_strider", "minecraft:prismarine_shard", 1),
            new Entry("minecraft:efficiency", "#c:dusts/redstone", 12),
            new Entry("minecraft:feather_falling", "#c:feathers", 1),
            new Entry("minecraft:fire_aspect", "minecraft:blaze_rod", 8),
            new Entry("minecraft:fire_protection", "minecraft:magma_cream", 16),
            new Entry("minecraft:flame", "#c:netherracks", 1),
            new Entry("minecraft:fortune", "#c:gems/emerald", 1),
            new Entry("minecraft:frost_walker", "minecraft:ice", 16),
            new Entry("minecraft:impaling", "#c:storage_blocks/iron", 1),
            new Entry("minecraft:infinity", "nautec:flawless_prismarine_crystal", 1),
            new Entry("minecraft:knockback", "minecraft:piston", 1),
            new Entry("minecraft:looting", "minecraft:skeleton_skull", 1),
            new Entry("minecraft:loyalty", "minecraft:lead", 1),
            new Entry("minecraft:luck_of_the_sea", "#c:gems/lapis", 1),
            new Entry("minecraft:lunge", "minecraft:rabbit_foot", 1),
            new Entry("minecraft:lure", "#minecraft:fishes", 1),
            new Entry("minecraft:mending", "nautec:resonant_shard", 1),
            new Entry("minecraft:multishot", "#minecraft:arrows", 16),
            new Entry("minecraft:piercing", "#c:gems/prismarine", 8),
            new Entry("minecraft:power", "minecraft:flint", 12),
            new Entry("minecraft:projectile_protection", "#c:leathers", 16),
            new Entry("minecraft:protection", "nautec:aquarine_steel_ingot", 16),
            new Entry("minecraft:punch", "#c:strings", 1),
            new Entry("minecraft:quick_charge", "minecraft:sugar", 16),
            new Entry("minecraft:respiration", "minecraft:glass_bottle", 1),
            new Entry("minecraft:riptide", "minecraft:firework_rocket", 8),
            new Entry("minecraft:sharpness", "#c:gems/quartz", 12),
            new Entry("minecraft:silk_touch", "#c:slime_balls", 1),
            new Entry("minecraft:smite", "minecraft:rotten_flesh", 12),
            new Entry("minecraft:soul_speed", "minecraft:soul_soil", 16),
            new Entry("minecraft:sweeping_edge", "#c:ingots/iron", 8),
            new Entry("minecraft:swift_sneak", "minecraft:echo_shard", 2),
            new Entry("minecraft:thorns", "minecraft:rose_bush", 4),
            new Entry("minecraft:unbreaking", "#c:obsidians", 1),
            new Entry("minecraft:wind_burst", "minecraft:breeze_rod", 4),

            new Entry("apothic_enchanting:berserkers_fury", "minecraft:blaze_powder", 8),
            new Entry("apothic_enchanting:boon_of_the_earth", "minecraft:amethyst_shard", 8),
            new Entry("apothic_enchanting:chainsaw", "#minecraft:logs", 16),
            new Entry("apothic_enchanting:chromatic", "#c:dyes", 8),
            new Entry("apothic_enchanting:crescendo_of_bolts", "minecraft:tripwire_hook", 4),
            new Entry("apothic_enchanting:endless_quiver", "minecraft:spectral_arrow", 16),
            new Entry("apothic_enchanting:growth_serum", "minecraft:bone_meal", 16),
            new Entry("apothic_enchanting:icy_thorns", "minecraft:packed_ice", 8),
            new Entry("apothic_enchanting:infusion", "minecraft:dragon_breath", 1),
            new Entry("apothic_enchanting:knowledge_of_the_ages", "minecraft:bookshelf", 4),
            new Entry("apothic_enchanting:life_mending", "minecraft:golden_apple", 1),
            new Entry("apothic_enchanting:miners_fervor", "#c:dusts/glowstone", 12),
            new Entry("apothic_enchanting:natures_blessing", "minecraft:moss_block", 8),
            new Entry("apothic_enchanting:rebounding", "minecraft:slime_block", 1),
            new Entry("apothic_enchanting:reflective_defenses", "#c:glass_blocks", 8),
            new Entry("apothic_enchanting:scavenger", "minecraft:bone", 16),
            new Entry("apothic_enchanting:shield_bash", "minecraft:shield", 1),
            new Entry("apothic_enchanting:stable_footing", "minecraft:scaffolding", 8),
            new Entry("apothic_enchanting:tempting", "minecraft:wheat", 16),
            new Entry("apothic_enchanting:worker_exploitation", "#minecraft:wool", 8),
            new Entry("apothic_spawners:capturing", "minecraft:name_tag", 1),
            new Entry("evilcraft:breaking", "minecraft:gravel", 8),
            new Entry("evilcraft:life_stealing", "minecraft:ghast_tear", 2),
            new Entry("evilcraft:poison_tip", "minecraft:poisonous_potato", 4),
            new Entry("evilcraft:unusing", "minecraft:cobweb", 4),
            new Entry("evilcraft:vengeance", "minecraft:wither_rose", 1),
            new Entry("combat_roll:acrobat", "minecraft:phantom_membrane", 2),
            new Entry("combat_roll:longfooted", "minecraft:rabbit_hide", 4),
            new Entry("combat_roll:multi_roll", "minecraft:sugar_cane", 8),
            new Entry("mysticalagriculture:mystical_enlightenment", "minecraft:experience_bottle", 4),
            new Entry("mysticalagriculture:soul_siphoner", "minecraft:soul_sand", 8),
            new Entry("reliquary:severing", "minecraft:wither_skeleton_skull", 1),
            new Entry("occultism:fracture_soul", "minecraft:crying_obsidian", 2),
            new Entry("ltxi:razor", "minecraft:iron_nugget", 16),
            new Entry("ftboceanmobs:rift_disruptor", "minecraft:popped_chorus_fruit", 4),
            new Entry("arsmagicalegacy:dismembering", "minecraft:bone_block", 2)
    );

    private final PackOutput.PathProvider recipes;

    public LaserEnchantingProvider(PackOutput output) {
        this.recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Entry entry : ENTRIES) {
            JsonObject recipe = new JsonObject();
            recipe.addProperty("type", Nautec.rl(LaserEnchantingRecipe.NAME).toString());
            recipe.addProperty("enchantment", entry.enchantment());
            JsonObject input = new JsonObject();
            JsonObject ingredient = new JsonObject();
            if (entry.reagent().startsWith("#")) {
                ingredient.addProperty("tag", entry.reagent().substring(1));
            } else {
                ingredient.addProperty("item", entry.reagent());
            }
            input.add("ingredient", ingredient);
            input.addProperty("count", entry.count());
            recipe.add("input", input);
            boolean vanilla = entry.namespace().equals("minecraft");
            if (!vanilla) {
                JsonObject loaded = new JsonObject();
                loaded.addProperty("type", "neoforge:mod_loaded");
                loaded.addProperty("modid", entry.namespace());
                JsonArray conditions = new JsonArray();
                conditions.add(loaded);
                recipe.add("neoforge:conditions", conditions);
            }
            String path = LaserEnchantingRecipe.NAME + "/" + (vanilla ? entry.path() : entry.namespace() + "/" + entry.path());
            futures.add(DataProvider.saveStable(cache, recipe, recipes.json(Nautec.rl(path))));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Laser Enchanting Recipes";
    }
}
