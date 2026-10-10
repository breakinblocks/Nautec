package com.breakinblocks.nautec.content.resonantstorage;



import net.minecraft.world.item.Item;
import java.util.List;
import com.breakinblocks.nautec.data.NTDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;


public class ResonantStorageItem extends BlockItem {
    public ResonantStorageItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ResonantLink link = stack.get(NTDataComponents.RESONANT_LINK.get());
        if (link == null) {
            tooltip.add(Component.translatable("nautec.resonant_storage.unlinked").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(describe(link.channel().access(), link.ownerName()).withStyle(ChatFormatting.GRAY));
        tooltip.add(link.channel().address().describe());
    }

    public static MutableComponent describe(ChannelAccess access, String ownerName) {
        Component mode = Component.translatable(access.translationKey());
        return ownerName.isEmpty() || access == ChannelAccess.PUBLIC
                ? Component.translatable("nautec.resonant_storage.channel", mode)
                : Component.translatable("nautec.resonant_storage.channel_owned", mode, ownerName);
    }
}
