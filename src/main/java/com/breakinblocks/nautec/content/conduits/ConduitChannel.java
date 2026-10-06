package com.breakinblocks.nautec.content.conduits;

import java.util.Locale;

public enum ConduitChannel {
    ITEMS,
    FLUIDS,
    ENERGY;

    public static final ConduitChannel[] ALL = values();

    public String translationKey() {
        return "nautec.conduit.channel." + name().toLowerCase(Locale.ROOT);
    }
}
