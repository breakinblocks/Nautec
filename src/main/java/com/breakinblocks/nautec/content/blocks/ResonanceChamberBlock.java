package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.content.blockentities.ResonanceChamberBlockEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import com.breakinblocks.nautec.utils.ItemUtils;

public class ResonanceChamberBlock extends LaserBlock {
    public ResonanceChamberBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean waterloggable() {
        return false;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(ResonanceChamberBlock::new);
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.RESONANCE_CHAMBER.get();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof ResonanceChamberBlockEntity be)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        var handler = be.getItemStackHandler();
        if (!stack.isEmpty()) {
            if (handler.isItemValid(0, stack)) {
                player.setItemInHand(hand, handler.insertItem(0, stack, false));
                return InteractionResult.SUCCESS;
            }
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        ItemStack result = handler.extractItem(1, handler.getSlotLimit(1), false);
        if (result.isEmpty()) {
            result = handler.extractItem(0, handler.getSlotLimit(0), false);
        }
        if (result.isEmpty()) {
            return InteractionResult.CONSUME;
        }
        ItemUtils.giveItemToPlayer(player, result, player.getInventory().getSelectedSlot());
        return InteractionResult.SUCCESS;
    }

    @Override
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        if (!(level.getBlockEntity(blockPos) instanceof ResonanceChamberBlockEntity be)) {
            return List.of();
        }

        if (be.isVenting()) {
            return List.of(Component.literal("Cracked, cooling down").withStyle(ChatFormatting.RED));
        }

        int percent = Math.round(be.getChargeFraction() * 100f);
        ChatFormatting colour = be.isCritical() ? ChatFormatting.GOLD
                : percent > 100 ? ChatFormatting.RED : ChatFormatting.WHITE;

        return List.of(
                Component.literal("Charge: " + percent + "%").withStyle(colour),
                Component.literal("Ceiling: " + Math.round(be.getStabilityCeiling())).withStyle(ChatFormatting.WHITE),
                Component.literal("Purity: " + String.format("%.2f", be.getPurity())).withStyle(ChatFormatting.WHITE)
        );
    }
}
