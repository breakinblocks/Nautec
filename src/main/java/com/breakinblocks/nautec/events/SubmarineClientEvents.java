package com.breakinblocks.nautec.events;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.screen.SubmarineHudPositionScreen;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.registries.NTKeybinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class SubmarineClientEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        while (NTKeybinds.SUBMARINE_HUD_KEYBIND.get().consumeClick()) {
            if (minecraft.screen == null) {
                minecraft.setScreen(new SubmarineHudPositionScreen());
            }
        }

        if (player.getControlledVehicle() instanceof SubmarineEntity submarine) {
            submarine.setInput(player.input.keyPresses);
            submarine.setSteering(minecraft.options.keyUse.isDown());
            submarine.setDescending(NTKeybinds.SUBMARINE_DESCEND_KEYBIND.get().isDown());
        }
    }

    @SubscribeEvent
    public static void onUseInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getControlledVehicle() instanceof SubmarineEntity) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onCameraDistance(CalculateDetachedCameraDistanceEvent event) {
        if (event.getCamera().entity() != null
                && event.getCamera().entity().getVehicle() instanceof SubmarineEntity) {
            event.setDistance((float) NTConfig.submarineCameraDistance);
        }
    }

    private SubmarineClientEvents() {
    }
}
