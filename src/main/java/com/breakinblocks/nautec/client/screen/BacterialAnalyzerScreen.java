package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.client.screen.NTGui;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.screen.NTAbstractContainerScreen;
import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.content.blockentities.BacterialAnalyzerBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class BacterialAnalyzerScreen extends NTAbstractContainerScreen<BacterialAnalyzerBlockEntity> {
    public static final ResourceLocation PROGRESS_ARROW = Nautec.rl("container/bacterial_analyzer/progress_arrow");
    public static final ResourceLocation PROGRESS_ARROW_OFF = Nautec.rl("container/bacterial_analyzer/progress_arrow_off");

    public BacterialAnalyzerScreen(NTAbstractContainerMenu<BacterialAnalyzerBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 174);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int i = this.leftPos;
        int j = this.topPos - 4;

        int progress = menu.blockEntity.getProgress();

        int j1 = Mth.ceil(((float) progress / NTConfig.bacteriaAnalyzerCraftingSpeed) * 24.0F);
        PanelStyle.icon(guiGraphics, PanelStyle.ICON_PETRI_DISH, i + 36, j + 40, 14, 12);
        NTGui.blitSprite(guiGraphics, PROGRESS_ARROW_OFF, i + 76, j + 29, 24, 24);
        NTGui.blitSprite(guiGraphics, PROGRESS_ARROW, 24, 24, 0, 0, i + 76, j + 29, j1, 24);
    }
}
