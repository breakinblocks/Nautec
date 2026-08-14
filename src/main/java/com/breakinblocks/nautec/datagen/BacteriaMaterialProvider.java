package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.data.generated.BacteriaBalance;
import com.breakinblocks.nautec.data.generated.BacteriaBalance.Rarity;
import com.breakinblocks.nautec.data.generated.BacteriaJsonWriter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BacteriaMaterialProvider implements DataProvider {
    private enum Kind {
        METAL("ingots"),
        ALLOY("ingots"),
        GEM("gems");

        private final String family;

        Kind(String family) {
            this.family = family;
        }
    }

    private record Entry(String name, String material, Kind kind, Rarity rarity, List<String> mods) {
    }

    private static final Ingredient PLACEHOLDER = Ingredient.of(Items.STONE);

    private static final List<Entry> ENTRIES = List.of(
            new Entry("stannophiles", "tin", Kind.METAL, Rarity.COMMON, List.of("ae2", "enderio", "energizedpower", "ftbic", "ftbmaterials", "gtceu", "immersiveengineering", "mekanism", "techreborn", "thermal")),
            new Entry("plumbophiles", "lead", Kind.METAL, Rarity.COMMON, List.of("colossal_reactors", "ftbic", "ftbmaterials", "gtceu", "immersiveengineering", "mekanism", "techreborn", "thermal")),
            new Entry("zincophiles", "zinc", Kind.METAL, Rarity.COMMON, List.of("create", "ftbmaterials", "gtceu", "techreborn", "thermal")),
            new Entry("aluminophiles", "aluminum", Kind.METAL, Rarity.COMMON, List.of("ftbic", "ftbmaterials", "gtceu", "immersiveengineering", "modern_industrialization", "techreborn", "xycraft_machines", "xycraft_world")),
            new Entry("bauxitophiles", "bauxite", Kind.METAL, Rarity.COMMON, List.of("ftbmaterials")),
            new Entry("niccolophiles", "nickel", Kind.METAL, Rarity.UNCOMMON, List.of("ftbic", "ftbmaterials", "gtceu", "immersiveengineering", "oritech", "techreborn", "thermal")),
            new Entry("argentophiles", "silver", Kind.METAL, Rarity.UNCOMMON, List.of("ftbic", "ftbmaterials", "gtceu", "ltxi", "occultism", "techreborn", "thermal")),
            new Entry("bismuthophiles", "bismuth", Kind.METAL, Rarity.UNCOMMON, List.of("gtceu", "techreborn")),
            new Entry("antimonophiles", "antimony", Kind.METAL, Rarity.UNCOMMON, List.of("ftbmaterials")),
            new Entry("borophiles", "boron", Kind.METAL, Rarity.UNCOMMON, List.of("colossal_reactors")),
            new Entry("cobaltophiles", "cobalt", Kind.METAL, Rarity.RARE, List.of("gtceu", "hephaestus", "tconstruct")),
            new Entry("osmophiles", "osmium", Kind.METAL, Rarity.RARE, List.of("ftbmaterials", "gtceu", "mekanism")),
            new Entry("titanophiles", "titanium", Kind.METAL, Rarity.RARE, List.of("ftbmaterials", "gtceu", "ltxi", "modern_industrialization", "techreborn")),
            new Entry("tungstophiles", "tungsten", Kind.METAL, Rarity.RARE, List.of("ftbmaterials", "gtceu", "modern_industrialization", "techreborn")),
            new Entry("uranophiles", "uranium", Kind.METAL, Rarity.RARE, List.of("colossal_reactors", "ftbic", "ftbmaterials", "gtceu", "immersiveengineering", "mekanism", "oritech", "techreborn")),
            new Entry("thoriophiles", "thorium", Kind.METAL, Rarity.RARE, List.of("gtceu", "techreborn")),
            new Entry("chromophiles", "chromium", Kind.METAL, Rarity.RARE, List.of("colossal_reactors", "ftbmaterials")),
            new Entry("manganophiles", "manganese", Kind.METAL, Rarity.RARE, List.of("gtceu", "techreborn")),
            new Entry("niobophiles", "niobium", Kind.METAL, Rarity.RARE, List.of("ltxi")),
            new Entry("monazitophiles", "monazite", Kind.METAL, Rarity.RARE, List.of("ftbmaterials")),
            new Entry("iesniophiles", "iesnium", Kind.METAL, Rarity.RARE, List.of("occultism")),
            new Entry("platinophiles", "platinum", Kind.METAL, Rarity.EPIC, List.of("ftbmaterials", "gtceu", "modern_industrialization", "oritech", "techreborn")),
            new Entry("palladophiles", "palladium", Kind.METAL, Rarity.EPIC, List.of("gtceu", "techreborn")),
            new Entry("iridophiles", "iridium", Kind.METAL, Rarity.EPIC, List.of("ftbic", "ftbmaterials", "gtceu", "mekanism", "modern_industrialization", "techreborn")),
            new Entry("beryllophiles", "beryllium", Kind.METAL, Rarity.RARE, List.of("gtceu", "techreborn")),
            new Entry("molybdophiles", "molybdenum", Kind.METAL, Rarity.RARE, List.of("gtceu", "techreborn")),
            new Entry("neodymophiles", "neodymium", Kind.METAL, Rarity.RARE, List.of("gtceu", "techreborn")),
            new Entry("vanadophiles", "vanadium", Kind.METAL, Rarity.RARE, List.of("gtceu", "techreborn")),
            new Entry("plutonophiles", "plutonium", Kind.METAL, Rarity.LEGENDARY, List.of("ftbic", "ftbmaterials", "oritech")),
            new Entry("vibranophiles", "vibranium", Kind.METAL, Rarity.LEGENDARY, List.of("allthemodium")),
            new Entry("adamantiophiles", "allthemodium", Kind.METAL, Rarity.LEGENDARY, List.of("allthemodium")),
            new Entry("unobtainophiles", "unobtainium", Kind.METAL, Rarity.LEGENDARY, List.of("allthemodium")),
            new Entry("neutronophiles", "neutronium", Kind.METAL, Rarity.LEGENDARY, List.of("avaritia", "extendedcrafting")),
            new Entry("desmophiles", "desh", Kind.METAL, Rarity.RARE, List.of("ad_astra", "beyond_earth")),
            new Entry("ostrophiles", "ostrum", Kind.METAL, Rarity.EPIC, List.of("ad_astra", "beyond_earth")),
            new Entry("calorophiles", "calorite", Kind.METAL, Rarity.EPIC, List.of("ad_astra", "beyond_earth")),
            new Entry("draconophiles", "draconium", Kind.METAL, Rarity.EPIC, List.of("draconicevolution")),
            new Entry("cyanophiles", "cyanite", Kind.METAL, Rarity.RARE, List.of("bigreactors")),
            new Entry("blutonophiles", "blutonium", Kind.METAL, Rarity.EPIC, List.of("bigreactors")),
            new Entry("steelophiles", "steel", Kind.ALLOY, Rarity.COMMON, List.of("cakesticklib", "create", "energizedpower", "ftbic", "ftbmaterials", "gtceu", "hephaestus", "immersiveengineering", "mekanism", "mffs", "modern_industrialization", "oritech", "techreborn")),
            new Entry("bronzophiles", "bronze", Kind.ALLOY, Rarity.COMMON, List.of("ftbic", "ftbmaterials", "gtceu", "mekanism", "modern_industrialization", "techreborn")),
            new Entry("brassophiles", "brass", Kind.ALLOY, Rarity.COMMON, List.of("create", "ftbmaterials", "gtceu", "techreborn", "thaumaturge")),
            new Entry("graphitophiles", "graphite", Kind.ALLOY, Rarity.COMMON, List.of("colossal_reactors", "ftbmaterials")),
            new Entry("invarophiles", "invar", Kind.ALLOY, Rarity.UNCOMMON, List.of("ftbic", "ftbmaterials", "gtceu", "techreborn", "thermal")),
            new Entry("electrophiles", "electrum", Kind.ALLOY, Rarity.UNCOMMON, List.of("ftbic", "ftbmaterials", "gtceu", "oritech", "techreborn", "thermal")),
            new Entry("constantophiles", "constantan", Kind.ALLOY, Rarity.UNCOMMON, List.of("ftbic", "ftbmaterials", "gtceu", "immersiveengineering", "thermal")),
            new Entry("inoxophiles", "stainless_steel", Kind.ALLOY, Rarity.UNCOMMON, List.of("colossal_reactors", "ftbmaterials")),
            new Entry("signalophiles", "signalum", Kind.ALLOY, Rarity.RARE, List.of("thermal")),
            new Entry("lumiophiles", "lumium", Kind.ALLOY, Rarity.RARE, List.of("ftbmaterials")),
            new Entry("enderophiles", "enderium", Kind.ALLOY, Rarity.EPIC, List.of("ftbic")),
            new Entry("ferrosuinophiles", "pig_iron", Kind.ALLOY, Rarity.COMMON, List.of("hephaestus", "tconstruct")),
            new Entry("chrysorosophiles", "rose_gold", Kind.ALLOY, Rarity.UNCOMMON, List.of("hephaestus", "tconstruct")),
            new Entry("amethystobronzophiles", "amethyst_bronze", Kind.ALLOY, Rarity.UNCOMMON, List.of("hephaestus", "tconstruct")),
            new Entry("hepatizophiles", "hepatizon", Kind.ALLOY, Rarity.RARE, List.of("hephaestus", "tconstruct")),
            new Entry("limosiderophiles", "slimesteel", Kind.ALLOY, Rarity.RARE, List.of("hephaestus", "tconstruct")),
            new Entry("equitophiles", "knightslime", Kind.ALLOY, Rarity.RARE, List.of("hephaestus", "tconstruct")),
            new Entry("manyullophiles", "manyullyn", Kind.ALLOY, Rarity.EPIC, List.of("hephaestus", "tconstruct")),
            new Entry("reginophiles", "queens_slime", Kind.ALLOY, Rarity.EPIC, List.of("hephaestus", "tconstruct")),
            new Entry("manastelophiles", "manasteel", Kind.ALLOY, Rarity.RARE, List.of("botania")),
            new Entry("elementophiles", "elementium", Kind.ALLOY, Rarity.EPIC, List.of("botania")),
            new Entry("terrastelophiles", "terrasteel", Kind.ALLOY, Rarity.LEGENDARY, List.of("botania")),
            new Entry("obsidianophiles", "refined_obsidian", Kind.ALLOY, Rarity.RARE, List.of("ftbmaterials")),
            new Entry("lucernophiles", "refined_glowstone", Kind.ALLOY, Rarity.RARE, List.of("ftbmaterials")),
            new Entry("nigrophiles", "netherite", Kind.ALLOY, Rarity.LEGENDARY, List.of("ftbic", "ftbmaterials", "occultism")),
            new Entry("apatitophiles", "apatite", Kind.GEM, Rarity.COMMON, List.of("forestry", "ftbmaterials", "thermal")),
            new Entry("rubellophiles", "ruby", Kind.GEM, Rarity.UNCOMMON, List.of("ftbmaterials", "gtceu", "irons_jewelry", "techreborn", "thermal")),
            new Entry("sapphirophiles", "sapphire", Kind.GEM, Rarity.UNCOMMON, List.of("ftbmaterials", "gtceu", "irons_jewelry", "techreborn")),
            new Entry("topazophiles", "topaz", Kind.GEM, Rarity.UNCOMMON, List.of("arsmagicalegacy", "irons_jewelry")),
            new Entry("peridotophiles", "peridot", Kind.GEM, Rarity.UNCOMMON, List.of("irons_jewelry", "thermal")),
            new Entry("fluorophiles", "fluorite", Kind.GEM, Rarity.UNCOMMON, List.of("ftbmaterials", "ltxi")),
            new Entry("ambrophiles", "amber", Kind.GEM, Rarity.UNCOMMON, List.of("thaumaturge")),
            new Entry("jadophiles", "jade", Kind.GEM, Rarity.UNCOMMON, List.of("occultism")),
            new Entry("cinnabarophiles", "cinnabar", Kind.GEM, Rarity.RARE, List.of("ftbmaterials", "thaumaturge")),
            new Entry("selenophiles", "moonstone", Kind.GEM, Rarity.RARE, List.of("arsmagicalegacy", "irons_jewelry")),
            new Entry("heliophiles", "sunstone", Kind.GEM, Rarity.RARE, List.of("arsmagicalegacy")),
            new Entry("nyctophiles", "dark_gem", Kind.GEM, Rarity.RARE, List.of("evilcraft", "occultism")),
            new Entry("melanoquartzophiles", "black_quartz", Kind.GEM, Rarity.RARE, List.of("actuallyadditions", "occultism", "quark")),
            new Entry("certusophiles", "certus_quartz", Kind.GEM, Rarity.RARE, List.of("ae2", "occultism"))
    );

    private final PackOutput.PathProvider bacteria;
    private final PackOutput.PathProvider recipes;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public BacteriaMaterialProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.bacteria = output.createPathProvider(PackOutput.Target.DATA_PACK, "nautec/bacteria");
        this.recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registries.thenCompose(provider -> {
            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (Entry entry : ENTRIES) {
                if (entry.mods().isEmpty()) {
                    continue;
                }

                Identifier id = Nautec.rl(entry.name());
                ResourceKey<Bacteria> key = ResourceKey.create(NTRegistries.BACTERIA_KEY, id);

                Bacteria.Resource resource = new Bacteria.Resource.ItemTagResource(
                        TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", entry.kind().family + "/" + entry.material())));

                JsonElement bacteriaJson = BacteriaJsonWriter.encodeBacteria(
                        BacteriaBalance.buildBacteria(resource, entry.rarity(), id), provider);
                JsonElement incubation = BacteriaJsonWriter.encodeRecipe(
                        BacteriaBalance.incubationRecipe(key, PLACEHOLDER, entry.rarity()), provider);
                JsonElement mutation = BacteriaJsonWriter.encodeRecipe(
                        BacteriaBalance.mutationRecipe(key, PLACEHOLDER, entry.rarity()), provider);

                incubation.getAsJsonObject().addProperty("nutrient", resource.asString());
                mutation.getAsJsonObject().addProperty("catalyst", resource.asString());

                futures.add(save(cache, bacteria.json(id), bacteriaJson, entry));
                futures.add(save(cache, recipes.json(Nautec.rl("bacteria_incubation/" + entry.name())), incubation, entry));
                futures.add(save(cache, recipes.json(Nautec.rl("bacteria_mutation/" + entry.name())), mutation, entry));
            }

            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    private static CompletableFuture<?> save(CachedOutput cache, Path path, JsonElement json, Entry entry) {
        JsonObject object = json.getAsJsonObject();
        object.add("neoforge:conditions", conditions(entry));
        return DataProvider.saveStable(cache, object, path);
    }

    private static JsonArray conditions(Entry entry) {
        JsonArray any = new JsonArray();
        for (String mod : entry.mods()) {
            JsonObject loaded = new JsonObject();
            loaded.addProperty("type", "neoforge:mod_loaded");
            loaded.addProperty("modid", mod);
            any.add(loaded);
        }

        JsonObject or = new JsonObject();
        or.addProperty("type", "neoforge:or");
        or.add("values", any);

        JsonArray conditions = new JsonArray();
        conditions.add(or);
        return conditions;
    }

    @Override
    public String getName() {
        return "Bacteria Materials";
    }
}
