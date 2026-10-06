package com.breakinblocks.nautec.content.conduits;

import java.util.Locale;

public enum RedstoneMode {
    IGNORE,
    HIGH,
    LOW;

    public static final RedstoneMode[] ALL = values();

    public boolean active(boolean powered) {
        return switch (this) {
            case IGNORE -> true;
            case HIGH -> powered;
            case LOW -> !powered;
        };
    }

    public RedstoneMode next() {
        return ALL[(ordinal() + 1) % ALL.length];
    }

    public String translationKey() {
        return "nautec.conduit.redstone." + name().toLowerCase(Locale.ROOT);
    }
}
