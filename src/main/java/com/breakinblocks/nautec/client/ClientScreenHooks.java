package com.breakinblocks.nautec.client;

import com.breakinblocks.nautec.network.OpenCharmScreenPayload;
import com.breakinblocks.nautec.client.screen.ResonanceCharmScreen;
import com.breakinblocks.nautec.client.screen.AugmentationStationScreen;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.AugmentationStationBlockEntity;
import com.breakinblocks.nautec.network.AugmentationStationSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;

public final class ClientScreenHooks {
    public static void augmentationStationSync(AugmentationStationSyncPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        if (minecraft.screen instanceof AugmentationStationScreen screen && screen.pos().equals(payload.pos())) {
            screen.update(payload);
        } else if (payload.open() && player.level().getBlockEntity(payload.pos()) instanceof AugmentationStationBlockEntity) {
            openScreen(player, new AugmentationStationScreen(player, payload));
        }
    }

    public static void openCharmScreen(OpenCharmScreenPayload payload) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            openScreen(player, new ResonanceCharmScreen(payload));
        }
    }

    public static void openScreen(Player player, Screen screen) {
        if (player.level().isClientSide()) {
            Minecraft.getInstance().setScreen(screen);
        }
    }
}
