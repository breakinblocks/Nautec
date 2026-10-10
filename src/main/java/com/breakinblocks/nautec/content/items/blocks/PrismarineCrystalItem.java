package com.breakinblocks.nautec.content.items.blocks;



import net.minecraft.world.item.Item;
import java.util.List;
import com.breakinblocks.nautec.data.NTDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class PrismarineCrystalItem extends BlockItem {
    public PrismarineCrystalItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static boolean isCultivated(ItemStack stack) {
        return Boolean.TRUE.equals(stack.get(NTDataComponents.CULTIVATED));
    }

    @Override
    public Component getName(ItemStack stack) {
        return isCultivated(stack) ? Component.translatable("nautec.cultivated_crystal.name") : super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (isCultivated(stack)) {
            tooltipComponents.add(Component.translatable("nautec.cultivated_crystal.tooltip.place").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("nautec.cultivated_crystal.tooltip.move").withStyle(ChatFormatting.AQUA));
        }
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        return super.place(BlockPlaceContext.at(context, context.getClickedPos().above(3), context.getNearestLookingDirection()));
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        BlockPos firstPos = context.getClickedPos().above(2);
        for (int i = 0; i < 6; i++) {
            BlockPos curPos = firstPos.below(i);
            if (context.getLevel().isOutsideBuildHeight(curPos) || !context.getLevel().getBlockState(curPos).canBeReplaced()) {
                return false;
            }
        }
        return super.canPlace(context, state);
    }
}
