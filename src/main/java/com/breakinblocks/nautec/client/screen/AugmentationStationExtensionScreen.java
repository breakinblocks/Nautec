package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.client.screen.NTAbstractContainerScreen;
import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.AugmentationStationExtensionBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class AugmentationStationExtensionScreen extends NTAbstractContainerScreen<AugmentationStationExtensionBlockEntity> {
    private static final int READOUT_X = 8;
    private static final int READOUT_Y = 22;
    private static final int READOUT_WIDTH = 62;
    private static final int READOUT_HEIGHT = 46;

    public AugmentationStationExtensionScreen(NTAbstractContainerMenu<AugmentationStationExtensionBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    private Slot armSlot() {
        return this.menu.slots.get(1);
    }

    private Slot partSlot() {
        return this.menu.slots.get(0);
    }

    private boolean powered() {
        return this.menu.blockEntity.getPower() >= NTConfig.augmentationStationPower;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        extractSlotHint(guiGraphics, armSlot(), PanelStyle.ICON_CLAW);
        PanelStyle.screen(guiGraphics, leftPos + READOUT_X, topPos + READOUT_Y, READOUT_WIDTH, READOUT_HEIGHT);
        boolean loaded = partSlot().hasItem();
        int dot = !loaded ? PanelStyle.READOUT_DIM : powered() && armSlot().hasItem() ? PanelStyle.ONLINE : PanelStyle.OFFLINE;
        guiGraphics.fill(leftPos + READOUT_X + 5, topPos + READOUT_Y + 5, leftPos + READOUT_X + 9, topPos + READOUT_Y + 9, dot);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        Slot arm = armSlot();
        Slot part = partSlot();
        graphics.text(this.font, Component.translatable("nautec.augmentation_station_extension.arm"), arm.x + 22, arm.y + 4, PanelStyle.LABEL, false);
        graphics.text(this.font, Component.translatable("nautec.augmentation_station_extension.part"), part.x + 22, part.y + 4, PanelStyle.LABEL, false);

        boolean loaded = part.hasItem();
        String state = !loaded ? "idle" : !arm.hasItem() ? "no_arm" : powered() ? "ready" : "low_power";
        graphics.text(this.font, Component.translatable("nautec.augmentation_station_extension.status." + state),
                READOUT_X + 13, READOUT_Y + 3, loaded ? PanelStyle.READOUT : PanelStyle.READOUT_DIM, false);
        graphics.text(this.font, Component.translatable("nautec.augmentation_station_extension.beam"), READOUT_X + 4, READOUT_Y + 18,
                PanelStyle.READOUT_DIM, false);
        int power = this.menu.blockEntity.getPower();
        int color = !loaded ? PanelStyle.READOUT_DIM : powered() ? PanelStyle.ONLINE : PanelStyle.OFFLINE;
        graphics.text(this.font, Component.translatable("nautec.augmentation_station.power", power), READOUT_X + 4, READOUT_Y + 30, color, false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        if (this.hoveredSlot == armSlot() && !armSlot().hasItem()) {
            guiGraphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("nautec.augmentation_station_extension.arm"),
                    Component.translatable("nautec.augmentation_station_extension.arm.desc").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
        } else if (this.hoveredSlot == partSlot() && !partSlot().hasItem()) {
            guiGraphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("nautec.augmentation_station_extension.part"),
                    Component.translatable("nautec.augmentation_station_extension.part.desc").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
        } else if (PanelStyle.inside(mouseX, mouseY, leftPos + READOUT_X, topPos + READOUT_Y, READOUT_WIDTH, READOUT_HEIGHT)) {
            guiGraphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("nautec.augmentation_station_extension.beam"),
                    Component.translatable("nautec.augmentation_station.extension.power", this.menu.blockEntity.getPower(), NTConfig.augmentationStationPower)
                            .withStyle(powered() ? ChatFormatting.GREEN : ChatFormatting.RED),
                    Component.translatable("nautec.augmentation_station.extension.power.desc").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
        }
    }
}
