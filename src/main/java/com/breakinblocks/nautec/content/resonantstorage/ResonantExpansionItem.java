package com.breakinblocks.nautec.content.resonantstorage;


import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;


public class ResonantExpansionItem extends Item {
    public ResonantExpansionItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("nautec.resonant_storage.expansion.hint", VaultStore.PAGE, CisternStore.tierCapacity(), ResonantStore.MAX_UPGRADES)
                .withStyle(ChatFormatting.GRAY));
    }
}
