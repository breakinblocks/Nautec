package com.breakinblocks.nautec.client.hud;

import com.breakinblocks.nautec.NTClientConfig;
import com.breakinblocks.nautec.client.screen.SubmarineHudPositionScreen;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

public final class SubmarineHudOverlay {
    public static final int PANEL_W = 92;
    public static final int PANEL_H = 28;

    private static final int CYAN = 0xFF3EFDFF;
    private static final int CYAN_DIM = 0xFF19646B;
    private static final int AMBER = 0xFFFFB03C;
    private static final int RED = 0xFFFF5A3C;
    private static final int RED_DIM = 0xFF6B231A;
    private static final int WHITE = 0xFFF0F4F5;
    private static final int PLATE = 0xD9070B10;
    private static final int CELL_EMPTY = 0x26FFFFFF;

    private static final int CELLS = 8;
    private static final int CELL_W = 4;
    private static final int CELL_H = 6;
    private static final int CELL_GAP = 1;

    private static final int POWER_ROW = 5;
    private static final int HULL_ROW = 15;

    public static void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen instanceof SubmarineHudPositionScreen) {
            return;
        }

        if (!(minecraft.player.getVehicle() instanceof SubmarineEntity submarine)) {
            return;
        }

        int x = panelX(guiGraphics.guiWidth(), NTClientConfig.hudX());
        int y = panelY(guiGraphics.guiHeight(), NTClientConfig.hudY());
        long ticks = minecraft.level != null ? minecraft.level.getGameTime() : 0L;
        drawGauge(guiGraphics, x, y, submarine.getPowerStored(), submarine.getPowerStorage().getPowerCapacity(),
                submarine.getHealth(), submarine.getMaxHealth(), ticks);
        drawControls(guiGraphics, x, y + PANEL_H + 3);
    }

    /**
     * A quiet reminder of the controls under the gauges. Piloting is not a vanilla scheme, so leaving
     * players to guess at it was the wrong call.
     */
    private static void drawControls(GuiGraphicsExtractor guiGraphics, int x, int y) {
        Minecraft minecraft = Minecraft.getInstance();
        String[] lines = {
                "W/S  throttle",
                "A/D  rudder",
                "SPACE  rise",
                "C  dive",
                "USE  steer"
        };

        int row = y;
        for (String line : lines) {
            guiGraphics.text(minecraft.font, line, x + 9, row, CYAN_DIM, false);
            row += 9;
        }
    }

    public static int panelX(int guiWidth, double fraction) {
        return (int) Math.round(fraction * (guiWidth - PANEL_W));
    }

    public static int panelY(int guiHeight, double fraction) {
        return (int) Math.round(fraction * (guiHeight - PANEL_H));
    }

    public static void drawGauge(GuiGraphicsExtractor guiGraphics, int x, int y, int power, int capacity,
                                 float health, float maxHealth, long ticks) {
        float powerFraction = capacity > 0 ? Mth.clamp((float) power / capacity, 0F, 1F) : 0F;
        float hullFraction = maxHealth > 0F ? Mth.clamp(health / maxHealth, 0F, 1F) : 0F;
        boolean blinkOn = ticks % 16 < 8;

        for (int row = 0; row < PANEL_H; row++) {
            int slant = (PANEL_H - 1 - row) / 4;
            guiGraphics.fill(x + slant, y + row, x + slant + PANEL_W - 6, y + row + 1, PLATE);
        }

        for (int row = 0; row < 2; row++) {
            int slant = (PANEL_H - 1 - row) / 4;
            guiGraphics.fill(x + slant, y + row, x + slant + PANEL_W - 6, y + row + 1, CYAN);
        }

        guiGraphics.fill(x + 5, y + 5, x + 6, y + PANEL_H - 3, CYAN);

        Minecraft minecraft = Minecraft.getInstance();

        boolean lowPower = powerFraction < 0.2F;
        drawRow(guiGraphics, x, y + POWER_ROW, "PWR", powerFraction, lowPower && !blinkOn ? CYAN_DIM : CYAN);
        String powerReadout = power <= 0 ? "CHG" : Math.round(powerFraction * 100F) + "%";
        int powerColor = power <= 0 ? (blinkOn ? CYAN : CYAN_DIM) : WHITE;
        drawReadout(guiGraphics, x, y + POWER_ROW, powerReadout, powerColor);

        boolean lowHull = hullFraction < 0.35F;
        int hullColor = lowHull ? (hullFraction < 0.15F && !blinkOn ? RED_DIM : RED) : CYAN;
        drawRow(guiGraphics, x, y + HULL_ROW, "HULL", hullFraction, hullColor);
        String hullReadout = Math.round(hullFraction * 100F) + "%";
        drawReadout(guiGraphics, x, y + HULL_ROW, hullReadout, lowHull ? AMBER : WHITE);
    }

    private static void drawRow(GuiGraphicsExtractor guiGraphics, int x, int rowY, String label, float fraction, int color) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.text(minecraft.font, label, x + 9, rowY + 1, WHITE, false);

        int filled = Mth.ceil(fraction * CELLS);
        int cellX = x + 32;
        for (int i = 0; i < CELLS; i++) {
            guiGraphics.fill(cellX, rowY + 1, cellX + CELL_W, rowY + 1 + CELL_H, i < filled ? color : CELL_EMPTY);
            cellX += CELL_W + CELL_GAP;
        }
    }

    private static void drawReadout(GuiGraphicsExtractor guiGraphics, int x, int rowY, String readout, int color) {
        Minecraft minecraft = Minecraft.getInstance();
        int width = minecraft.font.width(readout);
        guiGraphics.text(minecraft.font, readout, x + PANEL_W - 6 - width, rowY + 1, color, false);
    }

    private SubmarineHudOverlay() {
    }
}
