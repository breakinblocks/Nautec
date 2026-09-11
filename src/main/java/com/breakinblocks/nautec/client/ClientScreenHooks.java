package com.breakinblocks.nautec.client;

import com.breakinblocks.nautec.client.screen.AugmentationStationScreen;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.AugmentationStationBlockEntity;
import com.breakinblocks.nautec.network.OpenAugmentationScreenPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;

public final class ClientScreenHooks {
    public static void openAugmentationStationScreen(OpenAugmentationScreenPayload payload) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.level().getBlockEntity(payload.pos()) instanceof AugmentationStationBlockEntity be) {
            openScreen(player, new AugmentationStationScreen(be, player, be.getBlockState().getBlock().getName(),
                    payload.augmentType(), payload.preview()));
        }
    }

    public static void openScreen(Player player, Screen screen) {
        if (player.level().isClientSide()) {
            Minecraft.getInstance().setScreen(screen);
        }
    }
}
