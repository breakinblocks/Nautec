package com.breakinblocks.nautec.compat.starcatcher;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.entities.NautecFishingHook;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

public final class StarcatcherCompat {
    public static final String MOD_ID = "starcatcher";

    private static boolean broken;

    private StarcatcherCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static void init() {
        if (isLoaded()) {
            NeoForge.EVENT_BUS.addListener(StarcatcherBobEvents::onEntityTick);
        }
    }

    public static boolean isStarcatcherCatch(Player player) {
        if (broken || !isLoaded()) {
            return false;
        }
        try {
            return StarcatcherCatches.isReeling(player);
        } catch (LinkageError e) {
            broken = true;
            Nautec.LOGGER.error("Starcatcher changed its API, so Starcatcher catches in lucky zones get a single NauTec roll", e);
            return false;
        }
    }

    public static @Nullable List<ItemStack> rollCatch(NautecFishingHook hook, LootParams params) {
        if (broken || !NTConfig.starcatcherFishOnNautecRod || !isLoaded()) {
            return null;
        }
        try {
            return StarcatcherCatches.roll(hook, params);
        } catch (LinkageError e) {
            broken = true;
            Nautec.LOGGER.error("Starcatcher changed its API, so the NauTec Fishing Rod is back on vanilla fishing loot", e);
            return null;
        }
    }
}
