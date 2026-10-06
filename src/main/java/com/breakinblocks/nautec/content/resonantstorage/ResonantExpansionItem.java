package com.breakinblocks.nautec.content.resonantstorage;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class ResonantExpansionItem extends Item {
    public ResonantExpansionItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("nautec.resonant_storage.expansion.hint", VaultStore.PAGE, CisternStore.tierCapacity(), ResonantStore.MAX_UPGRADES)
                .withStyle(ChatFormatting.GRAY));
    }
}
