package com.breakinblocks.nautec.content.resonantstorage;

import com.breakinblocks.nautec.data.NTDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class ResonantStorageItem extends BlockItem {
    public ResonantStorageItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        ResonantLink link = stack.get(NTDataComponents.RESONANT_LINK.get());
        if (link == null) {
            tooltip.accept(Component.translatable("nautec.resonant_storage.unlinked").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.accept(describe(link.channel().access(), link.ownerName()).withStyle(ChatFormatting.GRAY));
        tooltip.accept(link.channel().address().describe());
    }

    public static MutableComponent describe(ChannelAccess access, String ownerName) {
        Component mode = Component.translatable(access.translationKey());
        return ownerName.isEmpty() || access == ChannelAccess.PUBLIC
                ? Component.translatable("nautec.resonant_storage.channel", mode)
                : Component.translatable("nautec.resonant_storage.channel_owned", mode, ownerName);
    }
}
