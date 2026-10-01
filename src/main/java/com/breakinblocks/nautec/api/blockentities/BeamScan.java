package com.breakinblocks.nautec.api.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record BeamScan(Direction direction, Status status, int distance) {
    public enum Status {
        CONNECTED,
        WRONG_SIDE,
        BLOCKED,
        NO_TARGET
    }

    public boolean connected() {
        return status == Status.CONNECTED;
    }

    public int connectedDistance() {
        return connected() ? distance : 0;
    }

    public BlockPos targetPos(BlockPos origin) {
        return origin.relative(direction, distance);
    }
}
