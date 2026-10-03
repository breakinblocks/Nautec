package com.breakinblocks.nautec.api.sides;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum SideKind implements StringRepresentable {
    ITEMS,
    FLUIDS;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "nautec.side_config.kind." + getSerializedName();
    }
}
