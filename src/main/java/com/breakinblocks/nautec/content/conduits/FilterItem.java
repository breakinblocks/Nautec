package com.breakinblocks.nautec.content.conduits;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class FilterItem extends Item {
    private final boolean intricate;

    public FilterItem(Properties properties, boolean intricate) {
        super(properties);
        this.intricate = intricate;
    }

    public boolean intricate() {
        return intricate;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("nautec.conduit.filter_item.hint").withStyle(ChatFormatting.GRAY));
        if (intricate) {
            tooltip.accept(Component.translatable("nautec.conduit.filter_item.intricate").withStyle(ChatFormatting.AQUA));
        }
    }
}
