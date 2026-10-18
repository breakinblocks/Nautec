package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.OsmosisFilterBlockEntity;
import com.breakinblocks.nautec.content.menus.OsmosisFilterMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class OsmosisFilterScreen extends NTMachineScreen<OsmosisFilterBlockEntity> {
    private static final int READOUT_X = 62;
    private static final int READOUT_Y = 18;
    private static final int READOUT_WIDTH = 96;
    private static final int READOUT_HEIGHT = 16;
    private static final int POWER_Y = 40;

    public OsmosisFilterScreen(NTMachineMenu<OsmosisFilterBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    private OsmosisFilterMenu filter() {
        return (OsmosisFilterMenu) this.menu;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        PanelStyle.screen(graphics, leftPos + READOUT_X, topPos + READOUT_Y, READOUT_WIDTH, READOUT_HEIGHT);
        PanelStyle.screen(graphics, leftPos + READOUT_X, topPos + POWER_Y, READOUT_WIDTH, READOUT_HEIGHT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        OsmosisFilterMenu filter = filter();
        boolean full = this.menu.getBlockEntity().getWaterAmount() >= filter.getFillLimit();
        Component rate = Component.translatable("nautec.osmosis_filter.rate", String.format("%,d", filter.getRate()));
        graphics.text(this.font, rate, READOUT_X + (READOUT_WIDTH - this.font.width(rate)) / 2, READOUT_Y + 4,
                full ? PanelStyle.READOUT_DIM : PanelStyle.READOUT, false);
        Component power = Component.translatable("nautec.osmosis_filter.power", String.format("%,d", filter.getPower()));
        graphics.text(this.font, power, READOUT_X + (READOUT_WIDTH - this.font.width(power)) / 2, POWER_Y + 4,
                filter.getPower() > 0 ? PanelStyle.READOUT : PanelStyle.READOUT_DIM, false);
        Component status = Component.translatable(full ? "nautec.osmosis_filter.full" : "nautec.osmosis_filter.filtering");
        graphics.text(this.font, status, READOUT_X + (READOUT_WIDTH - this.font.width(status)) / 2, POWER_Y + 24,
                full ? PanelStyle.DANGER : PanelStyle.SEND_COLOR, false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (isHovering(READOUT_X - 1, READOUT_Y - 1, READOUT_WIDTH + 2, READOUT_HEIGHT + 2, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("nautec.osmosis_filter.rate.desc"),
                    Component.translatable("nautec.osmosis_filter.limit", String.format("%,d", filter().getFillLimit()))
                            .withStyle(ChatFormatting.GRAY)
            ), mouseX, mouseY);
        } else if (isHovering(READOUT_X - 1, POWER_Y - 1, READOUT_WIDTH + 2, READOUT_HEIGHT + 2, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("nautec.osmosis_filter.power.desc", NTConfig.osmosisFilterApStep)
            ), mouseX, mouseY);
        }
    }
}
