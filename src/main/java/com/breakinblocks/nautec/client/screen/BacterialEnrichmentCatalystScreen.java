package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.api.menu.slots.SlotBacteriaStorage;
import com.breakinblocks.nautec.content.blockentities.BacterialEnrichmentCatalystBlockEntity;
import com.breakinblocks.nautec.content.menus.BacterialEnrichmentCatalystMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BacterialEnrichmentCatalystScreen extends NTMachineScreen<BacterialEnrichmentCatalystBlockEntity> {
    private static final String KEY = "nautec.enrichment_catalyst.";
    private static final String[] STATUS_NAMES = {
            "no_crystal", "wild_crystal", "shared", "too_impure", "no_source", "low_power", "no_colony", "small_colony", "overreach",
            "at_target", "at_cap", "paused", "running"
    };
    private static final int STATUS_X = 8;
    private static final int STATUS_Y = 17;
    private static final int STATUS_WIDTH = 160;
    private static final int PANEL_X = 8;
    private static final int PANEL_Y = 29;
    private static final int PANEL_WIDTH = 160;
    private static final int PANEL_HEIGHT = 72;
    private static final int ROW_HEIGHT = 9;
    private static final int REQUIREMENTS_Y = PANEL_Y + 4;
    private static final int READOUT_ROW_Y = PANEL_Y + 34;
    private static final int COLUMN_WIDTH = 72;
    private static final int RIGHT_COLUMN_X = PANEL_X + 83;
    private static final int BAR_X = PANEL_X + 5;
    private static final int BAR_Y = PANEL_Y + 55;
    private static final int BAR_WIDTH = PANEL_WIDTH - 10;
    private static final int BAR_HEIGHT = 4;
    private static final int AP_Y = PANEL_Y + 61;
    private static final int TARGET_X = 70;
    private static final int TARGET_Y = 104;
    private static final int TARGET_WIDTH = 98;
    private static final int TARGET_HEIGHT = 40;
    private static final int BUTTON_WIDTH = 22;
    private static final int BUTTON_HEIGHT = 11;
    private static final int BUTTON_GAP = 2;
    private static final int BUTTON_Y = TARGET_Y + 25;
    private static final int BAR_FILL = 0xFF2AA3BE;
    private static final int BAR_SHINE = 0xFF86E7F3;
    private static final int WAIT_COLOR = 0xFF8C6A1E;

    public BacterialEnrichmentCatalystScreen(NTMachineMenu<BacterialEnrichmentCatalystBlockEntity> menu, Inventory playerInventory,
                                             Component title) {
        super(menu, playerInventory, title, 176, BacterialEnrichmentCatalystMenu.INVENTORY_Y + 82);
        this.inventoryLabelY = BacterialEnrichmentCatalystMenu.INVENTORY_Y - 11;
    }

    private BacterialEnrichmentCatalystMenu catalyst() {
        return (BacterialEnrichmentCatalystMenu) this.menu;
    }

    private void press(int id) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
        }
    }

    private void addTargetButton(int index, String label, String tooltipKey, int id) {
        int x = this.leftPos + TARGET_X + 2 + index * (BUTTON_WIDTH + BUTTON_GAP);
        addRenderableWidget(new NTPanelButton(this.font, x, this.topPos + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT,
                () -> Component.literal(label), () -> PanelStyle.NEUTRAL, () -> PanelStyle.NEUTRAL_HOVER,
                () -> Component.translatable(KEY + tooltipKey), () -> press(id)));
    }

    @Override
    protected void init() {
        super.init();
        addTargetButton(0, "-0.5", "target_down", BacterialEnrichmentCatalystBlockEntity.BUTTON_TARGET_DOWN);
        addTargetButton(1, "-0.1", "target_down_fine", BacterialEnrichmentCatalystBlockEntity.BUTTON_TARGET_DOWN_FINE);
        addTargetButton(2, "+0.1", "target_up_fine", BacterialEnrichmentCatalystBlockEntity.BUTTON_TARGET_UP_FINE);
        addTargetButton(3, "+0.5", "target_up", BacterialEnrichmentCatalystBlockEntity.BUTTON_TARGET_UP);
    }

    private record Requirement(Component line, boolean met, Component description) {
    }

    private record Readout(Component line, Component description) {
    }

    private static String purity(float value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String count(long value) {
        return NumberFormat.getIntegerInstance(Locale.ROOT).format(value);
    }

    private int statusId() {
        return Math.clamp(catalyst().getStatus(), 0, STATUS_NAMES.length - 1);
    }

    private float cycleFraction() {
        int cycle = catalyst().getCycleAp();
        return cycle <= 0 ? 0F : Math.min(1F, (float) catalyst().getProgress() / cycle);
    }

    private Component statusText() {
        String key = KEY + "status." + STATUS_NAMES[statusId()];
        if (statusId() == BacterialEnrichmentCatalystBlockEntity.STATUS_RUNNING) {
            return Component.translatable(key, Math.round(cycleFraction() * 100F));
        }
        return Component.translatable(key);
    }

    private Component statusDescription() {
        BacterialEnrichmentCatalystMenu menu = catalyst();
        String key = KEY + "status." + STATUS_NAMES[statusId()] + ".desc";
        return switch (statusId()) {
            case BacterialEnrichmentCatalystBlockEntity.STATUS_TOO_IMPURE ->
                    Component.translatable(key, String.format(Locale.ROOT, "%.1f", NTConfig.enrichmentMinCrystalPurity));
            case BacterialEnrichmentCatalystBlockEntity.STATUS_LOW_POWER -> Component.translatable(key, count(NTConfig.enrichmentMinPower));
            case BacterialEnrichmentCatalystBlockEntity.STATUS_SMALL_COLONY -> Component.translatable(key, count(menu.getColonyCost()));
            case BacterialEnrichmentCatalystBlockEntity.STATUS_OVERREACH ->
                    Component.translatable(key, String.format(Locale.ROOT, "%.1f", NTConfig.enrichmentMaxOverreach));
            case BacterialEnrichmentCatalystBlockEntity.STATUS_AT_CAP ->
                    Component.translatable(key, String.format(Locale.ROOT, "%.1f", NTConfig.enrichmentMaxPurity));
            default -> Component.translatable(key);
        };
    }

    private int statusColor() {
        return switch (statusId()) {
            case BacterialEnrichmentCatalystBlockEntity.STATUS_RUNNING -> PanelStyle.SEND_COLOR;
            case BacterialEnrichmentCatalystBlockEntity.STATUS_AT_TARGET, BacterialEnrichmentCatalystBlockEntity.STATUS_AT_CAP,
                 BacterialEnrichmentCatalystBlockEntity.STATUS_PAUSED -> WAIT_COLOR;
            default -> PanelStyle.DANGER;
        };
    }

    private boolean hasCrystal() {
        return catalyst().getCrystalPurity() > 0F;
    }

    private List<Requirement> requirements() {
        BacterialEnrichmentCatalystMenu menu = catalyst();
        List<Requirement> rows = new ArrayList<>(3);
        int power = menu.getPower();
        int needed = NTConfig.enrichmentMinPower;
        rows.add(new Requirement(Component.translatable(KEY + "req.beam", count(power), count(needed)), power >= needed,
                Component.translatable(KEY + "req.beam.desc", count(needed))));

        float minPurity = (float) NTConfig.enrichmentMinCrystalPurity;
        int status = statusId();
        boolean crystalOk = status != BacterialEnrichmentCatalystBlockEntity.STATUS_NO_CRYSTAL
                && status != BacterialEnrichmentCatalystBlockEntity.STATUS_WILD_CRYSTAL
                && menu.getCrystalPurity() >= minPurity - 0.0001F;
        String minText = String.format(Locale.ROOT, "%.1f", minPurity);
        rows.add(new Requirement(Component.translatable(KEY + "req.crystal", minText), crystalOk,
                Component.translatable(KEY + "req.crystal.desc", minText)));

        BacteriaInstance colony = this.menu.blockEntity.getBacteriaStorage().getBacteria(0);
        long cost = hasCrystal() ? menu.getColonyCost() : 0L;
        boolean colonyOk = !colony.isEmpty() && colony.getSize() >= cost;
        rows.add(new Requirement(Component.translatable(KEY + "req.colony", count(colony.getSize()), count(cost)), colonyOk,
                Component.translatable(KEY + "req.colony.desc")));
        return rows;
    }

    private List<Readout> readouts() {
        BacterialEnrichmentCatalystMenu menu = catalyst();
        boolean crystal = hasCrystal();
        boolean source = menu.getPower() > 0;
        String none = "-";
        return List.of(
                new Readout(Component.translatable(KEY + "readout.crystal", crystal ? purity(menu.getCrystalPurity()) : none),
                        Component.translatable(KEY + "readout.crystal.desc")),
                new Readout(Component.translatable(KEY + "readout.source", source ? purity(menu.getSourcePurity()) : none),
                        Component.translatable(KEY + "readout.source.desc")),
                new Readout(Component.translatable(KEY + "readout.chance",
                        crystal && source ? String.format(Locale.ROOT, "%.1f", menu.getRegressionChance() * 100F) : none),
                        Component.translatable(KEY + "readout.chance.desc")),
                new Readout(Component.translatable(KEY + "readout.colony", crystal ? count(menu.getColonyCost()) : none),
                        Component.translatable(KEY + "readout.colony.desc")));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        BacterialEnrichmentCatalystMenu menu = catalyst();
        PanelStyle.scrollingText(graphics, this.font, statusText(), leftPos + STATUS_X, topPos + STATUS_Y, STATUS_WIDTH, statusColor());

        PanelStyle.screen(graphics, leftPos + PANEL_X, topPos + PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT);
        List<Requirement> rows = requirements();
        for (int i = 0; i < rows.size(); i++) {
            Requirement row = rows.get(i);
            int rowY = topPos + REQUIREMENTS_Y + i * ROW_HEIGHT;
            graphics.fill(leftPos + PANEL_X + 5, rowY + 2, leftPos + PANEL_X + 9, rowY + 6, row.met() ? PanelStyle.ONLINE : PanelStyle.OFFLINE);
            graphics.text(this.font, row.line(), leftPos + PANEL_X + 13, rowY, row.met() ? PanelStyle.READOUT : PanelStyle.OFFLINE, false);
        }

        List<Readout> readouts = readouts();
        for (int i = 0; i < readouts.size(); i++) {
            int x = leftPos + (i % 2 == 0 ? PANEL_X + 5 : RIGHT_COLUMN_X);
            int y = topPos + READOUT_ROW_Y + (i / 2) * ROW_HEIGHT;
            graphics.text(this.font, readouts.get(i).line(), x, y, PanelStyle.READOUT, false);
        }

        int barX = leftPos + BAR_X;
        int barY = topPos + BAR_Y;
        graphics.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, PanelStyle.SLOT_EDGE);
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, PanelStyle.SCREEN_FILL);
        int filled = Math.round(BAR_WIDTH * cycleFraction());
        if (filled > 0) {
            graphics.fill(barX, barY, barX + filled, barY + BAR_HEIGHT, BAR_FILL);
            graphics.fill(barX, barY, barX + filled, barY + 1, BAR_SHINE);
        }
        Component ap = Component.translatable(KEY + "progress", count(menu.getProgress()), count(menu.getCycleAp()));
        graphics.text(this.font, ap, leftPos + PANEL_X + PANEL_WIDTH / 2 - this.font.width(ap) / 2, topPos + AP_Y, PanelStyle.READOUT_DIM, false);

        PanelStyle.screen(graphics, leftPos + TARGET_X, topPos + TARGET_Y, TARGET_WIDTH, TARGET_HEIGHT);
        Component label = Component.translatable(KEY + "target");
        graphics.text(this.font, label, leftPos + TARGET_X + TARGET_WIDTH / 2 - this.font.width(label) / 2, topPos + TARGET_Y + 4,
                PanelStyle.READOUT_DIM, false);
        Component value = Component.literal(purity(menu.getTargetPurity()));
        graphics.text(this.font, value, leftPos + TARGET_X + TARGET_WIDTH / 2 - this.font.width(value) / 2, topPos + TARGET_Y + 14,
                PanelStyle.READOUT, false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (isHovering(STATUS_X, STATUS_Y - 1, STATUS_WIDTH, this.font.lineHeight + 1, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(this.font, List.of(statusText(), statusDescription().copy().withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            return;
        }
        List<Requirement> rows = requirements();
        for (int i = 0; i < rows.size(); i++) {
            if (isHovering(PANEL_X, REQUIREMENTS_Y - 1 + i * ROW_HEIGHT, PANEL_WIDTH, ROW_HEIGHT, mouseX, mouseY)) {
                Requirement row = rows.get(i);
                graphics.setComponentTooltipForNextFrame(this.font, List.of(row.line(), row.description().copy().withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
                return;
            }
        }
        List<Readout> readouts = readouts();
        for (int i = 0; i < readouts.size(); i++) {
            int x = i % 2 == 0 ? PANEL_X + 5 : RIGHT_COLUMN_X;
            int y = READOUT_ROW_Y + (i / 2) * ROW_HEIGHT - 1;
            if (isHovering(x, y, COLUMN_WIDTH, ROW_HEIGHT, mouseX, mouseY)) {
                Readout readout = readouts.get(i);
                graphics.setComponentTooltipForNextFrame(this.font, List.of(readout.line(), readout.description().copy().withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
                return;
            }
        }
        if (isHovering(BAR_X, BAR_Y - 1, BAR_WIDTH, AP_Y + this.font.lineHeight - BAR_Y + 1, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable(KEY + "progress.title"),
                    Component.translatable(KEY + "progress.desc").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            return;
        }
        if (isHovering(TARGET_X, TARGET_Y, TARGET_WIDTH, 24, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable(KEY + "target"),
                    Component.translatable(KEY + "target.desc").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            return;
        }
        if (this.menu.getCarried().isEmpty()) {
            for (SlotBacteriaStorage slot : this.menu.getBacteriaStorageSlots()) {
                if (isHovering(slot.getX(), slot.getY(), slot.getWidth(), slot.getHeight(), mouseX, mouseY)
                        && slot.getBacteriaStorage().getBacteria(slot.getSlot()).isEmpty()) {
                    graphics.setComponentTooltipForNextFrame(this.font, List.of(
                            Component.translatable(KEY + "colony_slot"),
                            Component.translatable(KEY + "colony_slot.desc").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
                    return;
                }
            }
        }
    }
}
