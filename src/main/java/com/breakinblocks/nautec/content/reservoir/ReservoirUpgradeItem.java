package com.breakinblocks.nautec.content.reservoir;

import com.breakinblocks.nautec.utils.Tooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ReservoirUpgradeItem extends Item {
    private final int tier;

    public ReservoirUpgradeItem(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    public int tier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        Tooltips.tt(tooltipComponents, Component.translatable("nautec.reservoir_upgrade.tooltip", ReservoirText.numeral(tier - 1),
                ReservoirText.numeral(tier), ReservoirText.buckets(DeepSeaReservoirBlockEntity.capacityFor(tier))), ChatFormatting.GRAY);
        Tooltips.trans(tooltipComponents, "nautec.reservoir_upgrade.hint", ChatFormatting.DARK_GRAY);
    }
}
