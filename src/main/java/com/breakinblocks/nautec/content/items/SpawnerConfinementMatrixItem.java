package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.ConfinedSpawnerBlockEntity;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class SpawnerConfinementMatrixItem extends Item {
    public SpawnerConfinementMatrixItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof ConfinedSpawnerBlockEntity confined) {
            if (player == null || !player.isSecondaryUseActive()) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide()) {
                release(level, pos, confined, player);
            }
            return InteractionResult.SUCCESS;
        }

        if (!(blockEntity instanceof SpawnerBlockEntity)) {
            return InteractionResult.PASS;
        }
        if (player != null && (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand()))) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!confine(level, pos)) {
            return InteractionResult.FAIL;
        }
        if (player == null || !player.hasInfiniteMaterials()) {
            context.getItemInHand().shrink(1);
        }
        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.8F, 1.4F);
        return InteractionResult.SUCCESS;
    }

    public static boolean confine(Level level, BlockPos pos) {
        if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner)) {
            return false;
        }
        BlockState original = level.getBlockState(pos);
        CompoundTag data = spawner.saveWithoutMetadata(level.registryAccess());
        if (!level.setBlock(pos, NTBlocks.CONFINED_SPAWNER.get().defaultBlockState(), Block.UPDATE_ALL)) {
            return false;
        }
        if (!(level.getBlockEntity(pos) instanceof ConfinedSpawnerBlockEntity confined)) {
            return false;
        }
        confined.confine(original, data);
        return true;
    }

    public static boolean release(Level level, BlockPos pos, ConfinedSpawnerBlockEntity confined, @Nullable Player player) {
        if (player != null && !level.mayInteract(player, pos)) {
            return false;
        }
        if (!confined.release()) {
            return false;
        }
        ItemStack matrix = new ItemStack(NTItems.SPAWNER_CONFINEMENT_MATRIX.get());
        if (player == null || !player.getInventory().add(matrix)) {
            Block.popResource(level, pos.above(), matrix);
        }
        level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.8F, 1.2F);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.translatable("nautec.spawner_confinement_matrix.use").withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.translatable("nautec.spawner_confinement_matrix.power",
                NTConfig.confinedSpawnerPowerPerTick).withStyle(ChatFormatting.AQUA));
        tooltipComponents.accept(Component.translatable("nautec.spawner_confinement_matrix.release").withStyle(ChatFormatting.GRAY));
    }
}
