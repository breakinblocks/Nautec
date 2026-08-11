package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.menus.SubmarineModuleMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SubmarineModuleScreen extends AbstractContainerScreen<SubmarineModuleMenu> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

    private static final int PLATE = 0xFF0B1118;
    private static final int PLATE_EDGE = 0xFF16222B;
    private static final int CYAN = 0xFF3EFDFF;
    private static final int CYAN_DIM = 0xFF19646B;
    private static final int SLOT_BG = 0xFF131E26;
    private static final int SLOT_EDGE = 0xFF273A45;
    private static final int TEXT = 0xFFF0F4F5;

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

        guiGraphics.fill(x, y, x + WIDTH, y + HEIGHT, PLATE);
        guiGraphics.fill(x, y, x + WIDTH, y + 1, PLATE_EDGE);
        guiGraphics.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, PLATE_EDGE);
        guiGraphics.fill(x, y, x + 1, y + HEIGHT, PLATE_EDGE);
        guiGraphics.fill(x + WIDTH - 1, y, x + WIDTH, y + HEIGHT, PLATE_EDGE);
        guiGraphics.fill(x + 1, y + 1, x + WIDTH - 1, y + 3, CYAN);
        guiGraphics.fill(x + 7, y + SubmarineModuleMenu.MODULE_ROW_Y + 19, x + WIDTH - 7,
                y + SubmarineModuleMenu.MODULE_ROW_Y + 20, CYAN_DIM);

        for (int slot = 0; slot < SubmarineEntity.MODULE_SLOTS; slot++) {
            drawCell(guiGraphics, x + 8 + slot * 18, y + SubmarineModuleMenu.MODULE_ROW_Y);

            String label = String.valueOf(slot + 1);
            int labelX = x + 8 + slot * 18 + (16 - this.font.width(label)) / 2;
            guiGraphics.text(this.font, label, labelX, y + SubmarineModuleMenu.MODULE_ROW_Y - 10, CYAN_DIM, false);
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawCell(guiGraphics, x + 8 + column * 18, y + SubmarineModuleMenu.INVENTORY_Y + row * 18);
            }
        }

        for (int column = 0; column < 9; column++) {
            drawCell(guiGraphics, x + 8 + column * 18, y + SubmarineModuleMenu.HOTBAR_Y);
        }
    }

    private void drawCell(GuiGraphicsExtractor guiGraphics, int x, int y) {
        guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, SLOT_EDGE);
        guiGraphics.fill(x, y, x + 16, y + 16, SLOT_BG);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        guiGraphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT, false);
        guiGraphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, TEXT, false);
    }
}
