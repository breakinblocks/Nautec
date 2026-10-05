package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class PanelStyle {
    public static final int PANEL = 0xFFC8C7B3;
    public static final int PANEL_LIGHT = 0xFFE7E7D6;
    public static final int OUTLINE = 0xFF070707;
    public static final int SLOT_EDGE = 0xFF1E2221;
    public static final int SLOT_FILL = 0xFF45504A;
    public static final int GHOST_FILL = 0xFF2F3A35;
    public static final int SCREEN_FILL = 0xFF16201F;
    public static final int SCREEN_EDGE = 0xFF2E3A37;
    public static final int LABEL = 0xFF404040;
    public static final int READOUT = 0xFFB3FCFF;
    public static final int READOUT_DIM = 0xFF6FA6A8;
    public static final int ENERGY_FILL = 0xFFD8443C;
    public static final int ENERGY_SHINE = 0xFFF29A8C;
    public static final int SEND_COLOR = 0xFF2E7D4F;
    public static final int SEND_HOVER = 0xFF3C9A63;
    public static final int RECEIVE_COLOR = 0xFF2F5C8C;
    public static final int RECEIVE_HOVER = 0xFF3C73A8;
    public static final int NEUTRAL = 0xFF45504A;
    public static final int NEUTRAL_HOVER = 0xFF5A6A62;
    public static final int DANGER = 0xFF8C2F2F;
    public static final int DANGER_HOVER = 0xFFA83C3C;
    public static final int ONLINE = 0xFF5FE8B0;
    public static final int OFFLINE = 0xFFE36A5C;
    public static final int WARNING = 0xFFE8C35F;
    public static final int TICK = 0xB016201F;
    public static final int GHOST_FADE = 0xA045504A;

    public static final Identifier BACTERIA_SLOT = Nautec.rl("container/bacteria_slot");
    public static final Identifier ICON_NUTRIENT = Nautec.rl("container/icons/nutrient");
    public static final Identifier ICON_UPGRADE = Nautec.rl("container/icons/upgrade");
    public static final Identifier ICON_CLAW = Nautec.rl("container/icons/claw");
    public static final Identifier ICON_PETRI_DISH = Nautec.rl("container/icons/petri_dish");
    public static final Identifier ICON_WHISK = Nautec.rl("container/icons/whisk");

    private PanelStyle() {
    }

    public static void panel(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, OUTLINE);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_LIGHT);
        graphics.fill(x + 3, y + 3, x + width - 1, y + height - 1, PANEL);
    }

    public static void screen(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, OUTLINE);
        graphics.fill(x, y, x + width, y + height, SCREEN_EDGE);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, SCREEN_FILL);
    }

    public static void slot(GuiGraphicsExtractor graphics, int x, int y) {
        slot(graphics, x, y, SLOT_FILL);
    }

    public static void slot(GuiGraphicsExtractor graphics, int x, int y, int fill) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, SLOT_EDGE);
        graphics.fill(x, y, x + 16, y + 16, fill);
    }

    public static void bacteriaSlot(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACTERIA_SLOT, x, y, 18, 18);
    }

    public static void tank(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, SLOT_EDGE);
        graphics.fill(x, y, x + width, y + height, SCREEN_FILL);
    }

    public static void tankTicks(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        for (int ty = y + height - 4; ty > y; ty -= 4) {
            int length = (y + height - ty) % 8 == 0 ? 5 : 3;
            graphics.fill(x + width - length, ty, x + width, ty + 1, TICK);
        }
    }

    public static void icon(GuiGraphicsExtractor graphics, Identifier sprite, int x, int y, int width, int height) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
    }

    public static void bar(GuiGraphicsExtractor graphics, int x, int y, int width, int height, float fraction, int fill, int shine) {
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, SLOT_EDGE);
        graphics.fill(x, y, x + width, y + height, SCREEN_FILL);
        int filled = Math.round(height * Math.max(0F, Math.min(1F, fraction)));
        if (filled > 0) {
            graphics.fill(x, y + height - filled, x + width, y + height, fill);
            graphics.fill(x, y + height - filled, x + 1, y + height, shine);
        }
    }

    public static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
