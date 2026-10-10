package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.network.SetGatewayAddressPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class GatewayScreen extends Screen {
    private static final int SWATCH = 18;
    private static final int GAP = 4;
    private static final int LABEL_W = 22;
    private static final int HEADER_H = 42;
    private static final int LINE_H = 10;

    private static final int PANEL_W = LABEL_W + GatewayAddress.PALETTE.size() * (SWATCH + GAP) + GAP * 5;
    private static final int HINT_W = PANEL_W - GAP * 4;

    private static final int SELECTED = PanelStyle.READOUT;
    private static final int UNSELECTED = 0x70000000;

    private final BlockPos pos;
    private final GatewayAddress original;
    private GatewayAddress selected;
    private NTPanelButton applyButton;
    private int hintLines = 1;
    private @Nullable DyeColor hovered;

    private GatewayScreen(BlockPos pos, GatewayAddress address) {
        super(Component.translatable("nautec.gateway.title"));
        this.pos = pos;
        this.original = address;
        this.selected = address;
    }

    public static void open(BlockPos pos, GatewayAddress address) {
        Minecraft.getInstance().setScreen(new GatewayScreen(pos, address));
    }

    private int panelX() {
        return (this.width - PANEL_W) / 2;
    }

    private int footerH() {
        return 30 + hintLines * LINE_H;
    }

    private int panelH() {
        return HEADER_H + GatewayAddress.SLOTS * (SWATCH + GAP) + footerH();
    }

    private int panelY() {
        return (this.height - panelH()) / 2;
    }

    private Component hint() {
        return Component.translatable(this.selected.equals(this.original) ? "nautec.gateway.no_change" : "nautec.gateway.free");
    }

    private int swatchX(int colour) {
        return panelX() + GAP * 2 + LABEL_W + colour * (SWATCH + GAP);
    }

    private int swatchY(int slot) {
        return panelY() + HEADER_H + slot * (SWATCH + GAP);
    }

    @Override
    protected void init() {
        super.init();

        this.hintLines = Math.max(
                this.font.split(Component.translatable("nautec.gateway.no_change"), HINT_W).size(),
                this.font.split(Component.translatable("nautec.gateway.free"), HINT_W).size());
        int buttonY = panelY() + panelH() - 22;
        int buttonW = 70;

        this.applyButton = addRenderableWidget(new NTPanelButton(this.font, panelX() + PANEL_W / 2 - buttonW - GAP, buttonY, buttonW, 16,
                () -> Component.translatable("nautec.gateway.apply"), () -> PanelStyle.SEND_COLOR, () -> PanelStyle.SEND_HOVER,
                this::hint, this::apply));

        addRenderableWidget(new NTPanelButton(this.font, panelX() + PANEL_W / 2 + GAP, buttonY, buttonW, 16,
                () -> Component.translatable("nautec.gateway.cancel"), () -> PanelStyle.NEUTRAL, () -> PanelStyle.NEUTRAL_HOVER,
                () -> Component.translatable("nautec.gateway.cancel"), this::onClose));

        refreshApply();
    }

    private void refreshApply() {
        if (this.applyButton != null) {
            this.applyButton.active = !this.selected.equals(this.original);
        }
    }

    private void apply() {
        if (!this.selected.equals(this.original)) {
            PacketDistributor.sendToServer(new SetGatewayAddressPayload(this.pos, this.selected));
        }
        onClose();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.fill(0, 0, this.width, this.height, 0x90000000);

        int x = panelX();
        int y = panelY();
        PanelStyle.panel(guiGraphics, x, y, PANEL_W, panelH());
        guiGraphics.drawString(this.font, this.title, x + 8, y + 8, PanelStyle.LABEL, false);
        PanelStyle.screen(guiGraphics, x + GAP * 2, y + 20, PANEL_W - GAP * 4, 16);
        guiGraphics.drawCenteredString(this.font, this.selected.describe(), x + PANEL_W / 2, y + 24, PanelStyle.READOUT);
        this.hovered = null;

        List<DyeColor> palette = GatewayAddress.PALETTE;
        for (int slot = 0; slot < GatewayAddress.SLOTS; slot++) {
            int rowY = swatchY(slot);
            PanelStyle.screen(guiGraphics, x + GAP * 2, rowY - 3, PANEL_W - GAP * 4, SWATCH + 6);
            guiGraphics.drawString(this.font, String.valueOf(slot + 1), x + GAP * 2 + 6, rowY + 5, PanelStyle.READOUT_DIM, false);

            for (int colour = 0; colour < palette.size(); colour++) {
                DyeColor dye = palette.get(colour);
                int sx = swatchX(colour);
                boolean chosen = this.selected.slots().get(slot) == dye;

                if (chosen) {
                    guiGraphics.fill(sx - 2, rowY - 2, sx + SWATCH + 2, rowY + SWATCH + 2, SELECTED);
                }
                guiGraphics.fill(sx, rowY, sx + SWATCH, rowY + SWATCH, 0xFF000000 | dye.getTextColor());
                if (!chosen) {
                    guiGraphics.fill(sx, rowY, sx + SWATCH, rowY + SWATCH, UNSELECTED);
                }
                if (mouseX >= sx && mouseX < sx + SWATCH && mouseY >= rowY && mouseY < rowY + SWATCH) {
                    this.hovered = dye;
                }
            }
        }

        List<FormattedCharSequence> lines = this.font.split(hint(), HINT_W);
        int lineY = y + panelH() - footerH() + 6;
        for (FormattedCharSequence line : lines) {
            guiGraphics.drawString(this.font, line, x + (PANEL_W - this.font.width(line)) / 2, lineY, PanelStyle.LABEL, false);
            lineY += LINE_H;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        DyeColor hovered = this.hovered;
        if (hovered != null) {
            guiGraphics.renderTooltip(this.font, Component.translatable("color.minecraft." + hovered.getSerializedName()), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<DyeColor> palette = GatewayAddress.PALETTE;
        for (int slot = 0; slot < GatewayAddress.SLOTS; slot++) {
            int rowY = swatchY(slot);
            if (mouseY < rowY || mouseY > rowY + SWATCH) {
                continue;
            }
            for (int colour = 0; colour < palette.size(); colour++) {
                int sx = swatchX(colour);
                if (mouseX >= sx && mouseX <= sx + SWATCH) {
                    this.selected = this.selected.withSlot(slot, palette.get(colour));
                    refreshApply();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
