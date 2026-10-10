package com.breakinblocks.nautec.client.events;

import com.breakinblocks.nautec.content.items.PrismMonocleItem;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.screen.AugmentationViewerScreen;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTKeybinds;
import com.breakinblocks.nautec.utils.AugmentHelper;
import com.breakinblocks.nautec.worldgen.NTBiomeWaterFog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

public final class NTClientEvents {
    @EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
    public static final class ClientInGameBus {
        @SubscribeEvent
        public static void onRenderFog(ViewportEvent.RenderFog event) {
            Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
            if (event.getType() == FogType.WATER && cameraEntity instanceof Player player) {
                float end = NTBiomeWaterFog.endDistance(player.level().getBiome(event.getCamera().getBlockPosition()));
                if (end > 0.0F) {
                    event.setFarPlaneDistance(end * Math.max(0.25F, ((LocalPlayer) player).getWaterVision()));
                    event.setCanceled(true);
                }
            }
            if (cameraEntity instanceof Player player && cameraEntity.isUnderWater()) {
                if (player.getItemBySlot(EquipmentSlot.HEAD).is(NTItems.DIVING_HELMET.get()) || PrismMonocleItem.isWorn(player)) {
                    event.setNearPlaneDistance(-8.0f);
                    event.setFarPlaneDistance(250.0f);
                    event.setCanceled(true);
                }
            }
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (NTKeybinds.AUGMENT_SCREEN_KEYBIND.get().consumeClick()) {
                if (mc.screen == null && mc.player != null && !AugmentHelper.getAugments(mc.player).isEmpty()) {
                    mc.setScreen(new AugmentationViewerScreen(Component.translatable("nautec.augment_viewer.title"), mc.player));
                }
            }
        }
    }
}
