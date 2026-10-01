package com.breakinblocks.nautec.api.gateways;

import com.breakinblocks.nautec.api.utils.HorizontalDirection;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class GatewayRing {
    public static final int SIZE = 13;
    public static final int CENTRE = 6;
    public static final double OUTER_RADIUS = 6.5;
    public static final double INNER_RADIUS = 4.5;
    public static final double OPENING_RADIUS = 4.45;
    public static final int CHEVRONS = 9;
    public static final float[] SLOT_ANGLES = {130F, 50F, 210F, 330F};

    private static final Vec3i CONTROLLER = new Vec3i(CENTRE, 0, 0);

    private GatewayRing() {
    }

    public static boolean isCore(int x, int y) {
        return x == CENTRE && y == 0;
    }

    public static boolean isRingCell(int x, int y) {
        double d = Math.hypot(x - CENTRE, y - CENTRE);
        return d >= INNER_RADIUS && d < OUTER_RADIUS;
    }

    public static int cellIndex(int x, int y) {
        return y * SIZE + x;
    }

    public static BlockPos cellPos(BlockPos core, HorizontalDirection direction, int x, int y) {
        BlockPos first = MultiblockHelper.getFirstBlockPos(direction, core, CONTROLLER);
        return MultiblockHelper.getCurPos(first, new Vec3i(x, y, 0), direction);
    }

    public static BlockPos coreOf(BlockPos cell, HorizontalDirection direction, int cellIndex) {
        int x = cellIndex % SIZE;
        int y = cellIndex / SIZE;
        int along = CENTRE - x;
        BlockPos lowered = cell.below(y);
        return switch (direction) {
            case NORTH -> lowered.offset(along, 0, 0);
            case EAST -> lowered.offset(0, 0, along);
            case SOUTH -> lowered.offset(-along, 0, 0);
            case WEST -> lowered.offset(0, 0, -along);
        };
    }

    public static List<BlockPos> ringCells(BlockPos core, HorizontalDirection direction) {
        List<BlockPos> cells = new ArrayList<>();
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (isRingCell(x, y) && !isCore(x, y)) {
                    cells.add(cellPos(core, direction, x, y));
                }
            }
        }
        return cells;
    }

    public static Vec3 centre(BlockPos core) {
        return Vec3.atCenterOf(core).add(0, CENTRE, 0);
    }

    public static Direction.Axis normalAxis(HorizontalDirection direction) {
        return direction == HorizontalDirection.NORTH || direction == HorizontalDirection.SOUTH
                ? Direction.Axis.Z
                : Direction.Axis.X;
    }

    public static Vec3 right(Vec3 normal) {
        return new Vec3(normal.z, 0, -normal.x);
    }

    public static Vec3 normal(Direction front) {
        return new Vec3(front.getStepX(), 0, front.getStepZ());
    }

    public static float yawOf(Vec3 normal) {
        return (float) Math.toDegrees(Math.atan2(-normal.x, normal.z));
    }

    public static int slotAt(Vec3 hit, BlockPos core, Direction front) {
        Vec3 n = normal(front);
        Vec3 offset = hit.subtract(centre(core));
        double x = offset.dot(right(n));
        double y = offset.y;
        float angle = (float) Math.toDegrees(Math.atan2(y, x));
        int best = 0;
        float bestDistance = Float.MAX_VALUE;
        for (int slot = 0; slot < SLOT_ANGLES.length; slot++) {
            float distance = Math.abs(wrapDegrees(angle - SLOT_ANGLES[slot]));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = slot;
            }
        }
        return best;
    }

    public static int slotOfChevron(int chevron) {
        float angle = 90F + 40F * chevron;
        for (int slot = 0; slot < SLOT_ANGLES.length; slot++) {
            if (Math.abs(wrapDegrees(angle - SLOT_ANGLES[slot])) < 1F) {
                return slot;
            }
        }
        return -1;
    }

    private static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360F;
        if (wrapped >= 180F) {
            wrapped -= 360F;
        }
        if (wrapped < -180F) {
            wrapped += 360F;
        }
        return wrapped;
    }
}
