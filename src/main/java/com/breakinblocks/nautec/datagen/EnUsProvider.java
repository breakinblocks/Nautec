package com.breakinblocks.nautec.datagen;

import com.klikli_dev.modonomicon.api.datagen.AbstractModonomiconLanguageProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconLanguageProvider;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.data.generated.BacteriaMaterials;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.utils.Utils;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import static com.breakinblocks.nautec.registries.NTBacterias.*;
import static com.breakinblocks.nautec.registries.NTItems.*;

public class EnUsProvider extends AbstractModonomiconLanguageProvider {
    public EnUsProvider(PackOutput output, ModonomiconLanguageProvider cacheProvider) {
        super(output, Nautec.MODID, "en_us", cacheProvider);
    }

    @Override
    protected void addTranslations() {
        curiosIdent("prism_monocle", "Monocle");
        curiosIdent("battery", "Battery");

        addItem(LUMINOUS_MEMBRANE, "Luminous Membrane");
        addItem(CHITIN_PLATE, "Chitin Plate");
        addItem(ABYSSAL_ORGAN, "Abyssal Organ");
        addItem(SILT_SKIPPER, "Silt Skipper");
        addItem(SILT_SKIPPER_BUCKET, "Bucket of Silt Skipper");
        addItem(SILT_SKIPPER_SPAWN_EGG, "Silt Skipper Spawn Egg");
        addItem(LANTERN_JELLY_SPAWN_EGG, "Lantern Jelly Spawn Egg");
        addItem(VENT_CRAWLER_SPAWN_EGG, "Vent Crawler Spawn Egg");
        addItem(ABYSSAL_MAW_SPAWN_EGG, "Abyssal Maw Spawn Egg");

        add("augment_type.nautec.abyssal_eyes", "Abyssal Eyes");
        add("augment_type.nautec.photophore_skin", "Photophore Skin");
        add("augment_type.nautec.vent_carapace", "Vent Carapace");

        addItem(WAVE_JET, "Wave Jet");
        add("nautec.wave_jet.needs_water", "The Wave Jet only bites under water");
        add("nautec.wave_jet.both_hands", "The Wave Jet takes both hands");
        add("nautec.wave_jet.spotlight", "Spotlight: ");
        add("nautec.wave_jet.spotlight_hint", "Toggle the spotlight with your Wave Jet key (F by default) while holding it");
        addItem(NAUTEC_FISHING_ROD, "Prismatic Fishing Rod");
        add("nautec.fishing_minigame.timing_bar", "Something is biting");
        add("nautec.fishing_minigame.rhythm", "It is fighting you");
        add("nautec.fishing_minigame.hold", "It is running with the line");
        add("nautec.fishing_minigame.prompt.timing_bar", "Strike when the marker hits the green");
        add("nautec.fishing_minigame.prompt.rhythm", "Strike on all three marks");
        add("nautec.fishing_minigame.prompt.hold", "Hold through the green, then let go");
        add("nautec.fishing_minigame.missed", "It slipped the hook");
        add("nautec.fishing_minigame.hooked", "Hooked it");

        addItem(SUBMARINE, "Sea Scout");
        add("entity.nautec.submarine", "Sea Scout");
        add("nautec.submarine.controls", "W/S throttle, mouse steers, Space rises, C dives");
        add("nautec.submarine.aim", "Hold use to look around without steering");
        add("nautec.submarine.oxygen", "Sealed and powered, it keeps its crew breathing");
        add("nautec.submarine.hull", "Hull integrity:");
        add("nautec.submarine.breached", "The hull is breached. Repair it on an anvil before launching");
        add("nautec.submarine.needs_water", "The Sea Scout needs open water to launch into");
        add("nautec.submarine.modules", "Sea Scout Modules");
        add("nautec.submarine.modules.installed", "Installed: %s");

        addItem(SOLAR_MODULE, "Solar Module");
        addItem(BOOSTER_MODULE, "Booster Module");
        addItem(STEALTH_MODULE, "Stealth Module");
        addItem(ARMOR_MODULE, "Armour Module");
        addItem(SONAR_MODULE, "Sonar Module");
        addItem(SHIELD_MODULE, "Shield Module");
        addItem(IMPULSE_LASER_MODULE, "Impulse Laser Module");
        addItem(TELEPORT_MODULE, "Teleport Module");

        add("nautec.submarine.ability.cooldown", "That module is still cycling");
        add("nautec.submarine.ability.no_power", "Not enough power for that module");
        add("nautec.submarine.ability.not_bound", "That teleport module has no anchor bound");
        add("nautec.submarine.ability.destination_blocked", "The anchor is blocked or dry");
        add("effect.nautec.stunned", "Stunned");

        add("nautec.gateway.title", "Gateway Address");
        add("nautec.gateway.apply", "Set");
        add("nautec.gateway.cancel", "Cancel");
        add("nautec.gateway.cost", "Costs");
        add("nautec.gateway.cost_entry", "%sx %s");
        add("nautec.gateway.no_change", "Pick a colour for each of the four fins");
        add("nautec.gateway.missing_dye", "You do not have the dye for that address");

        add("subtitles.nautec.submarine.engine", "Sea Scout thrusters churn");
        add("subtitles.nautec.submarine.ambient", "Sea Scout hull hums");
        add("subtitles.nautec.submarine.deploy", "Sea Scout canopy seals");
        add("subtitles.nautec.submarine.stow", "Sea Scout canopy opens");
        add("subtitles.nautec.submarine.sonar_ping", "Sonar pings");
        add("subtitles.nautec.submarine.boost", "Thrusters surge");
        add("subtitles.nautec.submarine.shield_pulse", "Shield discharges");
        add("subtitles.nautec.submarine.stealth", "Hull goes quiet");
        add("subtitles.nautec.submarine.laser_loop", "Impulse lasers burn");
        add("subtitles.nautec.submarine.teleport_charge", "Sea Scout folds the water");
        add("subtitles.nautec.submarine.teleport_whoosh", "Sea Scout jumps");
        add("subtitles.nautec.submarine.module_install", "Module seats home");
        add("subtitles.nautec.submarine.hull_damage", "Hull takes a hit");
        add("subtitles.nautec.submarine.low_power", "Power reserve warning");
        add("subtitles.nautec.gateway.ambient", "Gateway hums");
        add("subtitles.nautec.gateway.travel", "Gateway swallows");
        add("subtitles.nautec.gateway.recode", "Gateway address set");
        add("subtitles.nautec.gateway.unlinked", "Gateway finds no pair");
        add("subtitles.nautec.resonance.charge", "Resonance Chamber rings");
        add("subtitles.nautec.resonance.craft", "Resonance settles");
        add("subtitles.nautec.resonance.vent", "Resonance Chamber vents");
        add("subtitles.nautec.pressure_forge.work", "Pressure Forge works");
        add("subtitles.nautec.pressure_forge.complete", "Pressure Forge finishes");
        add("subtitles.nautec.dock.clamp", "Dock clamps hull");
        add("subtitles.nautec.dock.release", "Dock releases hull");
        add("subtitles.nautec.optics.hum", "Optics hum");
        add("subtitles.nautec.fuel_cell.burn", "Colony feeds the cell");
        add("subtitles.nautec.wave_jet.start", "Wave Jet spins up");
        add("subtitles.nautec.wave_jet.loop", "Wave Jet drives");
        add("subtitles.nautec.wave_jet.stop", "Wave Jet winds down");
        add("key.category.nautec.main", "Nautec");
        add("key.nautec.augment_screen", "Open the Augmentation Screen");
        add("key.nautec.throw_trident", "Throw Trident");
        add("key.nautec.leap", "Leap");
        add("key.nautec.throw_potion", "Throw Potion");
        add("key.nautec.throw_spreading_trident", "Throw Spreading Trident");
        add("key.nautec.activate_laser", "Activate Guardian Eye Augment Laser");
        add("key.nautec.submarine_descend", "Sea Scout Descend");
        add("key.nautec.submarine_ability", "Fire Sea Scout Module");
        add("key.nautec.wave_jet_light", "Toggle Wave Jet Spotlight");
        add("key.nautec.submarine_hud", "Reposition Sea Scout HUD");

        add("nautec.submarine.module.passive", "Passive, works from any slot");
        add("nautec.submarine.module.cost", "Draws %s power per use");
        add("nautec.submarine.module.cooldown", "%s second cooldown");
        add("nautec.submarine.module.solar", "Solar Module");
        add("nautec.submarine.module.solar.desc", "Trickles the hull's cells full again in open sunlit water");
        add("nautec.submarine.module.booster", "Booster Module");
        add("nautec.submarine.module.booster.desc", "Dumps the reserve into the thrusters for a short sprint");
        add("nautec.submarine.module.stealth", "Stealth Module");
        add("nautec.submarine.module.stealth.desc", "Masks the hull so the deep stops noticing it, at the cost of speed");
        add("nautec.submarine.module.armor", "Armour Module");
        add("nautec.submarine.module.armor.desc", "Plates the hull to shrug off heavier hits");
        add("nautec.submarine.module.sonar", "Sonar Module");
        add("nautec.submarine.module.sonar.desc", "Pings the surrounding rock and lights up ore and anything hunting you");
        add("nautec.submarine.module.shield", "Shield Module");
        add("nautec.submarine.module.shield.desc", "Bleeds power to soak damage, and discharges to throw off boarders");
        add("nautec.submarine.module.impulse_laser", "Impulse Laser Module");
        add("nautec.submarine.module.impulse_laser.desc", "Twin prismatic beams that cut whatever the nose is pointed at");
        add("nautec.submarine.module.teleport", "Teleport Module");
        add("nautec.submarine.module.teleport.desc", "Folds the water around the hull and drops it at a bound anchor");
        add("nautec.submarine.module.teleport.unbound", "No anchor bound");
        add("nautec.submarine.module.teleport.bound", "Anchor bound to %s");
        add("nautec.submarine.module.teleport.destination", "Anchor: %s in %s");
        add("nautec.submarine.module.teleport.needs_water", "The anchor has to be set in water");

        add("entity.nautec.silt_skipper", "Silt Skipper");
        add("entity.nautec.lantern_jelly", "Lantern Jelly");
        add("entity.nautec.vent_crawler", "Vent Crawler");
        add("entity.nautec.abyssal_maw", "Abyssal Maw");

        addBlock("budding_prismarine", "Budding Prismarine");
        addBlock("small_prismarine_bud", "Small Prismarine Bud");
        addBlock("medium_prismarine_bud", "Medium Prismarine Bud");
        addBlock("large_prismarine_bud", "Large Prismarine Bud");
        addBlock("prismarine_cluster", "Prismarine Cluster");
        addBlock("deep_kelp", "Deep Kelp");
        addBlock("deep_kelp_plant", "Deep Kelp Plant");
        addBlock("luminescent_algae", "Luminescent Algae");
        addBlock("prismarine_frond", "Prismarine Frond");
        addBlock("vent_tubeworm", "Vent Tubeworm");
        addBlock("abyssal_coral", "Abyssal Coral");
        addBlock("glow_polyp", "Glow Polyp");

        add("biome.nautec.abyssal_trench", "Abyssal Trench");
        add("biome.nautec.bioluminescent_grove", "Bioluminescent Grove");
        add("biome.nautec.hydrothermal_vents", "Hydrothermal Vents");
        add("biome.nautec.prismarine_reef", "Prismarine Reef");

        add("nautec.creative_tab.main", "NauTec");
        add("nautec.creative_tab.bacteria", "NauTec Bacteria");

        addFluidType(NTFluids.SALT_WATER.getFluidType(), "Salt Water");
        addFluidType(NTFluids.EAS.getFluidType(), "Electrolyte Algae Serum");
        addFluidType(NTFluids.ETCHING_ACID.getFluidType(), "Etching Acid");
        addFluidType(NTFluids.OIL.getFluidType(), "Oil");

        addItem(PRISM_MONOCLE, "Prism Monocle");
        addItem(AQUARINE_STEEL_INGOT, "Aquarine Steel Ingot");
        addItem(ATLANTIC_GOLD_INGOT, "Atlantic Gold Ingot");
        addItem(ATLANTIC_GOLD_NUGGET, "Atlantic Gold Nugget");
        addItem(NTFluids.SALT_WATER.getDeferredBucket(), "Salt Water Bucket");
        addItem(NTFluids.EAS.getDeferredBucket(), "Electrolyte Algae Serum (EAS) Bucket");
        addItem(NTFluids.OIL.getDeferredBucket(), "Oil Bucket");
        addItem(GLASS_VIAL, "Glass Vial");
        addItem(ELECTROLYTE_ALGAE_SERUM_VIAL, "Electrolyte Algae Serum (EAS) Vial");
        addItem(CROWBAR, "Crowbar");
        addItem(RUSTY_GEAR, "Rusty Gear");
        addItem(GEAR, "Gear");
        addItem(ANCIENT_VALVE, "Ancient Valve");
        addItem(PETRI_DISH, "Petri Dish");

        addItem("drowned_lungs", "Drowned Lungs");
        addItem("diving_helmet", "Diving Helmet");
        addItem("diving_chestplate", "Diving Chestplate");
        addItem("diving_leggings", "Diving Leggings");
        addItem("diving_boots", "Diving Boots");
        addItem("aquarine_steel_wrench", "Aquarine Steel Wrench");
        addItem("etching_acid_bucket", "Etching Acid Bucket");
        addItem("aquarine_steel_sword", "Aquarine Steel Sword");
        addItem("aquarine_steel_pickaxe", "Aquarine Steel Pickaxe");
        addItem("aquarine_steel_axe", "Aquarine Steel Axe");
        addItem("aquarine_steel_shovel", "Aquarine Steel Shovel");
        addItem("aquarine_steel_hoe", "Aquarine Steel Hoe");
        addItem("neptunes_trident", "Neptune's Trident");
        addItem("aquarine_steel_helmet", "Aquarine Steel Helmet");
        addItem("aquarine_steel_chestplate", "Aquarine Steel Chestplate");
        addItem("aquarine_steel_leggings", "Aquarine Steel Leggings");
        addItem("aquarine_steel_boots", "Aquarine Steel Boots");
        addItem("dolphin_fin", "Dolphin Fin");
        addItem("broken_whisk", "Broken Whisk");
        addItem("whisk", "Whisk");
        addItem("prismatic_battery", "Prismatic Battery");
        addItem("air_bottle", "Pressurized Air Bottle");
        addItem("aquarine_steel_compound", "Aquarine Steel Compound");
        addItem("prismarine_crystal_shard", "Prismarine Crystal Shard");
        addItem("claw_robot_arm", "Claw Robot Arm");
        addItem("syringe_robot_arm", "Syringe Robot Arm");
        addItem(HYDRAULIC_LEG, "Hydraulic Leg");
        addItem(SERVO_KNEE, "Servo Knee");
        addItem(SHOCK_ABSORBER, "Shock Absorber");
        addItem(TENDON_WEAVE, "Tendon Weave");
        addItem(MAGNETIC_COIL_ARM, "Magnetic Coil Arm");
        addItem(ENDER_COIL_ARM, "Ender Coil Arm");
        addItem(HYDRO_DRILL_ARM, "Hydro Drill Arm");
        addItem(TRIDENT_LAUNCHER_ARM, "Trident Launcher Arm");
        addItem(VOLLEY_TRIDENT_ARM, "Volley Trident Arm");
        addItem(BUOYANCY_TANK, "Buoyancy Tank");
        addItem(AUXILIARY_VENTRICLE, "Auxiliary Ventricle");
        addItem("deepslate_rod", "Deepslate Rod");
        addItem("brown_polymer", "Brown Polymer");
        addItem("cast_iron_ingot", "Cast Iron Ingot");
        addItem("cast_iron_rod", "Cast Iron Rod");
        addItem("cast_iron_nugget", "Cast Iron Nugget");
        addItem("salt", "Salt");
        addItem("damaged_aquatic_chip", "Damaged Aquatic Chip");
        addItem(LASER_CHANNELING_COIL, "Laser Channeling Coil");
        addItem(BURNT_COIL, "Burnt Coil");
        addItem(ELDRITCH_HEART, "Eldritch Heart");
        addItem(GUARDIAN_EYE, "Guardian Eye");
        addItem(VALVE, "Valve");
        addItem(CAST_IRON_COMPOUND, "Cast Iron Compound");
        addItem(GRAFTING_TOOL, "Grafting Tool");
        addItem(PRISMARINE_LENS, "Prismarine Lens");
        addItem(AQUATIC_CHIP, "Aquatic Chip");
        
        addItem("nautec_guide", "Nautec Guide");
        add("nautec_guide.desc.0","Nautec's Guide");

        addBlock("rusty_crate", "Rusty Crate");
        addBlock("polished_prismarine", "Polished Prismarine");
        addBlock("mixer", "Mixer");
        addBlock("long_distance_laser", "Long Distance Laser");
        addBlock("laser_junction", "Laser Junction");
        addBlock("prismarine_crystal", "Prismarine Crystal");
        addBlock("decorative_prismarine_crystal", "Decorative Prismarine Crystal");
        addBlock("decorative_prismarine_crystal_part", "Decorative Prismarine Crystal");
        addBlock("deep_sea_drain_wall", "Deep Sea Drain Wall");
        addBlock("augmentation_station", "Augmentation Station");
        addBlock("aquarine_steel_block", "Aquarine Steel Block");
        addBlock("aquatic_catalyst", "Aquatic Catalyst");
        addBlock("dark_prismarine_pillar", "Dark Prismarine Pillar");
        addBlock("chiseled_dark_prismarine", "Chiseled Dark Prismarine");
        addBlock("crate", "Crate");
        addBlock("prismarine_laser_relay", "Prismarine Laser Relay");
        addBlock("deep_sea_drain", "Deep Sea Drain");
        addBlock("augmentation_station_part", "Augmentation Station");
        addBlock("prismarine_crystal_part", "Prismarine Crystal");
        addBlock("anchor", "Anchor");
        addBlock("oil_barrel", "Oil Barrel");
        addBlock(NTBlocks.AUGMENTATION_STATION_EXTENSION, "Augmentation Station Extension");
        addBlock(NTBlocks.CHARGER, "Charger");
        addBlock(NTBlocks.SUBMARINE_DOCK, "Sea Scout Dock");
        addBlock(NTBlocks.PRESSURE_FORGE, "Abyssal Pressure Forge");
        addItem(FLAWLESS_PRISMARINE_CRYSTAL, "Flawless Prismarine Crystal");
        addItem(DEEP_STEEL_PLATING, "Deep Steel Plating");
        addBlock(NTBlocks.GATEWAY, "Gateway");
        addBlock(NTBlocks.RESONANCE_CHAMBER, "Resonance Chamber");
        addItem(RESONANT_SHARD, "Resonant Shard");
        addBlock(NTBlocks.PRISMATIC_MIRROR, "Prismatic Mirror");
        addBlock(NTBlocks.BEAM_SPLITTER, "Beam Splitter");
        addBlock(NTBlocks.FOCUSING_LENS, "Focusing Lens");
        addBlock(NTBlocks.PRISMARINE_SAND, "Prismarine Sand");
        addBlock(NTBlocks.CREATIVE_POWER_SOURCE,"Creative Power Source");
        addBlock(NTBlocks.CREATIVE_ENERGY_SOURCE, "Creative Energy Source");
        addBlock(NTBlocks.ENERGY_CONVERTER, "Energy Converter");
        addBlock(NTBlocks.BACTERIAL_ANALYZER_TOP, "Bacterial Analyzer");
        addBlock(NTBlocks.BIO_REACTOR_PART, "Bio Reactor");
        addBlock(NTBlocks.DRAIN_PART, "Deep Sea Drain");
        addBlock(NTBlocks.MUTATOR, "Mutator");
        addBlock(NTBlocks.INCUBATOR, "Incubator");
        addBlock(NTBlocks.BIO_REACTOR, "Bio Reactor");
        addBlock(NTBlocks.BACTERIAL_ANALYZER, "Bacterial Analyzer");
        addBlock(NTBlocks.BACTERIAL_FUEL_CELL, "Bacterial Fuel Cell");
        addBlock(NTBlocks.FISHING_STATION, "Fishing Station");
        addBlock(NTBlocks.BACTERIAL_CONTAINMENT_SHIELD, "Bacteria Containment Shield");
        addBlock(NTBlocks.CAST_IRON_BLOCK, "Cast Iron Block");

        add("multiblock.info.failed_to_construct", "Missing or invalid block");
        add("multiblock.info.actual_block", "Block: %s");
        add("multiblock.info.expected_block", "Expected: %s");
        add("multiblock.info.block_pos", "Coordinates: %d, %d, %d");

        add("augment_slot.nautec.head", "Head");
        add("augment_slot.nautec.eyes", "Eyes");
        add("augment_slot.nautec.body", "Body");
        add("augment_slot.nautec.lung", "Lungs");
        add("augment_slot.nautec.left_leg", "Left Leg");
        add("augment_slot.nautec.right_leg", "Right Leg");
        add("augment_slot.nautec.left_arm", "Left Arm");
        add("augment_slot.nautec.right_arm", "Right Arm");
        add("augment_slot.nautec.heart", "Heart");

        add("augment_type.nautec.drowned_lung", "Drowned Lung");
        add("augment_type.nautec.guardian_eye", "Guardian Eye");
        add("augment_type.nautec.dolphin_fin", "Dolphin Fin");
        add("augment_type.nautec.eldritch_heart", "Eldritch Heart");
        add("augment_type.nautec.throw_random_potion", "Random Potion Throwing");
        add("augment_type.nautec.throw_bouncing_trident", "Bouncing Trident");
        add("augment_type.nautec.spreading_trident", "Spreading Trident");
        add("augment_type.nautec.leap", "Leap");
        add("augment_type.nautec.prevent_fall_damage", "Prevent Fall Damage");
        add("augment_type.nautec.step_up", "Step Up");
        add("augment_type.nautec.underwater_mining_speed", "Underwater Mining Speed");
        add("augment_type.nautec.bonus_hearts", "Bonus Hearts");
        add("augment_type.nautec.creative_flight", "Creative Flight");
        add("augment_type.nautec.walking_speed", "Walking Speed");
        add("augment_type.nautec.magnet", "Magnet");
        add("augment_type.nautec.ender_magnet", "Ender Magnet");

        addBlock("oil_fluid", "Oil");
        addBlock("saltwater_fluid", "Salt Water");
        addBlock("eas_fluid", "Electrolyte Algae Serum");
        addBlock("etching_acid_fluid", "Etching Acid");

        add("nautec.air_bottle.fill","Right click a glass bottle on a bubble column to fill with pressurized air");
        add("nautec.air_bottle.craft_msg","Either Craft with Chestplate or drink while wearing chestplate to increase oxygen level");
        add("nautec.bacteria.name", "Name: ");
        add("nautec.bacteria.size", "Size: %s");
        add("nautec.bacteria.stats", "Stats: ");
        add("nautec.bacteria.unknown", "???");
        add("nautec.bacteria.vitality", "Vitality: ");
        add("nautec.bacteria.senescent", "Senescent");
        add("nautec.bacteria.percent", "%s%%");
        add("nautec.bacteria.resource", "  Resource: ");
        add("nautec.bacteria.hint.shift", "Shift");
        add("nautec.bacteria.hint.control", "Control");
        add("nautec.bacteria.hint.and", " and ");
        add("nautec.bacteria.stat.growth_rate", "Growth Rate");
        add("nautec.bacteria.stat.mutation_resistance", "Mutation Resistance");
        add("nautec.bacteria.stat.production_rate", "Production Rate");
        add("nautec.bacteria.stat.lifespan", "Lifespan");

        add("nautec.monocle.power", "Power: %s");
        add("nautec.monocle.purity", "Purity: %s");
        add("nautec.monocle.duration", "Duration: %s");
        add("nautec.monocle.no_colony", "No colony");
        add("nautec.monocle.output", "Output: %s AP/t");
        add("nautec.monocle.fuel", "Fuel: %s");
        add("nautec.monocle.address", "Address: ");
        add("nautec.monocle.fluid_stored", "Fluid Stored: %s");
        add("nautec.monocle.not_pressurised", "Not under pressure: needs y %s or lower with %s blocks of water above");
        add("nautec.monocle.pressurised", "Under pressure");
        add("nautec.monocle.acid", "Acid: %s mb");
        add("nautec.monocle.cracked", "Cracked, cooling down");
        add("nautec.monocle.charge", "Charge: %s%%");
        add("nautec.monocle.ceiling", "Ceiling: %s");
        add("nautec.monocle.dock.occupied", "Sea Scout docked");
        add("nautec.monocle.dock.empty", "No Sea Scout on the pad");

        add("nautec.jade.status.active", "Status: Active");
        add("nautec.jade.status.inactive", "Status: Inactive");
        add("nautec.jade.processing", "Processing: %sx %s");
        add("nautec.jade.remaining_duration", "Remaining Duration: %s ticks");
        add("nautec.jade.transferring", "Transferring: %s AP/T");
        add("nautec.jade.locked", "Locked");
        add("nautec.jade.inputs", "Inputs: %s");
        add("nautec.jade.outputs", "Outputs: %s");
        add("nautec.jade.fluid_input", "Fluid Input: %s - %s mB");
        add("nautec.jade.fluid_output", "Fluid Output: %s - %s mB");
        add("nautec.jade.mixing_progress", "Mixing Progress: %s / %s ticks");
        add("nautec.jade.energy", "Energy: %s AP");

        add("nautec.jei.power_per_tick", "Power per tick: %s AP/t");
        add("nautec.jei.total_power", "Total Power amount: %s AP");
        add("nautec.jei.duration_ticks", "Duration: %s");
        add("nautec.jei.purity", "Purity: %s");
        add("nautec.jei.purity_value", "%s purity");
        add("nautec.jei.seconds", "%ss");
        add("nautec.jei.only_in", "Only In: %s");
        add("nautec.jei.growth", "Growth: %s");
        add("nautec.jei.production", "Production: %s - %s");
        add("nautec.jei.info.prismarine_crystal_shard", "Prismarine Crystal Shards are pristine crystals, capable of channeling power like no other material. They can be obtained by breaking a prismarine crystal using an Aquarine Steel Pickaxe with its ability enabled.");
        add("nautec.jei.info.machine_parts", "These ancient machine components can be found in chests and are dropped by underwater mobs");

        add("nautec.augmentation_station.apply", "Apply");
        add("nautec.augment.empty_slot", "No Augment in slot");
        add("nautec.augment_viewer.title", "Augments");
        add("nautec.submarine.hud_position.title", "Sea Scout HUD Position");
        add("nautec.diving_suit.oxygen", "Oxygen: %s minutes %s seconds");
        add("nautec.diving_suit.refill", "Can be filled up using Bottles of pressurized air");
        add("nautec.tool.no_power", "Not enough power");
        add("nautec.tool.ability_enabled", "Ability Enabled");
        add("nautec.tool.ability_disabled", "Ability Disabled");
        add("nautec.network.action_failed", "Action failed: %s");

        add("nautec.tooltip.liquid.amount", "%s mB");
        add("nautec.tooltip.liquid.amount_with_capacity", "%s / %s mB");
        add("nautec.edible","Edible");
        add("nautec.armor.ability.desc", "Ability: Increases protection when powered");
        add("nautec.armor.power", "Power: ");
        add("nautec.armor.status", "Status: ");
        add("nautec.armor.enabled", "Enabled");
        add("nautec.armor.disabled", "Shift + Right Click to Enable");
        add("nautec.helm.desc", "Allows you to see better underwater.");
        add("nautec.tool.axe.ability", "Ability: Chop Down Entire Trees");
        add("nautec.tool.hoe.ability", "Ability: Till 3x3 Farmland");
        add("nautec.tool.pickaxe.ability", "Ability: Mine in a 3x3 Area");
        add("nautec.tool.shovel.ability", "Ability: Mine in a 3x3 Area");
        add("nautec.tool.sword.ability", "Ability: Deal 70% more damage and spawn lightnings at targets");
        add("nautec.tool.infuse-me", "Infuse in Algae Serum to unlock Abilities");
        add("nautec.tool.status", "Status: ");
        add("nautec.tool.power", "Power: ");
        add("nautec.tool.enabled", "Enabled");
        add("nautec.tool.disabled", "Shift + Right Click to Enable");

        add("block.nautec.brown_polymer_block", "Brown Polymer Block");

        add("config.jade.plugin_nautec.mixer", "Mixer");
        add("config.jade.plugin_nautec.aquatic_catalyst", "Aquatic Catalyst");
        add("config.jade.plugin_nautec.laser_junction", "Laser Junction");

        add("nautec.jei.category.aquatic_catalyst_channeling", "Aquatic Catalyst Channeling");
        add("nautec.jei.category.augmentation_effects", "Augmentation Effects");
        add("nautec.jei.category.bacteria_grafting", "Bacteria Grafting");
        add("nautec.jei.category.bacteria_incubation", "Bacteria Incubation");
        add("nautec.jei.category.bacteria_mutations", "Bacteria Mutations");
        add("nautec.jei.category.bio_reactor", "Bio Reactor");
        add("nautec.jei.category.item_etching", "Item Etching");
        add("nautec.jei.category.item_transformation", "Item Transformation");
        add("nautec.jei.category.mixing", "Mixing");

        for (ResourceKey<?> key : bacterias()) {
            addDirectBacteria(key);
        }

        for (String name : BacteriaMaterials.SHIPPED_NAMES) {
            add("bacteria." + Nautec.MODID + "." + name, Utils.prettify(name));
        }
    }

    private void addFluidType(Supplier<? extends FluidType> fluidType, String val) {
        add(Utils.registryTranslation(NeoForgeRegistries.FLUID_TYPES, fluidType.get()).getString(), val);
    }

    private void curiosIdent(String key, String val) {
        add("curios.identifier." + key, val);
    }

    private void addItem(String key, String val) {
        add("item.nautec." + key, val);
    }

    private void addBacteria(ResourceKey<?> key, String val) {
        add(key.registry().getPath() + "." + key.identifier().getNamespace() + "." + key.identifier().getPath(), val);
    }

    private void addDirectBacteria(ResourceKey<?> key) {
        add(key.registry().getPath() + "." + key.identifier().getNamespace() + "." + key.identifier().getPath(),
                Utils.prettify(key.identifier().getPath()));
    }

    private void addBlock(String key, String val) {
        add("block.nautec." + key, val);
    }
}
