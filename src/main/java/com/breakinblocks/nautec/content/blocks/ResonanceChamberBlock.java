package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.content.blockentities.ResonanceChamberBlockEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.utils.ItemUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

        if (stack.isEmpty()) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }
        // Recipe validation is server-only; acknowledge the click without changing client inventory.
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        var handler = be.getItemStackHandler();
        if (handler.isItemValid(ResonanceChamberBlockEntity.INPUT_SLOT, stack)) {
            ItemStack remainder = handler.insertItem(ResonanceChamberBlockEntity.INPUT_SLOT, stack, false);
            if (remainder.getCount() < stack.getCount()) {
                player.setItemInHand(hand, remainder);
                return InteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(pos) instanceof ResonanceChamberBlockEntity be)) {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        var handler = be.getItemStackHandler();
        ItemStack result = handler.extractItem(ResonanceChamberBlockEntity.OUTPUT_SLOT, 64, false);
        if (result.isEmpty()) {
            result = handler.extractItem(ResonanceChamberBlockEntity.INPUT_SLOT, 64, false);
        }
        if (result.isEmpty()) {
            return InteractionResult.PASS;
        }
        ItemUtils.giveItemToPlayer(player, result, player.getInventory().getSelectedSlot());
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof ResonanceChamberBlockEntity be)) {
            return 0;
        }
        var handler = be.getItemStackHandler();
        float fullness = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack item = handler.getStackInSlot(slot);
            if (!item.isEmpty()) {
                fullness += (float) item.getCount() / Math.min(handler.getSlotLimit(slot), item.getMaxStackSize());
            }
        }
        return fullness == 0 ? 0 : 1 + (int) (14 * fullness / handler.getSlots());
    }

    @Override
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        if (!(level.getBlockEntity(blockPos) instanceof ResonanceChamberBlockEntity be)) {
            return List.of();
        }

        if (be.isVenting()) {
            return List.of(Component.translatable("nautec.monocle.cracked").withStyle(ChatFormatting.RED));
        }

        int percent = Math.round(be.getChargeFraction() * 100f);
        ChatFormatting colour = be.isCritical() ? ChatFormatting.GOLD
                : percent > 100 ? ChatFormatting.RED : ChatFormatting.WHITE;

        return List.of(
                Component.translatable("nautec.monocle.charge", percent).withStyle(colour),
                Component.translatable("nautec.monocle.ceiling", Math.round(be.getStabilityCeiling())).withStyle(ChatFormatting.WHITE),
                Component.translatable("nautec.monocle.purity", String.format("%.2f", be.getPurity())).withStyle(ChatFormatting.WHITE)
        );
    }
}
