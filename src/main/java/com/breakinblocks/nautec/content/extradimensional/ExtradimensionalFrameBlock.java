package com.breakinblocks.nautec.content.extradimensional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ExtradimensionalFrameBlock extends Block {
    public static final EnumProperty<FramePart> PART = EnumProperty.create("part", FramePart.class);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape HALF = Block.box(0, 0, 0, 16, 8, 16);
    private static final VoxelShape RIM = Block.box(0, 0, 0, 16, 7, 16);
    private static final VoxelShape FLOOR = Block.box(0, 0, 0, 16, 2, 16);

    private final int tier;

    public ExtradimensionalFrameBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(defaultBlockState().setValue(PART, FramePart.LOOSE).setValue(FACING, Direction.NORTH));
    }

    public int tier() {
        return tier;
    }

    public int ring() {
        return tier - 1;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(PART)) {
            case INNER_EDGE, INNER_CORNER -> FLOOR;
            case EDGE, CORNER -> RIM;
            case LOOSE, PORT -> HALF;
        };
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !oldState.is(state.getBlock())) {
            ExtradimensionalStorageBlockEntity.requestScans(level, pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        ExtradimensionalStorageBlockEntity.requestScans(level, pos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!state.getValue(PART).formed()) {
            return InteractionResult.PASS;
        }
        ExtradimensionalStorageBlockEntity core = ExtradimensionalStorageBlockEntity.owner(level, pos);
        if (core == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ExtradimensionalStorageBlock.open(core, serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }
}
