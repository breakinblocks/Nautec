package com.breakinblocks.nautec.worldgen;

import com.breakinblocks.nautec.NTConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

import java.util.function.Supplier;

public final class MonumentLowering {
    public static final int DEPTH = 30;
    private static final int VANILLA_MIN_Y = 39;
    private static final ThreadLocal<Boolean> LOWER = new ThreadLocal<>();

    private MonumentLowering() {
    }

    public interface Flag {
        boolean nautec$lowersMonuments();

        void nautec$setLowersMonuments(boolean lowers);
    }

    public static boolean enabledIn(ResourceKey<Level> dimension) {
        return !NTConfig.monumentLoweringDimensionBlacklist.contains(dimension.location().toString());
    }

    public static boolean lowersIn(RandomState state) {
        return !(((Object) state) instanceof Flag flag) || flag.nautec$lowersMonuments();
    }

    public static boolean wasLowered(PiecesContainer pieces) {
        return pieces.isEmpty() || pieces.pieces().getFirst().getBoundingBox().minY() < VANILLA_MIN_Y;
    }

    public static boolean active() {
        Boolean lower = LOWER.get();
        return lower == null || lower;
    }

    public static <T> T with(boolean lower, Supplier<T> action) {
        Boolean previous = LOWER.get();
        LOWER.set(lower);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                LOWER.remove();
            } else {
                LOWER.set(previous);
            }
        }
    }
}
