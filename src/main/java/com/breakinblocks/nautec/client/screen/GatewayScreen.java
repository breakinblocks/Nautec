package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.network.SetGatewayAddressPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GatewayScreen extends Screen {
    private static final int SWATCH = 18;
    private static final int GAP = 4;
    private static final int LABEL_W = 22;
    private static final int HEADER_H = 42;
    private static final int FOOTER_H = 46;

    private static final int PANEL_W = LABEL_W + GatewayAddress.PALETTE.size() * (SWATCH + GAP) + GAP * 3;
    private static final int PANEL_H = HEADER_H + GatewayAddress.SLOTS * (SWATCH + GAP) + FOOTER_H;

    private static final int BACKDROP = 0xE8071B1F;
    private static final int BORDER = 0xFF3EFDFF;
    private static final int ROW_BACKDROP = 0x40103038;
    private static final int SELECTED = 0xFFFFFFFF;
    private static final int AFFORDABLE = 0x60000000;
    private static final int UNAFFORDABLE = 0xC0101010;

    private final BlockPos pos;
    private final GatewayAddress original;
    private GatewayAddress selected;
    private Button applyButton;

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

    private int panelY() {
        return (this.height - PANEL_H) / 2;
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

        int buttonY = panelY() + PANEL_H - FOOTER_H + 20;
        int buttonW = 70;

        this.applyButton = addRenderableWidget(Button.builder(
                        Component.translatable("nautec.gateway.apply"), button -> apply())
                .bounds(panelX() + PANEL_W / 2 - buttonW - GAP, buttonY, buttonW, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.translatable("nautec.gateway.cancel"), button -> onClose())
                .bounds(panelX() + PANEL_W / 2 + GAP, buttonY, buttonW, 20)
                .build());

        refreshApply();
    }

    private void refreshApply() {
        if (this.applyButton != null) {
            this.applyButton.active = !this.selected.equals(this.original) && affordable();
        }
    }

    private Map<DyeColor, Integer> cost() {
        Map<DyeColor, Integer> cost = new HashMap<>();
        for (DyeColor colour : SetGatewayAddressPayload.costOf(this.original, this.selected)) {
            cost.merge(colour, 1, Integer::sum);
        }
        return cost;
    }

    private boolean affordable() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return false;
        }
        if (this.minecraft.player.getAbilities().instabuild) {
            return true;
        }
        for (Map.Entry<DyeColor, Integer> entry : cost().entrySet()) {
            if (held(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private int held(DyeColor colour) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return 0;
        }
        if (this.minecraft.player.getAbilities().instabuild) {
            return Integer.MAX_VALUE;
        }
        return SetGatewayAddressPayload.count(this.minecraft.player.getInventory(), GatewayAddress.dyeItem(colour));
    }

    private void apply() {
        if (!this.selected.equals(this.original)) {
            ClientPacketDistributor.sendToServer(new SetGatewayAddressPayload(this.pos, this.selected));
        }
        onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x90000000);

        int x = panelX();
        int y = panelY();
        guiGraphics.fill(x - 1, y - 1, x + PANEL_W + 1, y + PANEL_H + 1, BORDER);
        guiGraphics.fill(x, y, x + PANEL_W, y + PANEL_H, BACKDROP);

        guiGraphics.centeredText(this.font, this.title, x + PANEL_W / 2, y + 10, 0xFF3EFDFF);
        guiGraphics.centeredText(this.font, this.selected.describe(), x + PANEL_W / 2, y + 24, 0xFFFFFFFF);

        List<DyeColor> palette = GatewayAddress.PALETTE;
        for (int slot = 0; slot < GatewayAddress.SLOTS; slot++) {
            int rowY = swatchY(slot);
            guiGraphics.fill(x + GAP, rowY - 2, x + PANEL_W - GAP, rowY + SWATCH + 2, ROW_BACKDROP);
            guiGraphics.text(this.font, String.valueOf(slot + 1), x + GAP * 2 + 6, rowY + 5, 0xFFB9CCCF, false);

            for (int colour = 0; colour < palette.size(); colour++) {
                DyeColor dye = palette.get(colour);
                int sx = swatchX(colour);
                boolean chosen = this.selected.slots().get(slot) == dye;
                boolean canPay = chosen || this.original.slots().get(slot) == dye || held(dye) > 0;

                if (chosen) {
                    guiGraphics.fill(sx - 2, rowY - 2, sx + SWATCH + 2, rowY + SWATCH + 2, SELECTED);
                }
                guiGraphics.fill(sx, rowY, sx + SWATCH, rowY + SWATCH, 0xFF000000 | dye.getTextColor());
                if (!canPay) {
                    guiGraphics.fill(sx, rowY, sx + SWATCH, rowY + SWATCH, UNAFFORDABLE);
                } else if (!chosen) {
                    guiGraphics.fill(sx, rowY, sx + SWATCH, rowY + SWATCH, AFFORDABLE);
                }
            }
        }

        guiGraphics.centeredText(this.font, costText(), x + PANEL_W / 2, y + PANEL_H - FOOTER_H + 6,
                affordable() ? 0xFFB9CCCF : 0xFFFF6B6B);

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    private Component costText() {
        Map<DyeColor, Integer> cost = cost();
        if (cost.isEmpty()) {
            return Component.translatable("nautec.gateway.no_change");
        }

        Component text = Component.translatable("nautec.gateway.cost");
        for (Map.Entry<DyeColor, Integer> entry : cost.entrySet()) {
            DyeColor dye = entry.getKey();
            text = Component.empty().append(text).append(Component.literal(" "))
                    .append(Component.translatable("nautec.gateway.cost_entry",
                                    entry.getValue(), Component.translatable(GatewayAddress.dyeItem(dye).getDescriptionId()))
                            .withStyle(style -> style.withColor(dye.getTextColor())));
        }
        return text;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        List<DyeColor> palette = GatewayAddress.PALETTE;
        for (int slot = 0; slot < GatewayAddress.SLOTS; slot++) {
            int rowY = swatchY(slot);
            if (event.y() < rowY || event.y() > rowY + SWATCH) {
                continue;
            }
            for (int colour = 0; colour < palette.size(); colour++) {
                int sx = swatchX(colour);
                if (event.x() >= sx && event.x() <= sx + SWATCH) {
                    this.selected = this.selected.withSlot(slot, palette.get(colour));
                    refreshApply();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
