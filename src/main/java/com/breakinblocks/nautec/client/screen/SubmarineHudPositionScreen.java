package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTClientConfig;
import com.breakinblocks.nautec.client.hud.SubmarineHudOverlay;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class SubmarineHudPositionScreen extends Screen {
    private double hudX = NTClientConfig.hudX();
    private double hudY = NTClientConfig.hudY();
    private boolean dragging;
    private double grabOffsetX;
    private double grabOffsetY;

    private static final double DEFAULT_X = 0.02;
    private static final double DEFAULT_Y = 0.75;
    private static final int INFO_W = 220;
    private static final int INFO_H = 46;

    public SubmarineHudPositionScreen() {
        super(Component.translatable("nautec.submarine.hud_position.title"));
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - INFO_W) / 2;
        addRenderableWidget(new NTPanelButton(this.font, x + INFO_W - 62, 18 + INFO_H - 20, 54, 14,
                () -> Component.translatable("nautec.submarine.hud_position.reset"), () -> PanelStyle.NEUTRAL, () -> PanelStyle.NEUTRAL_HOVER,
                () -> Component.translatable("nautec.submarine.hud_position.reset.desc"), () -> {
            this.hudX = DEFAULT_X;
            this.hudY = DEFAULT_Y;
        }));
        addRenderableWidget(new NTPanelButton(this.font, x + INFO_W - 120, 18 + INFO_H - 20, 54, 14,
                () -> Component.translatable("nautec.submarine.hud_position.done"), () -> PanelStyle.SEND_COLOR, () -> PanelStyle.SEND_HOVER,
                () -> Component.translatable("nautec.submarine.hud_position.done.desc"), this::onClose));
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.fill(0, 0, this.width, this.height, 0x90000000);
        int infoX = (this.width - INFO_W) / 2;
        PanelStyle.panel(guiGraphics, infoX, 18, INFO_W, INFO_H);
        guiGraphics.drawString(this.font, this.title, infoX + 8, 24, PanelStyle.LABEL, false);
        guiGraphics.drawString(this.font, Component.translatable("nautec.submarine.hud_position.drag"), infoX + 8, 36, PanelStyle.LABEL, false);
        guiGraphics.drawString(this.font, Component.translatable("nautec.submarine.hud_position.keys"), infoX + 8, 48, PanelStyle.READOUT_DIM, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

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
        int px = panelX();
        int py = panelY();
        boolean over = mouseX >= px && mouseX <= px + SubmarineHudOverlay.PANEL_W && mouseY >= py && mouseY <= py + SubmarineHudOverlay.TOTAL_H;
        if (this.dragging || over) {
            int edge = this.dragging ? PanelStyle.READOUT : PanelStyle.READOUT_DIM;
            guiGraphics.fill(px - 2, py - 2, px + SubmarineHudOverlay.PANEL_W + 2, py - 1, edge);
            guiGraphics.fill(px - 2, py + SubmarineHudOverlay.TOTAL_H + 1, px + SubmarineHudOverlay.PANEL_W + 2, py + SubmarineHudOverlay.TOTAL_H + 2, edge);
            guiGraphics.fill(px - 2, py - 2, px - 1, py + SubmarineHudOverlay.TOTAL_H + 2, edge);
            guiGraphics.fill(px + SubmarineHudOverlay.PANEL_W + 1, py - 2, px + SubmarineHudOverlay.PANEL_W + 2, py + SubmarineHudOverlay.TOTAL_H + 2, edge);
        }
        SubmarineHudOverlay.drawPanel(guiGraphics, px, py, power, capacity, health, maxHealth, ticks);
    }

    private int panelX() {
        return SubmarineHudOverlay.panelX(this.width, this.hudX);
    }

    private int panelY() {
        return SubmarineHudOverlay.panelY(this.height, this.hudY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = panelX();
        int y = panelY();
        if (mouseX >= x && mouseX <= x + SubmarineHudOverlay.PANEL_W
                && mouseY >= y && mouseY <= y + SubmarineHudOverlay.TOTAL_H) {
            this.dragging = true;
            this.grabOffsetX = mouseX - x;
            this.grabOffsetY = mouseY - y;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (this.dragging) {
            int spanX = Math.max(1, this.width - SubmarineHudOverlay.PANEL_W);
            int spanY = Math.max(1, this.height - SubmarineHudOverlay.TOTAL_H);
            this.hudX = Mth.clamp((mouseX - this.grabOffsetX) / spanX, 0.0, 1.0);
            this.hudY = Mth.clamp((mouseY - this.grabOffsetY) / spanY, 0.0, 1.0);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_H && hasControlDown()) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
