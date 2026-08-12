package com.breakinblocks.nautec.data.generated;

import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.content.bacteria.SimpleBacteria;
import com.breakinblocks.nautec.content.bacteria.SimpleBacteriaStats;
import com.breakinblocks.nautec.content.recipes.BacteriaIncubationRecipe;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.utils.ranges.FloatRange;
import com.breakinblocks.nautec.utils.ranges.IntRange;
import com.breakinblocks.nautec.utils.ranges.LongRange;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class BacteriaBalance {
    private BacteriaBalance() {
    }

    public enum Rarity {
        COMMON(LongRange.of(350, 600), FloatRange.of(0.40f, 0.70f), FloatRange.of(0.60f, 1.20f),
                FloatRange.of(0.00f, 0.05f), IntRange.of(1300, 2500), IntRange.of(10, 30), 0.05f, 10f, NTBacterias.LITHOPHILES),
        UNCOMMON(LongRange.of(300, 500), FloatRange.of(0.20f, 0.55f), FloatRange.of(0.50f, 1.10f),
                FloatRange.of(0.00f, 0.08f), IntRange.of(1100, 2300), IntRange.of(10, 30), 0.07f, 10f, NTBacterias.METALLOPHILES),
        RARE(LongRange.of(225, 425), FloatRange.of(0.15f, 0.50f), FloatRange.of(0.45f, 1.00f),
                FloatRange.of(0.00f, 0.10f), IntRange.of(1000, 2100), IntRange.of(8, 25), 0.10f, 5f, NTBacterias.FERROPHILES),
        EPIC(LongRange.of(150, 275), FloatRange.of(0.10f, 0.40f), FloatRange.of(0.50f, 1.10f),
                FloatRange.of(0.00f, 0.12f), IntRange.of(1100, 2300), IntRange.of(8, 25), 0.10f, 5f, NTBacterias.AURROPHILES),
        LEGENDARY(LongRange.of(125, 150), FloatRange.of(0.08f, 0.12f), FloatRange.of(0.40f, 0.90f),
                FloatRange.of(0.00f, 0.15f), IntRange.of(1000, 2000), IntRange.of(5, 15), 0.12f, 3f, NTBacterias.ADAMANTOPHILES);

        private final LongRange initialSize;
        private final FloatRange productionRate;
        private final FloatRange growthRate;
        private final FloatRange mutationResistance;
        private final IntRange lifespan;
        private final IntRange incubationGrowth;
        private final float consumeChance;
        private final float mutationChance;
        private final ResourceKey<Bacteria> mutationParent;

        Rarity(LongRange initialSize, FloatRange productionRate, FloatRange growthRate, FloatRange mutationResistance,
               IntRange lifespan, IntRange incubationGrowth, float consumeChance, float mutationChance,
               ResourceKey<Bacteria> mutationParent) {
            this.initialSize = initialSize;
            this.productionRate = productionRate;
            this.growthRate = growthRate;
            this.mutationResistance = mutationResistance;
            this.lifespan = lifespan;
            this.incubationGrowth = incubationGrowth;
            this.consumeChance = consumeChance;
            this.mutationChance = mutationChance;
            this.mutationParent = mutationParent;
        }

        public LongRange initialSize() {
            return initialSize;
        }

        public FloatRange productionRate() {
            return productionRate;
        }

        public FloatRange growthRate() {
            return growthRate;
        }

        public FloatRange mutationResistance() {
            return mutationResistance;
        }

        public IntRange lifespan() {
            return lifespan;
        }

        public IntRange incubationGrowth() {
            return incubationGrowth;
        }

        public float consumeChance() {
            return consumeChance;
        }

        public float mutationChance() {
            return mutationChance;
        }

        public ResourceKey<Bacteria> mutationParent() {
            return mutationParent;
        }

        public String lowerName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static Optional<Rarity> byName(String name) {
            return Arrays.stream(values())
                    .filter(rarity -> rarity.lowerName().equals(name.toLowerCase(Locale.ROOT)))
                    .findFirst();
        }

        public static List<String> names() {
            return Arrays.stream(values()).map(Rarity::lowerName).toList();
        }
    }

    public static int colorFor(Identifier id) {
        int hash = id.toString().hashCode();
        float hue = 0.45f + 0.30f * unit(hash);
        float saturation = 0.55f + 0.30f * unit(hash >>> 10);
        float value = 0.70f + 0.25f * unit(hash >>> 20);

        return ARGB.opaque(Mth.hsvToRgb(hue, saturation, value));
    }

    private static float unit(int bits) {
        return (bits & 0x3FF) / 1023.0f;
    }

    public static SimpleBacteria buildBacteria(Item resource, Rarity rarity, Identifier id) {
        return buildBacteria(new Bacteria.Resource.ItemResource(resource), rarity, id);
    }

    public static SimpleBacteria buildBacteria(Bacteria.Resource resource, Rarity rarity, Identifier id) {
        return SimpleBacteria.of()
                .initialSize(rarity.initialSize())
                .resource(resource)
                .productionRate(rarity.productionRate())
                .growthRate(rarity.growthRate())
                .mutationResistance(rarity.mutationResistance())
                .lifespan(rarity.lifespan())
                .color(colorFor(id))
                .build();
    }

    public static BacteriaIncubationRecipe incubationRecipe(ResourceKey<Bacteria> bacteria, Ingredient nutrient, Rarity rarity) {
        return new BacteriaIncubationRecipe(bacteria, nutrient, rarity.incubationGrowth(), rarity.consumeChance());
    }

    public static BacteriaMutationRecipe mutationRecipe(ResourceKey<Bacteria> bacteria, Ingredient catalyst, Rarity rarity) {
        return new BacteriaMutationRecipe(rarity.mutationParent(), bacteria, catalyst, rarity.mutationChance());
    }

    public static Optional<Rarity> inferRarity(Bacteria bacteria) {
        if (!(bacteria.stats() instanceof SimpleBacteriaStats stats)) {
            return Optional.empty();
        }

        return Arrays.stream(Rarity.values())
                .filter(rarity -> sameLongRange(rarity.initialSize(), bacteria.initialSize())
                        && sameFloatRange(rarity.productionRate(), stats.productionRate())
                        && sameIntRange(rarity.lifespan(), stats.lifespan()))
                .findFirst();
    }

    private static boolean sameLongRange(LongRange expected, LongRange actual) {
        return expected.getMin().equals(actual.getMin()) && expected.getMax().equals(actual.getMax());
    }

    private static boolean sameFloatRange(FloatRange expected, FloatRange actual) {
        return Math.abs(expected.getMin() - actual.getMin()) < 1.0e-5f
                && Math.abs(expected.getMax() - actual.getMax()) < 1.0e-5f;
    }

    private static boolean sameIntRange(IntRange expected, IntRange actual) {
        return expected.getMin().equals(actual.getMin()) && expected.getMax().equals(actual.getMax());
    }
}
