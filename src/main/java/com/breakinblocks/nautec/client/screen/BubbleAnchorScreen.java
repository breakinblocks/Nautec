package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.bubble.BubbleAnchorBlock;
import com.breakinblocks.nautec.content.bubble.BubbleAnchorBlockEntity;
import com.breakinblocks.nautec.content.menus.BubbleAnchorMenu;
import com.breakinblocks.nautec.network.BubbleAnchorTogglePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

public class BubbleAnchorScreen extends NTMachineScreen<BubbleAnchorBlockEntity> {
    private static final int BAR_X = 46;
    private static final int BAR_Y = 36;
    private static final int BAR_WIDTH = 6;
    private static final int BAR_HEIGHT = 32;
    private static final int FUEL_FILL = 0xFF5FE8B0;
    private static final int LASER_FILL = 0xFF4CCBE0;

    public BubbleAnchorScreen(NTMachineMenu<BubbleAnchorBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    private BubbleAnchorMenu anchor() {
        return (BubbleAnchorMenu) this.menu;
    }

    @Override
    protected void init() {
        super.init();
        BubbleAnchorMenu anchor = anchor();
        addRenderableWidget(toggle(60, 18, BubbleAnchorTogglePayload.ENABLED,
                () -> Component.translatable(anchor.isEnabled() ? "nautec.bubble_anchor.button.on" : "nautec.bubble_anchor.button.off"),
                () -> anchor.isEnabled() ? PanelStyle.SEND_COLOR : PanelStyle.DANGER,
                () -> anchor.isEnabled() ? PanelStyle.SEND_HOVER : PanelStyle.DANGER_HOVER,
                () -> Component.translatable("nautec.bubble_anchor.button.enabled.desc")));
        addRenderableWidget(toggle(60, 38, BubbleAnchorTogglePayload.FILL,
                () -> Component.translatable(anchor.fillsWater() ? "nautec.bubble_anchor.button.water" : "nautec.bubble_anchor.button.air"),
                () -> anchor.fillsWater() ? PanelStyle.RECEIVE_COLOR : PanelStyle.NEUTRAL,
                () -> anchor.fillsWater() ? PanelStyle.RECEIVE_HOVER : PanelStyle.NEUTRAL_HOVER,
                () -> Component.translatable(anchor.fillsWater() ? "nautec.bubble_anchor.button.water.desc" : "nautec.bubble_anchor.button.air.desc")));
        addRenderableWidget(toggle(60, 58, BubbleAnchorTogglePayload.ABOVE,
                () -> Component.translatable(anchor.isAbove() ? "nautec.bubble_anchor.button.above" : "nautec.bubble_anchor.button.centred"),
                () -> PanelStyle.NEUTRAL, () -> PanelStyle.NEUTRAL_HOVER,
                () -> Component.translatable("nautec.bubble_anchor.button.position.desc")));
    }

    private NTPanelButton toggle(int x, int y, int which, Supplier<Component> label, Supplier<Integer> color, Supplier<Integer> hover,
                                 Supplier<Component> tooltip) {
        return new NTPanelButton(this.font, leftPos + x, topPos + y, 108, 16, label, color, hover, tooltip,
                () -> PacketDistributor.sendToServer(new BubbleAnchorTogglePayload(this.menu.containerId, which)));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        BubbleAnchorMenu anchor = anchor();
        boolean laser = anchor.getStatus() == BubbleAnchorBlockEntity.STATUS_LASER;
        float fraction = laser ? 1F : anchor.getBurn() / (float) Math.max(1, anchor.getBurnTotal());
        PanelStyle.bar(graphics, leftPos + BAR_X, topPos + BAR_Y, BAR_WIDTH, BAR_HEIGHT, fraction,
                laser ? LASER_FILL : FUEL_FILL, PanelStyle.READOUT);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        int status = anchor().getStatus();
        boolean running = status == BubbleAnchorBlockEntity.STATUS_FUEL || status == BubbleAnchorBlockEntity.STATUS_LASER;
        Component text = Component.translatable(BubbleAnchorBlock.statusKey(status));
        graphics.drawString(this.font, text, this.imageWidth - 8 - this.font.width(text), this.titleLabelY, running ? PanelStyle.SEND_COLOR : PanelStyle.DANGER, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (mouseX >= leftPos + BAR_X - 1 && mouseX < leftPos + BAR_X + BAR_WIDTH + 1 && mouseY >= topPos + BAR_Y - 1 && mouseY < topPos + BAR_Y + BAR_HEIGHT + 1) {
            BubbleAnchorMenu anchor = anchor();
            int size = anchor.getRadius() * 2 + 1;
            Supplier<Component> fuelLine = () -> anchor.getStatus() == BubbleAnchorBlockEntity.STATUS_LASER
                    ? Component.translatable("nautec.bubble_anchor.status.laser")
                    : Component.translatable("nautec.bubble_anchor.time", anchor.getBurn() / 20);
            graphics.renderComponentTooltip(this.font, List.of(
                    fuelLine.get(),
                    Component.translatable("nautec.bubble_anchor.size", size, size, size).withStyle(ChatFormatting.GRAY),
                    Component.translatable("nautec.bubble_anchor.fuel.desc").withStyle(ChatFormatting.GRAY),
                    Component.translatable("nautec.bubble_anchor.laser.desc", NTConfig.bubbleAnchorLaserPower).withStyle(ChatFormatting.DARK_GRAY)
            ), mouseX, mouseY);
        }
    }
}
