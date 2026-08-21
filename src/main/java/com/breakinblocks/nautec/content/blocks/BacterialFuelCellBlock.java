package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.content.blockentities.BacterialFuelCellBlockEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BacterialFuelCellBlock extends LaserBlock {
    public BacterialFuelCellBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH));
    }

    @Override
    public boolean waterloggable() {
        return false;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(BacterialFuelCellBlock::new);
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.BACTERIAL_FUEL_CELL.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(BlockStateProperties.FACING));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state != null ? state.setValue(BlockStateProperties.FACING, context.getNearestLookingDirection().getOpposite()) : null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        IBacteriaStorage dish = stack.getCapability(NTCapabilities.BacteriaStorage.ITEM);
        if (dish == null || stack.getCount() != 1 || !(level.getBlockEntity(pos) instanceof BacterialFuelCellBlockEntity be)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        IBacteriaStorage cell = be.getBacteriaStorage();
        BacteriaInstance held = dish.getBacteria(0);

        if (held.isEmpty()) {
            BacteriaInstance taken = cell.extractBacteria(0, Long.MAX_VALUE, false);
            if (taken.isEmpty()) {
                return InteractionResult.CONSUME;
            }
            dish.setBacteria(0, taken);
            return InteractionResult.SUCCESS;
        }

        BacteriaInstance remainder = cell.insertBacteria(0, held, false);
        if (remainder.getSize() == held.getSize()) {
            return InteractionResult.CONSUME;
        }
        dish.setBacteria(0, remainder);
        return InteractionResult.SUCCESS;
    }

    @Override
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        if (!(level.getBlockEntity(blockPos) instanceof BacterialFuelCellBlockEntity be)) {
            return List.of();
        }

        BacteriaInstance bacteria = be.getBacteriaStorage().getBacteria(0);
        if (bacteria.isEmpty()) {
            return List.of(Component.translatable("nautec.monocle.no_colony").withStyle(ChatFormatting.GRAY));
        }

        return List.of(
                Component.translatable("nautec.monocle.output", BacterialFuelCellBlockEntity.powerOutput(bacteria)).withStyle(ChatFormatting.WHITE),
                Component.translatable("nautec.monocle.purity", String.format("%.2f", BacterialFuelCellBlockEntity.purityOutput(bacteria))).withStyle(ChatFormatting.WHITE),
                Component.translatable("nautec.monocle.fuel", bacteria.getSize()).withStyle(ChatFormatting.WHITE)
        );
    }
}
