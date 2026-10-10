package com.breakinblocks.nautec.client.sonar;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.render.NTShaders;
import com.breakinblocks.nautec.client.render.ScreenPass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import org.joml.Matrix4f;

/** Depth-reconstructed surface sweep, adapted from Scannable Reforged (MIT; LICENSE-SCANNABLE). */
@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class SonarPulseRenderer {
    private static final Matrix4f INVERSE_VIEW = new Matrix4f();

    private SonarPulseRenderer() {
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        if (!NautecSonarManager.isActive()) return;

        Minecraft minecraft = Minecraft.getInstance();
        Vec3 camera = event.getCamera().getPosition();
        float partialTick = minecraft.getTimer().getGameTimeDeltaPartialTick(false);
        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().set(event.getModelViewMatrix());
        SonarHighlightRenderer.render(poseStack, camera, partialTick);

        ShaderInstance shader = NTShaders.sonarWave();
        RenderTarget target = minecraft.getMainRenderTarget();
        if (shader == null || !ScreenPass.ready(target)) return;
        INVERSE_VIEW.set(event.getModelViewMatrix()).invert();
        int depth = -1;
        for (SonarScan scan : NautecSonarManager.scans()) {
            if (!scan.isPulseActive()) continue;
            if (depth < 0) depth = ScreenPass.sceneDepth(target);
            Vec3 center = scan.center().subtract(camera);
            shader.setSampler("DepthSampler", depth);
            shader.safeGetUniform("InvViewMat").set(INVERSE_VIEW);
            shader.safeGetUniform("Center").set((float) center.x, (float) center.y, (float) center.z, 0F);
            shader.safeGetUniform("Params").set(scan.pulseRadius(partialTick), SonarWave.BAND_WIDTH, 0F, 0F);
            ScreenPass.draw(shader, ScreenPass.Blend.ADDITIVE, event.getProjectionMatrix());
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        NautecSonarManager.clear();
        close();
    }

    @SubscribeEvent
    public static void onShutdown(GameShuttingDownEvent event) {
        close();
    }

    private static void close() {
        SonarHighlightRenderer.close();
    }
}
