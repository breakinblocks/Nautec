package com.breakinblocks.nautec.content.conduits;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum TapArm implements StringRepresentable {
    NONE("none"),
    CONDUIT("conduit"),
    MACHINE("machine");

    private final String name;

    TapArm(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
