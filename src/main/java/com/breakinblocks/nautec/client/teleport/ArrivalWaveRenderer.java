package com.breakinblocks.nautec.client.teleport;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.render.NTShaders;
import com.breakinblocks.nautec.client.render.ScreenPass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.List;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class ArrivalWaveRenderer {
    private static final Matrix4f INVERSE_VIEW = new Matrix4f();

    private ArrivalWaveRenderer() {
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        List<ArrivalWaves.Wave> waves = ArrivalWaves.waves();
        if (waves.isEmpty()) {
            return;
        }
        ShaderInstance shader = NTShaders.portalArrival();
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget target = minecraft.getMainRenderTarget();
        if (shader == null || !ScreenPass.ready(target)) {
            return;
        }

        Vec3 camera = event.getCamera().getPosition();
        float partialTick = minecraft.getTimer().getGameTimeDeltaPartialTick(false);
        INVERSE_VIEW.set(event.getModelViewMatrix()).invert();
        int depth = ScreenPass.sceneDepth(target);
        for (int index = 0; index < waves.size(); index++) {
            ArrivalWaves.Wave wave = waves.get(index);
            Vec3 center = wave.center().subtract(camera);
            shader.setSampler("DepthSampler", depth);
            shader.safeGetUniform("InvViewMat").set(INVERSE_VIEW);
            shader.safeGetUniform("Center").set((float) center.x, (float) center.y, (float) center.z, wave.seconds(partialTick));
            shader.safeGetUniform("Params").set(wave.front(partialTick), wave.radius(), wave.fade(partialTick), wave.seed());
            ScreenPass.draw(shader, ScreenPass.Blend.ADDITIVE, event.getProjectionMatrix());
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ArrivalWaves.clear();
    }
}
