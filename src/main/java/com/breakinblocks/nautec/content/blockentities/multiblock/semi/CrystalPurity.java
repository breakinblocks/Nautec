package com.breakinblocks.nautec.content.blockentities.multiblock.semi;

public final class CrystalPurity {
    public static final float MIN = 3f;
    public static final float CRADLE_MAX = 6f;
    public static final float MAX = 12f;

    private CrystalPurity() {
    }

    public static float clamp(float purity) {
        return Float.isFinite(purity) ? Math.clamp(purity, MIN, MAX) : MIN;
    }

    public static float snap(float purity) {
        return Math.round(purity * 100f) / 100f;
    }
}
