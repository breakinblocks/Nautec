package com.breakinblocks.nautec.client.teleport;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public final class TeleportStreakRenderer {
    private static final Identifier STREAKS = Nautec.rl("textures/effect/teleport_streaks.png");
    private static final int TINT = 0x9FE8FF;

    private static final float[] LAYER_SCALE = {1.0F, 1.35F, 1.9F};
    private static final float[] LAYER_GROWTH = {0.55F, 0.95F, 1.5F};
    private static final float[] LAYER_ALPHA = {0.85F, 0.55F, 0.3F};

    private TeleportStreakRenderer() {
    }

    public static void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !TeleportFxManager.isVisible()) {
            return;
        }

        float strength = TeleportFxManager.fadeStrength(deltaTracker.getGameTimeDeltaPartialTick(false));
        if (strength <= 0.01F) {
            return;
        }

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        float span = Math.max(width, height) * 1.6F;

        for (int layer = 0; layer < LAYER_SCALE.length; layer++) {
            float scale = LAYER_SCALE[layer] + LAYER_GROWTH[layer] * strength;
            float alpha = LAYER_ALPHA[layer] * strength * strength;
            int packed = Mth.clamp((int) (alpha * 255F), 0, 255);
            if (packed <= 1) {
                continue;
            }

            int size = (int) (span * scale);
            int x = (width - size) / 2;
            int y = (height - size) / 2;
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, STREAKS, x, y, 0F, 0F, size, size, size, size,
                    ARGB.color(packed, TINT));
        }
    }
}
