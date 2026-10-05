package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.content.menus.CrateMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class CrateScreen extends AbstractContainerScreen<CrateMenu> {
    public CrateScreen(CrateMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 167);
        this.titleLabelY = 6;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        PanelStyle.panel(guiGraphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        for (Slot slot : this.menu.slots) {
            if (slot.isActive()) {
                PanelStyle.slot(guiGraphics, this.leftPos + slot.x, this.topPos + slot.y);
            }
        }
    }
}
