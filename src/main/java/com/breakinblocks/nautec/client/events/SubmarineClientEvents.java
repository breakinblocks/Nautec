package com.breakinblocks.nautec.client.events;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.hud.SubmarineAbilityBarState;
import com.breakinblocks.nautec.client.screen.SubmarineHudPositionScreen;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.entities.submarine.SubmarineInput;
import com.breakinblocks.nautec.content.items.submarine.SubmarineModuleType;
import com.breakinblocks.nautec.network.SubmarineAbilityPayload;
import com.breakinblocks.nautec.network.SubmarineLaserPayload;
import com.breakinblocks.nautec.registries.NTKeybinds;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class SubmarineClientEvents {
    private static int laserSubmarine = -1;
    private static boolean laserHeld;
    private static @Nullable Player posedPlayer;
    private static float savedBodyRot;
    private static float savedBodyRotO;
    private static float savedHeadRot;
    private static float savedHeadRotO;
    private static float savedXRot;
    private static float savedXRotO;

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
            laserSubmarine = -1;
            laserHeld = false;
            return;
        }

        Input input = player.input;
        submarine.setInput(new SubmarineInput(input.up, input.down, input.left, input.right, input.jumping, input.shiftKeyDown,
                minecraft.options.keySprint.isDown()));
        submarine.setFreeLook(minecraft.options.keyUse.isDown());
        submarine.setDescending(NTKeybinds.SUBMARINE_DESCEND_KEYBIND.get().isDown());

        SubmarineAbilityBarState.follow(submarine.getId());

        boolean laserSelected = laserSelected(submarine);
        sendLaserHeld(submarine, laserSelected && minecraft.screen == null
                && (minecraft.options.keyAttack.isDown() || NTKeybinds.SUBMARINE_ABILITY_KEYBIND.get().isDown()));

        if (minecraft.screen != null) {
            return;
        }

        int heldSlot = player.getInventory().selected;
        for (int slot = 0; slot < SubmarineEntity.MODULE_SLOTS; slot++) {
            while (minecraft.options.keyHotbarSlots[slot].consumeClick()) {
                SubmarineAbilityBarState.select(slot);
            }
        }
        player.getInventory().selected = heldSlot;

        drain(minecraft.options.keySwapOffhand);

        while (NTKeybinds.SUBMARINE_ABILITY_KEYBIND.get().consumeClick()) {
            if (!laserSelected(submarine)) {
                fireSelected(submarine);
            }
        }
    }

    private static boolean laserSelected(SubmarineEntity submarine) {
        return submarine.getModuleType(SubmarineAbilityBarState.selected()) == SubmarineModuleType.IMPULSE_LASER;
    }

    private static void sendLaserHeld(SubmarineEntity submarine, boolean held) {
        if (submarine.getId() == laserSubmarine && held == laserHeld) {
            return;
        }
        laserSubmarine = submarine.getId();
        laserHeld = held;
        PacketDistributor.sendToServer(new SubmarineLaserPayload(submarine.getId(), held));
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

        int scroll = (int) Math.signum(event.getScrollDeltaY());
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
            if (!laserSelected(submarine)) {
                fireSelected(submarine);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !(player.getControlledVehicle() instanceof SubmarineEntity)) {
            return;
        }

        ResourceLocation layer = event.getName();
        if (layer.equals(VanillaGuiLayers.HOTBAR)
                || layer.equals(VanillaGuiLayers.SELECTED_ITEM_NAME)
                || layer.equals(VanillaGuiLayers.EXPERIENCE_LEVEL)
                || layer.equals(VanillaGuiLayers.VEHICLE_HEALTH)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        Player player = event.getEntity();
        if (!(player.getVehicle() instanceof SubmarineEntity submarine)
                || submarine.getControllingPassenger() != player) {
            return;
        }

        posedPlayer = player;
        savedBodyRot = player.yBodyRot;
        savedBodyRotO = player.yBodyRotO;
        savedHeadRot = player.yHeadRot;
        savedHeadRotO = player.yHeadRotO;
        savedXRot = player.getXRot();
        savedXRotO = player.xRotO;

        float bodyRot = Mth.rotLerp(event.getPartialTick(), submarine.yRotO, submarine.getYRot());
        float xRot = Mth.lerp(event.getPartialTick(), submarine.xRotO, submarine.getXRot());
        player.yBodyRot = bodyRot;
        player.yBodyRotO = bodyRot;
        player.yHeadRot = bodyRot;
        player.yHeadRotO = bodyRot;
        player.setXRot(xRot);
        player.xRotO = xRot;
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = posedPlayer;
        if (player == null || player != event.getEntity()) {
            return;
        }
        posedPlayer = null;
        player.yBodyRot = savedBodyRot;
        player.yBodyRotO = savedBodyRotO;
        player.yHeadRot = savedHeadRot;
        player.yHeadRotO = savedHeadRotO;
        player.setXRot(savedXRot);
        player.xRotO = savedXRotO;
    }

    @SubscribeEvent
    public static void onCameraDistance(CalculateDetachedCameraDistanceEvent event) {
        if (event.getCamera().getEntity() != null
                && event.getCamera().getEntity().getVehicle() instanceof SubmarineEntity) {
            event.setDistance((float) NTConfig.submarineCameraDistance);
        }
    }

    private static void fireSelected(SubmarineEntity submarine) {
        PacketDistributor.sendToServer(new SubmarineAbilityPayload(submarine.getId(), SubmarineAbilityBarState.selected()));
    }

    private SubmarineClientEvents() {
    }
}
