package com.breakinblocks.nautec.api.sides;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum SideMode implements StringRepresentable {
    BOTH(0xFF5FE8B0),
    INPUT(0xFF4CA3E0),
    OUTPUT(0xFFE3A72B),
    NONE(0xFF565656);

    public static final StringRepresentable.EnumCodec<SideMode> CODEC = StringRepresentable.fromEnum(SideMode::values);

    private final int color;

    SideMode(int color) {
        this.color = color;
    }

    public int color() {
        return color;
    }

    public boolean inserts() {
        return this == BOTH || this == INPUT;
    }

    public boolean extracts() {
        return this == BOTH || this == OUTPUT;
    }

    public SideMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public SideMode previous() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }

    public String translationKey() {
        return "nautec.side_config.mode." + getSerializedName();
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
