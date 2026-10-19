package com.breakinblocks.nautec.content.enchanter;

import com.breakinblocks.nautec.registries.NTBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AbyssalEnchanterPartBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final IntegerProperty CELL = IntegerProperty.create("cell", 0, EnchanterStructure.CELLS - 1);

    public AbyssalEnchanterPartBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(CELL, 0).setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(AbyssalEnchanterPartBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CELL, BlockStateProperties.WATERLOGGED);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AbyssalEnchanterPartBlockEntity(pos, state);
    }

    public static BlockPos corePos(BlockState state, BlockPos pos) {
        return EnchanterStructure.coreOf(pos, state.getValue(CELL));
    }

    public static @Nullable AbyssalEnchanterBlockEntity core(BlockGetter level, BlockState state, BlockPos pos) {
        return level.getBlockEntity(corePos(state, pos)) instanceof AbyssalEnchanterBlockEntity enchanter ? enchanter : null;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return EnchanterStructure.outline(state.getValue(CELL));
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos,
                                              BlockPos neighborPos) {
        BlockState flooded = EnchanterStructure.flood(state, direction, neighborState);
        if (flooded.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(flooded, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel) {
            BlockPos core = corePos(state, pos);
            if (level.getBlockState(core).is(NTBlocks.ABYSSAL_ENCHANTER.get())) {
                level.destroyBlock(core, true);
            }
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        BlockPos core = corePos(state, pos);
        BlockState coreState = level.getBlockState(core);
        if (!coreState.is(NTBlocks.ABYSSAL_ENCHANTER.get())) {
            return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        }
        BlockEntity coreEntity = level.getBlockEntity(core);
        level.setBlock(core, coreState.getFluidState().createLegacyBlock(), Block.UPDATE_ALL);
        if (willHarvest && !player.getAbilities().instabuild) {
            Block.dropResources(coreState, level, pos, coreEntity, player, player.getMainHandItem());
        }
        return true;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return AbyssalEnchanterBlock.open(level, corePos(state, pos), player);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                                       BlockHitResult hitResult) {
        return AbyssalEnchanterBlock.useOn(stack, level, corePos(state, pos), player, hand);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(NTBlocks.ABYSSAL_ENCHANTER.get());
    }
}
