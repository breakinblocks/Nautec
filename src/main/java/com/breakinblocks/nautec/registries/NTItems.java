package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.compat.modonomicon.ModonomiconCompat;
import com.breakinblocks.nautec.content.items.AirBottleItem;
import com.breakinblocks.nautec.content.items.AquarineArmorItem;
import com.breakinblocks.nautec.content.items.BatteryItem;
import com.breakinblocks.nautec.content.items.DivingSuitArmorItem;
import com.breakinblocks.nautec.content.items.GraftingToolItem;
import com.breakinblocks.nautec.content.items.NeptunesTridentItem;
import com.breakinblocks.nautec.content.items.PetriDishItem;
import com.breakinblocks.nautec.content.items.PrismMonocleItem;
import com.breakinblocks.nautec.content.items.RobotArmItem;
import com.breakinblocks.nautec.content.items.SubmarineItem;
import com.breakinblocks.nautec.content.items.WaveJetItem;
import com.breakinblocks.nautec.content.items.submarine.SubmarineModuleItem;
import com.breakinblocks.nautec.content.items.submarine.SubmarineModuleType;
import com.breakinblocks.nautec.content.items.submarine.TeleportModuleItem;
import com.breakinblocks.nautec.content.items.tools.AquarineAxeItem;
import com.breakinblocks.nautec.content.items.tools.AquarineHoeItem;
import com.breakinblocks.nautec.content.items.tools.AquarinePickaxeItem;
import com.breakinblocks.nautec.content.items.tools.AquarineShovelItem;
import com.breakinblocks.nautec.content.items.tools.AquarineSwordItem;
import com.breakinblocks.nautec.content.items.tools.AquarineWrenchItem;
import com.breakinblocks.nautec.content.items.tools.NautecFishingRodItem;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ComponentBacteriaStorage;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.ItemLike;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.food.FoodProperties;

public final class NTItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Nautec.MODID);
    private static final List<ItemLike> CREATIVE_TAB_ITEMS = new ArrayList<>();
    private static final List<ItemLike> BACTERIA_ITEMS = new ArrayList<>();
    private static final List<Supplier<BlockItem>> BLOCK_ITEMS = new ArrayList<>();

    public static List<ItemLike> creativeTabItems() {
        return Collections.unmodifiableList(CREATIVE_TAB_ITEMS);
    }

    public static List<ItemLike> bacteriaItems() {
        return Collections.unmodifiableList(BACTERIA_ITEMS);
    }

    public static List<Supplier<BlockItem>> blockItems() {
        return Collections.unmodifiableList(BLOCK_ITEMS);
    }

    static void addBacteriaItem(ItemLike item) {
        BACTERIA_ITEMS.add(item);
    }

    static void addBlockItem(Supplier<BlockItem> blockItem) {
        BLOCK_ITEMS.add(blockItem);
    }

    public static final Supplier<Item> NAUTEC_GUIDE;

    public static final DeferredItem<Item> AQUARINE_STEEL_INGOT = registerItem("aquarine_steel_ingot",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> ATLANTIC_GOLD_INGOT = registerItem("atlantic_gold_ingot",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> ATLANTIC_GOLD_NUGGET = registerItem("atlantic_gold_nugget",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> PRISMARINE_CRYSTAL_SHARD = registerItem("prismarine_crystal_shard",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> RESONANT_SHARD = registerItem("resonant_shard",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> FLAWLESS_PRISMARINE_CRYSTAL = registerItem("flawless_prismarine_crystal",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> DEEP_STEEL_PLATING = registerItem("deep_steel_plating",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> PRISMARINE_LENS = registerItem("prismarine_lens",
            Item::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<AirBottleItem> AIR_BOTTLE = registerItem("air_bottle",
            AirBottleItem::new, new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> AQUARINE_STEEL_COMPOUND = registerItem("aquarine_steel_compound",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> CAST_IRON_COMPOUND = registerItem("cast_iron_compound",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> SALT = registerItem("salt", Item::new, new Item.Properties());

    public static final DeferredItem<Item> RUSTY_GEAR = registerItem("rusty_gear",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> GEAR = registerItem("gear",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> ANCIENT_VALVE = registerItem("ancient_valve",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> VALVE = registerItem("valve",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> BROKEN_WHISK = registerItem("broken_whisk",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> WHISK = registerItem("whisk",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> DAMAGED_AQUATIC_CHIP = registerItem("damaged_aquatic_chip",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> AQUATIC_CHIP = registerItem("aquatic_chip",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> BURNT_COIL = registerItem("burnt_coil",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> LASER_CHANNELING_COIL = registerItem("laser_channeling_coil",
            Item::new, new Item.Properties());


    public static final DeferredItem<Item> CAST_IRON_INGOT = registerItem("cast_iron_ingot", Item::new, new Item.Properties());
    public static final DeferredItem<Item> CAST_IRON_NUGGET = registerItem("cast_iron_nugget", Item::new, new Item.Properties());
    public static final DeferredItem<Item> CAST_IRON_ROD = registerItem("cast_iron_rod", Item::new, new Item.Properties());
    public static final DeferredItem<Item> BROWN_POLYMER = registerItem("brown_polymer", Item::new, new Item.Properties());

    public static final DeferredItem<Item> DROWNED_LUNGS = registerItem("drowned_lungs",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> DOLPHIN_FIN = registerItem("dolphin_fin",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> GUARDIAN_EYE = registerItem("guardian_eye",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> ELDRITCH_HEART = registerItem("eldritch_heart",
            Item::new, new Item.Properties());

    public static final DeferredItem<RobotArmItem> CLAW_ROBOT_ARM = registerItem("claw_robot_arm",
            RobotArmItem::new, new Item.Properties());

    public static final DeferredItem<Item> LUMINOUS_MEMBRANE = registerItem("luminous_membrane",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> CHITIN_PLATE = registerItem("chitin_plate",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> ABYSSAL_ORGAN = registerItem("abyssal_organ",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> SILT_SKIPPER = registerItem("silt_skipper",
            Item::new, new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build()));
    public static final DeferredItem<MobBucketItem> SILT_SKIPPER_BUCKET = registerItem("silt_skipper_bucket",
            props -> new MobBucketItem(NTEntities.SILT_SKIPPER.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, props),
            new Item.Properties().stacksTo(1));

    public static final DeferredItem<WaveJetItem> WAVE_JET = registerItem("wave_jet",
            WaveJetItem::new, new Item.Properties().stacksTo(1));

    public static final DeferredItem<NautecFishingRodItem> NAUTEC_FISHING_ROD = registerItem("nautec_fishing_rod",
            NautecFishingRodItem::new, new Item.Properties().durability(128));

    public static final DeferredItem<Item> SILT_SKIPPER_SPAWN_EGG = registerItem("silt_skipper_spawn_egg",
            Item::new, () -> new Item.Properties().spawnEgg(NTEntities.SILT_SKIPPER.get()));
    public static final DeferredItem<Item> LANTERN_JELLY_SPAWN_EGG = registerItem("lantern_jelly_spawn_egg",
            Item::new, () -> new Item.Properties().spawnEgg(NTEntities.LANTERN_JELLY.get()));
    public static final DeferredItem<Item> VENT_CRAWLER_SPAWN_EGG = registerItem("vent_crawler_spawn_egg",
            Item::new, () -> new Item.Properties().spawnEgg(NTEntities.VENT_CRAWLER.get()));
    public static final DeferredItem<Item> ABYSSAL_MAW_SPAWN_EGG = registerItem("abyssal_maw_spawn_egg",
            Item::new, () -> new Item.Properties().spawnEgg(NTEntities.ABYSSAL_MAW.get()));

    public static final DeferredItem<Item> HYDRAULIC_LEG = registerItem("hydraulic_leg",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> SERVO_KNEE = registerItem("servo_knee",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> SHOCK_ABSORBER = registerItem("shock_absorber",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> TENDON_WEAVE = registerItem("tendon_weave",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> MAGNETIC_COIL_ARM = registerItem("magnetic_coil_arm",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> ENDER_COIL_ARM = registerItem("ender_coil_arm",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> HYDRO_DRILL_ARM = registerItem("hydro_drill_arm",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> TRIDENT_LAUNCHER_ARM = registerItem("trident_launcher_arm",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> VOLLEY_TRIDENT_ARM = registerItem("volley_trident_arm",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> SYRINGE_ROBOT_ARM = registerItem("syringe_robot_arm",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> BUOYANCY_TANK = registerItem("buoyancy_tank",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> AUXILIARY_VENTRICLE = registerItem("auxiliary_ventricle",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> GLASS_VIAL = registerItem("glass_vial", Item::new, new Item.Properties());
    public static final DeferredItem<Item> ELECTROLYTE_ALGAE_SERUM_VIAL = registerItem("eas_vial", Item::new, new Item.Properties());

    public static final DeferredItem<GraftingToolItem> GRAFTING_TOOL = bacteriaItem(registerItem("grafting_tool", GraftingToolItem::new,
            () -> new Item.Properties().durability(80)));
    public static final DeferredItem<PetriDishItem> PETRI_DISH = bacteriaItem(registerItem("petri_dish", PetriDishItem::new, () -> new Item.Properties()
            .stacksTo(1)
            .component(NTDataComponents.BACTERIA, ComponentBacteriaStorage.EMPTY)));

    public static final DeferredItem<BatteryItem> PRISMATIC_BATTERY = registerItem("prismatic_battery",
            BatteryItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<PrismMonocleItem> PRISM_MONOCLE = registerItem("prism_monocle",
            PrismMonocleItem::new, new Item.Properties().stacksTo(1));

    public static final DeferredItem<DivingSuitArmorItem> DIVING_HELMET = registerItem("diving_helmet", props -> new DivingSuitArmorItem(ArmorType.HELMET, props), new Item.Properties());
    public static final DeferredItem<DivingSuitArmorItem> DIVING_CHESTPLATE = registerItem("diving_chestplate", props -> new DivingSuitArmorItem(ArmorType.CHESTPLATE, props), () -> new Item.Properties().component(NTDataComponents.OXYGEN, 0));
    public static final DeferredItem<DivingSuitArmorItem> DIVING_LEGGINGS = registerItem("diving_leggings", props -> new DivingSuitArmorItem(ArmorType.LEGGINGS, props), new Item.Properties());
    public static final DeferredItem<DivingSuitArmorItem> DIVING_BOOTS = registerItem("diving_boots", props -> new DivingSuitArmorItem(ArmorType.BOOTS, props), new Item.Properties());

    public static final DeferredItem<AquarineArmorItem> AQUARINE_HELMET = registerItem("aquarine_steel_helmet", props -> new AquarineArmorItem(ArmorType.HELMET, props), new Item.Properties());
    public static final DeferredItem<AquarineArmorItem> AQUARINE_CHESTPLATE = registerItem("aquarine_steel_chestplate", props -> new AquarineArmorItem(ArmorType.CHESTPLATE, props), new Item.Properties());
    public static final DeferredItem<AquarineArmorItem> AQUARINE_LEGGINGS = registerItem("aquarine_steel_leggings", props -> new AquarineArmorItem(ArmorType.LEGGINGS, props), new Item.Properties());
    public static final DeferredItem<AquarineArmorItem> AQUARINE_BOOTS = registerItem("aquarine_steel_boots", props -> new AquarineArmorItem(ArmorType.BOOTS, props), new Item.Properties());

    public static final DeferredItem<NeptunesTridentItem> NEPTUNES_TRIDENT = registerItem("neptunes_trident",
            NeptunesTridentItem::new, new Item.Properties()
                    .attributes(NeptunesTridentItem.createAttributes())
                    .component(DataComponents.TOOL, NeptunesTridentItem.createToolProperties()));

    public static final DeferredItem<AquarineSwordItem> AQUARINE_SWORD = registerItem("aquarine_steel_sword", AquarineSwordItem::new, new Item.Properties());
    public static final DeferredItem<AquarineAxeItem> AQUARINE_AXE = registerItem("aquarine_steel_axe", AquarineAxeItem::new, new Item.Properties());
    public static final DeferredItem<AquarineHoeItem> AQUARINE_HOE = registerItem("aquarine_steel_hoe", AquarineHoeItem::new, new Item.Properties());
    public static final DeferredItem<AquarinePickaxeItem> AQUARINE_PICKAXE = registerItem("aquarine_steel_pickaxe", AquarinePickaxeItem::new, new Item.Properties());
    public static final DeferredItem<AquarineShovelItem> AQUARINE_SHOVEL = registerItem("aquarine_steel_shovel", AquarineShovelItem::new, new Item.Properties());

    public static final DeferredItem<SubmarineItem> SUBMARINE = registerItem("submarine",
            SubmarineItem::new, Item.Properties::new);

    public static final DeferredItem<SubmarineModuleItem> SOLAR_MODULE = moduleItem(SubmarineModuleType.SOLAR);
    public static final DeferredItem<SubmarineModuleItem> BOOSTER_MODULE = moduleItem(SubmarineModuleType.BOOSTER);
    public static final DeferredItem<SubmarineModuleItem> STEALTH_MODULE = moduleItem(SubmarineModuleType.STEALTH);
    public static final DeferredItem<SubmarineModuleItem> ARMOR_MODULE = moduleItem(SubmarineModuleType.ARMOR);
    public static final DeferredItem<SubmarineModuleItem> SONAR_MODULE = moduleItem(SubmarineModuleType.SONAR);
    public static final DeferredItem<SubmarineModuleItem> SHIELD_MODULE = moduleItem(SubmarineModuleType.SHIELD);
    public static final DeferredItem<SubmarineModuleItem> IMPULSE_LASER_MODULE = moduleItem(SubmarineModuleType.IMPULSE_LASER);
    public static final DeferredItem<TeleportModuleItem> TELEPORT_MODULE = registerItem(
            SubmarineModuleType.TELEPORT.itemId(), TeleportModuleItem::new, new Item.Properties());

    public static final List<DeferredItem<? extends SubmarineModuleItem>> SUBMARINE_MODULES = List.of(
            SOLAR_MODULE, BOOSTER_MODULE, STEALTH_MODULE, ARMOR_MODULE,
            SONAR_MODULE, SHIELD_MODULE, IMPULSE_LASER_MODULE, TELEPORT_MODULE);

    public static final DeferredItem<AquarineWrenchItem> AQUARINE_WRENCH = registerItem("aquarine_steel_wrench",
            AquarineWrenchItem::new, new Item.Properties());
    public static final DeferredItem<Item> CROWBAR = registerItem("crowbar",
            Item::new, new Item.Properties().stacksTo(1));

    private static DeferredItem<SubmarineModuleItem> moduleItem(SubmarineModuleType type) {
        return registerItem(type.itemId(), properties -> new SubmarineModuleItem(type, properties), new Item.Properties());
    }

    public static <T extends Item> DeferredItem<T> bacteriaItem(DeferredItem<T> item) {
        BACTERIA_ITEMS.add(item);
        return item;
    }

    public static <T extends Item> DeferredItem<T> registerItem(String name, Function<Item.Properties, T> itemConstructor, Item.Properties properties) {
        return registerItem(name, itemConstructor, properties, true);
    }

    public static <T extends Item> DeferredItem<T> registerItem(String name, Function<Item.Properties, T> itemConstructor, Supplier<Item.Properties> properties) {
        return registerItem(name, itemConstructor, properties, true);
    }

    private static <T extends Item> DeferredItem<T> registerItemBucket(String name, Supplier<T> item) {
        return ITEMS.register(name, item);
    }

    public static <T extends Item> DeferredItem<T> registerItem(String name, Supplier<T> item) {
        DeferredItem<T> toReturn = ITEMS.register(name, item);
        CREATIVE_TAB_ITEMS.add(toReturn);
        return toReturn;
    }

    public static <T extends Item> DeferredItem<T> registerItem(String name, Function<Item.Properties, T> itemConstructor, Item.Properties properties, boolean addToTab) {
        DeferredItem<T> toReturn = ITEMS.registerItem(name, itemConstructor, () -> properties);
        if (addToTab) {
            CREATIVE_TAB_ITEMS.add(toReturn);
        }
        return toReturn;
    }

    public static <T extends Item> DeferredItem<T> registerItem(String name, Function<Item.Properties, T> itemConstructor, Supplier<Item.Properties> properties, boolean addToTab) {
        DeferredItem<T> toReturn = ITEMS.registerItem(name, itemConstructor, properties);
        if (addToTab) {
            CREATIVE_TAB_ITEMS.add(toReturn);
        }
        return toReturn;
    }

    static {
        if (ModList.get().isLoaded("modonomicon")) {
            NAUTEC_GUIDE = ModonomiconCompat.registerItem();
        } else {
            NAUTEC_GUIDE = null;
        }
    }
}
