package com.breakinblocks.nautec.content.items;


import java.util.List;
import com.breakinblocks.nautec.NTConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.function.Consumer;

public class GraftingAnchorItem extends Item {
    private final boolean advanced;

    public GraftingAnchorItem(Properties properties) {
        this(properties, false);
    }

    public GraftingAnchorItem(Properties properties, boolean advanced) {
        super(properties);
        this.advanced = advanced;
    }

    public boolean isAdvanced() {
        return advanced;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("nautec.grafting_anchor.effect").withStyle(ChatFormatting.AQUA));
        if (advanced) {
            tooltipComponents.add(Component.translatable("nautec.advanced_grafting_anchor.effect",
                    String.format("%.0f", (NTConfig.advancedGraftingAnchorSpeed - 1) * 100),
                    String.format("%.0f", NTConfig.advancedGraftingAnchorPowerMultiplier)).withStyle(ChatFormatting.GOLD));
        }
    }
}
