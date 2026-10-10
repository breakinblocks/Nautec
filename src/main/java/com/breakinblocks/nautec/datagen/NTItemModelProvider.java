package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.fluids.NTFluid;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.client.model.generators.loaders.SeparateTransformsModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class NTItemModelProvider extends ItemModelProvider {
    public static final ResourceLocation ABILITY_ENABLED = Nautec.rl("ability_enabled");
    public static final ResourceLocation HAS_BACTERIA = Nautec.rl("has_bacteria");
    public static final ResourceLocation CAST = ResourceLocation.withDefaultNamespace("cast");

    private static final ModelFile GENERATED = new ModelFile.UncheckedModelFile("item/generated");
    private static final ModelFile HANDHELD = new ModelFile.UncheckedModelFile("item/handheld");
    private static final ModelFile HANDHELD_ROD = new ModelFile.UncheckedModelFile("item/handheld_rod");
    private static final ModelFile BUILTIN_ENTITY = new ModelFile.UncheckedModelFile("builtin/entity");
    private static final ModelFile BUCKET = new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath("neoforge", "item/bucket"));

    private final Set<Item> handled = new HashSet<>();

    public NTItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Nautec.MODID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "NauTec Item Model Definitions";
    }

    @Override
    protected void registerModels() {
        handled.clear();

        basicItem(NTItems.AQUARINE_STEEL_INGOT.get());
        basicItem(NTItems.ATLANTIC_GOLD_INGOT.get());
        basicItem(NTItems.ATLANTIC_GOLD_NUGGET.get());
        basicItem(NTItems.PRISMARINE_CRYSTAL_SHARD.get());
        basicItem(NTItems.RESONANT_SHARD.get());
        basicItem(NTItems.PRESSURE_SYNTHESIZER.get());
        basicItem(NTItems.ATLANTEAN_PRESSURE_SYNTHESIZER.get());
        basicItem(NTItems.FLAWLESS_PRISMARINE_CRYSTAL.get());
        basicItem(NTItems.DEEP_STEEL_PLATING.get());
        basicItem(NTItems.REACTOR_SPEED_UPGRADE.get());
        basicItem(NTItems.GRAFTING_ANCHOR.get());
        basicItem(NTItems.ADVANCED_GRAFTING_ANCHOR.get());
        basicItem(NTItems.ENERGY_CONVERSION_UPGRADE.get());
        basicItem(NTItems.AQUARINE_COPPER_COMPOUND.get());
        basicItem(NTItems.AQUARINE_COPPER_INGOT.get());
        basicItem(NTItems.AQUARINE_COPPER_NUGGET.get());
        basicItem(NTItems.CLAY_GASKET.get());
        basicItem(NTItems.EDDY_UPGRADE.get());
        basicItem(NTItems.SURGE_UPGRADE.get());
        basicItem(NTItems.RIPTIDE_UPGRADE.get());
        basicItem(NTItems.MAELSTROM_UPGRADE.get());
        basicItem(NTItems.FILTER.get());
        basicItem(NTItems.RESONANT_EXPANSION.get());
        basicItem(NTItems.INTRICATE_FILTER.get());
        handAuthoredItem(NTBlocks.CURRENT_CONDUIT.asItem());
        basicItem(NTItems.ADVANCED_ENERGY_CONVERSION_UPGRADE.get());
        basicItem(NTItems.ULTIMATE_ENERGY_CONVERSION_UPGRADE.get());
        basicItem(NTItems.REACTOR_YIELD_UPGRADE.get());
        basicItem(NTItems.REACTOR_EFFICIENCY_UPGRADE.get());
        basicItem(NTItems.REACTOR_FUSION_UPGRADE.get());
        basicItem(NTItems.SPAWNER_CONFINEMENT_MATRIX.get());
        basicItem(NTItems.TUNING_FORK.get());
        basicItem(NTItems.CONFIGURATION_CARD.get());
        basicItem(NTItems.RESONANCE_CHARM.get());
        handAuthoredItem(NTItems.PRISM_SATELLITE.get());
        basicItem(NTItems.DORMANT_CRYSTAL_SEED.get());
        basicItem(NTItems.PRISMARINE_CRYSTAL_SEED.get());
        basicItem(NTItems.PRISMARINE_LENS.get());
        basicItem(NTItems.AQUARINE_STEEL_COMPOUND.get());
        basicItem(NTItems.CAST_IRON_COMPOUND.get());
        basicItem(NTItems.SALT.get());
        basicItem(NTItems.KELP_SLURRY.get());
        basicItem(NTItems.ALGAL_LIPID.get());
        basicItem(NTItems.AIR_BOTTLE.get());
        basicItem(NTItems.BUBBLE_CAPSULE.get());

        basicItem(NTItems.ELDRITCH_HEART.get());
        basicItem(NTItems.DROWNED_LUNGS.get());
        basicItem(NTItems.GUARDIAN_EYE.get());
        basicItem(NTItems.DOLPHIN_FIN.get());

        basicItem(NTItems.CLAW_ROBOT_ARM.get());

        basicItem(NTItems.LUMINOUS_MEMBRANE.get());
        basicItem(NTItems.CHITIN_PLATE.get());
        basicItem(NTItems.ABYSSAL_ORGAN.get());
        basicItem(NTItems.SILT_SKIPPER.get());
        basicItem(NTItems.SILT_SKIPPER_BUCKET.get());
        basicItem(NTItems.SILT_SKIPPER_SPAWN_EGG.get());
        basicItem(NTItems.LANTERN_JELLY_SPAWN_EGG.get());
        basicItem(NTItems.VENT_CRAWLER_SPAWN_EGG.get());
        basicItem(NTItems.ABYSSAL_MAW_SPAWN_EGG.get());

        basicItem(NTItems.HYDRAULIC_LEG.get());
        basicItem(NTItems.SERVO_KNEE.get());
        basicItem(NTItems.SHOCK_ABSORBER.get());
        basicItem(NTItems.TENDON_WEAVE.get());
        basicItem(NTItems.MAGNETIC_COIL_ARM.get());
        basicItem(NTItems.ENDER_COIL_ARM.get());
        basicItem(NTItems.HYDRO_DRILL_ARM.get());
        basicItem(NTItems.TRIDENT_LAUNCHER_ARM.get());
        basicItem(NTItems.VOLLEY_TRIDENT_ARM.get());
        basicItem(NTItems.SYRINGE_ROBOT_ARM.get());
        basicItem(NTItems.BUOYANCY_TANK.get());
        basicItem(NTItems.AUXILIARY_VENTRICLE.get());

        basicItem(NTItems.CAST_IRON_INGOT.get());
        basicItem(NTItems.CAST_IRON_NUGGET.get());
        basicItem(NTItems.CAST_IRON_ROD.get());
        basicItem(NTItems.BROWN_POLYMER.get());

        basicItem(NTItems.RUSTY_GEAR.get());
        basicItem(NTItems.GEAR.get());
        basicItem(NTItems.BROKEN_WHISK.get());
        basicItem(NTItems.WHISK.get());
        basicItem(NTItems.BURNT_COIL.get());
        basicItem(NTItems.LASER_CHANNELING_COIL.get());
        basicItem(NTItems.AQUATIC_CHIP.get());
        basicItem(NTItems.DAMAGED_AQUATIC_CHIP.get());

        basicItem(NTItems.GLASS_VIAL.get());
        basicItem(NTItems.ELECTROLYTE_ALGAE_SERUM_VIAL.get());

        petriDishItem(NTItems.PETRI_DISH.get());

        basicItem(NTItems.PRISM_MONOCLE.get());
        basicItem(NTItems.EYE_OF_THE_SEA.get());

        basicItem(NTItems.DIVING_HELMET.get());
        basicItem(NTItems.DIVING_CHESTPLATE.get());
        basicItem(NTItems.DIVING_LEGGINGS.get());
        basicItem(NTItems.DIVING_BOOTS.get());

        handAuthoredItem(NTBlocks.RESONANCE_NODE.asItem());
        entityItem(NTItems.SUBMARINE.get(), Nautec.rl("item/submarine_base"));
        entityItem(NTItems.WAVE_JET.get(), Nautec.rl("item/wave_jet_base"));
        entityItem(NTBlocks.LASER_CRAFTING_MATRIX.asItem(), Nautec.rl("block/laser_crafting_matrix"));
        entityItem(NTBlocks.RESONANT_VAULT.asItem(), Nautec.rl("block/resonant_vault"));
        entityItem(NTItems.ATLANTEAN_RIFLE.get(), Nautec.rl("item/atlantean_rifle_base"));

        for (var module : NTItems.SUBMARINE_MODULES) {
            basicItem(module.get());
        }

        handAuthoredItem(NTItems.VALVE.get());
        handAuthoredItem(NTItems.ANCIENT_VALVE.get());
        handAuthoredItem(NTBlocks.BACTERIAL_ANALYZER.asItem());
        handAuthoredItem(NTItems.NAUTEC_GUIDE.get());

        neptunesTrident(NTItems.NEPTUNES_TRIDENT.get());

        fishingRod(NTItems.NAUTEC_FISHING_ROD.get());
        handheldItem(NTItems.AQUARINE_WRENCH.get());
        handheldItem(NTItems.GRAFTING_TOOL.get());

        for (NTFluid fluid : NTFluids.HELPER.getFluids()) {
            bucket(fluid.getStillFluid());
        }

        aquarineSteelTool(NTItems.AQUARINE_AXE.get());
        aquarineSteelTool(NTItems.AQUARINE_HOE.get());
        aquarineSteelTool(NTItems.AQUARINE_PICKAXE.get());
        aquarineSteelTool(NTItems.AQUARINE_SHOVEL.get());
        aquarineSteelTool(NTItems.AQUARINE_SWORD.get());
        basicItem(NTItems.PRISMATIC_BATTERY.get());

        basicItem(NTItems.AQUARINE_HELMET.get());
        basicItem(NTItems.AQUARINE_CHESTPLATE.get());
        basicItem(NTItems.AQUARINE_LEGGINGS.get());
        basicItem(NTItems.AQUARINE_BOOTS.get());

        parentItemBlock(NTBlocks.LASER_JUNCTION.asItem(), "_base");

        entityItem(NTBlocks.ANCHOR.asItem(), Nautec.rl("block/anchor"));
        entityItem(NTBlocks.PRISMARINE_CRYSTAL.asItem(), Nautec.rl("block/prismarine_crystal"));
        entityItem(NTBlocks.DECORATIVE_PRISMARINE_CRYSTAL.asItem(), Nautec.rl("block/decorative_prismarine_crystal"));

        blockTextureItem(NTBlocks.DEEP_KELP.get());
        blockTextureItem(NTBlocks.GLOW_POLYP.get());
        blockTextureItem(NTBlocks.LUMINESCENT_ALGAE.get());
        blockTextureItem(NTBlocks.PRISMARINE_FROND.get());
        blockTextureItem(NTBlocks.VENT_TUBEWORM.get());
        blockTextureItem(NTBlocks.ABYSSAL_CORAL.get());
        blockTextureItem(NTBlocks.SMALL_PRISMARINE_BUD.get());
        blockTextureItem(NTBlocks.MEDIUM_PRISMARINE_BUD.get());
        blockTextureItem(NTBlocks.LARGE_PRISMARINE_BUD.get());
        blockTextureItem(NTBlocks.PRISMARINE_CLUSTER.get());
        basicItem(NTBlocks.PRESSURE_HATCH.asItem());

        blockItems();
    }

    @Override
    public ItemModelBuilder basicItem(Item item) {
        handled.add(item);
        return getBuilder(key(item).toString())
                .parent(GENERATED)
                .texture("layer0", itemTexture(item, ""));
    }

    @Override
    public ItemModelBuilder handheldItem(Item item) {
        handled.add(item);
        return getBuilder(key(item).toString())
                .parent(HANDHELD)
                .texture("layer0", itemTexture(item, ""));
    }

    private void blockTextureItem(Block block) {
        Item item = block.asItem();
        handled.add(item);
        ResourceLocation name = key(item);
        getBuilder(name.toString())
                .parent(GENERATED)
                .texture("layer0", ModelPaths.blockModel(BuiltInRegistries.BLOCK.getKey(block)));
    }

    private void handAuthoredItem(Item item) {
        handled.add(item);
    }

    private void entityItem(Item item, ResourceLocation baseModel) {
        handled.add(item);
        Map<ItemDisplayContext, JsonObject> display = new LinkedHashMap<>();
        String[] guiLight = new String[1];
        String[] particle = new String[1];
        resolveBase(baseModel, display, guiLight, particle);

        ItemModelBuilder builder = getBuilder(key(item).toString()).parent(BUILTIN_ENTITY);
        if (particle[0] != null) {
            builder.texture("particle", particle[0]);
        }
        if (guiLight[0] != null) {
            builder.guiLight(guiLight[0].equals("front") ? BlockModel.GuiLight.FRONT : BlockModel.GuiLight.SIDE);
        }
        for (Map.Entry<ItemDisplayContext, JsonObject> entry : display.entrySet()) {
            JsonObject transform = entry.getValue();
            var vec = builder.transforms().transform(entry.getKey());
            float[] rotation = vector(transform, "rotation", 0.0F);
            float[] translation = vector(transform, "translation", 0.0F);
            float[] scale = vector(transform, "scale", 1.0F);
            vec.rotation(rotation[0], rotation[1], rotation[2])
                    .translation(translation[0], translation[1], translation[2])
                    .scale(scale[0], scale[1], scale[2]);
            if (transform.has("right_rotation")) {
                float[] right = vector(transform, "right_rotation", 0.0F);
                vec.rightRotation(right[0], right[1], right[2]);
            }
            vec.end();
        }
    }

    private void resolveBase(ResourceLocation model, Map<ItemDisplayContext, JsonObject> display, String[] guiLight, String[] particle) {
        JsonObject json = readModel(model);
        if (json.has("parent")) {
            ResourceLocation parent = ResourceLocation.parse(json.get("parent").getAsString());
            if (!parent.getPath().startsWith("builtin/")) {
                resolveBase(parent, display, guiLight, particle);
            }
        }
        if (json.has("gui_light")) {
            guiLight[0] = json.get("gui_light").getAsString();
        }
        if (json.has("textures")) {
            JsonObject textures = json.getAsJsonObject("textures");
            if (textures.has("particle")) {
                String value = textures.get("particle").getAsString();
                if (!value.startsWith("#")) {
                    particle[0] = value;
                }
            }
        }
        if (json.has("display")) {
            JsonObject own = json.getAsJsonObject("display");
            Map<ItemDisplayContext, JsonObject> parsed = new LinkedHashMap<>();
            for (ItemDisplayContext context : ItemDisplayContext.values()) {
                JsonElement element = own.get(context.getSerializedName());
                if (element != null && element.isJsonObject()) {
                    parsed.put(context, element.getAsJsonObject());
                }
            }
            if (!parsed.containsKey(ItemDisplayContext.THIRD_PERSON_LEFT_HAND) && parsed.containsKey(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {
                parsed.put(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, parsed.get(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND));
            }
            if (!parsed.containsKey(ItemDisplayContext.FIRST_PERSON_LEFT_HAND) && parsed.containsKey(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)) {
                parsed.put(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, parsed.get(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND));
            }
            display.putAll(parsed);
        }
    }

    private JsonObject readModel(ResourceLocation model) {
        try (Reader reader = existingFileHelper.getResource(model, PackType.CLIENT_RESOURCES, ".json", "models").openAsReader()) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read model " + model, e);
        }
    }

    private static float[] vector(JsonObject transform, String key, float fallback) {
        float[] out = {fallback, fallback, fallback};
        if (transform.has(key)) {
            JsonArray array = transform.getAsJsonArray(key);
            for (int i = 0; i < 3 && i < array.size(); i++) {
                out[i] = array.get(i).getAsFloat();
            }
        }
        return out;
    }

    private void neptunesTrident(Item item) {
        handled.add(item);
        ItemModelBuilder builder = getBuilder(key(item).toString())
                .texture("particle", itemTexture(item, ""))
                .guiLight(BlockModel.GuiLight.FRONT);
        ResourceLocation gui = Nautec.rl("item/neptunes_trident_gui");
        builder.customLoader(SeparateTransformsModelBuilder::begin)
                .base(nested().parent(getExistingFile(Nautec.rl("item/neptunes_trident_handheld"))))
                .perspective(ItemDisplayContext.GUI, nested().parent(getExistingFile(gui)))
                .perspective(ItemDisplayContext.FIXED, nested().parent(getExistingFile(gui)))
                .perspective(ItemDisplayContext.GROUND, nested().parent(getExistingFile(gui)));
    }

    private void fishingRod(Item item) {
        handled.add(item);
        ResourceLocation name = key(item);
        ItemModelBuilder cast = getBuilder(name + "_cast")
                .parent(HANDHELD_ROD)
                .texture("layer0", itemTexture(item, "_cast"));
        getBuilder(name.toString())
                .parent(HANDHELD_ROD)
                .texture("layer0", itemTexture(item, ""))
                .override()
                .predicate(CAST, 1.0F)
                .model(cast)
                .end();
    }

    private void bucket(Fluid fluid) {
        Item bucket = fluid.getBucket();
        handled.add(bucket);
        getBuilder(key(bucket).toString())
                .parent(BUCKET)
                .customLoader(DynamicFluidContainerModelBuilder::begin)
                .fluid(fluid)
                .applyTint(true);
    }

    private static @NotNull ResourceLocation key(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem());
    }

    private static ResourceLocation itemTexture(Item item, String suffix) {
        return ModelPaths.extend(ModelPaths.itemModel(key(item)), suffix);
    }

    private void blockItems() {
        for (Supplier<BlockItem> blockItem : NTItems.blockItems()) {
            BlockItem item = blockItem.get();
            if (handled.contains(item)) {
                continue;
            }
            parentItemBlock(item);
        }
    }

    public void parentItemBlock(Item item) {
        parentItemBlock(item, "");
    }

    public void parentItemBlock(Item item, String suffix) {
        handled.add(item);
        ResourceLocation name = key(item);
        getBuilder(name.toString())
                .parent(new ModelFile.UncheckedModelFile(ModelPaths.blockModel(name, suffix)));
    }

    public void petriDishItem(Item item) {
        ResourceLocation name = key(item);
        ItemModelBuilder bacteria = getBuilder(name + "_bacteria")
                .parent(GENERATED)
                .texture("layer0", itemTexture(item, ""))
                .texture("layer1", itemTexture(item, "_overlay"));
        basicItem(item)
                .override()
                .predicate(HAS_BACTERIA, 1.0F)
                .model(bacteria)
                .end();
    }

    public void aquarineSteelTool(Item item) {
        ResourceLocation name = key(item);
        ItemModelBuilder enabled = getBuilder(name + "_enabled")
                .parent(HANDHELD)
                .texture("layer0", itemTexture(item, "_enabled"));
        handheldItem(item)
                .override()
                .predicate(ABILITY_ENABLED, 1.0F)
                .model(enabled)
                .end();
    }
}
