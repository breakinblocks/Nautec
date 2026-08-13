package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTClientConfig;
import com.breakinblocks.nautec.client.hud.SubmarineHudOverlay;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class SubmarineHudPositionScreen extends Screen {
    private double hudX = NTClientConfig.hudX();
    private double hudY = NTClientConfig.hudY();
    private boolean dragging;
    private double grabOffsetX;
    private double grabOffsetY;

    public SubmarineHudPositionScreen() {
        super(Component.literal("Submarine HUD Position"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x90000000);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        int power = 73;
        int capacity = 100;
        float health = 62F;
        float maxHealth = 80F;
        if (this.minecraft != null && this.minecraft.player != null
                && this.minecraft.player.getControlledVehicle() instanceof SubmarineEntity submarine) {
            power = submarine.getPowerStored();
            capacity = submarine.getPowerStorage().getPowerCapacity();
            health = submarine.getHealth();
            maxHealth = submarine.getMaxHealth();
        }

        long ticks = this.minecraft != null && this.minecraft.level != null ? this.minecraft.level.getGameTime() : 0L;
        SubmarineHudOverlay.drawPanel(guiGraphics, panelX(), panelY(), power, capacity, health, maxHealth, ticks);

        guiGraphics.text(this.font, "Drag the readout where you want it", centeredX("Drag the readout where you want it"), 20, 0xFFF0F4F5, true);
        guiGraphics.text(this.font, "ESC or Ctrl+H to save", centeredX("ESC or Ctrl+H to save"), 32, 0xFF3EFDFF, true);
    }

    private int centeredX(String text) {
        return (this.width - this.font.width(text)) / 2;
    }

    private int panelX() {
        return SubmarineHudOverlay.panelX(this.width, this.hudX);
    }

    private int panelY() {
        return SubmarineHudOverlay.panelY(this.height, this.hudY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int x = panelX();
        int y = panelY();
        if (event.x() >= x && event.x() <= x + SubmarineHudOverlay.PANEL_W
                && event.y() >= y && event.y() <= y + SubmarineHudOverlay.TOTAL_H) {
            this.dragging = true;
            this.grabOffsetX = event.x() - x;
            this.grabOffsetY = event.y() - y;
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.dragging) {
            int spanX = Math.max(1, this.width - SubmarineHudOverlay.PANEL_W);
            int spanY = Math.max(1, this.height - SubmarineHudOverlay.TOTAL_H);
            this.hudX = Mth.clamp((event.x() - this.grabOffsetX) / spanX, 0.0, 1.0);
            this.hudY = Mth.clamp((event.y() - this.grabOffsetY) / spanY, 0.0, 1.0);
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.dragging = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_H && event.hasControlDown()) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        NTClientConfig.setHudPosition(this.hudX, this.hudY);
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
