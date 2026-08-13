package com.breakinblocks.nautec.client.hud;

import com.breakinblocks.nautec.NTClientConfig;
import com.breakinblocks.nautec.client.screen.SubmarineHudPositionScreen;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

public final class SubmarineHudOverlay {
    public static final int PANEL_W = 116;
    public static final int PANEL_H = 27;

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
    private static final int CELL_H = 7;
    private static final int CELL_GAP = 1;

    private static final int PAD_LEFT = 9;
    private static final int PAD_RIGHT = 8;
    private static final int COL_GAP = 5;
    private static final int SLANT = (PANEL_H - 1) / 4;

    private static final int POWER_ROW = 5;
    private static final int HULL_ROW = 15;

    private static final String POWER_LABEL = "PWR";
    private static final String HULL_LABEL = "HULL";

    private static final String[][] CONTROLS = {
            {"W/S", "throttle"},
            {"MOUSE", "steer"},
            {"SPACE", "rise"},
            {"C", "dive"},
            {"USE", "free look"}
    };

    private static final int CONTROL_ROW_H = 9;
    private static final int CONTROL_TOP_GAP = 4;

    public static final int TOTAL_H = PANEL_H + CONTROL_TOP_GAP + CONTROLS.length * CONTROL_ROW_H;

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
        drawPanel(guiGraphics, x, y, submarine.getPowerStored(), submarine.getPowerStorage().getPowerCapacity(),
                submarine.getHealth(), submarine.getMaxHealth(), ticks);
    }

    public static int panelX(int guiWidth, double fraction) {
        return (int) Math.round(fraction * (guiWidth - PANEL_W));
    }

    public static int panelY(int guiHeight, double fraction) {
        return (int) Math.round(fraction * (guiHeight - TOTAL_H));
    }

    public static void drawPanel(GuiGraphicsExtractor guiGraphics, int x, int y, int power, int capacity,
                                 float health, float maxHealth, long ticks) {
        drawGauge(guiGraphics, x, y, power, capacity, health, maxHealth, ticks);
        drawControls(guiGraphics, x, y + PANEL_H + CONTROL_TOP_GAP);
    }

    private static void drawGauge(GuiGraphicsExtractor guiGraphics, int x, int y, int power, int capacity,
                                  float health, float maxHealth, long ticks) {
        Font font = Minecraft.getInstance().font;
        float powerFraction = capacity > 0 ? Mth.clamp((float) power / capacity, 0F, 1F) : 0F;
        float hullFraction = maxHealth > 0F ? Mth.clamp(health / maxHealth, 0F, 1F) : 0F;
        boolean blinkOn = ticks % 16 < 8;

        for (int row = 0; row < PANEL_H; row++) {
            int slant = (PANEL_H - 1 - row) / 4;
            guiGraphics.fill(x + slant, y + row, x + slant + PANEL_W - SLANT, y + row + 1, PLATE);
        }

        for (int row = 0; row < 2; row++) {
            guiGraphics.fill(x + SLANT, y + row, x + PANEL_W, y + row + 1, CYAN);
        }

        guiGraphics.fill(x + SLANT, y + 5, x + SLANT + 1, y + PANEL_H - 4, CYAN);

        int labelWidth = Math.max(font.width(POWER_LABEL), font.width(HULL_LABEL));
        int cellsX = x + PAD_LEFT + labelWidth + COL_GAP;
        int readoutRight = x + PANEL_W - PAD_RIGHT;

        boolean lowPower = powerFraction < 0.2F;
        drawRow(guiGraphics, font, x, cellsX, y + POWER_ROW, POWER_LABEL, powerFraction,
                lowPower && !blinkOn ? CYAN_DIM : CYAN);
        String powerReadout = power <= 0 ? "CHG" : Math.round(powerFraction * 100F) + "%";
        int powerColor = power <= 0 ? (blinkOn ? CYAN : CYAN_DIM) : WHITE;
        drawReadout(guiGraphics, font, readoutRight, y + POWER_ROW, powerReadout, powerColor);

        boolean lowHull = hullFraction < 0.35F;
        int hullColor = lowHull ? (hullFraction < 0.15F && !blinkOn ? RED_DIM : RED) : CYAN;
        drawRow(guiGraphics, font, x, cellsX, y + HULL_ROW, HULL_LABEL, hullFraction, hullColor);
        String hullReadout = Math.round(hullFraction * 100F) + "%";
        drawReadout(guiGraphics, font, readoutRight, y + HULL_ROW, hullReadout, lowHull ? AMBER : WHITE);
    }

    private static void drawRow(GuiGraphicsExtractor guiGraphics, Font font, int x, int cellsX, int rowY,
                                String label, float fraction, int color) {
        guiGraphics.text(font, label, x + PAD_LEFT, rowY + 1, WHITE, false);

        int filled = Mth.ceil(fraction * CELLS);
        int cellX = cellsX;
        for (int i = 0; i < CELLS; i++) {
            guiGraphics.fill(cellX, rowY + 1, cellX + CELL_W, rowY + 1 + CELL_H, i < filled ? color : CELL_EMPTY);
            cellX += CELL_W + CELL_GAP;
        }
    }

    private static void drawReadout(GuiGraphicsExtractor guiGraphics, Font font, int readoutRight, int rowY,
                                    String readout, int color) {
        guiGraphics.text(font, readout, readoutRight - font.width(readout), rowY + 1, color, false);
    }

    private static void drawControls(GuiGraphicsExtractor guiGraphics, int x, int y) {
        Font font = Minecraft.getInstance().font;
        int keyWidth = 0;
        for (String[] control : CONTROLS) {
            keyWidth = Math.max(keyWidth, font.width(control[0]));
        }

        int row = y;
        for (String[] control : CONTROLS) {
            guiGraphics.text(font, control[0], x + PAD_LEFT, row, CYAN, false);
            guiGraphics.text(font, control[1], x + PAD_LEFT + keyWidth + COL_GAP, row, CYAN_DIM, false);
            row += CONTROL_ROW_H;
        }
    }

    private SubmarineHudOverlay() {
    }
}
