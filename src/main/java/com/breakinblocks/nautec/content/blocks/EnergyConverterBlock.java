package com.breakinblocks.nautec.content.blocks;

import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blocks.DisplayBlock;
import com.breakinblocks.nautec.content.blockentities.EnergyConverterBlockEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.Nullable;

public class EnergyConverterBlock extends Block implements EntityBlock, DisplayBlock {
    public EnergyConverterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyConverterBlockEntity(pos, state);
    }

    @Override
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        if (!(level.getBlockEntity(blockPos) instanceof EnergyConverterBlockEntity converter)) {
            return List.of();
        }
        return lines(converter.getSending(), converter.getBeams(), converter.getRate());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof EnergyConverterBlockEntity converter) {
            player.openMenu(converter, pos);
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    public static List<Component> lines(int sending, int beams, int rate) {
        if (beams <= 0) {
            return List.of(Component.translatable("nautec.energy_converter.no_target").withStyle(ChatFormatting.RED));
        }
        if (rate <= 0) {
            return List.of(Component.translatable("nautec.energy_converter.idle").withStyle(ChatFormatting.RED));
        }
        if (sending <= 0) {
            return List.of(Component.translatable("nautec.energy_converter.no_fe").withStyle(ChatFormatting.RED));
        }
        return List.of(
                Component.translatable("nautec.energy_converter.sending", sending).withStyle(ChatFormatting.AQUA),
                Component.translatable("nautec.energy_converter.beams", beams, sending / beams).withStyle(ChatFormatting.WHITE),
                Component.translatable("nautec.energy_converter.cost", sending * NTConfig.energyConverterFePerAp).withStyle(ChatFormatting.GRAY)
        );
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (blockEntityType != NTBlockEntityTypes.ENERGY_CONVERTER.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) -> ((EnergyConverterBlockEntity) blockEntity).commonTick();
    }
}
