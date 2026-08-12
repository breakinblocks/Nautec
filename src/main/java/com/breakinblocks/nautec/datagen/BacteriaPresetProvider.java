package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.data.generated.BacteriaBalance;
import com.breakinblocks.nautec.data.generated.BacteriaPreset;
import com.breakinblocks.nautec.data.generated.BacteriaPresetManager;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BacteriaPresetProvider implements DataProvider {
    private final PackOutput.PathProvider path;

    public BacteriaPresetProvider(PackOutput output) {
        this.path = output.createPathProvider(PackOutput.Target.DATA_PACK, BacteriaPresetManager.DIRECTORY);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (BacteriaPreset preset : presets()) {
            JsonElement json = BacteriaPreset.CODEC.encodeStart(JsonOps.INSTANCE, preset).getOrThrow();
            Path file = path.json(Identifier.fromNamespaceAndPath(Nautec.MODID, preset.name()));
            futures.add(DataProvider.saveStable(cache, json, file));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Bacteria Presets";
    }

    private static List<BacteriaPreset> presets() {
        List<BacteriaPreset> presets = new ArrayList<>();

        metal(presets, "stannophiles", "tin", BacteriaBalance.Rarity.COMMON);
        metal(presets, "plumbophiles", "lead", BacteriaBalance.Rarity.COMMON);
        metal(presets, "zincophiles", "zinc", BacteriaBalance.Rarity.COMMON);
        metal(presets, "aluminophiles", "aluminum", BacteriaBalance.Rarity.COMMON);
        metal(presets, "bauxitophiles", "bauxite", BacteriaBalance.Rarity.COMMON);
        metal(presets, "niccolophiles", "nickel", BacteriaBalance.Rarity.UNCOMMON);
        metal(presets, "argentophiles", "silver", BacteriaBalance.Rarity.UNCOMMON);
        metal(presets, "bismuthophiles", "bismuth", BacteriaBalance.Rarity.UNCOMMON);
        metal(presets, "antimonophiles", "antimony", BacteriaBalance.Rarity.UNCOMMON);
        metal(presets, "borophiles", "boron", BacteriaBalance.Rarity.UNCOMMON);
        metal(presets, "cobaltophiles", "cobalt", BacteriaBalance.Rarity.RARE);
        metal(presets, "osmophiles", "osmium", BacteriaBalance.Rarity.RARE);
        metal(presets, "titanophiles", "titanium", BacteriaBalance.Rarity.RARE);
        metal(presets, "tungstophiles", "tungsten", BacteriaBalance.Rarity.RARE);
        metal(presets, "uranophiles", "uranium", BacteriaBalance.Rarity.RARE);
        metal(presets, "thoriophiles", "thorium", BacteriaBalance.Rarity.RARE);
        metal(presets, "chromophiles", "chromium", BacteriaBalance.Rarity.RARE);
        metal(presets, "manganophiles", "manganese", BacteriaBalance.Rarity.RARE);
        metal(presets, "niobophiles", "niobium", BacteriaBalance.Rarity.RARE);
        metal(presets, "monazitophiles", "monazite", BacteriaBalance.Rarity.RARE);
        metal(presets, "iesniophiles", "iesnium", BacteriaBalance.Rarity.RARE);
        metal(presets, "platinophiles", "platinum", BacteriaBalance.Rarity.EPIC);
        metal(presets, "palladophiles", "palladium", BacteriaBalance.Rarity.EPIC);
        metal(presets, "iridophiles", "iridium", BacteriaBalance.Rarity.EPIC);
        metal(presets, "beryllophiles", "beryllium", BacteriaBalance.Rarity.RARE);
        metal(presets, "molybdophiles", "molybdenum", BacteriaBalance.Rarity.RARE);
        metal(presets, "neodymophiles", "neodymium", BacteriaBalance.Rarity.RARE);
        metal(presets, "vanadophiles", "vanadium", BacteriaBalance.Rarity.RARE);
        metal(presets, "plutonophiles", "plutonium", BacteriaBalance.Rarity.LEGENDARY);
        metal(presets, "vibranophiles", "vibranium", BacteriaBalance.Rarity.LEGENDARY);
        metal(presets, "adamantiophiles", "allthemodium", BacteriaBalance.Rarity.LEGENDARY);
        metal(presets, "unobtainophiles", "unobtainium", BacteriaBalance.Rarity.LEGENDARY);
        metal(presets, "neutronophiles", "neutronium", BacteriaBalance.Rarity.LEGENDARY);
        metal(presets, "desmophiles", "desh", BacteriaBalance.Rarity.RARE);
        metal(presets, "ostrophiles", "ostrum", BacteriaBalance.Rarity.EPIC);
        metal(presets, "calorophiles", "calorite", BacteriaBalance.Rarity.EPIC);
        metal(presets, "draconophiles", "draconium", BacteriaBalance.Rarity.EPIC);
        metal(presets, "cyanophiles", "cyanite", BacteriaBalance.Rarity.RARE);
        metal(presets, "blutonophiles", "blutonium", BacteriaBalance.Rarity.EPIC);

        alloy(presets, "steelophiles", "steel", BacteriaBalance.Rarity.COMMON);
        alloy(presets, "bronzophiles", "bronze", BacteriaBalance.Rarity.COMMON);
        alloy(presets, "brassophiles", "brass", BacteriaBalance.Rarity.COMMON);
        alloy(presets, "graphitophiles", "graphite", BacteriaBalance.Rarity.COMMON);
        alloy(presets, "invarophiles", "invar", BacteriaBalance.Rarity.UNCOMMON);
        alloy(presets, "electrophiles", "electrum", BacteriaBalance.Rarity.UNCOMMON);
        alloy(presets, "constantophiles", "constantan", BacteriaBalance.Rarity.UNCOMMON);
        alloy(presets, "inoxophiles", "stainless_steel", BacteriaBalance.Rarity.UNCOMMON);
        alloy(presets, "signalophiles", "signalum", BacteriaBalance.Rarity.RARE);
        alloy(presets, "lumiophiles", "lumium", BacteriaBalance.Rarity.RARE);
        alloy(presets, "enderophiles", "enderium", BacteriaBalance.Rarity.EPIC);
        alloy(presets, "ferrosuinophiles", "pig_iron", BacteriaBalance.Rarity.COMMON);
        alloy(presets, "chrysorosophiles", "rose_gold", BacteriaBalance.Rarity.UNCOMMON);
        alloy(presets, "amethystobronzophiles", "amethyst_bronze", BacteriaBalance.Rarity.UNCOMMON);
        alloy(presets, "hepatizophiles", "hepatizon", BacteriaBalance.Rarity.RARE);
        alloy(presets, "limosiderophiles", "slimesteel", BacteriaBalance.Rarity.RARE);
        alloy(presets, "equitophiles", "knightslime", BacteriaBalance.Rarity.RARE);
        alloy(presets, "manyullophiles", "manyullyn", BacteriaBalance.Rarity.EPIC);
        alloy(presets, "reginophiles", "queens_slime", BacteriaBalance.Rarity.EPIC);
        alloy(presets, "manastelophiles", "manasteel", BacteriaBalance.Rarity.RARE);
        alloy(presets, "elementophiles", "elementium", BacteriaBalance.Rarity.EPIC);
        alloy(presets, "terrastelophiles", "terrasteel", BacteriaBalance.Rarity.LEGENDARY);
        alloy(presets, "obsidianophiles", "refined_obsidian", BacteriaBalance.Rarity.RARE);
        alloy(presets, "lucernophiles", "refined_glowstone", BacteriaBalance.Rarity.RARE);
        alloy(presets, "nigrophiles", "netherite", BacteriaBalance.Rarity.LEGENDARY);

        gem(presets, "apatitophiles", "apatite", BacteriaBalance.Rarity.COMMON);
        gem(presets, "rubellophiles", "ruby", BacteriaBalance.Rarity.UNCOMMON);
        gem(presets, "sapphirophiles", "sapphire", BacteriaBalance.Rarity.UNCOMMON);
        gem(presets, "topazophiles", "topaz", BacteriaBalance.Rarity.UNCOMMON);
        gem(presets, "peridotophiles", "peridot", BacteriaBalance.Rarity.UNCOMMON);
        gem(presets, "fluorophiles", "fluorite", BacteriaBalance.Rarity.UNCOMMON);
        gem(presets, "ambrophiles", "amber", BacteriaBalance.Rarity.UNCOMMON);
        gem(presets, "jadophiles", "jade", BacteriaBalance.Rarity.UNCOMMON);
        gem(presets, "cinnabarophiles", "cinnabar", BacteriaBalance.Rarity.RARE);
        gem(presets, "selenophiles", "moonstone", BacteriaBalance.Rarity.RARE);
        gem(presets, "heliophiles", "sunstone", BacteriaBalance.Rarity.RARE);
        gem(presets, "nyctophiles", "dark_gem", BacteriaBalance.Rarity.RARE);
        gem(presets, "melanoquartzophiles", "black_quartz", BacteriaBalance.Rarity.RARE);
        gem(presets, "certusophiles", "certus_quartz", BacteriaBalance.Rarity.RARE);

        presets.add(BacteriaPreset.of("salinophiles",
                List.of("#c:ores/salt", "#c:dusts/salt", "#c:gems/salt"), BacteriaBalance.Rarity.COMMON,
                List.of("#c:dusts/salt", "#c:gems/salt")));
        presets.add(BacteriaPreset.of("nitrophiles",
                List.of("#c:ores/niter", "#c:dusts/niter", "#c:gems/niter"), BacteriaBalance.Rarity.COMMON,
                List.of("#c:dusts/niter", "#c:gems/niter")));
        presets.add(BacteriaPreset.of("sulfurophiles",
                List.of("#c:ores/sulfur", "#c:dusts/sulfur", "#c:gems/sulfur"), BacteriaBalance.Rarity.COMMON,
                List.of("#c:dusts/sulfur", "#c:gems/sulfur")));
        presets.add(BacteriaPreset.of("carbonophiles",
                List.of("#c:dusts/coal_coke", "#c:gems/coal_coke"), BacteriaBalance.Rarity.COMMON,
                List.of("#c:gems/coal_coke", "#c:dusts/coal_coke")));
        presets.add(BacteriaPreset.of("resinophiles",
                List.of("#c:rubber", "#c:resin", "#c:ingots/insulating_resin", "#c:plastic", "#c:plastics"),
                BacteriaBalance.Rarity.UNCOMMON,
                List.of("#c:rubber", "#c:resin", "#c:ingots/insulating_resin", "#c:plastic", "#c:plastics")));
        presets.add(BacteriaPreset.of("silicophiles",
                List.of("#c:ores/silicon", "#c:raw_materials/silicon", "#c:dusts/silicon", "#c:ingots/silicon", "#c:gems/silicon"),
                BacteriaBalance.Rarity.RARE,
                List.of("#c:ingots/silicon", "#c:gems/silicon", "#c:silicon", "#c:dusts/silicon")));

        return presets;
    }

    private static void metal(List<BacteriaPreset> presets, String name, String material, BacteriaBalance.Rarity rarity) {
        presets.add(BacteriaPreset.of(name, nutrients(material), rarity, List.of(
                "#c:ingots/" + material,
                "#c:raw_materials/" + material,
                "#c:dusts/" + material,
                "#c:gems/" + material)));
    }

    private static void alloy(List<BacteriaPreset> presets, String name, String material, BacteriaBalance.Rarity rarity) {
        presets.add(BacteriaPreset.of(name, nutrients(material), rarity, List.of(
                "#c:ingots/" + material,
                "#c:dusts/" + material,
                "#c:gems/" + material)));
    }

    private static void gem(List<BacteriaPreset> presets, String name, String material, BacteriaBalance.Rarity rarity) {
        presets.add(BacteriaPreset.of(name, nutrients(material), rarity, List.of(
                "#c:gems/" + material,
                "#c:dusts/" + material,
                "#c:ingots/" + material)));
    }

    private static List<String> nutrients(String material) {
        return List.of(
                "#c:ores/" + material,
                "#c:raw_materials/" + material,
                "#c:dusts/" + material,
                "#c:gems/" + material,
                "#c:ingots/" + material,
                "#c:storage_blocks/" + material);
    }
}
