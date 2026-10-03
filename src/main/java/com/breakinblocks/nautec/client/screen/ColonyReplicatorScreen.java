package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.ColonyReplicatorBlockEntity;
import com.breakinblocks.nautec.content.blocks.ColonyReplicatorBlock;
import com.breakinblocks.nautec.content.menus.ColonyReplicatorMenu;
import com.breakinblocks.nautec.network.ReplicatorModePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class ColonyReplicatorScreen extends NTMachineScreen<ColonyReplicatorBlockEntity> {
    public static final Identifier TEXTURE = Nautec.rl("textures/gui/colony_replicator.png");
    public static final Identifier PROGRESS_ARROW = Nautec.rl("container/bacterial_analyzer/progress_arrow");
    private static final int BAR_X = 88;
    private static final int BAR_Y = 18;
    private static final int BAR_WIDTH = 6;
    private static final int BAR_HEIGHT = 38;
    private static final int ARROW_X = 104;
    private static final int ARROW_Y = 18;
    private static final int OUTLINE = 0xFF1E2221;
    private static final int BAR_EMPTY = 0xFF45504A;
    private static final int BIOMASS = 0xFF5FE8B0;
    private static final int BIOMASS_READY = 0xFFACE97D;
    private static final int DIM = 0x88C8C7B3;
    private static final int LABEL = 0xFF404040;
    private static final int RUNNING = 0xFF2E7D4F;
    private static final int STOPPED = 0xFF8C2F2F;

    private final NumberFormat format = NumberFormat.getIntegerInstance();
    private Button mode;

    public ColonyReplicatorScreen(NTMachineMenu<ColonyReplicatorBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    private ColonyReplicatorMenu replicator() {
        return (ColonyReplicatorMenu) this.menu;
    }

    @Override
    protected void init() {
        super.init();
        mode = addRenderableWidget(Button.builder(Component.empty(),
                button -> ClientPacketDistributor.sendToServer(new ReplicatorModePayload(this.menu.containerId)))
                .bounds(leftPos + 100, topPos + 62, 68, 16).build());
    }

    @Override
    public void containerTick() {
        super.containerTick();
        boolean splice = replicator().isSplice();
        mode.setMessage(Component.translatable(splice ? "nautec.replicator.mode.splice" : "nautec.replicator.mode.replicate"));
        mode.setTooltip(Tooltip.create(Component.translatable(splice ? "nautec.replicator.mode.splice.desc" : "nautec.replicator.mode.replicate.desc")));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        extractSlotFrame(graphics, ColonyReplicatorMenu.TEMPLATE_X, ColonyReplicatorMenu.TEMPLATE_Y);
        extractSlotFrame(graphics, ColonyReplicatorMenu.PARTNER_X, ColonyReplicatorMenu.PARTNER_Y);
        extractSlotFrame(graphics, ColonyReplicatorMenu.FODDER_X, ColonyReplicatorMenu.FODDER_Y);
        extractSlotFrame(graphics, ColonyReplicatorMenu.RESULT_X, ColonyReplicatorMenu.RESULT_Y);
        if (!replicator().isSplice()) {
            int x = leftPos + ColonyReplicatorMenu.PARTNER_X;
            int y = topPos + ColonyReplicatorMenu.PARTNER_Y;
            graphics.fill(x, y, x + 16, y + 16, DIM);
        }

        int x = leftPos + BAR_X;
        int y = topPos + BAR_Y;
        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, OUTLINE);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, BAR_EMPTY);
        int biomass = replicator().getBiomass();
        int filled = Math.round(BAR_HEIGHT * Math.min(1F, biomass / (float) Math.max(1, NTConfig.replicatorBiomassCost)));
        if (filled > 0) {
            graphics.fill(x, y + BAR_HEIGHT - filled, x + BAR_WIDTH, y + BAR_HEIGHT, biomass >= NTConfig.replicatorBiomassCost ? BIOMASS_READY : BIOMASS);
        }

        int width = Mth.ceil((float) replicator().getProgress() / replicator().getDuration() * 24.0F);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_ARROW, 24, 24, 0, 0, leftPos + ARROW_X, topPos + ARROW_Y, width, 24);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        int status = replicator().getStatus();
        Component text = Component.translatable(ColonyReplicatorBlock.statusKey(status));
        graphics.text(this.font, text, this.imageWidth - 8 - this.font.width(text), this.titleLabelY,
                status == ColonyReplicatorBlockEntity.STATUS_RUNNING ? RUNNING : STOPPED, false);
        graphics.text(this.font, Component.translatable("nautec.replicator.label.fodder"), ColonyReplicatorMenu.FODDER_X - 2,
                ColonyReplicatorMenu.FODDER_Y - 10, LABEL, false);
    }

    private boolean over(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + w && mouseY >= topPos + y && mouseY < topPos + y + h;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
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
            graphics.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
        }
    }

    @Override
    public @NotNull Identifier getBackgroundTexture() {
        return TEXTURE;
    }
}
