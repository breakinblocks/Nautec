package com.breakinblocks.nautec.client.shockwave;

import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ShockwaveCooldown;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

public final class ShockwaveCooldownDecorator implements IItemDecorator {
    private static final int SLOT_SIZE = 16;
    private static final int OVERLAY_COLOR = Integer.MAX_VALUE;

    @Override
    public boolean render(GuiGraphicsExtractor guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        ShockwaveCooldown cooldown = stack.get(NTDataComponents.SHOCKWAVE_COOLDOWN.get());
        Minecraft minecraft = Minecraft.getInstance();
        if (cooldown == null || minecraft.level == null) {
            return false;
        }
        float remaining = cooldown.remainingFraction(minecraft.level.getGameTime(),
                minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true));
        if (remaining <= 0.0F) {
            return false;
        }
        int top = yOffset + Mth.floor(SLOT_SIZE * (1.0F - remaining));
        int bottom = top + Mth.ceil(SLOT_SIZE * remaining);
        guiGraphics.fill(RenderPipelines.GUI, xOffset, top, xOffset + SLOT_SIZE, bottom, OVERLAY_COLOR);
        return false;
    }
}
