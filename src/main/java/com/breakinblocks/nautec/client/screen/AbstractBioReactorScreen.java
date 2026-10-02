package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.api.menu.slots.SlotBacteriaStorage;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.AbstractBioReactorBlockEntity;
import com.breakinblocks.nautec.content.menus.BioReactorLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class AbstractBioReactorScreen<T extends AbstractBioReactorBlockEntity> extends NTMachineScreen<T> {
    public static final Identifier PROGRESS_ARROW = Nautec.rl("container/bio_reactor/progress_arrow");
    public static final Identifier PROGRESS_ARROW_OFF = Nautec.rl("container/bio_reactor/progress_arrow_off");
    private static final int PANEL = 0xFFC8C7B3;
    private static final int PANEL_LIGHT = 0xFFE7E7D6;
    private static final int OUTLINE = 0xFF070707;
    private static final int SLOT_EDGE = 0xFF1E2221;
    private static final int SLOT_FILL = 0xFF45504A;
    private static final int VITALITY_FILL = 0xFF4FE0C8;
    private static final int STARVING_FILL = 0xFFB8483E;
    private static final int PROGRESS_FILL = 0xFF7FD9A0;

    private final BioReactorLayout layout;
    private final Identifier texture;
    private boolean textureAvailable = true;

    protected AbstractBioReactorScreen(NTMachineMenu<T> menu, Inventory playerInventory, Component title, BioReactorLayout layout, Identifier texture) {
        super(menu, playerInventory, title, layout.imageWidth(), layout.imageHeight());
        this.layout = layout;
        this.texture = texture;
    }

    protected abstract boolean drawInputFrames();

    @Override
    protected void init() {
        super.init();
        this.textureAvailable = this.minecraft.getResourceManager().getResource(this.texture).isPresent();
    }

    @Override
    public @NotNull Identifier getBackgroundTexture() {
        return this.texture;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.textureAvailable) {
            super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
            if (drawInputFrames()) {
                for (int[] position : this.layout.nutrients()) {
                    itemFrame(guiGraphics, position[0], position[1]);
                }
                for (int[] position : this.layout.upgrades()) {
                    itemFrame(guiGraphics, position[0], position[1]);
                }
            }
            return;
        }

        int x0 = this.leftPos;
        int y0 = this.topPos;
        guiGraphics.fill(x0, y0, x0 + this.imageWidth, y0 + this.imageHeight, OUTLINE);
        guiGraphics.fill(x0 + 1, y0 + 1, x0 + this.imageWidth - 1, y0 + this.imageHeight - 1, PANEL_LIGHT);
        guiGraphics.fill(x0 + 3, y0 + 3, x0 + this.imageWidth - 1, y0 + this.imageHeight - 1, PANEL);
        for (Slot slot : this.menu.slots) {
            itemFrame(guiGraphics, slot.x, slot.y);
        }
        for (SlotBacteriaStorage slot : this.menu.getBacteriaStorageSlots()) {
            guiGraphics.fill(x0 + slot.getX(), y0 + slot.getY(), x0 + slot.getX() + 18, y0 + slot.getY() + 18, SLOT_EDGE);
            guiGraphics.fill(x0 + slot.getX() + 1, y0 + slot.getY() + 1, x0 + slot.getX() + 17, y0 + slot.getY() + 17, SLOT_FILL);
        }
        if (this.layout.progressArrows() != null) {
            for (int[] arrow : this.layout.progressArrows()) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_ARROW_OFF, x0 + arrow[0], y0 + arrow[1],
                        BioReactorLayout.ARROW_WIDTH, BioReactorLayout.ARROW_HEIGHT);
            }
        }
        if (this.layout.summaryArrow() != null) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_ARROW_OFF, x0 + this.layout.summaryArrow()[0], y0 + this.layout.summaryArrow()[1],
                    BioReactorLayout.ARROW_WIDTH, BioReactorLayout.ARROW_HEIGHT);
        }
    }

    private void itemFrame(GuiGraphicsExtractor guiGraphics, int x, int y) {
        int left = this.leftPos + x;
        int top = this.topPos + y;
        guiGraphics.fill(left - 1, top - 1, left + 17, top + 17, SLOT_EDGE);
        guiGraphics.fill(left, top, left + 16, top + 16, SLOT_FILL);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        T reactor = this.menu.blockEntity;
        int colonies = reactor.getColonySlots();
        float activeProgress = 0;
        int activeCount = 0;

        for (int i = 0; i < colonies; i++) {
            float progress = Math.max(0, Math.min(100, reactor.getProgress(i))) / 100f;
            BacteriaInstance colony = reactor.getBacteriaStorage().getBacteria(i);
            if (!colony.isEmpty()) {
                activeProgress += progress;
                activeCount++;
            }

            if (this.layout.progressArrows() != null && i < this.layout.progressArrows().length) {
                int[] arrow = this.layout.progressArrows()[i];
                int width = (int) (progress * BioReactorLayout.ARROW_WIDTH);
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_ARROW, BioReactorLayout.ARROW_WIDTH, BioReactorLayout.ARROW_HEIGHT,
                        0, 0, this.leftPos + arrow[0], this.topPos + arrow[1], width, BioReactorLayout.ARROW_HEIGHT);
            }

            if (this.layout.progressBars() != null && i < this.layout.progressBars().length) {
                int[] bar = this.layout.progressBars()[i];
                int left = this.leftPos + bar[0];
                int top = this.topPos + bar[1];
                guiGraphics.fill(left, top, left + bar[2], top + bar[3], SLOT_EDGE);
                guiGraphics.fill(left, top, left + Math.round(progress * bar[2]), top + bar[3], PROGRESS_FILL);
            }

            if (i < this.layout.vitalityBars().length) {
                int[] bar = this.layout.vitalityBars()[i];
                int left = this.leftPos + bar[0];
                int top = this.topPos + bar[1];
                guiGraphics.fill(left - 1, top - 1, left + bar[2] + 1, top + bar[3] + 1, SLOT_EDGE);
                if (!colony.isEmpty()) {
                    float capacity = reactor.getVitalityCapacity(i);
                    float fraction = capacity <= 0 ? 0 : Math.min(1, reactor.getVitality(i) / capacity);
                    int filled = Math.round(fraction * bar[3]);
                    if (filled > 0) {
                        guiGraphics.fill(left, top + bar[3] - filled, left + bar[2], top + bar[3], VITALITY_FILL);
                    } else {
                        guiGraphics.fill(left, top + bar[3] - 1, left + bar[2], top + bar[3], STARVING_FILL);
                    }
                }
                if (isHovering(bar[0] - 1, bar[1] - 1, bar[2] + 2, bar[3] + 2, mouseX, mouseY) && !colony.isEmpty()) {
                    guiGraphics.setComponentTooltipForNextFrame(this.font, vitalityTooltip(reactor, i), mouseX, mouseY);
                }
            }
        }

        if (this.layout.summaryArrow() != null) {
            int[] arrow = this.layout.summaryArrow();
            float average = activeCount == 0 ? 0 : activeProgress / activeCount;
            int width = (int) (average * BioReactorLayout.ARROW_WIDTH);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_ARROW, BioReactorLayout.ARROW_WIDTH, BioReactorLayout.ARROW_HEIGHT,
                    0, 0, this.leftPos + arrow[0], this.topPos + arrow[1], width, BioReactorLayout.ARROW_HEIGHT);
            if (isHovering(arrow[0], arrow[1], BioReactorLayout.ARROW_WIDTH, BioReactorLayout.ARROW_HEIGHT, mouseX, mouseY)) {
                guiGraphics.setComponentTooltipForNextFrame(this.font, statusTooltip(reactor), mouseX, mouseY);
            }
        }
        if (this.layout.progressArrows() != null) {
            for (int[] arrow : this.layout.progressArrows()) {
                if (isHovering(arrow[0], arrow[1], BioReactorLayout.ARROW_WIDTH, BioReactorLayout.ARROW_HEIGHT, mouseX, mouseY)) {
                    guiGraphics.setComponentTooltipForNextFrame(this.font, statusTooltip(reactor), mouseX, mouseY);
                }
            }
        }
    }

    private static List<Component> vitalityTooltip(AbstractBioReactorBlockEntity reactor, int colony) {
        List<Component> lines = new ArrayList<>();
        float vitality = reactor.getVitality(colony);
        if (vitality > 0) {
            float seconds = vitality / Math.max(0.0001f, reactor.getVitalityCost()) / 20f;
            lines.add(Component.translatable("nautec.bio_reactor.vitality", String.format(Locale.ROOT, "%.1f", seconds))
                    .withStyle(ChatFormatting.AQUA));
        } else {
            lines.add(Component.translatable("nautec.bio_reactor.starving").withStyle(ChatFormatting.RED));
        }
        return lines;
    }

    private static List<Component> statusTooltip(AbstractBioReactorBlockEntity reactor) {
        List<Component> lines = new ArrayList<>();
        ChatFormatting powerColor = reactor.getPower() >= reactor.getRequiredPower() ? ChatFormatting.GREEN : ChatFormatting.RED;
        lines.add(Component.translatable("nautec.bio_reactor.power", reactor.getPower(), reactor.getRequiredPower()).withStyle(powerColor));
        if (reactor.hasUpgrades()) {
            lines.add(Component.translatable("nautec.bio_reactor.upgrades",
                    String.format(Locale.ROOT, "%.2f", reactor.getSpeedMultiplier()),
                    reactor.getItemsPerCycle(),
                    Math.round(reactor.getVitalityCost() * 100)).withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }
}
