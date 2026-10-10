package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.menus.SubmarineModuleMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class SubmarineModuleScreen extends AbstractContainerScreen<SubmarineModuleMenu> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;


    public SubmarineModuleScreen(SubmarineModuleMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 6;
        this.inventoryLabelY = SubmarineModuleMenu.INVENTORY_Y - 12;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        PanelStyle.panel(guiGraphics, x, y, WIDTH, HEIGHT);
        for (Slot slot : this.menu.slots) {
            PanelStyle.slot(guiGraphics, x + slot.x, y + slot.y);
        }
        guiGraphics.fill(x + 7, y + SubmarineModuleMenu.MODULE_ROW_Y + 20, x + WIDTH - 7, y + SubmarineModuleMenu.MODULE_ROW_Y + 21, PanelStyle.PANEL_LIGHT);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, PanelStyle.LABEL, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, PanelStyle.LABEL, false);
        for (int slot = 0; slot < SubmarineEntity.MODULE_SLOTS; slot++) {
            String label = String.valueOf(slot + 1);
            guiGraphics.drawString(this.font, label, 8 + slot * 18 + (16 - this.font.width(label)) / 2, SubmarineModuleMenu.MODULE_ROW_Y - 10,
                    PanelStyle.READOUT_DIM, false);
        }
    }
}
