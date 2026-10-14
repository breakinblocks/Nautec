package com.breakinblocks.nautec.compat.ae2;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

public final class AE2Compat {
    public static final String MOD_ID = "ae2";

    private AE2Compat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static void init(IEventBus modEventBus) {
        if (isLoaded()) {
            AE2Content.init(modEventBus);
        }
    }
}
