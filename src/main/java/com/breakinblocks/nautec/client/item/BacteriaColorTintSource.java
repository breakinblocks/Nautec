package com.breakinblocks.nautec.client.item;

import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.utils.ARGB;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

public record BacteriaColorTintSource(int tintIndex) implements ItemColor {
    public static final int DEFAULT_TINT_INDEX = 1;

    public BacteriaColorTintSource() {
        this(DEFAULT_TINT_INDEX);
    }

    @Override
    public int getColor(ItemStack stack, int index) {
        if (index != this.tintIndex) {
            return -1;
        }
        return calculate(stack);
    }

    public static int calculate(ItemStack stack) {
        var component = stack.get(NTDataComponents.BACTERIA);
        ClientLevel level = Minecraft.getInstance().level;
        if (component == null || level == null) {
            return -1;
        }
        ResourceKey<Bacteria> bacteriaType = component.bacteriaInstance().getBacteria();
        Bacteria bacteria = BacteriaHelper.getBacteria(level.registryAccess(), bacteriaType);
        return bacteria != null ? ARGB.opaque(bacteria.stats().color()) : -1;
    }
}
