package com.breakinblocks.nautec.client.screen;

public final class LimitSteps {
    private LimitSteps() {
    }

    public static int step(int current, int direction, boolean fine, boolean coarse, int max) {
        if (fine || coarse) {
            long delta = (long) direction * (coarse ? 10 : 1);
            return (int) Math.max(0, Math.min(max, current + delta));
        }
        if (direction > 0) {
            long value = 1;
            while (value <= current) {
                value = next(value);
            }
            return (int) Math.min(value, max);
        }
        long result = 0;
        for (long value = 1; value < current; value = next(value)) {
            result = value;
        }
        return (int) Math.min(result, max);
    }

    private static long next(long value) {
        long scale = 1;
        while (scale * 10 <= value) {
            scale *= 10;
        }
        long lead = value / scale;
        return lead < 2 ? 2 * scale : lead < 5 ? 5 * scale : 10 * scale;
    }
}
