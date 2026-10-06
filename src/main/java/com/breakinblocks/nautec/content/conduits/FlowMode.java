package com.breakinblocks.nautec.content.conduits;

import java.util.Locale;

public enum FlowMode {
    OFF,
    INSERT,
    EXTRACT,
    BOTH;

    public static final FlowMode[] ALL = values();

    public boolean inserts() {
        return this == INSERT || this == BOTH;
    }

    public boolean extracts() {
        return this == EXTRACT || this == BOTH;
    }

    public FlowMode next() {
        return ALL[(ordinal() + 1) % ALL.length];
    }

    public FlowMode previous() {
        return ALL[(ordinal() + ALL.length - 1) % ALL.length];
    }

    public String translationKey() {
        return "nautec.conduit.flow." + name().toLowerCase(Locale.ROOT);
    }
}
