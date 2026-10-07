package com.breakinblocks.nautec.content.conduits;

public enum TapFlow {
    IDLE,
    INPUT,
    OUTPUT,
    BOTH;

    public static final TapFlow[] ALL = values();

    public static TapFlow of(boolean input, boolean output) {
        if (input) {
            return output ? BOTH : INPUT;
        }
        return output ? OUTPUT : IDLE;
    }
}
