package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.api.blocks.DisplayBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class SatelliteArrayTopBlock extends Block implements SimpleWaterloggedBlock, DisplayBlock {
    private static final VoxelShape SHAPE = Shapes.or(Block.box(6, 0, 6, 10, 8, 10), Block.box(0, 8, 0, 16, 12, 16));

    public SatelliteArrayTopBlock(Properties properties) {
        super(properties.pushReaction(PushReaction.BLOCK));
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(BlockStateProperties.WATERLOGGED));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction,
                                     BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (direction == Direction.DOWN && !(neighborState.getBlock() instanceof SatelliteArrayBlock)) {
            return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.defaultFluidState().createLegacyBlock() : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos below = pos.below();
        if (!level.isClientSide() && level.getBlockState(below).getBlock() instanceof SatelliteArrayBlock) {
            level.destroyBlock(below, !player.isCreative(), player);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockPos below = pos.below();
        if (stack.getItem() instanceof PrismSatelliteItem && level.getBlockState(below).getBlock() instanceof SatelliteArrayBlock) {
            return SatelliteArrayBlock.launch(stack, level, below, player);
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return SatelliteArrayBlock.open(level, pos.below(), player);
    }

    @Override
    public boolean display(Level level, BlockPos blockPos, Player player) {
        BlockPos below = blockPos.below();
        return level.getBlockState(below).getBlock() instanceof SatelliteArrayBlock base && base.display(level, below, player);
    }

    @Override
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        BlockPos below = blockPos.below();
        return level.getBlockState(below).getBlock() instanceof SatelliteArrayBlock base ? base.displayText(level, below, player) : List.of();
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        BlockState below = level.getBlockState(pos.below());
        return below.getBlock() instanceof SatelliteArrayBlock ? new ItemStack(below.getBlock()) : ItemStack.EMPTY;
    }
}
