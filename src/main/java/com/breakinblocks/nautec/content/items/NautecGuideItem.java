package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.compat.guideme.GuideMeCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.util.function.Consumer;

public class NautecGuideItem extends Item {
    public NautecGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            if (ModList.get().isLoaded("guideme")) {
                GuideMeCompat.openGuide(player);
            } else {
                player.sendOverlayMessage(Component.translatable("nautec_guide.missing_guideme").withStyle(ChatFormatting.RED));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("nautec_guide.desc.0").withStyle(ChatFormatting.GRAY));
    }
}
