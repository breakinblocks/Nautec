package com.breakinblocks.nautec;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = Nautec.MODID)
public final class NTConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder WORLDGEN_BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue KELP_HEIGHT = BUILDER
            .comment("The height of kelp to be able to grow.")
            .defineInRange("kelpHeight", 40, 25, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue MIXER_POWER_REQUIREMENT = BUILDER
            .comment("The amount of power required by the mixer each tick.")
            .defineInRange("mixerPowerRequirement", 10, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue AUGMENTATION_STATION_POWER_REQUIREMENT = BUILDER
            .comment("The amount of power required by the Augmentation Station each tick.")
            .defineInRange("augmentationPowerRequirement", 25, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue DRAIN_POWER_REQUIREMENT = BUILDER
            .comment("The amount of power required by the Deep Sea Drain each tick.")
            .defineInRange("drainPowerRequirement", 20, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue REGULAR_LASER_DISTANCE = BUILDER
            .comment("The distance of normals lasers.")
            .defineInRange("laserDistance", 16, 0, 128);
    private static final ModConfigSpec.IntValue LONG_DISTANCE_LASER_DISTANCE = BUILDER
            .comment("The distance of Long Distance Laser lasers.")
            .defineInRange("longDistanceLaserDistance", 64, 0, 128);

    private static final ModConfigSpec.IntValue MIXER_INPUT_CAPACITY = BUILDER
            .comment("The capacity of the Mixers Input Tank")
            .defineInRange("mixerInputCapacity", 1_000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue MIXER_OUTPUT_CAPACITY = BUILDER
            .comment("The capacity of the Mixers Output Tank")
            .defineInRange("mixerOutputCapacity", 1_000, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue DRAIN_SALT_WATER_AMOUNT = BUILDER
            .comment("The amount of salt water collected by the Deep Sea Drain each second (mb)")
            .defineInRange("drainSaltWaterAmount", 500, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue DRAIN_CAPACITY = BUILDER
            .comment("The fluid capacity of the Deep Sea Drain")
            .defineInRange("drainCapacity", 128_000, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.BooleanValue SPAWN_BOOK_IN_INVENTORY = BUILDER
            .comment("Determines whether to give the player a book when joining a new world")
            .define("spawnBookInInventory", true);
    private static final ModConfigSpec.BooleanValue COLLECT_SALT_WATER = BUILDER
            .comment("Determines whether the player should be able to collect salt water when picking up water in an ocean biome")
            .define("collectSaltWater", true);
    private static final ModConfigSpec.BooleanValue COLLECT_AIR_WITH_BOTTLE = BUILDER
            .comment("Determines whether the player should be able to collect pressurized air bottles by right-clicking on a bubble column")
            .define("collectAirWithBottle", true);

    private static final ModConfigSpec.IntValue GUARDIAN_AUGMENT_DAMAGE = BUILDER
            .comment("The amount of damage the guardian augments laser deals")
            .defineInRange("guardianAugmentDamage", 3, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.BooleanValue ALLOW_AUGMENT_RENDERING = BUILDER
            .comment("Set to false to disable the rendering of augments, this can improve performance")
            .define("allowAugmentRendering", true);

    private static final ModConfigSpec.IntValue FISHER_LASER_LEVEL = BUILDER
            .comment("The amount laser power required to have the fisher work")
            .defineInRange("fisherLaserLevel", 1, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue FISHER_DURATION = BUILDER
            .comment("The amount of ticks the fisher takes to make a new item")
            .defineInRange("fisherRunDuration", 40, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue FISHER_RADIUS = BUILDER
            .comment("The radius on the x and z plane the fisher checks for the water")
            .defineInRange("fisherRadius", 2, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue FISHER_DEPTH = BUILDER
            .comment("The y depth the fisher checks for water")
            .defineInRange("fisherDepth", 2, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue BACTERIA_GROWTH_RATE_CAP = BUILDER
            .comment("The maximum rate at which bacteria can grow")
            .defineInRange("bacteriaGrowthRateCap", 5, 0, Float.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue BACTERIA_PRODUCTION_RATE_CAP = BUILDER
            .comment("The maximum rate at which bacteria can produce")
            .defineInRange("bacteriaProductionRateCap", 2, 0, Float.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue BACTERIA_MUTATION_RESISTANCE_CAP = BUILDER
            .comment("The maximum rate at which bacteria can resist mutation")
            .defineInRange("bacteriaMutationResistanceCap", 1, 0, Float.MAX_VALUE);

    private static final ModConfigSpec.LongValue BACTERIA_COLONY_SIZE_CAP = BUILDER
            .comment("The maximum size a bacteria colony can grow to")
            .defineInRange("bacteriaColonySizeCap", 40_000, 0, Long.MAX_VALUE);

    private static final ModConfigSpec.IntValue BACTERIA_LIFESPAN_CAP = BUILDER
            .comment("The maximum lifespan of a bacteria colony")
            .defineInRange("bacteriaLifespanCap", 24000, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue BACTERIA_ANALYZER_CRAFTING_SPEED = BUILDER
            .comment("The amount of ticks it takes for the Bacterial Analyzer to analyze a Petri Dish")
            .defineInRange("bacteriaAnalyzerCraftingSpeed", 60, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue BACTERIA_ANALYZER_POWER_USAGE = BUILDER
            .comment("The amount of power used by the Bacterial Analyzer each tick")
            .defineInRange("bacteriaAnalyzerPowerUsage", 5, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue MUTATOR_CRAFTING_SPEED = BUILDER
            .comment("The amount of ticks it takes for the Mutator to attempt a mutation")
            .defineInRange("mutatorCraftingSpeed", 240, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue MUTATOR_POWER_USAGE = BUILDER
            .comment("The amount of power used by the Mutator each tick")
            .defineInRange("mutatorPowerUsage", 10, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue MUTATOR_FAILURE_SHRINK = BUILDER
            .comment("The fraction of a colony lost when a mutation attempt fails, before mutation resistance reduces it")
            .defineInRange("mutatorFailureShrink", 0.25, 0, 1);

    private static final ModConfigSpec.IntValue INCUBATOR_CRAFTING_SPEED = BUILDER
            .comment("The amount of ticks it takes for the Incubator to complete one growth cycle")
            .defineInRange("incubatorCraftingSpeed", 100, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue INCUBATOR_POWER_USAGE = BUILDER
            .comment("The amount of power used by the Incubator each tick")
            .defineInRange("incubatorPowerUsage", 20, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue BIO_REACTOR_BASE_SPEED = BUILDER
            .comment("The base amount of progress a Bio Reactor colony makes each tick, before production rate and colony size scale it")
            .defineInRange("bioReactorBaseSpeed", 5.6, 0, 1000);

    private static final ModConfigSpec.IntValue BIO_REACTOR_POWER_BASE = BUILDER
            .comment("The amount of power the Bio Reactor requires before any colonies are counted")
            .defineInRange("bioReactorPowerBase", 25, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue BIO_REACTOR_POWER_PER_COLONY = BUILDER
            .comment("The extra amount of power the Bio Reactor requires for each colony it holds")
            .defineInRange("bioReactorPowerPerColony", 25, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue BIO_REACTOR_DECAY_FRACTION = BUILDER
            .comment("The fraction of a colony that dies off each production cycle once it has outlived its lifespan")
            .defineInRange("bioReactorDecayFraction", 0.10, 0, 1);

    private static final ModConfigSpec.IntValue FUEL_CELL_POWER_BASE = BUILDER
            .comment("The base amount of power a Bacterial Fuel Cell emits each tick, before production rate scales it")
            .defineInRange("fuelCellPowerBase", 24, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue FUEL_CELL_BURN_RATE = BUILDER
            .comment("The base amount of colony consumed by a Bacterial Fuel Cell each tick, before production rate scales it")
            .defineInRange("fuelCellBurnRate", 0.5, 0, 1000);

    private static final ModConfigSpec.DoubleValue FUEL_CELL_MAX_PURITY = BUILDER
            .comment("The purity a Bacterial Fuel Cell emits at when its colony is at the mutation resistance cap")
            .defineInRange("fuelCellMaxPurity", 2.5, 0, 10);

    private static final ModConfigSpec.DoubleValue MIRROR_PURITY_FACTOR = BUILDER
            .comment("The fraction of incoming purity a Prismatic Mirror passes on when it turns a beam")
            .defineInRange("mirrorPurityFactor", 0.9, 0, 1);

    private static final ModConfigSpec.DoubleValue SPLITTER_PURITY_FACTOR = BUILDER
            .comment("The fraction of incoming purity each branch of a Beam Splitter carries")
            .defineInRange("splitterPurityFactor", 0.8, 0, 1);

    private static final ModConfigSpec.DoubleValue LENS_PURITY_BONUS = BUILDER
            .comment("The purity a Focusing Lens adds to a beam passing straight through it")
            .defineInRange("lensPurityBonus", 0.5, 0, 10);

    private static final ModConfigSpec.DoubleValue RESONANCE_BASE_CEILING = BUILDER
            .comment("The charge a Resonance Chamber can hold at zero purity. Purity raises this ceiling")
            .defineInRange("resonanceBaseCeiling", 2000.0, 1, Double.MAX_VALUE);

    private static final ModConfigSpec.IntValue RESONANCE_POWER_USAGE = BUILDER
            .comment("The amount of power a Resonance Chamber requires before it starts building charge")
            .defineInRange("resonancePowerUsage", 20, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue RESONANCE_CRITICAL_LOW = BUILDER
            .comment("The fraction of the stability ceiling at which a Resonance Chamber becomes critical and can craft")
            .defineInRange("resonanceCriticalLow", 0.9, 0, 1);

    private static final ModConfigSpec.DoubleValue RESONANCE_CRITICAL_HIGH = BUILDER
            .comment("The fraction of the stability ceiling above which a Resonance Chamber vents instead of crafting")
            .defineInRange("resonanceCriticalHigh", 1.1, 1, 10);

    private static final ModConfigSpec.IntValue RESONANCE_VENT_COOLDOWN = BUILDER
            .comment("The amount of ticks a Resonance Chamber stays cracked and inert after venting")
            .defineInRange("resonanceVentCooldown", 200, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue RESONANCE_VENT_RADIUS = BUILDER
            .comment("The radius in blocks a Resonance Chamber vent reaches")
            .defineInRange("resonanceVentRadius", 4.0, 0, 32);

    private static final ModConfigSpec.DoubleValue RESONANCE_VENT_DAMAGE = BUILDER
            .comment("The damage a Resonance Chamber vent deals to everything in range")
            .defineInRange("resonanceVentDamage", 8.0, 0, 1000);

    private static final ModConfigSpec.IntValue PRESSURE_FORGE_DEPTH = BUILDER
            .comment("The Y level at or below which an Abyssal Pressure Forge will run. Recipes may demand deeper still")
            .defineInRange("pressureForgeDepth", 0, -64, 320);

    private static final ModConfigSpec.IntValue PRESSURE_FORGE_WATER_COLUMN = BUILDER
            .comment("How many blocks of water must sit directly above an Abyssal Pressure Forge for it to run")
            .defineInRange("pressureForgeWaterColumn", 8, 0, 320);

    private static final ModConfigSpec.IntValue PRESSURE_FORGE_POWER_USAGE = BUILDER
            .comment("The amount of power an Abyssal Pressure Forge requires each tick")
            .defineInRange("pressureForgePowerUsage", 40, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue PRESSURE_FORGE_ACID_USAGE = BUILDER
            .comment("The amount of Etching Acid in mb an Abyssal Pressure Forge consumes per craft")
            .defineInRange("pressureForgeAcidUsage", 250, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue PRESSURE_FORGE_CAPACITY = BUILDER
            .comment("The Etching Acid capacity of an Abyssal Pressure Forge")
            .defineInRange("pressureForgeCapacity", 4_000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue WAVE_JET_THRUST = BUILDER
            .comment("Thrust the Wave Jet adds each tick while held under water")
            .defineInRange("waveJetThrust", 0.035, 0.001, 1.0);

    private static final ModConfigSpec.IntValue WAVE_JET_POWER_USAGE = BUILDER
            .comment("The amount of power the Wave Jet uses each tick while running")
            .defineInRange("waveJetPowerUsage", 2, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue WAVE_JET_LIGHT_RANGE = BUILDER
            .comment("How far ahead the Wave Jet spotlight throws its pool of light, in blocks")
            .defineInRange("waveJetLightRange", 12, 1, 32);

    private static final ModConfigSpec.IntValue WAVE_JET_LIGHT_LEVEL = BUILDER
            .comment("Light level the Wave Jet spotlight casts where it lands, 0 to disable world lighting and leave only the visible cone")
            .defineInRange("waveJetLightLevel", 14, 0, 15);

    private static final ModConfigSpec.IntValue WAVE_JET_LIGHT_POWER_USAGE = BUILDER
            .comment("The amount of power the Wave Jet spotlight uses each tick while lit")
            .defineInRange("waveJetLightPowerUsage", 1, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue RIFLE_POWER_CAPACITY = BUILDER
            .comment("How much power an Atlantean Rifle holds")
            .defineInRange("riflePowerCapacity", 1_000_000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue RIFLE_MAX_INPUT = BUILDER
            .comment("How much power an Atlantean Rifle accepts per tick while charging")
            .defineInRange("rifleMaxInput", 20_000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue RIFLE_CHARGE_TICKS = BUILDER
            .comment("Ticks the Atlantean Rifle spins up for before the beam appears")
            .defineInRange("rifleChargeTicks", 10, 0, 200);

    private static final ModConfigSpec.IntValue RIFLE_RAMP_TICKS = BUILDER
            .comment("Ticks of continuous fire it takes the Atlantean Rifle to reach full damage and full power draw")
            .defineInRange("rifleRampTicks", 200, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue RIFLE_BASE_DAMAGE = BUILDER
            .comment("Damage the Atlantean Rifle beam deals every two ticks when it first fires")
            .defineInRange("rifleBaseDamage", 4.0, 0.0, 1024.0);

    private static final ModConfigSpec.DoubleValue RIFLE_MAX_DAMAGE = BUILDER
            .comment("Damage the Atlantean Rifle beam deals every two ticks once fully ramped")
            .defineInRange("rifleMaxDamage", 20.0, 0.0, 1024.0);

    private static final ModConfigSpec.IntValue RIFLE_BASE_DRAIN = BUILDER
            .comment("Power the Atlantean Rifle draws every two ticks when it first fires")
            .defineInRange("rifleBaseDrain", 1_000, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue RIFLE_MAX_DRAIN = BUILDER
            .comment("Power the Atlantean Rifle draws every two ticks once fully ramped")
            .defineInRange("rifleMaxDrain", 5_000, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue RIFLE_RANGE = BUILDER
            .comment("How far the Atlantean Rifle beam reaches, in blocks")
            .defineInRange("rifleRange", 128.0, 1.0, 256.0);

    private static final ModConfigSpec.IntValue DOCK_POWER_USAGE = BUILDER
            .comment("The amount of power a Sea Scout Dock requires before it will service a hull")
            .defineInRange("dockPowerUsage", 20, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue DOCK_CHARGE_RATE = BUILDER
            .comment("The amount of power a Sea Scout Dock pushes into a docked hull each tick")
            .defineInRange("dockChargeRate", 40, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue GATEWAY_COOLDOWN = BUILDER
            .comment("The amount of ticks something is barred from using another gateway after arriving, which is what stops it bouncing straight back")
            .defineInRange("gatewayCooldown", 100, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue ABYSSAL_EYES_DEPTH = BUILDER
            .comment("The Y level at or below which the Abyssal Eyes augment grants night vision")
            .defineInRange("abyssalEyesDepth", 45, -64, 320);

    private static final ModConfigSpec.DoubleValue PHOTOPHORE_SKIN_RADIUS = BUILDER
            .comment("The radius in which the Photophore Skin augment reveals nearby creatures")
            .defineInRange("photophoreSkinRadius", 12.0, 1.0, 64.0);

    private static final ModConfigSpec.IntValue SUBMARINE_POWER_CAPACITY = BUILDER
            .comment("The power capacity of the submarine")
            .defineInRange("submarinePowerCapacity", 1_000_000, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_IDLE_POWER_USAGE = BUILDER
            .comment("The amount of power the submarine uses each tick while occupied")
            .defineInRange("submarineIdlePowerUsage", 1, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_MOVE_POWER_USAGE = BUILDER
            .comment("The extra amount of power the submarine uses each tick while under way")
            .defineInRange("submarineMovePowerUsage", 6, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_OXYGEN_POWER_USAGE = BUILDER
            .comment("The extra amount of power the submarine uses each tick while keeping its occupants breathing")
            .defineInRange("submarineOxygenPowerUsage", 2, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_SPEED = BUILDER
            .comment("Thrust applied to the submarine each tick while the throttle is open")
            .defineInRange("submarineSpeed", 0.056, 0.001, 1.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_MAX_SPEED = BUILDER
            .comment("Maximum speed of the submarine in blocks per tick. Values above 1.5 risk tripping server movement checks")
            .defineInRange("submarineMaxSpeed", 0.81, 0.05, 1.5);
    private static final ModConfigSpec.DoubleValue SUBMARINE_CAMERA_DISTANCE = BUILDER
            .comment("Third person camera distance while riding the submarine, in blocks. Vanilla default is 4")
            .defineInRange("submarineCameraDistance", 10.0, 4.0, 32.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_MAX_HEALTH = BUILDER
            .comment("Hull integrity of the submarine, in half hearts")
            .defineInRange("submarineMaxHealth", 80.0, 1.0, 1024.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_ARMOR = BUILDER
            .comment("Armor points of the submarine hull. Diamond armor is 20")
            .defineInRange("submarineArmor", 20.0, 0.0, 30.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_ARMOR_TOUGHNESS = BUILDER
            .comment("Armor toughness of the submarine hull. Diamond armor is 8")
            .defineInRange("submarineArmorToughness", 8.0, 0.0, 20.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_KNOCKBACK_RESISTANCE = BUILDER
            .comment("How strongly the submarine resists being knocked around when it is hit. 1 ignores knockback entirely")
            .defineInRange("submarineKnockbackResistance", 0.0, 0.0, 1.0);
    private static final ModConfigSpec.IntValue SUBMARINE_AUTOREPAIR_INTERVAL = BUILDER
            .comment("Ticks between hull self repair pulses. Set to 0 to disable self repair")
            .defineInRange("submarineAutorepairIntervalTicks", 200, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_AUTOREPAIR_PERCENT = BUILDER
            .comment("Fraction of the hull restored by each self repair pulse")
            .defineInRange("submarineAutorepairPercent", 0.01, 0.0, 1.0);
    private static final ModConfigSpec.ConfigValue<String> SUBMARINE_REPAIR_ITEM = BUILDER
            .comment("The item that repairs a submarine in an anvil")
            .define("submarineRepairItem", "nautec:deep_steel_plating");
    private static final ModConfigSpec.DoubleValue SUBMARINE_REPAIR_PERCENT = BUILDER
            .comment("Fraction of the hull restored by each repair item used in an anvil")
            .defineInRange("submarineRepairPercent", 0.20, 0.01, 1.0);

    private static final ModConfigSpec.DoubleValue SUBMARINE_SOLAR_PERCENT = BUILDER
            .comment("Percent of the submarine's power capacity the Solar Module collects every 5 seconds in open sunlit water")
            .defineInRange("submarineSolarPercentPer5s", 1.0, 0.0, 100.0);
    private static final ModConfigSpec.IntValue SUBMARINE_BOOST_POWER = BUILDER
            .comment("Power drawn by one activation of the Booster Module")
            .defineInRange("submarineBoostPowerCost", 20_000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_BOOST_DURATION = BUILDER
            .comment("Ticks the Booster Module stays lit")
            .defineInRange("submarineBoostDurationTicks", 200, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_BOOST_COOLDOWN = BUILDER
            .comment("Ticks after the boost expires before it can be used again")
            .defineInRange("submarineBoostCooldownTicks", 60, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_BOOST_SPEED = BUILDER
            .comment("Extra speed the Booster Module gives, as a fraction of the base speed")
            .defineInRange("submarineBoostSpeedBonus", 0.45, 0.0, 4.0);
    private static final ModConfigSpec.IntValue SUBMARINE_STEALTH_POWER = BUILDER
            .comment("Power drawn by one activation of the Stealth Module")
            .defineInRange("submarineStealthPowerCost", 50_000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_STEALTH_DURATION = BUILDER
            .comment("Ticks the Stealth Module hides the submarine from mobs")
            .defineInRange("submarineStealthDurationTicks", 2_400, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_STEALTH_COOLDOWN = BUILDER
            .comment("Ticks after stealth expires before it can be used again")
            .defineInRange("submarineStealthCooldownTicks", 200, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_STEALTH_SLOW = BUILDER
            .comment("Speed the Stealth Module gives up while it is running, as a fraction of the base speed")
            .defineInRange("submarineStealthSpeedPenalty", 0.15, 0.0, 0.9);
    private static final ModConfigSpec.IntValue SUBMARINE_SONAR_POWER = BUILDER
            .comment("Power drawn by one Sonar Module ping")
            .defineInRange("submarineSonarPowerCost", 30_000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_SONAR_COOLDOWN = BUILDER
            .comment("Ticks between Sonar Module pings")
            .defineInRange("submarineSonarCooldownTicks", 900, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_SONAR_RADIUS = BUILDER
            .comment("Radius in blocks the Sonar Module marks hostile creatures in")
            .defineInRange("submarineSonarRadius", 32.0, 1.0, 128.0);
    private static final ModConfigSpec.IntValue SUBMARINE_SHIELD_POWER = BUILDER
            .comment("Power drawn by one Shield Module discharge")
            .defineInRange("submarineShieldPowerCost", 25_000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_SHIELD_COOLDOWN = BUILDER
            .comment("Ticks between Shield Module discharges")
            .defineInRange("submarineShieldCooldownTicks", 100, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_SHIELD_STUN = BUILDER
            .comment("Ticks a Shield Module discharge stuns the creatures it hits")
            .defineInRange("submarineShieldStunTicks", 60, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_SHIELD_DAMAGE = BUILDER
            .comment("Damage a Shield Module discharge deals")
            .defineInRange("submarineShieldDamage", 10.0, 0.0, 1024.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_SHIELD_RADIUS = BUILDER
            .comment("Radius in blocks of a Shield Module discharge")
            .defineInRange("submarineShieldRadius", 5.0, 1.0, 32.0);
    private static final ModConfigSpec.IntValue SUBMARINE_SHIELD_POWER_PER_HEART = BUILDER
            .comment("Power an installed Shield Module burns to absorb one heart of incoming damage")
            .defineInRange("submarineShieldPowerPerHeart", 10_000, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_LASER_POWER = BUILDER
            .comment("Power drawn by each Impulse Laser damage cycle")
            .defineInRange("submarineLaserPowerCost", 10_000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_LASER_RANGE = BUILDER
            .comment("Range in blocks of the Impulse Laser beams")
            .defineInRange("submarineLaserRange", 64.0, 1.0, 128.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_LASER_DAMAGE = BUILDER
            .comment("Flat damage each Impulse Laser cycle deals")
            .defineInRange("submarineLaserDamage", 10.0, 0.0, 1024.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_LASER_HEALTH_PERCENT = BUILDER
            .comment("Extra Impulse Laser damage per cycle, as a fraction of the target's maximum health")
            .defineInRange("submarineLaserHealthPercent", 0.025, 0.0, 1.0);
    private static final ModConfigSpec.IntValue SUBMARINE_TELEPORT_POWER = BUILDER
            .comment("Power drawn by one Teleport Module jump")
            .defineInRange("submarineTeleportPowerCost", 200_000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_TELEPORT_MIN_POWER = BUILDER
            .comment("Fraction of the power capacity that must be stored before a jump may be started")
            .defineInRange("submarineTeleportMinPowerPercent", 0.20, 0.0, 1.0);
    private static final ModConfigSpec.IntValue SUBMARINE_TELEPORT_COOLDOWN = BUILDER
            .comment("Ticks between Teleport Module jumps")
            .defineInRange("submarineTeleportCooldownTicks", 600, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_ARMOR_MODULE_TOUGHNESS = BUILDER
            .comment("Armor toughness of the hull while an Armor Module is installed. Netherite armor is 12")
            .defineInRange("submarineArmorModuleToughness", 12.0, 0.0, 20.0);

    private static final ModConfigSpec.BooleanValue LUCKY_ZONES_ENABLED = BUILDER
            .comment("Whether lucky fishing zones appear on the water around players")
            .define("luckyZonesEnabled", true);
    private static final ModConfigSpec.IntValue LUCKY_ZONE_INTERVAL = BUILDER
            .comment("Seconds between attempts to place a lucky fishing zone near a player")
            .defineInRange("luckyZoneIntervalSeconds", 45, 1, 3600);
    private static final ModConfigSpec.IntValue LUCKY_ZONE_SPAWN_DISTANCE = BUILDER
            .comment("How far from the player a lucky fishing zone may appear")
            .defineInRange("luckyZoneSpawnDistance", 32, 4, 128);
    private static final ModConfigSpec.IntValue LUCKY_ZONE_MIN_SEPARATION = BUILDER
            .comment("Minimum distance between two lucky fishing zones")
            .defineInRange("luckyZoneMinSeparation", 48, 4, 256);
    private static final ModConfigSpec.IntValue LUCKY_ZONE_PER_CHUNK = BUILDER
            .comment("Maximum lucky fishing zones in a single chunk")
            .defineInRange("luckyZonesPerChunk", 1, 1, 16);
    private static final ModConfigSpec.IntValue LUCKY_ZONE_LIFETIME = BUILDER
            .comment("Seconds a lucky fishing zone lasts before fading away")
            .defineInRange("luckyZoneLifetimeSeconds", 300, 10, 36000);
    private static final ModConfigSpec.IntValue LUCKY_ZONE_MIN_RADIUS = BUILDER
            .comment("Smallest lucky fishing zone radius. Every block in the radius must be open water")
            .defineInRange("luckyZoneMinRadius", 2, 1, 16);
    private static final ModConfigSpec.IntValue LUCKY_ZONE_MAX_RADIUS = BUILDER
            .comment("Largest lucky fishing zone radius. Every block in the radius must be open water")
            .defineInRange("luckyZoneMaxRadius", 6, 1, 16);
    private static final ModConfigSpec.BooleanValue LUCKY_ZONE_CONSUMED = BUILDER
            .comment("Whether a lucky fishing zone disappears after one successful catch")
            .define("luckyZoneConsumedOnCatch", true);
    private static final ModConfigSpec.IntValue LUCKY_ZONE_BITE_SPEED = BUILDER
            .comment("How many times faster fish bite while the bobber floats in a lucky fishing zone")
            .defineInRange("luckyZoneBiteSpeed", 2, 1, 10);

    private static final ModConfigSpec.BooleanValue ENABLE_BIOME_INJECTION = WORLDGEN_BUILDER
            .comment("Determines whether Nautec's ocean biomes are added to the world's biome layout. Turning this off leaves vanilla oceans untouched",
                    "This only applies when Lithostitched is absent. With Lithostitched installed, placement comes from the biome injectors in data/nautec/lithostitched/biome_injector, which a datapack can override or empty out")
            .define("enableBiomeInjection", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> INJECTABLE_WORLD_PRESETS = WORLDGEN_BUILDER
            .comment("The multi-noise presets Nautec's ocean biomes are added to. Packs using a custom overworld preset should list it here",
                    "Ignored when Lithostitched is installed, since the biome injectors target the overworld dimension directly and work with Terralith and Tectonic")
            .defineList("injectableWorldPresets", List.of("minecraft:overworld"), () -> "minecraft:overworld", entry -> entry instanceof String);

    static final ModConfigSpec SPEC = BUILDER.build();
    static final ModConfigSpec WORLDGEN_SPEC = WORLDGEN_BUILDER.build();

    public static int kelpHeight;
    public static boolean spawnBookInInventory;
    public static boolean collectSaltWater;
    public static boolean collectAirWithBottle;

    public static int mixerPower;
    public static int drainPower;
    public static int augmentationStationPower;
    public static int laserDistance;
    public static int longDistanceLaserDistance;

    public static int mixerInputCapacity;
    public static int mixerOutputCapacity;

    public static int drainSaltWaterAmount;
    public static int drainCapacity;

    public static int guardianAugmentDamage;
    public static boolean allowAugmentRendering;

    public static int fisherLaserLevel;
    public static int fisherRunDuration;
    public static int fisherRadius;
    public static int fisherDepth;

    public static float bacteriaGrowthRateCap;
    public static float bacteriaProductionRateCap;
    public static float bacteriaMutationResistanceCap;
    public static long bacteriaColonySizeCap;
    public static int bacteriaLifespanCap;

    public static int bacteriaAnalyzerCraftingSpeed;
    public static int bacteriaAnalyzerPowerUsage;

    public static int mutatorCraftingSpeed;
    public static int mutatorPowerUsage;
    public static double mutatorFailureShrink = 0.25;

    public static int incubatorCraftingSpeed = 100;
    public static int incubatorPowerUsage = 20;

    public static double bioReactorBaseSpeed = 5.6;
    public static int bioReactorPowerBase = 25;
    public static int bioReactorPowerPerColony = 25;
    public static double bioReactorDecayFraction = 0.10;
    public static int fuelCellPowerBase = 24;
    public static double fuelCellBurnRate = 0.5;
    public static double fuelCellMaxPurity = 2.5;
    public static double mirrorPurityFactor = 0.9;
    public static double splitterPurityFactor = 0.8;
    public static double lensPurityBonus = 0.5;
    public static double resonanceBaseCeiling = 2000.0;
    public static int resonancePowerUsage = 20;
    public static double resonanceCriticalLow = 0.9;
    public static double resonanceCriticalHigh = 1.1;
    public static int resonanceVentCooldown = 200;
    public static double resonanceVentRadius = 4.0;
    public static double resonanceVentDamage = 8.0;
    public static int gatewayCooldown = 100;
    public static double waveJetThrust = 0.035;
    public static int waveJetPowerUsage = 2;
    public static int waveJetLightRange = 12;
    public static int waveJetLightLevel = 14;
    public static int waveJetLightPowerUsage = 1;
    public static int riflePowerCapacity = 1_000_000;
    public static int rifleMaxInput = 20_000;
    public static int rifleChargeTicks = 10;
    public static int rifleRampTicks = 200;
    public static double rifleBaseDamage = 4.0;
    public static double rifleMaxDamage = 20.0;
    public static int rifleBaseDrain = 1_000;
    public static int rifleMaxDrain = 5_000;
    public static double rifleRange = 128.0;
    public static int dockPowerUsage = 20;
    public static int dockChargeRate = 40;
    public static int pressureForgeDepth = 0;
    public static int pressureForgeWaterColumn = 8;
    public static int pressureForgePowerUsage = 40;
    public static int pressureForgeAcidUsage = 250;
    public static int pressureForgeCapacity = 4_000;

    public static boolean luckyZonesEnabled;
    public static int luckyZoneIntervalSeconds;
    public static int luckyZoneSpawnDistance;
    public static int luckyZoneMinSeparation;
    public static int luckyZonesPerChunk;
    public static int luckyZoneLifetimeSeconds;
    public static int luckyZoneMinRadius;
    public static int luckyZoneMaxRadius;
    public static boolean luckyZoneConsumedOnCatch;
    public static int luckyZoneBiteSpeed;

    public static int abyssalEyesDepth;
    public static double photophoreSkinRadius;

    public static int submarinePowerCapacity = 1_000_000;
    public static int submarineIdlePowerUsage = 1;
    public static int submarineMovePowerUsage = 6;
    public static int submarineOxygenPowerUsage = 2;
    public static double submarineSpeed = 0.056;
    public static double submarineMaxSpeed = 0.81;
    public static double submarineCameraDistance = 10.0;
    public static double submarineMaxHealth = 80.0;
    public static double submarineArmor = 20.0;
    public static double submarineArmorToughness = 8.0;
    public static double submarineKnockbackResistance = 0.0;
    public static int submarineAutorepairIntervalTicks = 200;
    public static double submarineAutorepairPercent = 0.01;
    public static String submarineRepairItem = "minecraft:diamond";
    public static double submarineRepairPercent = 0.20;

    public static double submarineSolarPercentPer5s = 1.0;
    public static int submarineBoostPowerCost = 20_000;
    public static int submarineBoostDurationTicks = 200;
    public static int submarineBoostCooldownTicks = 60;
    public static double submarineBoostSpeedBonus = 0.45;
    public static int submarineStealthPowerCost = 50_000;
    public static int submarineStealthDurationTicks = 2_400;
    public static int submarineStealthCooldownTicks = 200;
    public static double submarineStealthSpeedPenalty = 0.15;
    public static int submarineSonarPowerCost = 30_000;
    public static int submarineSonarCooldownTicks = 900;
    public static double submarineSonarRadius = 32.0;
    public static int submarineShieldPowerCost = 25_000;
    public static int submarineShieldCooldownTicks = 100;
    public static int submarineShieldStunTicks = 60;
    public static double submarineShieldDamage = 10.0;
    public static double submarineShieldRadius = 5.0;
    public static int submarineShieldPowerPerHeart = 10_000;
    public static int submarineLaserPowerCost = 10_000;
    public static double submarineLaserRange = 64.0;
    public static double submarineLaserDamage = 10.0;
    public static double submarineLaserHealthPercent = 0.025;
    public static int submarineTeleportPowerCost = 200_000;
    public static double submarineTeleportMinPowerPercent = 0.20;
    public static int submarineTeleportCooldownTicks = 600;
    public static double submarineArmorModuleToughness = 12.0;

    public static boolean biomeInjectionEnabled() {
        return !WORLDGEN_SPEC.isLoaded() || ENABLE_BIOME_INJECTION.getAsBoolean();
    }

    public static Set<String> injectableWorldPresets() {
        if (!WORLDGEN_SPEC.isLoaded()) {
            return Set.of("minecraft:overworld");
        }
        return Set.copyOf(INJECTABLE_WORLD_PRESETS.get());
    }

    static {
        loadValues();
    }

    private static <T> T value(ModConfigSpec.ConfigValue<T> value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) loadValues();
    }

    private static void loadValues() {
        kelpHeight = value(KELP_HEIGHT);
        spawnBookInInventory = value(SPAWN_BOOK_IN_INVENTORY);
        collectSaltWater = value(COLLECT_SALT_WATER);
        collectAirWithBottle = value(COLLECT_AIR_WITH_BOTTLE);

        mixerPower = value(MIXER_POWER_REQUIREMENT);
        drainPower = value(DRAIN_POWER_REQUIREMENT);
        augmentationStationPower = value(AUGMENTATION_STATION_POWER_REQUIREMENT);

        laserDistance = value(REGULAR_LASER_DISTANCE);
        longDistanceLaserDistance = value(LONG_DISTANCE_LASER_DISTANCE);

        mixerInputCapacity = value(MIXER_INPUT_CAPACITY);
        mixerOutputCapacity = value(MIXER_OUTPUT_CAPACITY);

        drainSaltWaterAmount = value(DRAIN_SALT_WATER_AMOUNT);
        drainCapacity = value(DRAIN_CAPACITY);

        guardianAugmentDamage = value(GUARDIAN_AUGMENT_DAMAGE);
        allowAugmentRendering = value(ALLOW_AUGMENT_RENDERING);

        fisherLaserLevel = value(FISHER_LASER_LEVEL);
        fisherRunDuration = value(FISHER_DURATION);
        fisherDepth = value(FISHER_DEPTH);
        fisherRadius = value(FISHER_RADIUS);

        bacteriaGrowthRateCap = value(BACTERIA_GROWTH_RATE_CAP).floatValue();
        bacteriaProductionRateCap = value(BACTERIA_PRODUCTION_RATE_CAP).floatValue();
        bacteriaMutationResistanceCap = value(BACTERIA_MUTATION_RESISTANCE_CAP).floatValue();
        bacteriaColonySizeCap = value(BACTERIA_COLONY_SIZE_CAP);
        bacteriaLifespanCap = value(BACTERIA_LIFESPAN_CAP);

        bacteriaAnalyzerCraftingSpeed = value(BACTERIA_ANALYZER_CRAFTING_SPEED);
        bacteriaAnalyzerPowerUsage = value(BACTERIA_ANALYZER_POWER_USAGE);

        mutatorCraftingSpeed = value(MUTATOR_CRAFTING_SPEED);
        mutatorPowerUsage = value(MUTATOR_POWER_USAGE);
        mutatorFailureShrink = value(MUTATOR_FAILURE_SHRINK);

        incubatorCraftingSpeed = value(INCUBATOR_CRAFTING_SPEED);
        incubatorPowerUsage = value(INCUBATOR_POWER_USAGE);

        bioReactorBaseSpeed = value(BIO_REACTOR_BASE_SPEED);
        bioReactorPowerBase = value(BIO_REACTOR_POWER_BASE);
        bioReactorPowerPerColony = value(BIO_REACTOR_POWER_PER_COLONY);
        bioReactorDecayFraction = value(BIO_REACTOR_DECAY_FRACTION);
        fuelCellPowerBase = value(FUEL_CELL_POWER_BASE);
        fuelCellBurnRate = value(FUEL_CELL_BURN_RATE);
        fuelCellMaxPurity = value(FUEL_CELL_MAX_PURITY);
        mirrorPurityFactor = value(MIRROR_PURITY_FACTOR);
        splitterPurityFactor = value(SPLITTER_PURITY_FACTOR);
        lensPurityBonus = value(LENS_PURITY_BONUS);
        resonanceBaseCeiling = value(RESONANCE_BASE_CEILING);
        resonancePowerUsage = value(RESONANCE_POWER_USAGE);
        resonanceCriticalLow = value(RESONANCE_CRITICAL_LOW);
        resonanceCriticalHigh = value(RESONANCE_CRITICAL_HIGH);
        resonanceVentCooldown = value(RESONANCE_VENT_COOLDOWN);
        resonanceVentRadius = value(RESONANCE_VENT_RADIUS);
        resonanceVentDamage = value(RESONANCE_VENT_DAMAGE);
        gatewayCooldown = value(GATEWAY_COOLDOWN);
        waveJetThrust = value(WAVE_JET_THRUST);
        waveJetPowerUsage = value(WAVE_JET_POWER_USAGE);
        waveJetLightRange = value(WAVE_JET_LIGHT_RANGE);
        waveJetLightLevel = value(WAVE_JET_LIGHT_LEVEL);
        waveJetLightPowerUsage = value(WAVE_JET_LIGHT_POWER_USAGE);
        riflePowerCapacity = value(RIFLE_POWER_CAPACITY);
        rifleMaxInput = value(RIFLE_MAX_INPUT);
        rifleChargeTicks = value(RIFLE_CHARGE_TICKS);
        rifleRampTicks = value(RIFLE_RAMP_TICKS);
        rifleBaseDamage = value(RIFLE_BASE_DAMAGE);
        rifleMaxDamage = value(RIFLE_MAX_DAMAGE);
        rifleBaseDrain = value(RIFLE_BASE_DRAIN);
        rifleMaxDrain = value(RIFLE_MAX_DRAIN);
        rifleRange = value(RIFLE_RANGE);
        dockPowerUsage = value(DOCK_POWER_USAGE);
        dockChargeRate = value(DOCK_CHARGE_RATE);
        pressureForgeDepth = value(PRESSURE_FORGE_DEPTH);
        pressureForgeWaterColumn = value(PRESSURE_FORGE_WATER_COLUMN);
        pressureForgePowerUsage = value(PRESSURE_FORGE_POWER_USAGE);
        pressureForgeAcidUsage = value(PRESSURE_FORGE_ACID_USAGE);
        pressureForgeCapacity = value(PRESSURE_FORGE_CAPACITY);

        luckyZonesEnabled = value(LUCKY_ZONES_ENABLED);
        luckyZoneIntervalSeconds = value(LUCKY_ZONE_INTERVAL);
        luckyZoneSpawnDistance = value(LUCKY_ZONE_SPAWN_DISTANCE);
        luckyZoneMinSeparation = value(LUCKY_ZONE_MIN_SEPARATION);
        luckyZonesPerChunk = value(LUCKY_ZONE_PER_CHUNK);
        luckyZoneLifetimeSeconds = value(LUCKY_ZONE_LIFETIME);
        luckyZoneMinRadius = value(LUCKY_ZONE_MIN_RADIUS);
        luckyZoneMaxRadius = Math.max(value(LUCKY_ZONE_MIN_RADIUS), value(LUCKY_ZONE_MAX_RADIUS));
        luckyZoneConsumedOnCatch = value(LUCKY_ZONE_CONSUMED);
        luckyZoneBiteSpeed = value(LUCKY_ZONE_BITE_SPEED);

        abyssalEyesDepth = value(ABYSSAL_EYES_DEPTH);
        photophoreSkinRadius = value(PHOTOPHORE_SKIN_RADIUS);

        submarinePowerCapacity = value(SUBMARINE_POWER_CAPACITY);
        submarineIdlePowerUsage = value(SUBMARINE_IDLE_POWER_USAGE);
        submarineMovePowerUsage = value(SUBMARINE_MOVE_POWER_USAGE);
        submarineOxygenPowerUsage = value(SUBMARINE_OXYGEN_POWER_USAGE);
        submarineSpeed = value(SUBMARINE_SPEED);
        submarineMaxSpeed = value(SUBMARINE_MAX_SPEED);
        submarineCameraDistance = value(SUBMARINE_CAMERA_DISTANCE);
        submarineMaxHealth = value(SUBMARINE_MAX_HEALTH);
        submarineArmor = value(SUBMARINE_ARMOR);
        submarineArmorToughness = value(SUBMARINE_ARMOR_TOUGHNESS);
        submarineKnockbackResistance = value(SUBMARINE_KNOCKBACK_RESISTANCE);
        submarineAutorepairIntervalTicks = value(SUBMARINE_AUTOREPAIR_INTERVAL);
        submarineAutorepairPercent = value(SUBMARINE_AUTOREPAIR_PERCENT);
        submarineRepairItem = value(SUBMARINE_REPAIR_ITEM);
        submarineRepairPercent = value(SUBMARINE_REPAIR_PERCENT);

        submarineSolarPercentPer5s = value(SUBMARINE_SOLAR_PERCENT);
        submarineBoostPowerCost = value(SUBMARINE_BOOST_POWER);
        submarineBoostDurationTicks = value(SUBMARINE_BOOST_DURATION);
        submarineBoostCooldownTicks = value(SUBMARINE_BOOST_COOLDOWN);
        submarineBoostSpeedBonus = value(SUBMARINE_BOOST_SPEED);
        submarineStealthPowerCost = value(SUBMARINE_STEALTH_POWER);
        submarineStealthDurationTicks = value(SUBMARINE_STEALTH_DURATION);
        submarineStealthCooldownTicks = value(SUBMARINE_STEALTH_COOLDOWN);
        submarineStealthSpeedPenalty = value(SUBMARINE_STEALTH_SLOW);
        submarineSonarPowerCost = value(SUBMARINE_SONAR_POWER);
        submarineSonarCooldownTicks = value(SUBMARINE_SONAR_COOLDOWN);
        submarineSonarRadius = value(SUBMARINE_SONAR_RADIUS);
        submarineShieldPowerCost = value(SUBMARINE_SHIELD_POWER);
        submarineShieldCooldownTicks = value(SUBMARINE_SHIELD_COOLDOWN);
        submarineShieldStunTicks = value(SUBMARINE_SHIELD_STUN);
        submarineShieldDamage = value(SUBMARINE_SHIELD_DAMAGE);
        submarineShieldRadius = value(SUBMARINE_SHIELD_RADIUS);
        submarineShieldPowerPerHeart = value(SUBMARINE_SHIELD_POWER_PER_HEART);
        submarineLaserPowerCost = value(SUBMARINE_LASER_POWER);
        submarineLaserRange = value(SUBMARINE_LASER_RANGE);
        submarineLaserDamage = value(SUBMARINE_LASER_DAMAGE);
        submarineLaserHealthPercent = value(SUBMARINE_LASER_HEALTH_PERCENT);
        submarineTeleportPowerCost = value(SUBMARINE_TELEPORT_POWER);
        submarineTeleportMinPowerPercent = value(SUBMARINE_TELEPORT_MIN_POWER);
        submarineTeleportCooldownTicks = value(SUBMARINE_TELEPORT_COOLDOWN);
        submarineArmorModuleToughness = value(SUBMARINE_ARMOR_MODULE_TOUGHNESS);
    }
}
