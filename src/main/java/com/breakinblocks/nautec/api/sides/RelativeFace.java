package com.breakinblocks.nautec.api.sides;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum RelativeFace implements StringRepresentable {
    FRONT,
    BACK,
    LEFT,
    RIGHT,
    TOP,
    BOTTOM;

    public static final StringRepresentable.EnumCodec<RelativeFace> CODEC = StringRepresentable.fromEnum(RelativeFace::values);

    public static RelativeFace of(Direction front, Direction side) {
        if (side == Direction.UP) {
            return TOP;
        }
        if (side == Direction.DOWN) {
            return BOTTOM;
        }
        Direction facing = front.getAxis().isHorizontal() ? front : Direction.NORTH;
        if (side == facing) {
            return FRONT;
        }
        if (side == facing.getOpposite()) {
            return BACK;
        }
        return side == facing.getClockWise() ? LEFT : RIGHT;
    }

    public Direction toDirection(Direction front) {
        Direction facing = front.getAxis().isHorizontal() ? front : Direction.NORTH;
        return switch (this) {
            case FRONT -> facing;
            case BACK -> facing.getOpposite();
            case LEFT -> facing.getClockWise();
            case RIGHT -> facing.getCounterClockWise();
            case TOP -> Direction.UP;
            case BOTTOM -> Direction.DOWN;
        };
    }

    public String translationKey() {
        return "nautec.side_config.face." + getSerializedName();
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
