package com.breakinblocks.nautec.utils;

public final class PushBackoff {
    public static final int MAX_ENERGY_IDLE = 20;

    private final int maxIdle;
    private int idle;
    private long next;

    public PushBackoff(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public PushBackoff() {
        this(MAX_ENERGY_IDLE);
    }

    public static PushBackoff[] array(int size) {
        PushBackoff[] backoffs = new PushBackoff[size];
        for (int i = 0; i < size; i++) {
            backoffs[i] = new PushBackoff();
        }
        return backoffs;
    }

    public boolean ready(long time) {
        return time >= next;
    }

    public void record(boolean moved, long time) {
        if (moved) {
            idle = 0;
            next = 0;
            return;
        }
        idle = Math.min(maxIdle, Math.max(1, idle * 2));
        next = time + idle;
    }
}
