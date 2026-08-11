package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public final class SubmarineAnvilRepair {
    public record Result(ItemStack output, int xpCost, int materialCost) {
    }

    private SubmarineAnvilRepair() {
    }

    public static float maxHealth() {
        return (float) NTConfig.submarineMaxHealth;
    }

    public static float healthOf(ItemStack stack) {
        Float health = stack.get(NTDataComponents.SUBMARINE_HEALTH);
        return health == null ? maxHealth() : Mth.clamp(health, 0F, maxHealth());
    }

    public static boolean isBreached(ItemStack stack) {
        Float health = stack.get(NTDataComponents.SUBMARINE_HEALTH);
        return health != null && health <= 0F;
    }

    public static Item repairItem() {
        Identifier id = Identifier.tryParse(NTConfig.submarineRepairItem);
        Item item = id == null ? null : BuiltInRegistries.ITEM.getValue(id);
        return item == null || item == Items.AIR ? Items.DIAMOND : item;
    }

    public static @Nullable Result compute(ItemStack left, ItemStack right) {
        if (!left.is(NTItems.SUBMARINE.get()) || left.getCount() != 1 || !right.is(repairItem())) {
            return null;
        }

        float max = maxHealth();
        float health = healthOf(left);
        float perItem = max * (float) NTConfig.submarineRepairPercent;
        if (health >= max || perItem <= 0F) {
            return null;
        }

        int used = Math.min(right.getCount(), Mth.ceil((max - health) / perItem));
        if (used <= 0) {
            return null;
        }

        ItemStack output = left.copy();
        output.set(NTDataComponents.SUBMARINE_HEALTH, Math.min(max, health + used * perItem));
        return new Result(output, used * 2, used);
    }
}
