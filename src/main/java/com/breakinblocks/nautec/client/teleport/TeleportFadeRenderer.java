package com.breakinblocks.nautec.client.teleport;

import com.breakinblocks.nautec.Nautec;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import com.breakinblocks.nautec.utils.ARGB;
import net.minecraft.util.Mth;

public final class TeleportFadeRenderer {
    private static final ResourceLocation VIGNETTE = Nautec.rl("textures/effect/teleport_vignette.png");
    private static final int ABYSS = 0x061418;
    private static final int TINT_ALPHA = 170;

    private TeleportFadeRenderer() {
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !TeleportFxManager.isVisible()) {
            return;
        }

        float strength = TeleportFxManager.screenStrength(deltaTracker.getGameTimeDeltaPartialTick(false));
        if (strength <= 0F) {
            return;
        }

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        int vignetteAlpha = Mth.clamp((int) (strength * 255F), 0, 255);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.setColor(1F, 1F, 1F, vignetteAlpha / 255F);
        guiGraphics.blit(VIGNETTE, 0, 0, 0F, 0F, width, height, width, height);
        guiGraphics.setColor(1F, 1F, 1F, 1F);

        float closing = Mth.clamp((strength - 0.55F) / 0.45F, 0F, 1F);
        if (closing > 0F) {
            guiGraphics.fill(0, 0, width, height, ARGB.color((int) (closing * TINT_ALPHA), ABYSS));
        }
    }
}
