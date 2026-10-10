package com.breakinblocks.nautec.content.conduits;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public abstract class ConduitPartBlock extends Block implements SimpleWaterloggedBlock {
    public static final Direction[] DIRECTIONS = Direction.values();

    private final Function<BlockState, VoxelShape> shapes;

    protected ConduitPartBlock(Properties properties) {
        super(properties);
        this.shapes = getShapeForEachState(this::shapeFor)::get;
    }

    @SuppressWarnings("unchecked")
    protected static <E extends Enum<E> & StringRepresentable> EnumProperty<E>[] arms(Class<E> type) {
        EnumProperty<E>[] arms = new EnumProperty[DIRECTIONS.length];
        for (Direction direction : DIRECTIONS) {
            arms[direction.ordinal()] = EnumProperty.create(direction.getSerializedName(), type);
        }
        return arms;
    }

    protected abstract EnumProperty<?>[] armProperties();

    protected abstract VoxelShape shapeFor(BlockState state);

    protected final BlockState withArms(BlockState state, Enum<?> value) {
        state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        for (EnumProperty<?> arm : armProperties()) {
            state = with(state, arm, value);
        }
        return state;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState with(BlockState state, EnumProperty arm, Enum<?> value) {
        return state.setValue(arm, (Comparable) value);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(armProperties()).add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.apply(state);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return !state.getValue(BlockStateProperties.WATERLOGGED);
    }

    @Override
    protected @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    protected static BlockState waterloggedFor(BlockState state, LevelReader level, BlockPos pos) {
        return state.setValue(BlockStateProperties.WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
    }

    protected static void scheduleWater(BlockState state, LevelReader level, LevelAccessor tickAccess, BlockPos pos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
    }

    private boolean armsDiffer(BlockState a, BlockState b) {
        for (EnumProperty<?> arm : armProperties()) {
            if (a.getValue(arm) != b.getValue(arm)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && (!oldState.is(this) || armsDiffer(oldState, state))) {
            ConduitNetworks.invalidate(level, pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, world, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock()) && world instanceof ServerLevel level) {
            ConduitNetworks.invalidate(level, pos);
        }
    }
}
