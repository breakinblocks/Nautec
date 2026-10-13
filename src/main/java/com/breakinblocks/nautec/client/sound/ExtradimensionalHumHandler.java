package com.breakinblocks.nautec.client.sound;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.extradimensional.ExtradimensionalStorageBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class ExtradimensionalHumHandler {
    private static final double START_RANGE_SQR = 40 * 40;
    private static final double STOP_RANGE_SQR = 48 * 48;
    private static final Map<ExtradimensionalStorageBlockEntity, ExtradimensionalHumSound> PLAYING = new IdentityHashMap<>();

    private ExtradimensionalHumHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            stopAll();
            return;
        }
        SoundManager sounds = minecraft.getSoundManager();
        Iterator<Map.Entry<ExtradimensionalStorageBlockEntity, ExtradimensionalHumSound>> playing = PLAYING.entrySet().iterator();
        while (playing.hasNext()) {
            Map.Entry<ExtradimensionalStorageBlockEntity, ExtradimensionalHumSound> entry = playing.next();
            ExtradimensionalStorageBlockEntity storage = entry.getKey();
            ExtradimensionalHumSound sound = entry.getValue();
            if (!audible(storage, minecraft, player, STOP_RANGE_SQR) || sound.isStopped() || !sounds.isActive(sound)) {
                sound.end();
                playing.remove();
            }
        }
        for (ExtradimensionalStorageBlockEntity storage : ExtradimensionalStorageBlockEntity.CLIENT_LOADED) {
            if (!PLAYING.containsKey(storage) && audible(storage, minecraft, player, START_RANGE_SQR)) {
                ExtradimensionalHumSound sound = new ExtradimensionalHumSound(storage);
                sounds.play(sound);
                PLAYING.put(storage, sound);
            }
        }
    }

    private static boolean audible(ExtradimensionalStorageBlockEntity storage, Minecraft minecraft, Player player, double rangeSqr) {
        return !storage.isRemoved() && storage.getLevel() == minecraft.level
                && storage.getBlockPos().getCenter().distanceToSqr(player.position()) <= rangeSqr;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        stopAll();
        ExtradimensionalStorageBlockEntity.CLIENT_LOADED.clear();
    }

    private static void stopAll() {
        for (ExtradimensionalHumSound sound : PLAYING.values()) {
            sound.end();
        }
        PLAYING.clear();
    }
}
