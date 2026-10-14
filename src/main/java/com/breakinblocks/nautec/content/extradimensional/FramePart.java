package com.breakinblocks.nautec.content.extradimensional;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum FramePart implements StringRepresentable {
    LOOSE,
    INNER_EDGE,
    INNER_CORNER,
    EDGE,
    CORNER,
    PORT;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean formed() {
        return this != LOOSE;
    }
}
