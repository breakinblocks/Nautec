package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.content.augments.ResonanceAugment;
import com.breakinblocks.nautec.network.OpenResonanceAugmentPayload;
import com.breakinblocks.nautec.utils.AugmentHelper;
import com.breakinblocks.nautec.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AugmentationViewerScreen extends Screen {
    private static final int WIDTH = 252;
    private static final int ROW = 15;
    private static final int LIST_X = 84;
    private static final int LIST_Y = 20;
    private static final int LIST_WIDTH = WIDTH - LIST_X - 8;
    private static final int VIEW_X = 8;
    private static final int VIEW_WIDTH = 70;
    private static final int ROW_HOVER = 0xFF2A3634;

    private final Player player;
    private int imageHeight;
    private int leftPos;
    private int topPos;

    public AugmentationViewerScreen(Component title, Player player) {
        super(title);
        this.player = player;
    }

    private List<AugmentSlot> slots() {
        List<AugmentSlot> slots = new ArrayList<>();
        NTRegistries.AUGMENT_SLOT.forEach(slots::add);
        return slots;
    }

    private int listHeight() {
        return Math.max(6, slots().size()) * ROW + 4;
    }

    @Override
    protected void init() {
        super.init();
        this.imageHeight = LIST_Y + listHeight() + 8;
        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractTransparentBackground(graphics);
        PanelStyle.panel(graphics, leftPos, topPos, WIDTH, imageHeight);
        PanelStyle.screen(graphics, leftPos + VIEW_X, topPos + LIST_Y, VIEW_WIDTH, listHeight());
        PanelStyle.screen(graphics, leftPos + LIST_X, topPos + LIST_Y, LIST_WIDTH, listHeight());
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, leftPos + VIEW_X + 1, topPos + LIST_Y + 1,
                leftPos + VIEW_X + VIEW_WIDTH - 1, topPos + LIST_Y + listHeight() - 1, 36, 0.0625F, mouseX, mouseY, player);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.text(this.font, this.title, leftPos + 8, topPos + 8, PanelStyle.LABEL, false);

        List<AugmentSlot> slots = slots();
        int installed = 0;
        Component hoverName = null;
        Augment hoverAugment = null;
        AugmentSlot hoverSlot = null;
        for (int i = 0; i < slots.size(); i++) {
            AugmentSlot slot = slots.get(i);
            Augment augment = AugmentHelper.getAugmentBySlot(player, slot);
            int x = leftPos + LIST_X + 2;
            int y = topPos + LIST_Y + 2 + i * ROW;
            boolean hover = PanelStyle.inside(mouseX, mouseY, x, y, LIST_WIDTH - 4, ROW);
            if (hover) {
                graphics.fill(x, y, x + LIST_WIDTH - 4, y + ROW, ROW_HOVER);
            }
            Component slotName = Utils.registryTranslation(NTRegistries.AUGMENT_SLOT, slot);
            graphics.text(this.font, slotName, x + 3, y + 4, PanelStyle.READOUT_DIM, false);
            int nameX = x + 58;
            int nameWidth = LIST_WIDTH - 4 - 58 - 4;
            Component name = augment == null
                    ? Component.translatable("nautec.augment_viewer.empty")
                    : Utils.registryTranslation(NTRegistries.AUGMENT_TYPE, augment.getAugmentType());
            String shown = this.font.plainSubstrByWidth(name.getString(), nameWidth);
            if (shown.length() < name.getString().length()) {
                shown = this.font.plainSubstrByWidth(name.getString(), nameWidth - this.font.width("...")) + "...";
            }
            graphics.text(this.font, Component.literal(shown), nameX, y + 4,
                    augment == null ? PanelStyle.SLOT_FILL : augment.isOnCooldown() ? PanelStyle.WARNING : PanelStyle.READOUT, false);
            if (augment != null) {
                installed++;
            }
            if (hover) {
                hoverName = name;
                hoverAugment = augment;
                hoverSlot = slot;
            }
        }

        Component count = Component.translatable("nautec.augment_viewer.count", installed, slots.size());
        graphics.text(this.font, count, leftPos + WIDTH - 8 - this.font.width(count), topPos + 8, PanelStyle.LABEL, false);

        if (hoverSlot != null) {
            List<Component> lines = new ArrayList<>();
            lines.add(Utils.registryTranslation(NTRegistries.AUGMENT_SLOT, hoverSlot).copy().withStyle(ChatFormatting.GRAY));
            if (hoverAugment == null) {
                lines.add(Component.translatable("nautec.augment_viewer.empty.desc").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                lines.add(hoverName);
                if (hoverAugment instanceof ResonanceAugment) {
                    lines.add(Component.translatable("nautec.augment_viewer.resonance").withStyle(ChatFormatting.DARK_GRAY));
                }
                if (hoverAugment.isOnCooldown()) {
                    lines.add(Component.translatable("nautec.augment_viewer.cooldown",
                            String.format(Locale.ROOT, "%.1f", hoverAugment.getCooldown() / 20f)).withStyle(ChatFormatting.YELLOW));
                }
            }
            graphics.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        List<AugmentSlot> slots = slots();
        for (int i = 0; i < slots.size(); i++) {
            int x = leftPos + LIST_X + 2;
            int y = topPos + LIST_Y + 2 + i * ROW;
            if (PanelStyle.inside(event.x(), event.y(), x, y, LIST_WIDTH - 4, ROW)
                    && AugmentHelper.getAugmentBySlot(player, slots.get(i)) instanceof ResonanceAugment) {
                ClientPacketDistributor.sendToServer(new OpenResonanceAugmentPayload());
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
