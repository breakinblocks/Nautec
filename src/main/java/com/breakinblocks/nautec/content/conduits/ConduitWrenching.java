package com.breakinblocks.nautec.content.conduits;

import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public final class ConduitWrenching {
    private ConduitWrenching() {
    }

    public static @Nullable InteractionResult use(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        boolean sneaking = player != null && player.isSecondaryUseActive();
        if (state.getBlock() instanceof CurrentConduitBlock) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            if (sneaking) {
                return convert(level, pos, state, player) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
            }
            Direction arm = ConduitShapes.pick(pos, context.getClickLocation(), context.getClickedFace(), ConduitShapes.CONDUIT_MIN, ConduitShapes.CONDUIT_MAX);
            CurrentConduitBlock.toggleArm(level, pos, state, arm);
            return InteractionResult.SUCCESS;
        }
        if (state.getBlock() instanceof ConduitTapBlock && level.getBlockEntity(pos) instanceof ConduitTapBlockEntity tap) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            if (sneaking) {
                revert(level, pos, state, tap, player);
                return InteractionResult.SUCCESS;
            }
            Direction face = ConduitShapes.pick(pos, context.getClickLocation(), context.getClickedFace(), ConduitShapes.TAP_MIN, ConduitShapes.TAP_MAX);
            TapFace config = tap.face(face);
            config.setDisabled(!config.disabled());
            tap.configChanged(face);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 0.6F, config.disabled() ? 0.8F : 1.4F);
            if (player != null) {
                player.displayClientMessage(Component.translatable(config.disabled() ? "nautec.conduit.face.disabled" : "nautec.conduit.face.enabled",
                        Component.translatable("nautec.conduit.face." + face.getSerializedName())).withStyle(ChatFormatting.AQUA), true);
            }
            return InteractionResult.SUCCESS;
        }
        return null;
    }

    public static boolean convert(Level level, BlockPos pos, BlockState state, @Nullable Player player) {
        BlockState tapState = NTBlocks.CONDUIT_TAP.get().defaultBlockState()
                .setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
        level.setBlock(pos, tapState, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof ConduitTapBlockEntity tap) {
            tap.setPowered(level.hasNeighborSignal(pos));
            tap.refreshArms();
        }
        level.playSound(null, pos, SoundEvents.COPPER_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
        return true;
    }

    public static void revert(Level level, BlockPos pos, BlockState state, ConduitTapBlockEntity tap, @Nullable Player player) {
        for (int slot = 0; slot < ConduitTapBlockEntity.SLOTS; slot++) {
            ItemStack stack = tap.getItemStackHandler().getStackInSlot(slot);
            if (!stack.isEmpty()) {
                give(player, level, pos, stack.copy());
                tap.getItemStackHandler().setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        BlockState conduit = NTBlocks.CURRENT_CONDUIT.get().defaultBlockState()
                .setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
        level.setBlock(pos, CurrentConduitBlock.refresh(level, pos, conduit), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 1.0F, 1.2F);
    }

    private static void give(@Nullable Player player, Level level, BlockPos pos, ItemStack stack) {
        if (player != null && player.getInventory().add(stack)) {
            return;
        }
        Block.popResource(level, pos, stack);
    }
}
