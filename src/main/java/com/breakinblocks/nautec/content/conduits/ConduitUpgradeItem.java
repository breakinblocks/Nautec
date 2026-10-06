package com.breakinblocks.nautec.content.conduits;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

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
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        TapRates.of(tier).describe(line -> tooltip.accept(line.copy().withStyle(ChatFormatting.AQUA)));
        tooltip.accept(Component.translatable("nautec.conduit.upgrade.hint").withStyle(ChatFormatting.GRAY));
    }
}
