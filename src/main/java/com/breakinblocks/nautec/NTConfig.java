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

    // Biology
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
            .comment("The amount of ticks it takes for the Bacterial Analyzer to analyze a Petri Dish")
            .defineInRange("mutatorCraftingSpeed", 240, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue MUTATOR_POWER_USAGE = BUILDER
            .comment("The amount of power used by the Mutator each tick")
            .defineInRange("mutatorPowerUsage", 10, 0, Integer.MAX_VALUE);

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
            .defineInRange("submarineSpeed", 0.045, 0.001, 1.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_MAX_SPEED = BUILDER
            .comment("Maximum speed of the submarine in blocks per tick. Values above 1.5 risk tripping server movement checks")
            .defineInRange("submarineMaxSpeed", 0.65, 0.05, 1.5);
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
            .define("submarineRepairItem", "minecraft:diamond");
    private static final ModConfigSpec.DoubleValue SUBMARINE_REPAIR_PERCENT = BUILDER
            .comment("Fraction of the hull restored by each repair item used in an anvil")
            .defineInRange("submarineRepairPercent", 0.20, 0.01, 1.0);

    // Submarine modules
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

    // Lucky fishing zones
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
            .defineInRange("luckyZoneMinRadius", 1, 1, 8);
    private static final ModConfigSpec.IntValue LUCKY_ZONE_MAX_RADIUS = BUILDER
            .comment("Largest lucky fishing zone radius. Every block in the radius must be open water")
            .defineInRange("luckyZoneMaxRadius", 3, 1, 8);
    private static final ModConfigSpec.BooleanValue LUCKY_ZONE_CONSUMED = BUILDER
            .comment("Whether a lucky fishing zone disappears after one successful catch")
            .define("luckyZoneConsumedOnCatch", true);

    // Worldgen
    private static final ModConfigSpec.BooleanValue ENABLE_BIOME_INJECTION = BUILDER
            .comment("Determines whether Nautec's ocean biomes are added to the world's biome layout. Turning this off leaves vanilla oceans untouched")
            .define("enableBiomeInjection", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> INJECTABLE_WORLD_PRESETS = BUILDER
            .comment("The multi-noise presets Nautec's ocean biomes are added to. Packs using a custom overworld preset should list it here")
            .defineList("injectableWorldPresets", List.of("minecraft:overworld"), () -> "minecraft:overworld", entry -> entry instanceof String);



    static final ModConfigSpec SPEC = BUILDER.build();

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

    public static boolean luckyZonesEnabled;
    public static int luckyZoneIntervalSeconds;
    public static int luckyZoneSpawnDistance;
    public static int luckyZoneMinSeparation;
    public static int luckyZonesPerChunk;
    public static int luckyZoneLifetimeSeconds;
    public static int luckyZoneMinRadius;
    public static int luckyZoneMaxRadius;
    public static boolean luckyZoneConsumedOnCatch;

    public static int abyssalEyesDepth;
    public static double photophoreSkinRadius;

    public static int submarinePowerCapacity = 1_000_000;
    public static int submarineIdlePowerUsage = 1;
    public static int submarineMovePowerUsage = 6;
    public static int submarineOxygenPowerUsage = 2;
    public static double submarineSpeed = 0.045;
    public static double submarineMaxSpeed = 0.65;
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
        return !SPEC.isLoaded() || ENABLE_BIOME_INJECTION.getAsBoolean();
    }

    public static Set<String> injectableWorldPresets() {
        if (!SPEC.isLoaded()) {
            return Set.of("minecraft:overworld");
        }
        return Set.copyOf(INJECTABLE_WORLD_PRESETS.get());
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }

        kelpHeight = KELP_HEIGHT.get();
        spawnBookInInventory = SPAWN_BOOK_IN_INVENTORY.get();
        collectSaltWater = COLLECT_SALT_WATER.getAsBoolean();
        collectAirWithBottle = COLLECT_AIR_WITH_BOTTLE.getAsBoolean();

        mixerPower = MIXER_POWER_REQUIREMENT.get();
        drainPower = DRAIN_POWER_REQUIREMENT.get();
        augmentationStationPower = AUGMENTATION_STATION_POWER_REQUIREMENT.get();

        laserDistance = REGULAR_LASER_DISTANCE.get();
        longDistanceLaserDistance = LONG_DISTANCE_LASER_DISTANCE.get();

        mixerInputCapacity = MIXER_INPUT_CAPACITY.getAsInt();
        mixerOutputCapacity = MIXER_OUTPUT_CAPACITY.getAsInt();

        drainSaltWaterAmount = DRAIN_SALT_WATER_AMOUNT.getAsInt();
        drainCapacity = DRAIN_CAPACITY.getAsInt();

        guardianAugmentDamage = GUARDIAN_AUGMENT_DAMAGE.get();
        allowAugmentRendering = ALLOW_AUGMENT_RENDERING.get();

        fisherLaserLevel = FISHER_LASER_LEVEL.getAsInt();
        fisherRunDuration = FISHER_DURATION.getAsInt();
        fisherDepth = FISHER_DEPTH.getAsInt();
        fisherRadius = FISHER_RADIUS.getAsInt();

        bacteriaGrowthRateCap = (float) BACTERIA_GROWTH_RATE_CAP.getAsDouble();
        bacteriaProductionRateCap = (float) BACTERIA_PRODUCTION_RATE_CAP.getAsDouble();
        bacteriaMutationResistanceCap = (float) BACTERIA_MUTATION_RESISTANCE_CAP.getAsDouble();
        bacteriaColonySizeCap = BACTERIA_COLONY_SIZE_CAP.get();
        bacteriaLifespanCap = BACTERIA_LIFESPAN_CAP.get();

        bacteriaAnalyzerCraftingSpeed = BACTERIA_ANALYZER_CRAFTING_SPEED.get();
        bacteriaAnalyzerPowerUsage = BACTERIA_ANALYZER_POWER_USAGE.get();

        mutatorCraftingSpeed = MUTATOR_CRAFTING_SPEED.get();
        mutatorPowerUsage = MUTATOR_POWER_USAGE.get();

        luckyZonesEnabled = LUCKY_ZONES_ENABLED.getAsBoolean();
        luckyZoneIntervalSeconds = LUCKY_ZONE_INTERVAL.getAsInt();
        luckyZoneSpawnDistance = LUCKY_ZONE_SPAWN_DISTANCE.getAsInt();
        luckyZoneMinSeparation = LUCKY_ZONE_MIN_SEPARATION.getAsInt();
        luckyZonesPerChunk = LUCKY_ZONE_PER_CHUNK.getAsInt();
        luckyZoneLifetimeSeconds = LUCKY_ZONE_LIFETIME.getAsInt();
        luckyZoneMinRadius = LUCKY_ZONE_MIN_RADIUS.getAsInt();
        luckyZoneMaxRadius = Math.max(LUCKY_ZONE_MIN_RADIUS.getAsInt(), LUCKY_ZONE_MAX_RADIUS.getAsInt());
        luckyZoneConsumedOnCatch = LUCKY_ZONE_CONSUMED.getAsBoolean();

        abyssalEyesDepth = ABYSSAL_EYES_DEPTH.get();
        photophoreSkinRadius = PHOTOPHORE_SKIN_RADIUS.getAsDouble();

        submarinePowerCapacity = SUBMARINE_POWER_CAPACITY.getAsInt();
        submarineIdlePowerUsage = SUBMARINE_IDLE_POWER_USAGE.getAsInt();
        submarineMovePowerUsage = SUBMARINE_MOVE_POWER_USAGE.getAsInt();
        submarineOxygenPowerUsage = SUBMARINE_OXYGEN_POWER_USAGE.getAsInt();
        submarineSpeed = SUBMARINE_SPEED.getAsDouble();
        submarineMaxSpeed = SUBMARINE_MAX_SPEED.getAsDouble();
        submarineCameraDistance = SUBMARINE_CAMERA_DISTANCE.getAsDouble();
        submarineMaxHealth = SUBMARINE_MAX_HEALTH.getAsDouble();
        submarineArmor = SUBMARINE_ARMOR.getAsDouble();
        submarineArmorToughness = SUBMARINE_ARMOR_TOUGHNESS.getAsDouble();
        submarineKnockbackResistance = SUBMARINE_KNOCKBACK_RESISTANCE.getAsDouble();
        submarineAutorepairIntervalTicks = SUBMARINE_AUTOREPAIR_INTERVAL.getAsInt();
        submarineAutorepairPercent = SUBMARINE_AUTOREPAIR_PERCENT.getAsDouble();
        submarineRepairItem = SUBMARINE_REPAIR_ITEM.get();
        submarineRepairPercent = SUBMARINE_REPAIR_PERCENT.getAsDouble();

        submarineSolarPercentPer5s = SUBMARINE_SOLAR_PERCENT.getAsDouble();
        submarineBoostPowerCost = SUBMARINE_BOOST_POWER.getAsInt();
        submarineBoostDurationTicks = SUBMARINE_BOOST_DURATION.getAsInt();
        submarineBoostCooldownTicks = SUBMARINE_BOOST_COOLDOWN.getAsInt();
        submarineBoostSpeedBonus = SUBMARINE_BOOST_SPEED.getAsDouble();
        submarineStealthPowerCost = SUBMARINE_STEALTH_POWER.getAsInt();
        submarineStealthDurationTicks = SUBMARINE_STEALTH_DURATION.getAsInt();
        submarineStealthCooldownTicks = SUBMARINE_STEALTH_COOLDOWN.getAsInt();
        submarineStealthSpeedPenalty = SUBMARINE_STEALTH_SLOW.getAsDouble();
        submarineSonarPowerCost = SUBMARINE_SONAR_POWER.getAsInt();
        submarineSonarCooldownTicks = SUBMARINE_SONAR_COOLDOWN.getAsInt();
        submarineSonarRadius = SUBMARINE_SONAR_RADIUS.getAsDouble();
        submarineShieldPowerCost = SUBMARINE_SHIELD_POWER.getAsInt();
        submarineShieldCooldownTicks = SUBMARINE_SHIELD_COOLDOWN.getAsInt();
        submarineShieldStunTicks = SUBMARINE_SHIELD_STUN.getAsInt();
        submarineShieldDamage = SUBMARINE_SHIELD_DAMAGE.getAsDouble();
        submarineShieldRadius = SUBMARINE_SHIELD_RADIUS.getAsDouble();
        submarineShieldPowerPerHeart = SUBMARINE_SHIELD_POWER_PER_HEART.getAsInt();
        submarineLaserPowerCost = SUBMARINE_LASER_POWER.getAsInt();
        submarineLaserRange = SUBMARINE_LASER_RANGE.getAsDouble();
        submarineLaserDamage = SUBMARINE_LASER_DAMAGE.getAsDouble();
        submarineLaserHealthPercent = SUBMARINE_LASER_HEALTH_PERCENT.getAsDouble();
        submarineTeleportPowerCost = SUBMARINE_TELEPORT_POWER.getAsInt();
        submarineTeleportMinPowerPercent = SUBMARINE_TELEPORT_MIN_POWER.getAsDouble();
        submarineTeleportCooldownTicks = SUBMARINE_TELEPORT_COOLDOWN.getAsInt();
        submarineArmorModuleToughness = SUBMARINE_ARMOR_MODULE_TOUGHNESS.getAsDouble();
    }

}
