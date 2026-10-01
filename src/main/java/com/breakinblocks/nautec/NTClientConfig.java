package com.breakinblocks.nautec;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class NTClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.DoubleValue SUBMARINE_HUD_X = BUILDER
            .comment("Horizontal position of the submarine power HUD, as a fraction of the screen width")
            .defineInRange("submarineHudX", 0.02, 0.0, 1.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_HUD_Y = BUILDER
            .comment("Vertical position of the submarine power HUD, as a fraction of the screen height")
            .defineInRange("submarineHudY", 0.75, 0.0, 1.0);
    private static final ModConfigSpec.BooleanValue SHOW_DIVING_SUIT_AIR_IN_SPACE = BUILDER
            .comment("Determines whether the Diving Suit air bar also shows on Stellaris planets with no air, not only underwater")
            .define("showDivingSuitAirInSpace", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean showDivingSuitAirInSpace() {
        return !SPEC.isLoaded() || SHOW_DIVING_SUIT_AIR_IN_SPACE.getAsBoolean();
    }

    public static double hudX() {
        return SPEC.isLoaded() ? SUBMARINE_HUD_X.getAsDouble() : 0.02;
    }

    public static double hudY() {
        return SPEC.isLoaded() ? SUBMARINE_HUD_Y.getAsDouble() : 0.75;
    }

    public static void setHudPosition(double x, double y) {
        SUBMARINE_HUD_X.set(Math.clamp(x, 0.0, 1.0));
        SUBMARINE_HUD_Y.set(Math.clamp(y, 0.0, 1.0));
    }

    private NTClientConfig() {
    }
}
