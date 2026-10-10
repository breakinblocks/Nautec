package com.breakinblocks.nautec.client;

import com.breakinblocks.nautec.api.client.screen.NTGui;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import com.breakinblocks.nautec.utils.ARGB;

public final class GuiUtils {
    public static final ResourceLocation BACTERIA = Nautec.rl("item/petri_dish_overlay");

    public static void renderBacteria(GuiGraphics guiGraphics, BacteriaInstance instance, int x, int y) {
        TextureAtlasSprite sprite = NTGui.blockSprite(BACTERIA);
        if (!instance.isEmpty()) {
            Bacteria bacteria = BacteriaHelper.getBacteria(Minecraft.getInstance().level.registryAccess(), instance.getBacteria());
            int color = bacteria.stats().color();
            NTGui.blitSprite(guiGraphics, sprite, x + 1, y, 16, 16, ARGB.color(ARGB.alpha(color), ARGB.red(color), ARGB.green(color), ARGB.blue(color)));
        }
    }

    public static boolean isHovering(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        return guiGraphics.containsPointInScissor(mouseX, mouseY)
                && mouseX > x
                && mouseY > y
                && mouseX < x + width
                && mouseY < y + height;
    }
}
