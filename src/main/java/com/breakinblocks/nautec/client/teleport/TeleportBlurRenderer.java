package com.breakinblocks.nautec.client.teleport;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.render.NTShaders;
import com.breakinblocks.nautec.client.render.ScreenPass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Blurs the composited world before hands and HUD, including when Iris is active. */
@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class TeleportBlurRenderer {
    private TeleportBlurRenderer() {
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        float strength = TeleportFxManager.screenStrength(minecraft.getTimer().getGameTimeDeltaPartialTick(false));
        if (strength <= 0F) {
            return;
        }
        ShaderInstance shader = NTShaders.teleportBlur();
        RenderTarget target = minecraft.getMainRenderTarget();
        if (shader == null || !ScreenPass.ready(target)) {
            return;
        }
        float dimension = Math.max(target.width, target.height);
        shader.setSampler("SceneSampler", ScreenPass.sceneColor(target));
        shader.safeGetUniform("Params").set(strength, target.width / dimension, target.height / dimension, 0F);
        ScreenPass.draw(shader, ScreenPass.Blend.NONE, event.getProjectionMatrix());
    }
}
