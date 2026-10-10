package com.breakinblocks.nautec.content.items;


import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.function.Consumer;

public class CrystalSeedItem extends Item {
    private final boolean awakened;

    public CrystalSeedItem(Properties properties, boolean awakened) {
        super(properties);
        this.awakened = awakened;
    }

    public boolean isAwakened() {
        return awakened;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return awakened || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        String key = awakened ? "nautec.crystal_seed.awakened.tooltip" : "nautec.crystal_seed.dormant.tooltip";
        tooltipComponents.add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
    }
}
