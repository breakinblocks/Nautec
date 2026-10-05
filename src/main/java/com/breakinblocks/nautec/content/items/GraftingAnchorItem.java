package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.NTConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

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
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.translatable("nautec.grafting_anchor.effect").withStyle(ChatFormatting.AQUA));
        if (advanced) {
            tooltipComponents.accept(Component.translatable("nautec.advanced_grafting_anchor.effect",
                    String.format("%.0f", (NTConfig.advancedGraftingAnchorSpeed - 1) * 100),
                    String.format("%.0f", NTConfig.advancedGraftingAnchorPowerMultiplier)).withStyle(ChatFormatting.GOLD));
        }
    }
}
