package com.breakinblocks.nautec.content.conduits;


import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ConduitUpgradeItem extends Item {
    private final int tier;

    public ConduitUpgradeItem(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    public int tier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        TapRates.of(tier).describe(line -> tooltip.add(line.copy().withStyle(ChatFormatting.AQUA)));
        tooltip.add(Component.translatable("nautec.conduit.upgrade.hint").withStyle(ChatFormatting.GRAY));
    }
}
