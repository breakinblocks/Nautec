package com.breakinblocks.nautec.api.client.screen;

import com.breakinblocks.nautec.client.screen.GhostSlots;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.renderer.Rect2i;
import com.breakinblocks.nautec.client.screen.SideConfigPanel;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public abstract class NTAbstractContainerScreen<T extends ContainerBlockEntity> extends AbstractContainerScreen<NTAbstractContainerMenu<T>> implements SideConfigHost {
    private @Nullable SideConfigPanel sidePanel;

    public NTAbstractContainerScreen(NTAbstractContainerMenu<T> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    public NTAbstractContainerScreen(NTAbstractContainerMenu<T> menu, Inventory playerInventory, Component title, int imageWidth, int imageHeight) {
        super(menu, playerInventory, title, imageWidth, imageHeight);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getBackgroundTexture(), leftPos, topPos, 0F, 0F, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void init() {
        super.init();
        if (sidePanel == null) {
            sidePanel = SideConfigPanel.create(menu.blockEntity, menu.containerId);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        GhostSlots.extract(guiGraphics, this.menu, this.menu.blockEntity, this.leftPos, this.topPos);
        GhostSlots.tooltip(guiGraphics, font, this.menu.blockEntity, this.hoveredSlot, mouseX, mouseY);
        if (sidePanel != null) {
            sidePanel.extract(guiGraphics, font, sideAnchorX(), sideAnchorY(), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (sidePanel != null && sidePanel.mouseClicked(event, sideAnchorX(), sideAnchorY())) {
            return true;
        }
        if (GhostSlots.click(event, this.menu, this.menu.blockEntity, this.hoveredSlot)) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
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
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
        if (sidePanel != null && sidePanel.contains(mouseX, mouseY, sideAnchorX(), sideAnchorY())) {
            return false;
        }
        return super.hasClickedOutside(mouseX, mouseY, left, top);
    }

    public abstract @NotNull Identifier getBackgroundTexture();
}
