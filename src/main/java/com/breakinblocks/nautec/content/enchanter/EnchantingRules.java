package com.breakinblocks.nautec.content.enchanter;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.tags.NTTags;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class EnchantingRules {
    private EnchantingRules() {
    }

    public static boolean allowed(Holder<Enchantment> enchantment) {
        if (enchantment.is(NTTags.Enchantments.ABYSSAL_ENCHANTER_BLACKLIST)) {
            return false;
        }
        String id = enchantment.getRegisteredName();
        for (String entry : NTConfig.abyssalEnchanterBlacklist) {
            if (entry.equals(id) || entry.endsWith(":*") && id.startsWith(entry.substring(0, entry.length() - 1))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBook(ItemStack stack) {
        return stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK);
    }

    public static boolean accepts(ItemStack target, Holder<Enchantment> enchantment) {
        if (target.isEmpty()) {
            return false;
        }
        if (!isBook(target) && !target.supportsEnchantment(enchantment)) {
            return false;
        }
        return compatible(EnchantmentHelper.getEnchantmentsForCrafting(target), enchantment);
    }

    public static boolean compatible(ItemEnchantments existing, Holder<Enchantment> enchantment) {
        for (Holder<Enchantment> other : existing.keySet()) {
            if (!other.equals(enchantment) && !Enchantment.areCompatible(other, enchantment)) {
                return false;
            }
        }
        return true;
    }

    public static int existingLevel(ItemStack target, Holder<Enchantment> enchantment) {
        return EnchantmentHelper.getEnchantmentsForCrafting(target).getLevel(enchantment);
    }

    public static int maxLevel(Holder<Enchantment> enchantment) {
        return Math.max(1, enchantment.value().getMaxLevel());
    }

    public static int levelCap(Holder<Enchantment> enchantment) {
        int max = maxLevel(enchantment);
        return NTConfig.abyssalEnchanterOverenchant ? Math.min(255, max + NTConfig.abyssalEnchanterOverenchantLevels) : max;
    }

    public static int levelCap(Holder<Enchantment> enchantment, ItemStack target) {
        return isBook(target) ? maxLevel(enchantment) : levelCap(enchantment);
    }

    public static int overLevels(Holder<Enchantment> enchantment, int level) {
        return Math.max(0, level - maxLevel(enchantment));
    }

    public static double overFactor(Holder<Enchantment> enchantment, int level) {
        return Math.pow(NTConfig.abyssalEnchanterOverenchantFactor, overLevels(enchantment, level));
    }

    public static int rarity(Holder<Enchantment> enchantment) {
        int rarity = Math.max(1, enchantment.value().getAnvilCost());
        return enchantment.is(EnchantmentTags.TREASURE) ? rarity * 2 : rarity;
    }

    public static int sustainedPower(Holder<Enchantment> enchantment, int level, float multiplier) {
        double power = NTConfig.abyssalEnchanterBasePower
                * Math.pow(rarity(enchantment), NTConfig.abyssalEnchanterRarityExponent)
                * Math.pow(Math.max(1, level), NTConfig.abyssalEnchanterLevelExponent)
                * multiplier;
        return (int) Math.max(1, Math.min(Integer.MAX_VALUE, Math.ceil(power)));
    }

    public static long totalPower(Holder<Enchantment> enchantment, int level, float multiplier) {
        double total = (double) sustainedPower(enchantment, level, multiplier) * NTConfig.abyssalEnchanterTicksPerLevel * Math.max(1, level)
                * overFactor(enchantment, level);
        return (long) Math.min(Long.MAX_VALUE, Math.ceil(total));
    }

    public static int experienceLevels(Holder<Enchantment> enchantment, int level, float multiplier) {
        double levels = enchantment.value().getMinCost(Math.max(1, level)) * NTConfig.abyssalEnchanterXpScale * multiplier;
        return (int) Math.max(1, Math.min(10_000, Math.ceil(levels)));
    }

    public static long pointsToReach(int level) {
        long n = level;
        if (n <= 16) {
            return n * n + 6 * n;
        }
        if (n <= 31) {
            return (long) Math.floor(2.5 * n * n - 40.5 * n + 360);
        }
        return (long) Math.floor(4.5 * n * n - 162.5 * n + 2220);
    }

    public static int experienceMb(Holder<Enchantment> enchantment, int level, float multiplier) {
        double mb = (double) pointsToReach(experienceLevels(enchantment, level, multiplier)) * NTConfig.abyssalEnchanterMbPerPoint
                * overFactor(enchantment, level);
        return (int) Math.min(Integer.MAX_VALUE, Math.ceil(mb));
    }

    public static float instability(float purity) {
        double stable = Math.max(0.01, NTConfig.abyssalEnchanterStablePurity);
        return (float) Math.clamp(1.0 - purity / stable, 0.0, 1.0);
    }

    public static float curseChance(float purity) {
        if (!NTConfig.abyssalEnchanterCurses || purity >= NTConfig.abyssalEnchanterCursePurity) {
            return 0F;
        }
        return (float) Math.clamp((NTConfig.abyssalEnchanterCursePurity - purity) * NTConfig.abyssalEnchanterCurseChance, 0.0, 1.0);
    }
}
