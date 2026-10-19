package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.client.screen.FluidTankRenderer;
import com.breakinblocks.nautec.content.menus.DeepSeaReservoirMenu;
import com.breakinblocks.nautec.content.reservoir.DeepSeaReservoirBlockEntity;
import com.breakinblocks.nautec.content.reservoir.ReservoirFluid;
import com.breakinblocks.nautec.content.reservoir.ReservoirText;
import com.breakinblocks.nautec.api.client.screen.NTGui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Locale;

public class DeepSeaReservoirScreen extends AbstractContainerScreen<DeepSeaReservoirMenu> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 190;
    private static final int GAUGE_X = 8;
    private static final int GAUGE_Y = 18;
    private static final int GAUGE_WIDTH = 46;
    private static final int GAUGE_HEIGHT = 74;
    private static final int READOUT_X = 60;
    private static final int READOUT_Y = 18;
    private static final int READOUT_WIDTH = 108;
    private static final int READOUT_HEIGHT = 54;
    private static final int REMOVE_X = 84;
    private static final int REMOVE_WIDTH = 84;
    private static final int REMOVE_HEIGHT = 18;

    private FluidTankRenderer gauge;
    private long gaugeCapacity;
    private boolean confirming;
    private int confirmTier;

    public DeepSeaReservoirScreen(DeepSeaReservoirMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.titleLabelY = 6;
        this.inventoryLabelY = DeepSeaReservoirMenu.INVENTORY_Y - 11;
    }

    private DeepSeaReservoirBlockEntity reservoir() {
        return this.menu.blockEntity;
    }

    private FluidTankRenderer gauge() {
        long capacity = Math.max(1, reservoir().capacity());
        if (gauge == null || capacity != gaugeCapacity) {
            gaugeCapacity = capacity;
            gauge = new FluidTankRenderer(capacity, true, GAUGE_WIDTH - 2, GAUGE_HEIGHT - 2);
        }
        return gauge;
    }

    private void press(int id) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.menu.clickMenuButton(this.minecraft.player, id);
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
        }
    }

    @Override
    protected void init() {
        super.init();
        NTPanelButton remove = new NTPanelButton(this.font, this.leftPos + REMOVE_X, this.topPos + DeepSeaReservoirMenu.UPGRADE_Y - 1,
                REMOVE_WIDTH, REMOVE_HEIGHT,
                this::removeLabel,
                () -> confirming() ? PanelStyle.DANGER : PanelStyle.NEUTRAL,
                () -> confirming() ? PanelStyle.DANGER_HOVER : PanelStyle.NEUTRAL_HOVER,
                this::removeTooltip,
                this::onRemove);
        addRenderableWidget(remove);
    }

    private boolean confirming() {
        return confirming && confirmTier == reservoir().tier() && reservoir().excessIfRemoved() > 0;
    }

    private Component removeLabel() {
        if (reservoir().tier() <= 1) {
            return Component.translatable("nautec.deep_sea_reservoir.screen.base_tier");
        }
        if (confirming()) {
            return Component.translatable("nautec.deep_sea_reservoir.screen.remove.confirm", ReservoirText.buckets(reservoir().excessIfRemoved()));
        }
        return Component.translatable("nautec.deep_sea_reservoir.screen.remove");
    }

    private Component removeTooltip() {
        int tier = reservoir().tier();
        if (tier <= 1) {
            return Component.translatable("nautec.deep_sea_reservoir.screen.base_tier.desc");
        }
        long excess = reservoir().excessIfRemoved();
        if (excess > 0) {
            return Component.translatable("nautec.deep_sea_reservoir.screen.remove.void_desc", ReservoirText.buckets(excess),
                    ReservoirText.numeral(tier - 1));
        }
        return Component.translatable("nautec.deep_sea_reservoir.screen.remove.desc", ReservoirText.numeral(tier - 1));
    }

    private void onRemove() {
        int tier = reservoir().tier();
        if (tier <= 1) {
            return;
        }
        if (reservoir().excessIfRemoved() > 0 && !confirming()) {
            confirming = true;
            confirmTier = tier;
            return;
        }
        confirming = false;
        press(DeepSeaReservoirMenu.REMOVE_BUTTON);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        PanelStyle.panel(graphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        for (Slot slot : this.menu.slots) {
            PanelStyle.slot(graphics, this.leftPos + slot.x, this.topPos + slot.y);
        }
        int x = this.leftPos + GAUGE_X;
        int y = this.topPos + GAUGE_Y;
        PanelStyle.tank(graphics, x, y, GAUGE_WIDTH, GAUGE_HEIGHT);
        ReservoirFluid contents = reservoir().contents();
        if (!contents.isEmpty()) {
            gauge().render(graphics, x + 1, y + 1, contents.resource().toStack(1), contents.amount());
        }
        PanelStyle.tankTicks(graphics, x, y, GAUGE_WIDTH, GAUGE_HEIGHT);
        PanelStyle.screen(graphics, this.leftPos + READOUT_X, this.topPos + READOUT_Y, READOUT_WIDTH, READOUT_HEIGHT);
        int ux = this.leftPos + DeepSeaReservoirMenu.UPGRADE_X;
        int uy = this.topPos + DeepSeaReservoirMenu.UPGRADE_Y;
        int tier = reservoir().tier();
        if (tier < ReservoirFluid.MAX_TIER && this.menu.getSlot(0).getItem().isEmpty()) {
            graphics.renderItem(new ItemStack(DeepSeaReservoirMenu.upgradeFor(tier + 1)), ux, uy);
            NTGui.pushOverItems(graphics);
            graphics.fill(ux, uy, ux + 16, uy + 16, PanelStyle.GHOST_FADE);
            NTGui.popOverItems(graphics);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, PanelStyle.LABEL, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, PanelStyle.LABEL, false);
        DeepSeaReservoirBlockEntity reservoir = reservoir();
        ReservoirFluid contents = reservoir.contents();
        long capacity = reservoir.capacity();
        int x = READOUT_X + 5;
        int y = READOUT_Y + 5;
        graphics.drawString(this.font, Component.translatable("nautec.deep_sea_reservoir.screen.tier", ReservoirText.numeral(reservoir.tier())),
                x, y, PanelStyle.READOUT, false);
        Component name = contents.isEmpty() ? Component.translatable("nautec.deep_sea_reservoir.screen.empty") : contents.resource().getHoverName();
        graphics.drawString(this.font, this.font.plainSubstrByWidth(name.getString(), READOUT_WIDTH - 10), x, y + 12,
                contents.isEmpty() ? PanelStyle.READOUT_DIM : PanelStyle.READOUT, false);
        Component amount = Component.translatable("nautec.deep_sea_reservoir.screen.amount", ReservoirText.buckets(contents.amount()),
                ReservoirText.buckets(capacity));
        if (this.font.width(amount) > READOUT_WIDTH - 10) {
            amount = Component.translatable("nautec.deep_sea_reservoir.screen.amount_short", ReservoirText.buckets(contents.amount()),
                    ReservoirText.buckets(capacity));
        }
        graphics.drawString(this.font, amount, x, y + 24, PanelStyle.READOUT, false);
        double percent = capacity <= 0 ? 0 : 100.0 * contents.amount() / capacity;
        graphics.drawString(this.font, Component.translatable("nautec.deep_sea_reservoir.screen.percent",
                String.format(Locale.ROOT, percent > 0 && percent < 0.1 ? "%.3f" : "%.1f", percent)), x, y + 36, PanelStyle.READOUT_DIM, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (PanelStyle.inside(mouseX, mouseY, this.leftPos + GAUGE_X, this.topPos + GAUGE_Y, GAUGE_WIDTH, GAUGE_HEIGHT)) {
            ReservoirFluid contents = reservoir().contents();
            if (contents.isEmpty()) {
                graphics.renderTooltip(this.font, Component.translatable("nautec.deep_sea_reservoir.screen.empty_gauge",
                        ReservoirText.full(reservoir().capacity())), mouseX, mouseY);
            } else {
                FluidStack type = contents.resource().toStack(1);
                graphics.renderComponentTooltip(this.font, gauge().getTooltip(type, contents.amount()), mouseX, mouseY);
            }
        }
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.menu.getCarried().isEmpty()
                && PanelStyle.inside(mouseX, mouseY, this.leftPos + GAUGE_X, this.topPos + GAUGE_Y, GAUGE_WIDTH, GAUGE_HEIGHT)) {
            press(DeepSeaReservoirMenu.GAUGE_BUTTON);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
