package com.breakinblocks.nautec.events;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.hud.SubmarineAbilityBarState;
import com.breakinblocks.nautec.client.screen.SubmarineHudPositionScreen;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.network.SubmarineAbilityPayload;
import com.breakinblocks.nautec.registries.NTKeybinds;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

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

        if (!(player.getControlledVehicle() instanceof SubmarineEntity submarine)) {
            SubmarineAbilityBarState.clear();
            return;
        }

        submarine.setInput(player.input.keyPresses);
        submarine.setSteering(minecraft.options.keyUse.isDown());
        submarine.setDescending(NTKeybinds.SUBMARINE_DESCEND_KEYBIND.get().isDown());

        SubmarineAbilityBarState.follow(submarine.getId());

        if (minecraft.screen != null) {
            return;
        }

        int heldSlot = player.getInventory().getSelectedSlot();
        for (int slot = 0; slot < SubmarineEntity.MODULE_SLOTS; slot++) {
            while (minecraft.options.keyHotbarSlots[slot].consumeClick()) {
                SubmarineAbilityBarState.select(slot);
            }
        }
        player.getInventory().setSelectedSlot(heldSlot);

        drain(minecraft.options.keySwapOffhand);

        while (NTKeybinds.SUBMARINE_ABILITY_KEYBIND.get().consumeClick()) {
            fireSelected(submarine);
        }
    }

    private static void drain(KeyMapping mapping) {
        while (mapping.consumeClick()) {
        }
    }

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !(player.getControlledVehicle() instanceof SubmarineEntity)) {
            return;
        }

        int scroll = event.getAccumulatedScrollY();
        if (scroll != 0) {
            SubmarineAbilityBarState.step(-Integer.signum(scroll));
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onInteractionInput(InputEvent.InteractionKeyMappingTriggered event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !(player.getControlledVehicle() instanceof SubmarineEntity submarine)) {
            return;
        }

        if (event.isUseItem()) {
            event.setSwingHand(false);
            event.setCanceled(true);
            return;
        }

        if (event.isAttack()) {
            event.setSwingHand(false);
            event.setCanceled(true);
            fireSelected(submarine);
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !(player.getVehicle() instanceof SubmarineEntity)) {
            return;
        }

        Identifier layer = event.getName();
        if (layer.equals(VanillaGuiLayers.HOTBAR)
                || layer.equals(VanillaGuiLayers.SELECTED_ITEM_NAME)
                || layer.equals(VanillaGuiLayers.EXPERIENCE_LEVEL)) {
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

    private static void fireSelected(SubmarineEntity submarine) {
        ClientPacketDistributor.sendToServer(new SubmarineAbilityPayload(submarine.getId(), SubmarineAbilityBarState.selected()));
    }

    private SubmarineClientEvents() {
    }
}
