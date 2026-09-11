package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.content.blockentities.PressureForgeBlockEntity;
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
import com.breakinblocks.nautec.utils.ItemUtils;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

import java.util.List;

public class PressureForgeBlock extends LaserBlock {
    public PressureForgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean waterloggable() {
        return false;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(PressureForgeBlock::new);
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.PRESSURE_FORGE.get();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof PressureForgeBlockEntity forge)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (FluidUtil.interactWithFluidHandler(player, hand, pos, forge.getFluidTank(), null)) {
            return InteractionResult.SUCCESS;
        }
        return ItemUtils.insertHeldItem(forge.getItemHandler(), 0, stack, player, hand);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof PressureForgeBlockEntity forge)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        int slot = forge.getItemStackHandler().getStackInSlot(1).isEmpty() ? 0 : 1;
        return ItemUtils.extractItemToPlayer(forge.getItemHandler(), slot, player);
    }

    @Override
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        if (!(level.getBlockEntity(blockPos) instanceof PressureForgeBlockEntity forge)) {
            return List.of();
        }

        if (!forge.isPressurised()) {
            return List.of(Component.translatable("nautec.monocle.not_pressurised", NTConfig.pressureForgeDepth, NTConfig.pressureForgeWaterColumn)
                    .withStyle(ChatFormatting.RED));
        }

        return List.of(
                Component.translatable("nautec.monocle.pressurised").withStyle(ChatFormatting.AQUA),
                Component.translatable("nautec.monocle.acid", forge.getFluidTank().getFluidAmount()).withStyle(ChatFormatting.WHITE),
                Component.translatable("nautec.monocle.purity", String.format("%.2f", forge.getPurity())).withStyle(ChatFormatting.WHITE)
        );
    }
}
