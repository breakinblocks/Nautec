package com.breakinblocks.nautec.content.items;


import java.util.List;
import com.breakinblocks.nautec.content.blockentities.PressureForgeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.function.Consumer;

public class PressureSynthesizerItem extends Item {
    private final PressureForgeBlockEntity.Synthesizer tier;

    public PressureSynthesizerItem(Properties properties, PressureForgeBlockEntity.Synthesizer tier) {
        super(properties);
        this.tier = tier;
    }

    public PressureForgeBlockEntity.Synthesizer tier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("nautec.pressure_synthesizer.tooltip." + tier.getSerializedName()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("nautec.pressure_synthesizer.tooltip.fit").withStyle(ChatFormatting.DARK_GRAY));
    }
}
