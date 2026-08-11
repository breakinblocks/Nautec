package com.breakinblocks.nautec.client.hud;

import com.breakinblocks.nautec.NTClientConfig;
import com.breakinblocks.nautec.client.screen.SubmarineHudPositionScreen;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

public final class SubmarineHudOverlay {
    public static final int PANEL_W = 122;
    public static final int PANEL_H = 24;

    private static final int CYAN = 0xFF3EFDFF;
    private static final int CYAN_DIM = 0xFF19646B;
    private static final int WHITE = 0xFFF0F4F5;
    private static final int PLATE = 0xD9070B10;
    private static final int CELL_EMPTY = 0x26FFFFFF;

    private static final int CELLS = 8;
    private static final int CELL_W = 5;
    private static final int CELL_H = 10;
    private static final int CELL_GAP = 2;

    public static void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen instanceof SubmarineHudPositionScreen) {
            return;
        }

        if (!(minecraft.player.getControlledVehicle() instanceof SubmarineEntity submarine)) {
            return;
        }

        int x = panelX(guiGraphics.guiWidth(), NTClientConfig.hudX());
        int y = panelY(guiGraphics.guiHeight(), NTClientConfig.hudY());
        long ticks = minecraft.level != null ? minecraft.level.getGameTime() : 0L;
        drawGauge(guiGraphics, x, y, submarine.getPowerStored(), submarine.getPowerStorage().getPowerCapacity(), ticks);
    }

    public static int panelX(int guiWidth, double fraction) {
        return (int) Math.round(fraction * (guiWidth - PANEL_W));
    }

    public static int panelY(int guiHeight, double fraction) {
        return (int) Math.round(fraction * (guiHeight - PANEL_H));
    }

    public static void drawGauge(GuiGraphicsExtractor guiGraphics, int x, int y, int power, int capacity, long ticks) {
        float fraction = capacity > 0 ? Mth.clamp((float) power / capacity, 0F, 1F) : 0F;
        boolean blinkOn = ticks % 16 < 8;

        for (int row = 0; row < PANEL_H; row++) {
            int slant = (PANEL_H - 1 - row) / 4;
            guiGraphics.fill(x + slant, y + row, x + slant + PANEL_W - 6, y + row + 1, PLATE);
        }

        for (int row = 0; row < 2; row++) {
            int slant = (PANEL_H - 1 - row) / 4;
            guiGraphics.fill(x + slant, y + row, x + slant + PANEL_W - 6, y + row + 1, CYAN);
        }

        guiGraphics.fill(x + 4, y + 6, x + 6, y + PANEL_H - 4, CYAN);

        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.text(minecraft.font, "PWR", x + 10, y + 9, WHITE, false);

        int filled = Mth.ceil(fraction * CELLS);
        boolean low = fraction < 0.2F;
        int cellX = x + 34;
        int cellY = y + 8;
        for (int i = 0; i < CELLS; i++) {
            int color = i < filled ? (low && !blinkOn ? CYAN_DIM : CYAN) : CELL_EMPTY;
            guiGraphics.fill(cellX, cellY, cellX + CELL_W, cellY + CELL_H, color);
            cellX += CELL_W + CELL_GAP;
        }

        String readout = power <= 0 ? "CHG" : Math.round(fraction * 100F) + "%";
        int readoutColor = power <= 0 ? (blinkOn ? CYAN : CYAN_DIM) : WHITE;
        int readoutWidth = minecraft.font.width(readout);
        guiGraphics.text(minecraft.font, readout, x + PANEL_W - 8 - readoutWidth, y + 9, readoutColor, false);
    }

    private SubmarineHudOverlay() {
    }
}
