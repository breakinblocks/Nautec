package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.fusion.FusionControllerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.fusion.FusionStructure;
import com.breakinblocks.nautec.content.menus.FusionControllerMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FusionControllerScreen extends AbstractContainerScreen<FusionControllerMenu> {
    private static final int PANEL = PanelStyle.PANEL;
    private static final int PANEL_LIGHT = PanelStyle.PANEL_LIGHT;
    private static final int OUTLINE = PanelStyle.OUTLINE;
    private static final int SLOT_EDGE = PanelStyle.SLOT_EDGE;
    private static final int SCREEN_FILL = PanelStyle.SCREEN_FILL;
    private static final int SCREEN_EDGE = PanelStyle.SCREEN_EDGE;
    private static final int LABEL = PanelStyle.LABEL;
    private static final int READOUT = PanelStyle.READOUT;
    private static final int READOUT_DIM = PanelStyle.READOUT_DIM;
    private static final int HEAT_COLD = 0xFFE8873A;
    private static final int HEAT_HOT = 0xFF52E8FF;
    private static final int FUEL_FILL = 0xFF3F8FD6;
    private static final int FUEL_SHINE = 0xFF8CC4F2;
    private static final int ENERGY_FILL = PanelStyle.ENERGY_FILL;
    private static final int ENERGY_SHINE = PanelStyle.ENERGY_SHINE;
    private static final int GAUGE_FILL = 0xFF52E8FF;
    private static final int GAUGE_LIMIT = 0xFFE8873A;
    private static final int RUNNING_TEXT = 0xFF2E7D4F;
    private static final int WARMING_TEXT = 0xFF9A6A12;
    private static final int STOPPED_TEXT = 0xFF8C2F2F;

    private static final int IMAGE_WIDTH = 200;
    private static final int IMAGE_HEIGHT = 158;
    private static final int BAR_TOP = 20;
    private static final int BAR_HEIGHT = 96;
    private static final int BAR_WIDTH = 9;
    private static final int HEAT_X = 9;
    private static final int FUEL_X = 23;
    private static final int ENERGY_X = 37;
    private static final int READOUT_X = 54;
    private static final int READOUT_Y = 20;
    private static final int READOUT_WIDTH = 138;
    private static final int READOUT_HEIGHT = 96;
    private static final int LINE = 11;
    private static final int GAUGE_Y = 34;
    private static final int FOOTER_Y = 122;

    public FusionControllerScreen(FusionControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    private static String number(long value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    private static String decimal(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, OUTLINE);
        graphics.fill(x + 1, y + 1, x + this.imageWidth - 1, y + this.imageHeight - 1, PANEL_LIGHT);
        graphics.fill(x + 3, y + 3, x + this.imageWidth - 1, y + this.imageHeight - 1, PANEL);

        int heatColor = lerpColor(HEAT_COLD, HEAT_HOT, this.menu.getHeat());
        bar(graphics, x + HEAT_X, this.menu.getHeat(), heatColor, PANEL_LIGHT);
        bar(graphics, x + FUEL_X, this.menu.getFuel() / (float) FusionControllerBlockEntity.FUEL_CAPACITY, FUEL_FILL, FUEL_SHINE);
        bar(graphics, x + ENERGY_X, this.menu.getEnergy() / (float) Math.max(1, NTConfig.fusionEnergyBuffer), ENERGY_FILL, ENERGY_SHINE);

        int rx = x + READOUT_X;
        int ry = y + READOUT_Y;
        graphics.fill(rx - 1, ry - 1, rx + READOUT_WIDTH + 1, ry + READOUT_HEIGHT + 1, OUTLINE);
        graphics.fill(rx, ry, rx + READOUT_WIDTH, ry + READOUT_HEIGHT, SCREEN_EDGE);
        graphics.fill(rx + 1, ry + 1, rx + READOUT_WIDTH - 1, ry + READOUT_HEIGHT - 1, SCREEN_FILL);

        int gx = rx + 5;
        int gy = ry + GAUGE_Y - READOUT_Y;
        int gw = READOUT_WIDTH - 10;
        graphics.fill(gx - 1, gy - 1, gx + gw + 1, gy + 5, SCREEN_EDGE);
        graphics.fill(gx, gy, gx + gw, gy + 4, SLOT_EDGE);
        int ceiling = Math.max(1, NTConfig.fusionMaxOutput);
        int fill = Math.round(gw * Math.min(1F, this.menu.getOutput() / (float) ceiling));
        if (fill > 0) {
            graphics.fill(gx, gy, gx + fill, gy + 4, GAUGE_FILL);
        }
        if (this.menu.getCeiling() > 0) {
            int mark = gx + Math.min(gw - 1, Math.round(gw * Math.min(1F, this.menu.getCeiling() / (float) ceiling)));
            graphics.fill(mark, gy - 1, mark + 1, gy + 5, GAUGE_LIMIT);
        }
    }

    private void bar(GuiGraphicsExtractor graphics, int x, float fraction, int fill, int shine) {
        int top = this.topPos + BAR_TOP;
        int bottom = top + BAR_HEIGHT;
        graphics.fill(x - 1, top - 1, x + BAR_WIDTH + 1, bottom + 1, OUTLINE);
        graphics.fill(x, top, x + BAR_WIDTH, bottom, SLOT_EDGE);
        int height = Math.round(BAR_HEIGHT * Math.max(0F, Math.min(1F, fraction)));
        if (height > 0) {
            graphics.fill(x, bottom - height, x + BAR_WIDTH, bottom, fill);
            graphics.fill(x + 1, bottom - height, x + 2, bottom, shine);
        }
    }

    private static int lerpColor(int from, int to, float t) {
        float clamped = Math.max(0F, Math.min(1F, t));
        int r = Math.round(((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * clamped);
        int g = Math.round(((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * clamped);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * clamped);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, LABEL, false);
        Component status = statusText();
        graphics.text(this.font, status, this.imageWidth - 8 - this.font.width(status), this.titleLabelY, statusColor(), false);

        int x = READOUT_X + 5;
        int y = READOUT_Y + 4;
        graphics.text(this.font, Component.translatable("nautec.fusion.output", number(this.menu.getOutput())), x, y, READOUT, false);
        y = GAUGE_Y + 8;
        graphics.text(this.font, Component.translatable("nautec.fusion.injected", number(this.menu.getInjected()), decimal(this.menu.getPurity())), x, y, READOUT_DIM, false);
        y += LINE;
        graphics.text(this.font, Component.translatable("nautec.fusion.ceiling", number(this.menu.getCeiling())), x, y, READOUT_DIM, false);
        y += LINE;
        int width = Math.max(0, this.menu.getRadius() * 2 - 1);
        graphics.text(this.font, Component.translatable("nautec.fusion.chamber", width, width), x, y, READOUT_DIM, false);
        y += LINE;
        graphics.text(this.font, Component.translatable("nautec.fusion.parts", this.menu.getInjectors(), FusionStructure.MAX_INJECTORS, this.menu.getCoils()), x, y, READOUT_DIM, false);
        y += LINE;
        graphics.text(this.font, Component.translatable("nautec.fusion.burn", decimal(burnRate())), x, y, READOUT_DIM, false);
        y += LINE;
        graphics.text(this.font, Component.translatable("nautec.fusion.satellites", this.menu.getSatellites(), FusionStructure.MAX_SATELLITES), x, y, READOUT_DIM, false);

        List<FormattedCharSequence> footer = this.font.split(footerText(), this.imageWidth - 16);
        int fy = FOOTER_Y;
        for (FormattedCharSequence line : footer) {
            graphics.text(this.font, line, 8, fy, statusColor(), false);
            fy += this.font.lineHeight;
        }
    }

    private double burnRate() {
        return this.menu.getOutput() / (double) Math.max(1, NTConfig.fusionFePerMb);
    }

    private Component statusText() {
        return Component.translatable(this.menu.getStatus().translationKey());
    }

    private FormattedText footerText() {
        FusionControllerBlockEntity.Status status = this.menu.getStatus();
        if (status == FusionControllerBlockEntity.Status.INCOMPLETE) {
            return Component.translatable(this.menu.getProblem().translationKey());
        }
        return Component.translatable(status.translationKey() + ".desc");
    }

    private int statusColor() {
        FusionControllerBlockEntity.Status status = this.menu.getStatus();
        if (status.running()) {
            return RUNNING_TEXT;
        }
        return status == FusionControllerBlockEntity.Status.IGNITING || status == FusionControllerBlockEntity.Status.BUFFER_FULL ? WARMING_TEXT : STOPPED_TEXT;
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        List<Component> lines = hoverLines(mouseX, mouseY);
        if (!lines.isEmpty()) {
            graphics.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
        }
    }

    private List<Component> hoverLines(int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        int barTop = this.topPos + BAR_TOP;
        if (inside(mouseX, mouseY, this.leftPos + HEAT_X - 1, barTop - 1, BAR_WIDTH + 2, BAR_HEIGHT + 2)) {
            lines.add(Component.translatable("nautec.fusion.heat", Math.round(this.menu.getHeat() * 100)));
            lines.add(Component.translatable("nautec.fusion.heat.desc", number(NTConfig.fusionIgnitionEnergy)).withStyle(ChatFormatting.GRAY));
            return lines;
        }
        if (inside(mouseX, mouseY, this.leftPos + FUEL_X - 1, barTop - 1, BAR_WIDTH + 2, BAR_HEIGHT + 2)) {
            lines.add(Component.translatable("nautec.fusion.fuel", number(this.menu.getFuel()), number(FusionControllerBlockEntity.FUEL_CAPACITY)));
            lines.add(Component.translatable("nautec.fusion.fuel.desc", number(NTConfig.fusionFePerMb)).withStyle(ChatFormatting.GRAY));
            return lines;
        }
        if (inside(mouseX, mouseY, this.leftPos + ENERGY_X - 1, barTop - 1, BAR_WIDTH + 2, BAR_HEIGHT + 2)) {
            lines.add(Component.translatable("nautec.fusion.energy", number(this.menu.getEnergy()), number(NTConfig.fusionEnergyBuffer)));
            lines.add(Component.translatable("nautec.fusion.energy.desc").withStyle(ChatFormatting.GRAY));
            return lines;
        }
        int rx = this.leftPos + READOUT_X;
        int ry = this.topPos + READOUT_Y;
        if (!inside(mouseX, mouseY, rx, ry, READOUT_WIDTH, READOUT_HEIGHT)) {
            Component status = statusText();
            int width = this.font.width(status);
            if (inside(mouseX, mouseY, this.leftPos + this.imageWidth - 8 - width, this.topPos + this.titleLabelY - 1, width, this.font.lineHeight + 1)) {
                lines.add(status);
                lines.add(Component.translatable(this.menu.getStatus().translationKey() + ".desc").withStyle(ChatFormatting.GRAY));
            }
            return lines;
        }
        int row = mouseY - ry;
        if (row < GAUGE_Y - READOUT_Y + 6) {
            lines.add(Component.translatable("nautec.fusion.output", number(this.menu.getOutput())));
            lines.add(Component.translatable("nautec.fusion.output.desc", number(FusionStructure.fePerAp(this.menu.getSatellites()))).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("nautec.fusion.gauge.desc").withStyle(ChatFormatting.DARK_GRAY));
            return lines;
        }
        int index = (row - (GAUGE_Y - READOUT_Y + 6)) / LINE;
        switch (index) {
            case 0 -> {
                lines.add(Component.translatable("nautec.fusion.injected", number(this.menu.getInjected()), decimal(this.menu.getPurity())));
                lines.add(Component.translatable("nautec.fusion.injected.desc", decimal(NTConfig.fusionMinPurity)).withStyle(ChatFormatting.GRAY));
            }
            case 1 -> {
                lines.add(Component.translatable("nautec.fusion.ceiling", number(this.menu.getCeiling())));
                lines.add(Component.translatable("nautec.fusion.ceiling.desc", number(NTConfig.fusionCoilContainment), number(NTConfig.fusionSatelliteContainment),
                        number(FusionStructure.maxOutput(this.menu.getSatellites()))).withStyle(ChatFormatting.GRAY));
            }
            case 2 -> {
                int width = Math.max(0, this.menu.getRadius() * 2 - 1);
                lines.add(Component.translatable("nautec.fusion.chamber", width, width));
                lines.add(Component.translatable("nautec.fusion.chamber.desc").withStyle(ChatFormatting.GRAY));
            }
            case 3 -> {
                lines.add(Component.translatable("nautec.fusion.parts", this.menu.getInjectors(), FusionStructure.MAX_INJECTORS, this.menu.getCoils()));
                lines.add(Component.translatable("nautec.fusion.parts.desc").withStyle(ChatFormatting.GRAY));
            }
            case 4 -> {
                lines.add(Component.translatable("nautec.fusion.burn", decimal(burnRate())));
                lines.add(Component.translatable("nautec.fusion.burn.desc", number(NTConfig.fusionFePerMb)).withStyle(ChatFormatting.GRAY));
            }
            case 5 -> {
                lines.add(Component.translatable("nautec.fusion.satellites", this.menu.getSatellites(), FusionStructure.MAX_SATELLITES));
                lines.add(Component.translatable("nautec.fusion.satellites.desc", number(NTConfig.fusionSatelliteContainment),
                        number(NTConfig.fusionSatelliteFePerAp)).withStyle(ChatFormatting.GRAY));
            }
            default -> {
            }
        }
        return lines;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
