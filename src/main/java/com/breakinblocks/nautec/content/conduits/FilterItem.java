package com.breakinblocks.nautec.content.conduits;


import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("nautec.conduit.filter_item.hint").withStyle(ChatFormatting.GRAY));
        if (intricate) {
            tooltip.add(Component.translatable("nautec.conduit.filter_item.intricate").withStyle(ChatFormatting.AQUA));
        }
    }
}
