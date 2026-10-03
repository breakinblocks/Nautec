package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.sides.RelativeFace;
import com.breakinblocks.nautec.api.sides.SideKind;
import com.breakinblocks.nautec.api.sides.SideMode;
import com.breakinblocks.nautec.network.SetSideConfigPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class SideConfigPanel {
    private static final int OUTLINE = 0xFF1E2221;
    private static final int PANEL = 0xFFC8C7B3;
    private static final int LIGHT = 0xFFE7E7D6;
    private static final int TEXT = 0xFF1E2221;
    private static final int TAB = 18;
    private static final int CELL = 16;
    private static final int PITCH = 18;
    private static final int WIDTH = 62;
    private static final RelativeFace[][] GRID = {
            {null, RelativeFace.TOP, null},
            {RelativeFace.LEFT, RelativeFace.FRONT, RelativeFace.RIGHT},
            {null, RelativeFace.BOTTOM, RelativeFace.BACK}
    };

    private final ContainerBlockEntity machine;
    private final int containerId;
    private final boolean leftward;
    private boolean open;
    private SideKind kind;

    private SideConfigPanel(ContainerBlockEntity machine, int containerId, boolean leftward) {
        this.machine = machine;
        this.containerId = containerId;
        this.leftward = leftward;
        this.kind = machine.hasSideConfig(SideKind.ITEMS) ? SideKind.ITEMS : SideKind.FLUIDS;
    }

    public static @Nullable SideConfigPanel create(ContainerBlockEntity machine, int containerId) {
        return create(machine, containerId, false);
    }

    public static @Nullable SideConfigPanel create(ContainerBlockEntity machine, int containerId, boolean leftward) {
        return machine.hasSideConfig() ? new SideConfigPanel(machine, containerId, leftward) : null;
    }

    private boolean bothKinds() {
        return machine.hasSideConfig(SideKind.ITEMS) && machine.hasSideConfig(SideKind.FLUIDS);
    }

    private int gridTop() {
        return bothKinds() ? 34 : 22;
    }

    private int height() {
        return gridTop() + 3 * PITCH + 2;
    }

    public Rect2i area(int anchorX, int anchorY) {
        int w = open ? WIDTH : TAB;
        int h = open ? height() : TAB;
        return new Rect2i(leftward ? anchorX - w : anchorX, anchorY, w, h);
    }

    public boolean contains(double mouseX, double mouseY, int anchorX, int anchorY) {
        Rect2i area = area(anchorX, anchorY);
        return mouseX >= area.getX() && mouseX < area.getX() + area.getWidth() && mouseY >= area.getY() && mouseY < area.getY() + area.getHeight();
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private void box(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, OUTLINE);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, LIGHT);
        g.fill(x + 2, y + 2, x + w - 1, y + h - 1, PANEL);
    }

    private void icon(GuiGraphicsExtractor g, int x, int y) {
        g.fill(x + 4, y + 4, x + 14, y + 14, OUTLINE);
        g.fill(x + 5, y + 5, x + 9, y + 9, SideMode.INPUT.color());
        g.fill(x + 9, y + 5, x + 13, y + 9, SideMode.OUTPUT.color());
        g.fill(x + 5, y + 9, x + 9, y + 13, SideMode.BOTH.color());
        g.fill(x + 9, y + 9, x + 13, y + 13, SideMode.NONE.color());
    }

    public void extract(GuiGraphicsExtractor g, Font font, int anchorX, int anchorY, int mouseX, int mouseY) {
        Rect2i area = area(anchorX, anchorY);
        int x = area.getX();
        int y = area.getY();
        if (!open) {
            box(g, x, y, TAB, TAB);
            icon(g, x, y);
            if (inside(mouseX, mouseY, x, y, TAB, TAB)) {
                g.setComponentTooltipForNextFrame(font, List.of(Component.translatable("nautec.side_config.title")), mouseX, mouseY);
            }
            return;
        }

        box(g, x, y, WIDTH, height());
        icon(g, x, y);
        g.text(font, Component.translatable("nautec.side_config.short"), x + 20, y + 6, TEXT, false);

        if (bothKinds()) {
            for (SideKind option : SideKind.values()) {
                int bx = x + 4 + option.ordinal() * 28;
                int by = y + 20;
                boolean selected = option == kind;
                g.fill(bx, by, bx + 26, by + 11, OUTLINE);
                g.fill(bx + 1, by + 1, bx + 25, by + 10, selected ? 0xFF45504A : LIGHT);
                Component label = Component.translatable(option.translationKey() + ".short");
                g.text(font, label, bx + 13 - font.width(label) / 2, by + 2, selected ? LIGHT : TEXT, false);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                RelativeFace face = GRID[row][column];
                if (face == null) {
                    continue;
                }
                int cx = x + 5 + column * PITCH;
                int cy = y + gridTop() + row * PITCH;
                SideMode mode = machine.getSideConfig().get(kind, face);
                g.fill(cx, cy, cx + CELL, cy + CELL, OUTLINE);
                g.fill(cx + 1, cy + 1, cx + CELL - 1, cy + CELL - 1, mode.color());
                Component letter = Component.translatable(face.translationKey() + ".short");
                g.text(font, letter, cx + CELL / 2 - font.width(letter) / 2 + 1, cy + 4, TEXT, false);
                if (inside(mouseX, mouseY, cx, cy, CELL, CELL)) {
                    g.setComponentTooltipForNextFrame(font, List.of(
                            Component.translatable("nautec.side_config.tooltip", Component.translatable(face.translationKey()),
                                    Component.translatable(kind.translationKey()), Component.translatable(mode.translationKey())),
                            Component.translatable(mode.translationKey() + ".desc").withStyle(ChatFormatting.GRAY),
                            Component.translatable("nautec.side_config.click").withStyle(ChatFormatting.DARK_GRAY)
                    ), mouseX, mouseY);
                }
            }
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, int anchorX, int anchorY) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (!contains(mouseX, mouseY, anchorX, anchorY)) {
            return false;
        }
        Rect2i area = area(anchorX, anchorY);
        int x = area.getX();
        int y = area.getY();
        if (inside(mouseX, mouseY, x, y, TAB, TAB)) {
            open = !open;
            return true;
        }
        if (!open) {
            return true;
        }
        if (bothKinds()) {
            for (SideKind option : SideKind.values()) {
                if (inside(mouseX, mouseY, x + 4 + option.ordinal() * 28, y + 20, 26, 11)) {
                    kind = option;
                    return true;
                }
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                RelativeFace face = GRID[row][column];
                if (face != null && inside(mouseX, mouseY, x + 5 + column * PITCH, y + gridTop() + row * PITCH, CELL, CELL)) {
                    SideMode current = machine.getSideConfig().get(kind, face);
                    SideMode next = event.button() == 1 ? current.previous() : current.next();
                    machine.getSideConfig().set(kind, face, next);
                    ClientPacketDistributor.sendToServer(new SetSideConfigPayload(containerId, kind, face, next));
                    return true;
                }
            }
        }
        return true;
    }
}
