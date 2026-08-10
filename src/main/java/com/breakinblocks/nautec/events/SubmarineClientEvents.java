package com.breakinblocks.nautec.events;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class SubmarineClientEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !(player.getControlledVehicle() instanceof SubmarineEntity submarine)) {
            return;
        }

        submarine.setInput(player.input.keyPresses);
        submarine.setSteering(minecraft.options.keyUse.isDown());
    }

    /**
     * The use key is the steering control while piloting, so stop it also swinging whatever is in the
     * pilot's hand or placing blocks against the sea floor.
     */
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

    private SubmarineClientEvents() {
    }
}
