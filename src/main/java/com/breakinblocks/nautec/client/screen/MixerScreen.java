package com.breakinblocks.nautec.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MixerScreen extends NTMachineScreen<MixerBlockEntity> {
    public MixerScreen(NTMachineMenu<MixerBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        PanelStyle.icon(guiGraphics, PanelStyle.ICON_WHISK, leftPos + 66, topPos + 37, 8, 25);
    }
}
