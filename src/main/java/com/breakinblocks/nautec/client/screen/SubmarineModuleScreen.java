package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.menus.SubmarineModuleMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class SubmarineModuleScreen extends AbstractContainerScreen<SubmarineModuleMenu> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;


    public SubmarineModuleScreen(SubmarineModuleMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, WIDTH, HEIGHT);
        this.titleLabelY = 6;
        this.inventoryLabelY = SubmarineModuleMenu.INVENTORY_Y - 12;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;
        PanelStyle.panel(guiGraphics, x, y, WIDTH, HEIGHT);
        for (Slot slot : this.menu.slots) {
            PanelStyle.slot(guiGraphics, x + slot.x, y + slot.y);
        }
        guiGraphics.fill(x + 7, y + SubmarineModuleMenu.MODULE_ROW_Y + 20, x + WIDTH - 7, y + SubmarineModuleMenu.MODULE_ROW_Y + 21, PanelStyle.PANEL_LIGHT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        guiGraphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, PanelStyle.LABEL, false);
        guiGraphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, PanelStyle.LABEL, false);
        for (int slot = 0; slot < SubmarineEntity.MODULE_SLOTS; slot++) {
            String label = String.valueOf(slot + 1);
            guiGraphics.text(this.font, label, 8 + slot * 18 + (16 - this.font.width(label)) / 2, SubmarineModuleMenu.MODULE_ROW_Y - 10,
                    PanelStyle.READOUT_DIM, false);
        }
    }
}
