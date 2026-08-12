package com.breakinblocks.nautec.data.generated;

import com.breakinblocks.nautec.Nautec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid = Nautec.MODID)
public final class GeneratedPackEvents {
    private GeneratedPackEvents() {
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(BacteriaPresetManager.LISTENER_ID, new BacteriaPresetManager());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        BacteriaPresets.Result result = BacteriaPresets.applyAll(event.getServer().registryAccess());

        if (!result.written().isEmpty()) {
            Nautec.LOGGER.info("Wrote {} bacteria from presets: {}. They become active the next time this world loads.",
                    result.written().size(), String.join(", ", result.written()));
        }
        if (!result.skipped().isEmpty()) {
            Nautec.LOGGER.debug("{} bacteria presets are waiting for a mod to fill their tags", result.skipped().size());
        }
    }
}
