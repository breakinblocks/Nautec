package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.network.ResonanceSyncPayload;
import org.jetbrains.annotations.Nullable;

public final class ResonanceClientState {
    private static @Nullable ResonanceSyncPayload latest;

    private ResonanceClientState() {
    }

    public static void update(ResonanceSyncPayload payload) {
        latest = payload;
    }

    public static @Nullable ResonanceSyncPayload latest() {
        return latest;
    }

    public static void clear() {
        latest = null;
    }
}
