package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.api.blocks.DisplayBlock;
import com.breakinblocks.nautec.api.gateways.GatewayRing;
import com.breakinblocks.nautec.api.utils.HorizontalDirection;
import com.breakinblocks.nautec.content.blockentities.GatewayBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GatewayRingPartBlock extends Block implements SimpleWaterloggedBlock, DisplayBlock {
    public static final IntegerProperty CELL = IntegerProperty.create("cell", 0, GatewayRing.SIZE * GatewayRing.SIZE - 1);
    public static final EnumProperty<HorizontalDirection> ORIENTATION = EnumProperty.create("orientation", HorizontalDirection.class);

    public GatewayRingPartBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(CELL, 0)
                .setValue(ORIENTATION, HorizontalDirection.NORTH)
                .setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CELL, ORIENTATION, BlockStateProperties.WATERLOGGED);
    }

    @Override
    protected @NotNull MapCodec<? extends Block> codec() {
        return simpleCodec(GatewayRingPartBlock::new);
    }

    public static BlockPos corePos(BlockState state, BlockPos pos) {
        return GatewayRing.coreOf(pos, state.getValue(ORIENTATION), state.getValue(CELL));
    }

    public static @Nullable GatewayBlockEntity core(BlockGetter level, BlockState state, BlockPos pos) {
        return level.getBlockEntity(corePos(state, pos)) instanceof GatewayBlockEntity gateway ? gateway : null;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return GatewayBlock.FORMED_DESTROY_PROGRESS;
    }

    @Override
    protected @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull LevelReader level, @NotNull ScheduledTickAccess tickAccess,
                                              @NotNull BlockPos pos, @NotNull Direction direction, @NotNull BlockPos neighborPos,
                                              @NotNull BlockState neighborState, @NotNull RandomSource random) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        GatewayBlockEntity gateway = core(level, state, pos);
        if (gateway != null) {
            gateway.unformRing();
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        GatewayBlockEntity gateway = core(level, state, pos);
        if (gateway == null) {
            return InteractionResult.PASS;
        }
        return GatewayBlock.useOnRing(stack, level, gateway, player, hitResult);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        GatewayBlockEntity gateway = core(level, state, pos);
        if (gateway == null) {
            return InteractionResult.PASS;
        }
        return GatewayBlock.openScreen(player, gateway);
    }

    @Override
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        GatewayBlockEntity gateway = core(level, level.getBlockState(blockPos), blockPos);
        return gateway == null ? List.of() : GatewayBlock.describe(gateway);
    }
}
