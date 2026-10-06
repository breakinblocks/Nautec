package com.breakinblocks.nautec.content.conduits;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum ConduitArm implements StringRepresentable {
    NONE("none"),
    CONNECTED("connected"),
    BLOCKED("blocked");

    private final String name;

    ConduitArm(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
