package com.breakinblocks.nautec.content.enchanter;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record EnchantingOutcome(ItemStack result, Holder<Enchantment> enchantment, int level, Kind kind,
                                @Nullable Holder<Enchantment> bonus, @Nullable Holder<Enchantment> curse) {
    public enum Kind {
        EXACT,
        DRIFTED,
        SWAPPED,
        BONUS
    }

    private static final float DRIFT_SHARE = 0.6F;
    private static final float SWAP_SHARE = 0.3F;
    private static final float DRIFT_UP_CHANCE = 0.2F;
    private static final float DEEP_DRIFT_PURITY = 1.0F;

    public static EnchantingOutcome roll(HolderLookup.Provider registries, ItemStack target, Holder<Enchantment> enchantment, int level,
                                         float purity, RandomSource random) {
        ItemStack result = target.is(Items.BOOK) ? new ItemStack(Items.ENCHANTED_BOOK) : target.copyWithCount(1);
        Holder<Enchantment> chosen = enchantment;
        int chosenLevel = level;
        Kind kind = Kind.EXACT;
        Holder<Enchantment> bonus = null;

        if (random.nextFloat() < EnchantingRules.instability(purity)) {
            float roll = random.nextFloat();
            if (roll >= DRIFT_SHARE && roll < DRIFT_SHARE + SWAP_SHARE) {
                Holder<Enchantment> swap = pick(registries, EnchantmentTags.IN_ENCHANTING_TABLE, result, enchantment, null, random);
                if (swap != null) {
                    chosen = swap;
                    chosenLevel = Math.clamp(level, 1, EnchantingRules.maxLevel(swap));
                    kind = Kind.SWAPPED;
                }
            } else if (roll >= DRIFT_SHARE + SWAP_SHARE) {
                bonus = pick(registries, EnchantmentTags.IN_ENCHANTING_TABLE, result, enchantment, enchantment, random);
                if (bonus != null) {
                    kind = Kind.BONUS;
                }
            }
            if (kind == Kind.EXACT) {
                chosenLevel = drift(level, EnchantingRules.maxLevel(enchantment), purity, random);
                kind = chosenLevel == level ? Kind.EXACT : Kind.DRIFTED;
            }
        }

        add(result, chosen, chosenLevel);
        if (bonus != null) {
            add(result, bonus, 1);
        }

        Holder<Enchantment> curse = null;
        if (random.nextFloat() < EnchantingRules.curseChance(purity)) {
            curse = pick(registries, EnchantmentTags.CURSE, result, null, null, random);
            if (curse != null) {
                add(result, curse, EnchantingRules.maxLevel(curse));
            }
        }
        return new EnchantingOutcome(result, chosen, chosenLevel, kind, bonus, curse);
    }

    private static int drift(int level, int max, float purity, RandomSource random) {
        if (level < max && random.nextFloat() < DRIFT_UP_CHANCE) {
            return level + 1;
        }
        int drop = 1 + (purity < DEEP_DRIFT_PURITY ? random.nextInt(2) : 0);
        return Math.max(1, level - drop);
    }

    private static void add(ItemStack stack, Holder<Enchantment> enchantment, int level) {
        EnchantmentHelper.updateEnchantments(stack, enchantments ->
                enchantments.set(enchantment, Math.max(enchantments.getLevel(enchantment), level)));
    }

    private static @Nullable Holder<Enchantment> pick(HolderLookup.Provider registries, TagKey<Enchantment> pool, ItemStack stack,
                                                      @Nullable Holder<Enchantment> exclude, @Nullable Holder<Enchantment> alongside,
                                                      RandomSource random) {
        List<Holder<Enchantment>> candidates = new ArrayList<>();
        int totalWeight = 0;
        HolderLookup.RegistryLookup<Enchantment> lookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
        for (Holder<Enchantment> candidate : lookup.getOrThrow(pool)) {
            if (candidate.equals(exclude) || !EnchantingRules.allowed(candidate) || !EnchantingRules.accepts(stack, candidate)) {
                continue;
            }
            if (alongside != null && !Enchantment.areCompatible(alongside, candidate)) {
                continue;
            }
            candidates.add(candidate);
            totalWeight += Math.max(1, candidate.value().getWeight());
        }
        if (candidates.isEmpty()) {
            return null;
        }
        int roll = random.nextInt(totalWeight);
        for (Holder<Enchantment> candidate : candidates) {
            roll -= Math.max(1, candidate.value().getWeight());
            if (roll < 0) {
                return candidate;
            }
        }
        return candidates.getLast();
    }
}
