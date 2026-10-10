package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.generators.CombustionDynamoBlockEntity;
import com.breakinblocks.nautec.content.menus.CombustionDynamoMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class CombustionDynamoScreen extends NTMachineScreen<CombustionDynamoBlockEntity> {
    private static final int READOUT_X = 102;
    private static final int READOUT_Y = 18;
    private static final int READOUT_WIDTH = 50;
    private static final int BAR_X = 158;
    private static final int BAR_Y = 18;
    private static final int BAR_WIDTH = 10;
    private static final int BAR_HEIGHT = 52;
    private static final int BURN_Y = CombustionDynamoMenu.ADDITIVE_Y + 19;
    private static final int GHOST = 0xAA45504A;

    public CombustionDynamoScreen(NTMachineMenu<CombustionDynamoBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    private CombustionDynamoMenu dynamo() {
        return (CombustionDynamoMenu) this.menu;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        CombustionDynamoMenu dynamo = dynamo();
        PanelStyle.screen(graphics, leftPos + READOUT_X, topPos + READOUT_Y, READOUT_WIDTH, 16);

        int slotX = leftPos + CombustionDynamoMenu.ADDITIVE_X;
        int slotY = topPos + CombustionDynamoMenu.ADDITIVE_Y;
        if (this.menu.getBlockEntity().getItemStackHandler().getStackInSlot(CombustionDynamoBlockEntity.ADDITIVE_SLOT).isEmpty()) {
            graphics.renderFakeItem(new ItemStack(Items.REDSTONE), slotX, slotY);
            graphics.fill(slotX, slotY, slotX + 16, slotY + 16, GHOST);
        }

        int burnX = slotX;
        int burnY = topPos + BURN_Y;
        graphics.fill(burnX - 1, burnY - 1, burnX + 17, burnY + 4, PanelStyle.SLOT_EDGE);
        graphics.fill(burnX, burnY, burnX + 16, burnY + 3, PanelStyle.SCREEN_FILL);
        int total = dynamo.getAdditiveTotal();
        if (total > 0 && dynamo.getAdditiveLeft() > 0) {
            int width = Math.max(1, Math.round(16F * dynamo.getAdditiveLeft() / total));
            graphics.fill(burnX, burnY, burnX + width, burnY + 3, PanelStyle.WARNING);
        }

        int x = leftPos + BAR_X;
        int y = topPos + BAR_Y;
        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, PanelStyle.SLOT_EDGE);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, PanelStyle.SCREEN_FILL);
        int filled = Math.round(BAR_HEIGHT * Math.min(1F, dynamo.getFeStored() / (float) Math.max(1, dynamo.getCapacity())));
        if (filled > 0) {
            graphics.fill(x, y + BAR_HEIGHT - filled, x + BAR_WIDTH, y + BAR_HEIGHT, PanelStyle.ENERGY_FILL);
            graphics.fill(x, y + BAR_HEIGHT - filled, x + 1, y + BAR_HEIGHT, PanelStyle.ENERGY_SHINE);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        CombustionDynamoMenu dynamo = dynamo();
        Component value = Component.translatable("nautec.combustion_dynamo.rate", dynamo.getRate());
        graphics.drawString(this.font, value, READOUT_X + (READOUT_WIDTH - this.font.width(value)) / 2, READOUT_Y + 4,
                dynamo.getRate() > 0 ? PanelStyle.READOUT : PanelStyle.READOUT_DIM, false);

        CombustionDynamoBlockEntity.Status status = dynamo.getStatus();
        boolean running = status == CombustionDynamoBlockEntity.Status.RUNNING;
        Component line = Component.translatable(status.translationKey());
        graphics.drawString(this.font, line, READOUT_X + (READOUT_WIDTH - this.font.width(line)) / 2, READOUT_Y + 22,
                running ? PanelStyle.SEND_COLOR : PanelStyle.DANGER, false);
        if (running && dynamo.getAdditiveLeft() > 0) {
            Component boost = Component.translatable("nautec.combustion_dynamo.boosted");
            graphics.drawString(this.font, boost, READOUT_X + (READOUT_WIDTH - this.font.width(boost)) / 2, READOUT_Y + 33,
                    PanelStyle.LABEL, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        CombustionDynamoMenu dynamo = dynamo();
        if (isHovering(BAR_X - 1, BAR_Y - 1, BAR_WIDTH + 2, BAR_HEIGHT + 2, mouseX, mouseY)) {
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("nautec.combustion_dynamo.fe",
                            String.format("%,d", dynamo.getFeStored()), String.format("%,d", dynamo.getCapacity()))
            ), mouseX, mouseY);
        } else if (isHovering(READOUT_X - 1, READOUT_Y - 1, READOUT_WIDTH + 2, 18, mouseX, mouseY)) {
            float fuel = dynamo.getAdditiveLeft() > 0 ? dynamo.getFuelPercent() / 100F : 1F;
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("nautec.combustion_dynamo.rate.desc"),
                    Component.translatable("nautec.combustion_dynamo.oil_use",
                            String.format("%.2f", fuel * 20F / Math.max(1, NTConfig.combustionDynamoTicksPerOil))).withStyle(ChatFormatting.GRAY),
                    Component.translatable("nautec.combustion_dynamo.water_use",
                            String.format("%.1f", fuel * 20F * NTConfig.combustionDynamoWaterPerTick)).withStyle(ChatFormatting.GRAY)
            ), mouseX, mouseY);
        } else if (isHovering(CombustionDynamoMenu.ADDITIVE_X - 1, CombustionDynamoMenu.ADDITIVE_Y - 1, 18, 24, mouseX, mouseY)
                && this.menu.getCarried().isEmpty()
                && this.menu.getBlockEntity().getItemStackHandler().getStackInSlot(CombustionDynamoBlockEntity.ADDITIVE_SLOT).isEmpty()) {
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("nautec.combustion_dynamo.additive"),
                    Component.translatable("nautec.combustion_dynamo.additive.desc").withStyle(ChatFormatting.GRAY),
                    Component.translatable("nautec.combustion_dynamo.additive.left", dynamo.getAdditiveLeft() / 20).withStyle(ChatFormatting.DARK_GRAY)
            ), mouseX, mouseY);
        }
    }
}
