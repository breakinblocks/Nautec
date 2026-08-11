package com.breakinblocks.nautec.client.teleport;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public final class TeleportFadeRenderer {
    private static final Identifier VIGNETTE = Nautec.rl("textures/effect/teleport_vignette.png");
    private static final int ABYSS = 0x061418;
    private static final int TINT_ALPHA = 170;

    private TeleportFadeRenderer() {
    }

    public static void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !TeleportFxManager.isVisible()) {
            return;
        }

        float strength = TeleportFxManager.fadeStrength(deltaTracker.getGameTimeDeltaPartialTick(false));
        if (strength <= 0F) {
            return;
        }

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        int vignetteAlpha = Mth.clamp((int) (strength * 255F), 0, 255);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, VIGNETTE, 0, 0, 0F, 0F, width, height, width, height,
                ARGB.color(vignetteAlpha, 0xFFFFFF));

        float closing = Mth.clamp((strength - 0.55F) / 0.45F, 0F, 1F);
        if (closing > 0F) {
            guiGraphics.fill(0, 0, width, height, ARGB.color((int) (closing * TINT_ALPHA), ABYSS));
        }
    }
}
