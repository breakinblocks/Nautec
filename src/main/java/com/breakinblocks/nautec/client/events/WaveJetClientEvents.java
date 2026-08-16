package com.breakinblocks.nautec.client.events;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.items.WaveJetSpotlight;
import com.breakinblocks.nautec.network.ToggleWaveJetLightPayload;
import com.breakinblocks.nautec.registries.NTKeybinds;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class WaveJetClientEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        KeyMapping toggle = NTKeybinds.WAVE_JET_LIGHT_KEYBIND.get();
        if (WaveJetSpotlight.heldWaveJet(player) == null
                || player.getControlledVehicle() instanceof SubmarineEntity) {
            drain(toggle);
            return;
        }

        boolean toggled = false;
        while (toggle.consumeClick()) {
            toggled = minecraft.screen == null;
        }

        if (toggle.same(minecraft.options.keySwapOffhand)) {
            drain(minecraft.options.keySwapOffhand);
        }

        if (toggled) {
            ClientPacketDistributor.sendToServer(new ToggleWaveJetLightPayload());
        }
    }

    private static void drain(KeyMapping mapping) {
        while (mapping.consumeClick()) {
        }
    }

    private WaveJetClientEvents() {
    }
}
