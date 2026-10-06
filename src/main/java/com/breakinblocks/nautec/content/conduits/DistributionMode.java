package com.breakinblocks.nautec.content.conduits;

import java.util.Locale;

public enum DistributionMode {
    ROUND_ROBIN,
    NEAREST,
    RANDOM;

    public static final DistributionMode[] ALL = values();

    public DistributionMode next() {
        return ALL[(ordinal() + 1) % ALL.length];
    }

    public String translationKey() {
        return "nautec.conduit.distribution." + name().toLowerCase(Locale.ROOT);
    }
}
