package com.breakinblocks.nautec.content.reservoir;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

public final class ReservoirStructure {
    public static final int CELLS = 27;
    public static final int CORE_CELL = 13;

    private ReservoirStructure() {
    }

    public static int dx(int cell) {
        return cell % 3 - 1;
    }

    public static int dy(int cell) {
        return cell / 3 % 3 - 1;
    }

    public static int dz(int cell) {
        return cell / 9 - 1;
    }

    public static BlockPos cellPos(BlockPos core, int cell) {
        return core.offset(dx(cell), dy(cell), dz(cell));
    }

    public static BlockPos coreOf(BlockPos part, int cell) {
        return part.offset(-dx(cell), -dy(cell), -dz(cell));
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
                    .setValue(DeepSeaReservoirPartBlock.CELL, cell)
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
            if (state.getBlock() instanceof DeepSeaReservoirPartBlock && state.getValue(DeepSeaReservoirPartBlock.CELL) == cell) {
                level.setBlock(pos, state.getFluidState().createLegacyBlock(), Block.UPDATE_ALL);
            }
        }
    }
}
