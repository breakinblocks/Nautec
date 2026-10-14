package com.breakinblocks.nautec.compat.refinedstorage;

import com.breakinblocks.nautec.content.distributor.NetworkStorages;
import net.neoforged.fml.ModList;

public final class RefinedStorageCompat {
    public static final String MOD_ID = "refinedstorage";

    private RefinedStorageCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static void init() {
        if (isLoaded()) {
            NetworkStorages.register(RSNetworkStorage::find);
        }
    }
}
