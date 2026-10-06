package com.breakinblocks.nautec.content.conduits;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ConduitShapes {
    public static final double CONDUIT_MIN = 5;
    public static final double CONDUIT_MAX = 11;
    public static final double TAP_MIN = 4;
    public static final double TAP_MAX = 12;

    private static final VoxelShape CONDUIT_CORE = Block.box(CONDUIT_MIN, CONDUIT_MIN, CONDUIT_MIN, CONDUIT_MAX, CONDUIT_MAX, CONDUIT_MAX);
    private static final VoxelShape TAP_CORE = Block.box(TAP_MIN, TAP_MIN, TAP_MIN, TAP_MAX, TAP_MAX, TAP_MAX);
    private static final VoxelShape[] ARMS = new VoxelShape[6];
    private static final VoxelShape[] FLANGES = new VoxelShape[6];
    private static final VoxelShape[] CONDUIT_SHAPES = new VoxelShape[64];
    private static final VoxelShape[] TAP_SHAPES = new VoxelShape[64 * 64];

    static {
        for (Direction direction : ConduitPartBlock.DIRECTIONS) {
            ARMS[direction.ordinal()] = arm(direction, CONDUIT_MIN, CONDUIT_MAX, 0, CONDUIT_MIN);
            FLANGES[direction.ordinal()] = Shapes.or(arm(direction, 3, 13, 0, 2), arm(direction, CONDUIT_MIN, CONDUIT_MAX, 2, TAP_MIN));
        }
    }

    private ConduitShapes() {
    }

    private static VoxelShape arm(Direction direction, double low, double high, double near, double far) {
        return switch (direction) {
            case DOWN -> Block.box(low, near, low, high, far, high);
            case UP -> Block.box(low, 16 - far, low, high, 16 - near, high);
            case NORTH -> Block.box(low, low, near, high, high, far);
            case SOUTH -> Block.box(low, low, 16 - far, high, high, 16 - near);
            case WEST -> Block.box(near, low, low, far, high, high);
            case EAST -> Block.box(16 - far, low, low, 16 - near, high, high);
        };
    }

    public static VoxelShape conduit(int arms) {
        VoxelShape shape = CONDUIT_SHAPES[arms];
        if (shape == null) {
            shape = CONDUIT_CORE;
            for (Direction direction : ConduitPartBlock.DIRECTIONS) {
                if ((arms & (1 << direction.ordinal())) != 0) {
                    shape = Shapes.or(shape, ARMS[direction.ordinal()]);
                }
            }
            CONDUIT_SHAPES[arms] = shape;
        }
        return shape;
    }

    public static VoxelShape tap(int conduits, int machines) {
        int index = conduits * 64 + machines;
        VoxelShape shape = TAP_SHAPES[index];
        if (shape == null) {
            shape = TAP_CORE;
            for (Direction direction : ConduitPartBlock.DIRECTIONS) {
                int bit = 1 << direction.ordinal();
                if ((machines & bit) != 0) {
                    shape = Shapes.or(shape, FLANGES[direction.ordinal()]);
                } else if ((conduits & bit) != 0) {
                    shape = Shapes.or(shape, ARMS[direction.ordinal()]);
                }
            }
            TAP_SHAPES[index] = shape;
        }
        return shape;
    }

    public static Direction pick(BlockPos pos, Vec3 hit, Direction clicked, double coreMin, double coreMax) {
        double x = (hit.x - pos.getX()) * 16;
        double y = (hit.y - pos.getY()) * 16;
        double z = (hit.z - pos.getZ()) * 16;
        Direction best = clicked;
        double depth = 0.01;
        double[] offsets = {coreMin - y, y - coreMax, coreMin - z, z - coreMax, coreMin - x, x - coreMax};
        for (Direction direction : ConduitPartBlock.DIRECTIONS) {
            double beyond = offsets[direction.ordinal()];
            if (beyond > depth) {
                depth = beyond;
                best = direction;
            }
        }
        return best;
    }
}
