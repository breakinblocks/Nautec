package com.breakinblocks.nautec.content.reservoir;

import com.breakinblocks.nautec.registries.NTBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeepSeaReservoirPartBlock extends Block implements SimpleWaterloggedBlock {
    public static final IntegerProperty CELL = IntegerProperty.create("cell", 0, ReservoirStructure.CELLS - 1);

    public DeepSeaReservoirPartBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(CELL, 0).setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    protected @NotNull MapCodec<? extends Block> codec() {
        return simpleCodec(DeepSeaReservoirPartBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CELL, BlockStateProperties.WATERLOGGED);
    }

    public static BlockPos corePos(BlockState state, BlockPos pos) {
        return ReservoirStructure.coreOf(pos, state.getValue(CELL));
    }

    public static @Nullable DeepSeaReservoirBlockEntity core(BlockGetter level, BlockState state, BlockPos pos) {
        return level.getBlockEntity(corePos(state, pos)) instanceof DeepSeaReservoirBlockEntity reservoir ? reservoir : null;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
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
        BlockPos core = corePos(state, pos);
        if (level.getBlockState(core).is(NTBlocks.DEEP_SEA_RESERVOIR.get())) {
            level.destroyBlock(core, true);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack tool, boolean willHarvest, FluidState fluid) {
        BlockPos core = corePos(state, pos);
        BlockState coreState = level.getBlockState(core);
        if (!coreState.is(NTBlocks.DEEP_SEA_RESERVOIR.get())) {
            return super.onDestroyedByPlayer(state, level, pos, player, tool, willHarvest, fluid);
        }
        BlockEntity coreEntity = level.getBlockEntity(core);
        level.setBlock(core, coreState.getFluidState().createLegacyBlock(), Block.UPDATE_ALL);
        if (willHarvest && !player.preventsBlockDrops()) {
            Block.dropResources(coreState, level, pos, coreEntity, player, tool);
        }
        return true;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return DeepSeaReservoirBlock.open(level, corePos(state, pos), player);
    }

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                                   BlockHitResult hitResult) {
        return DeepSeaReservoirBlock.useOn(stack, level, corePos(state, pos), player, hand);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(NTBlocks.DEEP_SEA_RESERVOIR.get());
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        DeepSeaReservoirBlockEntity reservoir = core(level, state, pos);
        return reservoir == null ? 0 : reservoir.comparatorSignal();
    }
}
