package com.breakinblocks.nautec.content.enchanter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class EnchanterStructure {
    public static final int CELLS = 18;
    public static final int CORE_CELL = 4;
    private static final VoxelShape[] OUTLINES = new VoxelShape[CELLS];

    static {
        for (int cell = 0; cell < CELLS; cell++) {
            OUTLINES[cell] = Shapes.box(-1 - dx(cell), -dy(cell), -1 - dz(cell), 2 - dx(cell), 2 - dy(cell), 2 - dz(cell));
        }
    }

    private EnchanterStructure() {
    }

    public static int dx(int cell) {
        return cell % 3 - 1;
    }

    public static int dz(int cell) {
        return cell / 3 % 3 - 1;
    }

    public static int dy(int cell) {
        return cell / 9;
    }

    public static BlockPos cellPos(BlockPos core, int cell) {
        return core.offset(dx(cell), dy(cell), dz(cell));
    }

    public static BlockPos coreOf(BlockPos part, int cell) {
        return part.offset(-dx(cell), -dy(cell), -dz(cell));
    }

    public static boolean isOutward(int cell, Direction direction) {
        return switch (direction) {
            case DOWN -> dy(cell) == 0;
            case UP -> dy(cell) == 1;
            case WEST -> dx(cell) == -1;
            case EAST -> dx(cell) == 1;
            case NORTH -> dz(cell) == -1;
            case SOUTH -> dz(cell) == 1;
        };
    }

    public static boolean hasRoom(Level level, BlockPos core) {
        for (int cell = 0; cell < CELLS; cell++) {
            BlockPos pos = cellPos(core, cell);
            if (level.isOutsideBuildHeight(pos) || !level.getBlockState(pos).canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    public static VoxelShape outline(int cell) {
        return OUTLINES[cell];
    }

    public static BlockState flood(BlockState state, Direction direction, BlockState neighborState) {
        if (!state.getValue(BlockStateProperties.WATERLOGGED) && direction != Direction.DOWN
                && neighborState.getFluidState().isSourceOfType(Fluids.WATER)) {
            return state.setValue(BlockStateProperties.WATERLOGGED, true);
        }
        return state;
    }

    public static boolean water(Level level, BlockPos pos) {
        return level.getFluidState(pos).getType() == Fluids.WATER;
    }

    public static void build(Level level, BlockPos core, Block part) {
        for (int cell = 0; cell < CELLS; cell++) {
            if (cell == CORE_CELL) {
                continue;
            }
            BlockPos pos = cellPos(core, cell);
            BlockState state = part.defaultBlockState()
                    .setValue(AbyssalEnchanterPartBlock.CELL, cell)
                    .setValue(BlockStateProperties.WATERLOGGED, water(level, pos));
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
    }

    public static void clear(Level level, BlockPos core) {
        for (int cell = 0; cell < CELLS; cell++) {
            if (cell == CORE_CELL) {
                continue;
            }
            BlockPos pos = cellPos(core, cell);
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof AbyssalEnchanterPartBlock && state.getValue(AbyssalEnchanterPartBlock.CELL) == cell) {
                level.setBlock(pos, state.getFluidState().createLegacyBlock(), Block.UPDATE_ALL);
            }
        }
    }
}
