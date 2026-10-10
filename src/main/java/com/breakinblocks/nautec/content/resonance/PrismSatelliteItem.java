package com.breakinblocks.nautec.content.resonance;


import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;


public class PrismSatelliteItem extends Item {
    public PrismSatelliteItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("nautec.prism_satellite.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
