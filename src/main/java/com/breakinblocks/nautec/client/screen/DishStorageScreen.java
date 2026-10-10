package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.dishstorage.DishStorageBlockEntity;
import com.breakinblocks.nautec.content.dishstorage.DishStorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class DishStorageScreen extends NTMachineScreen<DishStorageBlockEntity> {
    private static final int BAR_X = 154;
    private static final int BAR_WIDTH = 8;
    private static final int BAR_HEIGHT = DishStorageMenu.VISIBLE_ROWS * 18;
    private static final int THUMB = 0xFF6FD3C8;
    private static final int THUMB_IDLE = 0xFF45504A;

    private boolean dragging;

    public DishStorageScreen(NTMachineMenu<DishStorageBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 228);
        this.inventoryLabelY = DishStorageMenu.INVENTORY_Y - 11;
    }

    private DishStorageMenu storage() {
        return (DishStorageMenu) this.menu;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int x = leftPos + BAR_X;
        int y = topPos + DishStorageMenu.GRID_Y - 1;
        guiGraphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, PanelStyle.SLOT_EDGE);
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, PanelStyle.SCREEN_FILL);
        int max = storage().maxOffset();
        int thumbHeight = max == 0 ? BAR_HEIGHT : Math.max(10, BAR_HEIGHT * DishStorageMenu.VISIBLE_ROWS / storage().rows());
        int thumbY = max == 0 ? y : y + (BAR_HEIGHT - thumbHeight) * storage().getOffset() / max;
        guiGraphics.fill(x + 1, thumbY + 1, x + BAR_WIDTH - 1, thumbY + thumbHeight - 1, max == 0 ? THUMB_IDLE : THUMB);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        Component count = Component.translatable("nautec.dish_storage.count", this.menu.blockEntity.storedCount(),
                this.menu.blockEntity.getCapacity());
        guiGraphics.drawString(this.font, count, this.imageWidth - 14 - this.font.width(count), this.titleLabelY, PanelStyle.LABEL, false);
    }

    private void scrollTo(int offset) {
        int clamped = Mth.clamp(offset, 0, storage().maxOffset());
        if (clamped != storage().getOffset() && this.minecraft != null && this.minecraft.gameMode != null) {
            storage().setOffset(clamped);
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, clamped);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (storage().maxOffset() > 0 && scrollY != 0) {
            scrollTo(storage().getOffset() - (int) Math.signum(scrollY));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean overBar(double mouseX, double mouseY) {
        return isHovering(BAR_X, DishStorageMenu.GRID_Y - 1, BAR_WIDTH, BAR_HEIGHT, mouseX, mouseY);
    }

    private void dragTo(double mouseY) {
        int max = storage().maxOffset();
        double top = topPos + DishStorageMenu.GRID_Y - 1;
        double fraction = Mth.clamp((mouseY - top) / BAR_HEIGHT, 0, 1);
        scrollTo((int) Math.round(fraction * max));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (storage().maxOffset() > 0 && overBar(mouseX, mouseY)) {
            dragging = true;
            dragTo(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging) {
            dragTo(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
