package com.breakinblocks.nautec.content.enchanter;

import com.breakinblocks.nautec.utils.Tooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class AbyssalEnchanterItem extends BlockItem {
    public AbyssalEnchanterItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (!EnchanterStructure.hasRoom(context.getLevel(), context.getClickedPos())) {
            Player player = context.getPlayer();
            if (player != null && !context.getLevel().isClientSide()) {
                player.displayClientMessage(Component.translatable("nautec.abyssal_enchanter.no_room").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }
        return super.place(context);
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        return EnchanterStructure.hasRoom(context.getLevel(), context.getClickedPos()) && super.canPlace(context, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        Tooltips.trans(tooltipComponents, "nautec.abyssal_enchanter.tooltip", ChatFormatting.GRAY);
        Tooltips.trans(tooltipComponents, "nautec.abyssal_enchanter.size_hint", ChatFormatting.DARK_GRAY);
    }
}
