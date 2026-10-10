package com.breakinblocks.nautec.api.client.screen;

import net.minecraft.world.inventory.Slot;
import com.breakinblocks.nautec.client.screen.PanelStyle;
import com.breakinblocks.nautec.client.screen.GhostSlots;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.renderer.Rect2i;
import com.breakinblocks.nautec.client.screen.SideConfigPanel;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public abstract class NTAbstractContainerScreen<T extends ContainerBlockEntity> extends AbstractContainerScreen<NTAbstractContainerMenu<T>> implements SideConfigHost {
    private @Nullable SideConfigPanel sidePanel;

    public NTAbstractContainerScreen(NTAbstractContainerMenu<T> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.titleLabelY = 6;
    }

    public NTAbstractContainerScreen(NTAbstractContainerMenu<T> menu, Inventory playerInventory, Component title, int imageWidth, int imageHeight) {
        super(menu, playerInventory, title);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 6;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        ResourceLocation texture = getBackgroundTexture();
        if (texture != null) {
            NTGui.blit(guiGraphics, texture, leftPos, topPos, 0F, 0F, imageWidth, imageHeight, 256, 256);
            return;
        }
        PanelStyle.panel(guiGraphics, leftPos, topPos, imageWidth, imageHeight);
        for (Slot slot : this.menu.slots) {
            if (slot.isActive()) {
                PanelStyle.slot(guiGraphics, leftPos + slot.x, topPos + slot.y);
            }
        }
    }

    protected void extractSlotHint(GuiGraphics guiGraphics, @Nullable Slot slot, ResourceLocation icon) {
        if (slot != null && !slot.hasItem()) {
            PanelStyle.icon(guiGraphics, icon, leftPos + slot.x, topPos + slot.y, 16, 16);
        }
    }

    @Override
    protected void init() {
        super.init();
        if (sidePanel == null) {
            sidePanel = SideConfigPanel.create(menu.blockEntity, menu.containerId);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        GhostSlots.extract(guiGraphics, this.menu, this.menu.blockEntity, this.leftPos, this.topPos);
        GhostSlots.tooltip(guiGraphics, font, this.menu.blockEntity, this.hoveredSlot, mouseX, mouseY);
        if (sidePanel != null) {
            sidePanel.extract(guiGraphics, font, sideAnchorX(), sideAnchorY(), mouseX, mouseY);
        }
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (sidePanel != null && sidePanel.mouseClicked(mouseX, mouseY, button, sideAnchorX(), sideAnchorY())) {
            return true;
        }
        if (GhostSlots.click(this.menu, this.menu.blockEntity, this.hoveredSlot)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int sideAnchorX() {
        return this.leftPos + this.imageWidth + 2;
    }

    private int sideAnchorY() {
        return this.topPos + 4;
    }

    @Override
    public @Nullable Rect2i sideConfigArea() {
        return sidePanel == null ? null : sidePanel.area(sideAnchorX(), sideAnchorY());
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        if (sidePanel != null && sidePanel.contains(mouseX, mouseY, sideAnchorX(), sideAnchorY())) {
            return false;
        }
        return super.hasClickedOutside(mouseX, mouseY, left, top, button);
    }

    public @Nullable ResourceLocation getBackgroundTexture() {
        return null;
    }
}
