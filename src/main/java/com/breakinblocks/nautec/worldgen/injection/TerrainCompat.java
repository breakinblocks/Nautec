package com.breakinblocks.nautec.worldgen.injection;

import com.breakinblocks.nautec.Nautec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.List;

@EventBusSubscriber(modid = Nautec.MODID)
public final class TerrainCompat {
    private static final List<String> TERRAIN_REPLACERS = List.of("tectonic");

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        List<String> present = TERRAIN_REPLACERS.stream().filter(id -> ModList.get().isLoaded(id)).toList();
        if (present.isEmpty()) {
            return;
        }

        Nautec.LOGGER.info("{} replaces the overworld terrain pipeline, so Nautec's Deeper Oceans has no effect and that mod decides how deep the oceans are. "
                + "Nautec's ocean floor features follow the real sea floor, so they place correctly either way.", String.join(", ", present));
    }

    private TerrainCompat() {
    }
}
