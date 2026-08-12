package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public abstract class OpticsBlock extends LaserBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    private static final VoxelShape SHAPE_NS = Block.box(2, 2, 4, 14, 14, 12);
    private static final VoxelShape SHAPE_EW = Block.box(4, 2, 2, 12, 14, 14);
    private static final VoxelShape SHAPE_UD = Block.box(2, 4, 2, 14, 12, 14);

    protected OpticsBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public boolean waterloggable() {
        return true;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST, WEST -> SHAPE_EW;
            case DOWN, UP -> SHAPE_UD;
            case NORTH, SOUTH -> SHAPE_NS;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state != null ? state.setValue(FACING, context.getNearestLookingDirection()) : null;
    }
}
