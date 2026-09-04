package com.breakinblocks.nautec.compat.apotheosis;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.data.generated.BacteriaJsonWriter;
import com.breakinblocks.nautec.data.generated.GeneratedPackFinder;
import com.breakinblocks.nautec.data.generated.GeneratedPackPaths;
import com.breakinblocks.nautec.registries.NTItems;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.ModList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ApotheosisCompat {
    public static final String MOD_ID = "apotheosis";
    private static final String BOW_CATEGORY = "apotheosis:bow";
    private static final String OVERRIDE_PATH = "apotheosis/data_maps/item/loot_category_overrides.json";

    private ApotheosisCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static Path overrideFile() {
        return GeneratedPackPaths.dataRoot().resolve(OVERRIDE_PATH);
    }

    public static void syncLootCategoryOverride() {
        Path file = overrideFile();
        try {
            if (isLoaded()) {
                GeneratedPackFinder.scaffold();
                Files.createDirectories(file.getParent());
                BacteriaJsonWriter.writeAtomic(file, BacteriaJsonWriter.pretty(lootCategoryOverride()));
            } else if (Files.exists(file)) {
                Files.delete(file);
            }
        } catch (IOException e) {
            Nautec.LOGGER.error("Could not update the Apotheosis loot category override at {}", file, e);
        }
    }

    private static JsonObject lootCategoryOverride() {
        JsonObject values = new JsonObject();
        values.addProperty(BuiltInRegistries.ITEM.getKey(NTItems.ATLANTEAN_RIFLE.get()).toString(), BOW_CATEGORY);
        JsonObject root = new JsonObject();
        root.add("values", values);
        return root;
    }
}
