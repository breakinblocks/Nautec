package com.breakinblocks.nautec.client.sonar;

import net.minecraft.util.Mth;

public final class SonarWave {
    public static final int DURATION_TICKS = 40;
    public static final float BAND_WIDTH = 10F;
    private static final float START_OFFSET_TICKS = 4F;
    private static final int REFERENCE_RENDER_DISTANCE = 12;

    private SonarWave() {
    }

    /** Scannable's accelerating sweep; the trailing edge reaches the scan range at completion. */
    public static float durationTicks(int renderDistance) {
        return Math.max(1F, (float) DURATION_TICKS * renderDistance / REFERENCE_RENDER_DISTANCE);
    }

    public static float radius(float range, float ageTicks, float durationTicks) {
        float duration = Math.max(1F, durationTicks);
        float time = Mth.clamp(ageTicks, 0F, duration);
        float progress = time * (time + 2F * START_OFFSET_TICKS)
                / (duration * (duration + 2F * START_OFFSET_TICKS));
        return BAND_WIDTH + Math.max(0F, range) * progress;
    }
}
