package com.breakinblocks.nautec.content.reservoir;

import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.utils.Tooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class DeepSeaReservoirItem extends BlockItem {
    public DeepSeaReservoirItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static int tierOf(ItemStack stack) {
        Integer tier = stack.get(NTDataComponents.RESERVOIR_TIER.get());
        return tier == null ? 1 : Math.clamp(tier, 1, ReservoirFluid.MAX_TIER);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        BlockPos core = context.getClickedPos().above();
        if (!ReservoirStructure.hasRoom(context.getLevel(), core)) {
            Player player = context.getPlayer();
            if (player != null && !context.getLevel().isClientSide()) {
                player.sendOverlayMessage(Component.translatable("nautec.deep_sea_reservoir.no_room").withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }
        return super.place(BlockPlaceContext.at(context, core, context.getHorizontalDirection()));
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        return ReservoirStructure.hasRoom(context.getLevel(), context.getClickedPos()) && super.canPlace(context, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        int tier = tierOf(stack);
        long capacity = DeepSeaReservoirBlockEntity.capacityFor(tier);
        Tooltips.tt(tooltipComponents, Component.translatable("nautec.deep_sea_reservoir.tier", ReservoirText.numeral(tier),
                ReservoirText.buckets(capacity)), ChatFormatting.AQUA);
        ReservoirFluid fluid = stack.get(NTDataComponents.RESERVOIR_FLUID.get());
        if (fluid != null && !fluid.isEmpty()) {
            Tooltips.tt(tooltipComponents, Component.translatable("nautec.deep_sea_reservoir.holds", fluid.resource().getHoverName(),
                    ReservoirText.buckets(fluid.amount())), ChatFormatting.GRAY);
        }
        Tooltips.trans(tooltipComponents, "nautec.deep_sea_reservoir.size_hint", ChatFormatting.DARK_GRAY);
    }
}
