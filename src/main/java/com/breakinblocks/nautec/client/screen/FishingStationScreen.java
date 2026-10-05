package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.FishingStationBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FishingStationScreen extends NTMachineScreen<FishingStationBlockEntity> {
    public FishingStationScreen(NTMachineMenu<FishingStationBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
}
