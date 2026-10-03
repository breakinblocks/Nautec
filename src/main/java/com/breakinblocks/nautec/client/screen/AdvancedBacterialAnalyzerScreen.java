package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.AdvancedBacterialAnalyzerBlockEntity;
import com.breakinblocks.nautec.content.blocks.AdvancedBacterialAnalyzerBlock;
import com.breakinblocks.nautec.content.menus.AdvancedBacterialAnalyzerMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AdvancedBacterialAnalyzerScreen extends NTMachineScreen<AdvancedBacterialAnalyzerBlockEntity> {
    public static final Identifier TEXTURE = Nautec.rl("textures/gui/advanced_bacterial_analyzer.png");
    public static final Identifier PROGRESS_ARROW = Nautec.rl("container/bacterial_analyzer/progress_arrow");
    private static final int PROGRESS_FILL = 0x8046E8C8;

    public AdvancedBacterialAnalyzerScreen(NTMachineMenu<AdvancedBacterialAnalyzerBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    private AdvancedBacterialAnalyzerMenu analyzer() {
        return (AdvancedBacterialAnalyzerMenu) this.menu;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        for (Slot slot : this.menu.slots) {
            if (slot.container != this.menu.getInv()) {
                extractSlotFrame(guiGraphics, slot.x, slot.y);
            }
        }
        int duration = analyzer().getDuration();
        int best = 0;
        for (int dish = 0; dish < AdvancedBacterialAnalyzerBlockEntity.DISHES; dish++) {
            int progress = analyzer().getProgress(dish);
            best = Math.max(best, progress);
            if (progress > 0) {
                int x = leftPos + AdvancedBacterialAnalyzerMenu.INPUT_X + (dish % 3) * 18;
                int y = topPos + AdvancedBacterialAnalyzerMenu.GRID_Y + (dish / 3) * 18;
                int height = Mth.ceil((float) progress / duration * 16.0F);
                guiGraphics.fill(x, y + 16 - height, x + 16, y + 16, PROGRESS_FILL);
            }
        }
        int width = Mth.ceil((float) best / duration * 24.0F);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_ARROW, 24, 24, 0, 0, leftPos + 76, topPos + 25, width, 24);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        if (isHovering(76, 36, 24, 12, mouseX, mouseY)) {
            int status = analyzer().getStatus();
            boolean running = status == AdvancedBacterialAnalyzerBlockEntity.STATUS_RUNNING;
            guiGraphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable(AdvancedBacterialAnalyzerBlock.statusKey(status)).withStyle(running ? ChatFormatting.AQUA : ChatFormatting.RED),
                    Component.translatable("nautec.advanced_analyzer.requirements", NTConfig.advancedAnalyzerPowerUsage,
                            String.format("%.1f", NTConfig.advancedAnalyzerPurity)).withStyle(ChatFormatting.GRAY)
            ), mouseX, mouseY);
        }
    }

    @Override
    public @NotNull Identifier getBackgroundTexture() {
        return TEXTURE;
    }
}
