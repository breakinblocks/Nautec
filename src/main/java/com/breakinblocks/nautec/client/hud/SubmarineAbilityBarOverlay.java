package com.breakinblocks.nautec.client.hud;

import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.items.submarine.SubmarineModuleItem;
import com.breakinblocks.nautec.content.items.submarine.SubmarineModuleType;
import com.breakinblocks.nautec.content.items.submarine.TeleportModuleItem;
import com.breakinblocks.nautec.data.components.TeleportAnchor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public final class SubmarineAbilityBarOverlay {
    private static final int CELL = 20;
    private static final int GAP = 2;
    private static final int BAR_BOTTOM_MARGIN = 6;

    private static final int CYAN = 0xFF3EFDFF;
    private static final int CYAN_DIM = 0xFF19646B;
    private static final int WHITE = 0xFFF0F4F5;
    private static final int PLATE = 0xD9070B10;
    private static final int EDGE = 0x66FFFFFF;
    private static final int SWEEP = 0x9E0B1118;
    private static final int PASSIVE = 0x593EFDFF;

    private SubmarineAbilityBarOverlay() {
    }

    public static void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        if (!(minecraft.player.getVehicle() instanceof SubmarineEntity submarine)) {
            return;
        }

        int slots = SubmarineEntity.MODULE_SLOTS;
        int width = slots * CELL + (slots - 1) * GAP;
        int x = (guiGraphics.guiWidth() - width) / 2;
        int y = guiGraphics.guiHeight() - CELL - BAR_BOTTOM_MARGIN;
        int selected = SubmarineAbilityBarState.selected();

        for (int slot = 0; slot < slots; slot++) {
            drawCell(guiGraphics, submarine, slot, x + slot * (CELL + GAP), y, slot == selected);
        }

        drawSelectionLabel(guiGraphics, submarine, selected, guiGraphics.guiWidth() / 2, y - 12);
    }

    private static void drawCell(GuiGraphicsExtractor guiGraphics, SubmarineEntity submarine, int slot, int x, int y, boolean selected) {
        guiGraphics.fill(x, y, x + CELL, y + CELL, PLATE);

        int border = selected ? CYAN : EDGE;
        guiGraphics.fill(x, y, x + CELL, y + 1, border);
        guiGraphics.fill(x, y + CELL - 1, x + CELL, y + CELL, border);
        guiGraphics.fill(x, y, x + 1, y + CELL, border);
        guiGraphics.fill(x + CELL - 1, y, x + CELL, y + CELL, border);

        ItemStack module = submarine.getModule(slot);
        if (module.isEmpty()) {
            Minecraft minecraft = Minecraft.getInstance();
            String label = String.valueOf(slot + 1);
            int labelX = x + (CELL - minecraft.font.width(label)) / 2;
            guiGraphics.text(minecraft.font, label, labelX, y + 7, CYAN_DIM, false);
            return;
        }

        guiGraphics.item(module, x + 2, y + 2);

        SubmarineModuleType type = SubmarineModuleItem.typeOf(module);
        if (type != null && type.isPassive()) {
            guiGraphics.fill(x + 1, y + CELL - 3, x + CELL - 1, y + CELL - 1, PASSIVE);
            return;
        }

        if (SubmarineAbilityBarState.isActive(slot)) {
            guiGraphics.fill(x + 1, y + CELL - 3, x + CELL - 1, y + CELL - 1, CYAN);
        }

        float progress = SubmarineAbilityBarState.cooldownProgress(slot);
        if (progress > 0F) {
            int height = Mth.ceil(progress * (CELL - 2));
            guiGraphics.fill(x + 1, y + CELL - 1 - height, x + CELL - 1, y + CELL - 1, SWEEP);
        }
    }

    private static void drawSelectionLabel(GuiGraphicsExtractor guiGraphics, SubmarineEntity submarine, int selected, int centerX, int y) {
        ItemStack module = submarine.getModule(selected);
        SubmarineModuleType type = SubmarineModuleItem.typeOf(module);
        if (type == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Component label = type.displayName();
        guiGraphics.text(minecraft.font, label, centerX - minecraft.font.width(label) / 2, y, WHITE, true);

        TeleportAnchor anchor = TeleportModuleItem.anchorOf(module);
        if (anchor == null) {
            return;
        }

        Component destination = Component.literal(anchor.pos().pos().toShortString());
        guiGraphics.text(minecraft.font, destination, centerX - minecraft.font.width(destination) / 2, y - 10, CYAN, true);
    }
}
