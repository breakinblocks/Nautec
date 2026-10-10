package com.breakinblocks.nautec.client.screen;

import net.minecraft.client.gui.screens.Screen;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.EnergyConverterBlockEntity;
import com.breakinblocks.nautec.content.items.EnergyConversionUpgradeItem;
import com.breakinblocks.nautec.content.menus.EnergyConverterMenu;
import com.breakinblocks.nautec.network.SetConverterRatePayload;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class EnergyConverterScreen extends NTMachineScreen<EnergyConverterBlockEntity> {
    private static final int RATE_Y = 28;
    private static final int VALUE_X = 58;
    private static final int VALUE_WIDTH = 60;
    private static final int BAR_X = 68;
    private static final int BAR_Y = 51;
    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 6;
    private static final int GHOST = 0xAA45504A;

    public EnergyConverterScreen(NTMachineMenu<EnergyConverterBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    private EnergyConverterMenu converter() {
        return (EnergyConverterMenu) this.menu;
    }

    private static ItemStack upgradeFor(int slot) {
        return switch (slot) {
            case 0 -> NTItems.ENERGY_CONVERSION_UPGRADE.get().getDefaultInstance();
            case 1 -> NTItems.ADVANCED_ENERGY_CONVERSION_UPGRADE.get().getDefaultInstance();
            default -> NTItems.ULTIMATE_ENERGY_CONVERSION_UPGRADE.get().getDefaultInstance();
        };
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(step(8, 24, -10));
        addRenderableWidget(step(34, 22, -1));
        addRenderableWidget(step(120, 22, 1));
        addRenderableWidget(step(144, 24, 10));
    }

    private NTPanelButton step(int x, int width, int amount) {
        return new NTPanelButton(this.font, leftPos + x, topPos + RATE_Y, width, 16,
                () -> Component.literal(amount > 0 ? "+" + amount : String.valueOf(amount)),
                () -> amount > 0 ? PanelStyle.SEND_COLOR : PanelStyle.NEUTRAL,
                () -> amount > 0 ? PanelStyle.SEND_HOVER : PanelStyle.NEUTRAL_HOVER,
                () -> Component.translatable("nautec.energy_converter.step.desc", Math.abs(amount) * 10, Math.abs(amount) * 100), () -> {
            long delta = amount;
            if (Screen.hasControlDown()) {
                delta *= 100;
            } else if (Screen.hasShiftDown()) {
                delta *= 10;
            }
            EnergyConverterMenu converter = converter();
            int rate = (int) Math.max(0, Math.min(converter.getMaxRate(), converter.getRate() + delta));
            PacketDistributor.sendToServer(new SetConverterRatePayload(this.menu.containerId, rate));
        });
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        PanelStyle.screen(graphics, leftPos + VALUE_X, topPos + RATE_Y, VALUE_WIDTH, 16);

        for (int slot = 0; slot < EnergyConverterBlockEntity.UPGRADE_SLOTS; slot++) {
            int sx = EnergyConverterMenu.SLOT_X + slot * 18;
            if (!this.menu.getBlockEntity().getItemStackHandler().getStackInSlot(slot).isEmpty()) {
                continue;
            }
            graphics.renderFakeItem(upgradeFor(slot), leftPos + sx, topPos + EnergyConverterMenu.SLOT_Y);
            graphics.fill(leftPos + sx, topPos + EnergyConverterMenu.SLOT_Y, leftPos + sx + 16, topPos + EnergyConverterMenu.SLOT_Y + 16, GHOST);
        }

        int x = leftPos + BAR_X;
        int y = topPos + BAR_Y;
        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, PanelStyle.SLOT_EDGE);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, PanelStyle.SCREEN_FILL);
        int filled = Math.round(BAR_WIDTH * Math.min(1F, converter().getFeStored() / (float) Math.max(1, converter().getCapacity())));
        if (filled > 0) {
            graphics.fill(x, y, x + filled, y + BAR_HEIGHT, PanelStyle.ENERGY_FILL);
            graphics.fill(x, y, x + filled, y + 1, PanelStyle.ENERGY_SHINE);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        EnergyConverterMenu converter = converter();
        graphics.drawString(this.font, Component.translatable("nautec.energy_converter.rate"), 8, 17, PanelStyle.LABEL, false);

        Component value = Component.translatable("nautec.energy_converter.rate.value", converter.getRate());
        graphics.drawString(this.font, value, VALUE_X + (VALUE_WIDTH - this.font.width(value)) / 2, RATE_Y + 4, PanelStyle.READOUT, false);

        Component cost = Component.translatable("nautec.energy_converter.cost", String.format("%,d", (long) converter.getRate() * converter.getFePerAp()));
        graphics.drawString(this.font, cost, this.imageWidth - 8 - this.font.width(cost), 17, PanelStyle.LABEL, false);

        graphics.drawString(this.font, Component.translatable("nautec.energy_converter.max", converter.getMaxRate()), BAR_X, BAR_Y + 9, PanelStyle.LABEL, false);

        Component status;
        boolean running = false;
        if (converter.getBeams() <= 0) {
            status = Component.translatable("nautec.energy_converter.status.no_target");
        } else if (converter.getRate() <= 0) {
            status = Component.translatable("nautec.energy_converter.idle");
        } else if (converter.getSending() <= 0) {
            status = Component.translatable("nautec.energy_converter.no_fe");
        } else {
            status = Component.translatable("nautec.energy_converter.sending", converter.getSending());
            running = true;
        }
        graphics.drawString(this.font, status, BAR_X, BAR_Y + 19, running ? PanelStyle.SEND_COLOR : PanelStyle.DANGER, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        EnergyConverterMenu converter = converter();
        if (isHovering(BAR_X - 1, BAR_Y - 1, BAR_WIDTH + 2, BAR_HEIGHT + 2, mouseX, mouseY)) {
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("nautec.energy_converter.fe",
                            String.format("%,d", converter.getFeStored()), String.format("%,d", converter.getCapacity())),
                    Component.translatable("nautec.energy_converter.ratio", converter.getFePerAp()).withStyle(ChatFormatting.GRAY)
            ), mouseX, mouseY);
        } else if (isHovering(VALUE_X - 1, RATE_Y - 1, VALUE_WIDTH + 2, 18, mouseX, mouseY)) {
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("nautec.energy_converter.rate.desc", converter.getMaxRate()),
                    Component.translatable("nautec.energy_converter.beams", Math.max(1, converter.getBeams()), converter.getRate() / Math.max(1, converter.getBeams()))
                            .withStyle(ChatFormatting.GRAY)
            ), mouseX, mouseY);
        } else {
            for (int slot = 0; slot < EnergyConverterBlockEntity.UPGRADE_SLOTS; slot++) {
                int sx = EnergyConverterMenu.SLOT_X + slot * 18;
                if (this.menu.getBlockEntity().getItemStackHandler().getStackInSlot(slot).isEmpty()
                        && isHovering(sx, EnergyConverterMenu.SLOT_Y, 16, 16, mouseX, mouseY)) {
                    ItemStack upgrade = upgradeFor(slot);
                    EnergyConversionUpgradeItem item = (EnergyConversionUpgradeItem) upgrade.getItem();
                    graphics.renderComponentTooltip(this.font, List.of(
                            upgrade.getHoverName(),
                            Component.translatable("nautec.energy_conversion_upgrade.effect", item.getTier().ap()).withStyle(ChatFormatting.AQUA),
                            Component.translatable("nautec.energy_conversion_upgrade.limit", EnergyConversionUpgradeItem.MAX_PER_SLOT).withStyle(ChatFormatting.GRAY)
                    ), mouseX, mouseY);
                }
            }
        }
    }
}
