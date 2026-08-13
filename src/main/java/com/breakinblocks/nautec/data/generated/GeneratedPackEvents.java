package com.breakinblocks.nautec.data.generated;

import com.breakinblocks.nautec.Nautec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Nautec.MODID)
public final class GeneratedPackEvents {
    private GeneratedPackEvents() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        List<String> removed = new ArrayList<>();

        for (String name : BacteriaMaterials.SHIPPED_NAMES) {
            if (removeIfPresent(GeneratedPackPaths.bacteriaFile(name))
                    | removeIfPresent(GeneratedPackPaths.incubationRecipeFile(name))
                    | removeIfPresent(GeneratedPackPaths.mutationRecipeFile(name))) {
                removed.add(name);
            }
        }

        if (!removed.isEmpty()) {
            Nautec.LOGGER.info("Cleared {} superseded bacteria from the generated pack, they ship with the mod now: {}",
                    removed.size(), String.join(", ", removed));
        }
    }

    private static boolean removeIfPresent(Path path) {
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            Nautec.LOGGER.warn("Could not remove superseded generated file {}", path, e);
            return false;
        }
    }
}
