package com.breakinblocks.nautec.content.fishing;

import net.minecraft.util.RandomSource;

import java.util.List;

public enum MinigameKind {
    TIMING_BAR(1),
    RHYTHM(3),
    HOLD(2);

    private static final MinigameKind[] VALUES = values();

    private static final int LEAD_IN = 12;
    private static final int MIN_WINDOW = 8;
    private static final int MAX_WINDOW = 15;
    private static final int RHYTHM_WINDOW = 7;
    private static final int HOLD_MIN_TICKS = 6;

    private final int reportSize;

    MinigameKind(int reportSize) {
        this.reportSize = reportSize;
    }

    public int reportSize() {
        return this.reportSize;
    }

    public static MinigameKind byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : TIMING_BAR;
    }

    public static MinigameKind random(RandomSource random) {
        return VALUES[random.nextInt(VALUES.length)];
    }

    public List<int[]> windows(long seed) {
        return switch (this) {
            case TIMING_BAR -> List.of(new int[]{start(seed, length(seed)), length(seed)});
            case RHYTHM -> {
                int slot = (FishingMinigame.DURATION_TICKS - LEAD_IN) / 3;
                List<int[]> windows = new java.util.ArrayList<>(3);
                for (int i = 0; i < 3; i++) {
                    int base = LEAD_IN + i * slot;
                    int jitter = (int) Math.floorMod(seed >> (i * 8), Math.max(1, slot - RHYTHM_WINDOW));
                    windows.add(new int[]{base + jitter, RHYTHM_WINDOW});
                }
                yield List.copyOf(windows);
            }
            case HOLD -> {
                int len = MIN_WINDOW + HOLD_MIN_TICKS + (int) Math.floorMod(seed >> 20, 8L);
                yield List.of(new int[]{start(seed, len), len});
            }
        };
    }

    private static int length(long seed) {
        return MIN_WINDOW + (int) Math.floorMod(seed >> 16, MAX_WINDOW - MIN_WINDOW + 1L);
    }

    private static int start(long seed, int length) {
        int span = FishingMinigame.DURATION_TICKS - length - LEAD_IN;
        return LEAD_IN + (int) Math.floorMod(seed, Math.max(1L, span));
    }

    public boolean validate(long seed, int[] report) {
        if (report == null || report.length != this.reportSize) {
            return false;
        }
        for (int tick : report) {
            if (tick < 0 || tick > FishingMinigame.DURATION_TICKS) {
                return false;
            }
        }

        List<int[]> windows = windows(seed);
        return switch (this) {
            case TIMING_BAR -> inside(windows.getFirst(), report[0]);
            case RHYTHM -> {
                for (int i = 0; i < 3; i++) {
                    if (!inside(windows.get(i), report[i])) {
                        yield false;
                    }
                    if (i > 0 && report[i] <= report[i - 1]) {
                        yield false;
                    }
                }
                yield true;
            }
            case HOLD -> {
                int[] window = windows.getFirst();
                int pressed = report[0];
                int released = report[1];
                yield released - pressed >= HOLD_MIN_TICKS
                        && inside(window, pressed)
                        && inside(window, released);
            }
        };
    }

    private static boolean inside(int[] window, int tick) {
        return tick >= window[0] && tick < window[0] + window[1];
    }
}
