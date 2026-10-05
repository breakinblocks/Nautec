package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.MutatorBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class MutatorScreen extends NTMachineScreen<MutatorBlockEntity> {
    public static final Identifier PROGRESS_ARROW = Nautec.rl("container/mutator/progress_arrow");
    public static final Identifier PROGRESS_ARROW_OFF = Nautec.rl("container/mutator/progress_arrow_off");

    public MutatorScreen(NTMachineMenu<MutatorBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        int i = this.leftPos;
        int j = this.topPos;

        int progress = menu.blockEntity.getProgress();

        int j1 = Mth.ceil(((float) progress / NTConfig.mutatorCraftingSpeed) * 62f);

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_ARROW_OFF, i + 56, j + 36, 62, 14);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_ARROW, 62, 14, 0, 0, i + 56, j + 36, j1, 14);
    }
}
