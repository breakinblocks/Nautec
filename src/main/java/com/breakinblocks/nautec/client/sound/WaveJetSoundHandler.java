package com.breakinblocks.nautec.client.sound;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.items.WaveJetItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class WaveJetSoundHandler {
    private static final Map<Integer, WaveJetSound> ACTIVE = new HashMap<>();

    private WaveJetSoundHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            ACTIVE.clear();
            return;
        }

        ACTIVE.values().removeIf(WaveJetSound::isStopped);

        for (Player player : level.players()) {
            if (isRunning(player) && !ACTIVE.containsKey(player.getId())) {
                WaveJetSound sound = new WaveJetSound(player);
                ACTIVE.put(player.getId(), sound);
                Minecraft.getInstance().getSoundManager().play(sound);
            }
        }
    }

    public static boolean isRunning(Player player) {
        return player.isUsingItem()
                && player.getUseItem().getItem() instanceof WaveJetItem
                && player.isInWater();
    }
}
