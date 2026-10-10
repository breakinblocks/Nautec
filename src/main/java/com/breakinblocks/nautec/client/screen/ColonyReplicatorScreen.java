package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.client.screen.NTGui;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.ColonyReplicatorBlockEntity;
import com.breakinblocks.nautec.content.blocks.ColonyReplicatorBlock;
import com.breakinblocks.nautec.content.menus.ColonyReplicatorMenu;
import com.breakinblocks.nautec.network.ReplicatorModePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class ColonyReplicatorScreen extends NTMachineScreen<ColonyReplicatorBlockEntity> {
    public static final ResourceLocation PROGRESS_ARROW = Nautec.rl("container/bacterial_analyzer/progress_arrow");
    public static final ResourceLocation PROGRESS_ARROW_OFF = Nautec.rl("container/bacterial_analyzer/progress_arrow_off");
    private static final int BAR_X = 88;
    private static final int BAR_Y = 18;
    private static final int BAR_WIDTH = 6;
    private static final int BAR_HEIGHT = 38;
    private static final int ARROW_X = 104;
    private static final int ARROW_Y = 18;
    private static final int BIOMASS = 0xFF5FE8B0;
    private static final int BIOMASS_READY = 0xFFACE97D;
    private static final int DIM = 0x88C8C7B3;

    private final NumberFormat format = NumberFormat.getIntegerInstance();

    public ColonyReplicatorScreen(NTMachineMenu<ColonyReplicatorBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    private ColonyReplicatorMenu replicator() {
        return (ColonyReplicatorMenu) this.menu;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new NTPanelButton(this.font, leftPos + 100, topPos + 62, 68, 16,
                () -> Component.translatable(replicator().isSplice() ? "nautec.replicator.mode.splice" : "nautec.replicator.mode.replicate"),
                () -> replicator().isSplice() ? PanelStyle.RECEIVE_COLOR : PanelStyle.NEUTRAL,
                () -> replicator().isSplice() ? PanelStyle.RECEIVE_HOVER : PanelStyle.NEUTRAL_HOVER,
                () -> Component.translatable(replicator().isSplice() ? "nautec.replicator.mode.splice.desc" : "nautec.replicator.mode.replicate.desc"),
                () -> PacketDistributor.sendToServer(new ReplicatorModePayload(this.menu.containerId))));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        if (!replicator().isSplice()) {
            int x = leftPos + ColonyReplicatorMenu.PARTNER_X;
            int y = topPos + ColonyReplicatorMenu.PARTNER_Y;
            graphics.fill(x, y, x + 18, y + 18, DIM);
        }

        int biomass = replicator().getBiomass();
        PanelStyle.bar(graphics, leftPos + BAR_X, topPos + BAR_Y, BAR_WIDTH, BAR_HEIGHT,
                biomass / (float) Math.max(1, NTConfig.replicatorBiomassCost),
                biomass >= NTConfig.replicatorBiomassCost ? BIOMASS_READY : BIOMASS, PanelStyle.READOUT);

        int width = Mth.ceil((float) replicator().getProgress() / replicator().getDuration() * 24.0F);
        NTGui.blitSprite(graphics, PROGRESS_ARROW_OFF, leftPos + ARROW_X, topPos + ARROW_Y, 24, 24);
        NTGui.blitSprite(graphics, PROGRESS_ARROW, 24, 24, 0, 0, leftPos + ARROW_X, topPos + ARROW_Y, width, 24);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        int status = replicator().getStatus();
        Component text = Component.translatable(ColonyReplicatorBlock.statusKey(status));
        graphics.drawString(this.font, text, this.imageWidth - 8 - this.font.width(text), this.titleLabelY,
                status == ColonyReplicatorBlockEntity.STATUS_RUNNING ? PanelStyle.SEND_COLOR : PanelStyle.DANGER, false);
    }

    private boolean over(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + w && mouseY >= topPos + y && mouseY < topPos + y + h;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        ColonyReplicatorBlockEntity machine = this.menu.blockEntity;
        List<Component> lines = new ArrayList<>();
        if (over(mouseX, mouseY, ColonyReplicatorMenu.TEMPLATE_X, ColonyReplicatorMenu.TEMPLATE_Y, 16, 16)
                && machine.getBacteriaStorage().getBacteria(ColonyReplicatorBlockEntity.TEMPLATE).isEmpty()) {
            lines.add(Component.translatable("nautec.replicator.slot.template"));
            lines.add(Component.translatable("nautec.replicator.slot.template.desc").withStyle(ChatFormatting.GRAY));
        } else if (over(mouseX, mouseY, ColonyReplicatorMenu.PARTNER_X, ColonyReplicatorMenu.PARTNER_Y, 16, 16)
                && machine.getBacteriaStorage().getBacteria(ColonyReplicatorBlockEntity.PARTNER).isEmpty()) {
            lines.add(Component.translatable("nautec.replicator.slot.partner"));
            lines.add(Component.translatable("nautec.replicator.slot.partner.desc").withStyle(ChatFormatting.GRAY));
        } else if (over(mouseX, mouseY, ColonyReplicatorMenu.FODDER_ITEM_X, ColonyReplicatorMenu.FODDER_ITEM_Y, 16, 16)
                && machine.getItemStackHandler().getStackInSlot(ColonyReplicatorBlockEntity.FODDER_ITEM).isEmpty()) {
            lines.add(Component.translatable("nautec.replicator.slot.fodder_item"));
            lines.add(Component.translatable("nautec.replicator.slot.fodder_item.desc").withStyle(ChatFormatting.GRAY));
        } else if (over(mouseX, mouseY, ColonyReplicatorMenu.FODDER_X, ColonyReplicatorMenu.FODDER_Y, 16, 16)
                && machine.getBacteriaStorage().getBacteria(ColonyReplicatorBlockEntity.FODDER).isEmpty()) {
            lines.add(Component.translatable("nautec.replicator.slot.fodder"));
            lines.add(Component.translatable("nautec.replicator.slot.fodder.desc").withStyle(ChatFormatting.GRAY));
        } else if (over(mouseX, mouseY, BAR_X - 1, BAR_Y - 1, BAR_WIDTH + 2, BAR_HEIGHT + 2)) {
            lines.add(Component.translatable("nautec.replicator.biomass.bar", format.format(replicator().getBiomass()),
                    format.format(NTConfig.replicatorBiomassCost)));
            lines.add(Component.translatable("nautec.replicator.biomass.desc").withStyle(ChatFormatting.GRAY));
        } else if (over(mouseX, mouseY, ARROW_X, ARROW_Y + 10, 24, 12)) {
            int status = replicator().getStatus();
            lines.add(Component.translatable(ColonyReplicatorBlock.statusKey(status))
                    .withStyle(status == ColonyReplicatorBlockEntity.STATUS_RUNNING ? ChatFormatting.AQUA : ChatFormatting.RED));
            lines.add(Component.translatable("nautec.replicator.requirements", NTConfig.replicatorPowerUsage,
                    String.format("%.1f", NTConfig.replicatorPurity)).withStyle(ChatFormatting.GRAY));
        }
        if (!lines.isEmpty()) {
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        }
    }
}
