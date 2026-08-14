package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.items.IBacteriaItem;
import com.breakinblocks.nautec.data.NTDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class PetriDishItem extends Item implements IBacteriaItem {
    public PetriDishItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        BacteriaInstance bacteria = stack.get(NTDataComponents.BACTERIA).bacteriaInstance();
        bacteria.getExpandableTooltip(context.registries(), tooltipFlag.hasShiftDown(), tooltipFlag.hasControlDown())
                .forEach(tooltipComponents);
    }
}
