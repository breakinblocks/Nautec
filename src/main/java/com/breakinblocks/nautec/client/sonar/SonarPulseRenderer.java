package com.breakinblocks.nautec.client.sonar;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.render.NTRenderPipelines;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.OptionalInt;

/** Depth-reconstructed surface sweep, adapted from Scannable Reforged (MIT; LICENSE-SCANNABLE). */
@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class SonarPulseRenderer {
    private static final int UNIFORM_SIZE = new Std140SizeCalculator().putMat4f().putVec4().putVec4().get();
    private static @Nullable MappableRingBuffer uniforms;

    private SonarPulseRenderer() {
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent.AfterLevel event) {
        if (!NautecSonarManager.isActive()) return;

        Minecraft minecraft = Minecraft.getInstance();
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().set(event.getModelViewMatrix());
        SonarHighlightRenderer.render(poseStack, camera, partialTick);
        if (!NautecSonarManager.isPulseActive()) return;

        RenderTarget target = minecraft.getMainRenderTarget();
        if (target.getColorTextureView() == null || target.getDepthTextureView() == null) return;

        if (uniforms == null) {
            uniforms = new MappableRingBuffer(() -> "Nautec sonar uniforms",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, UNIFORM_SIZE);
        }

        // Use the same camera and projection as the depth buffer, before hand rendering clears it.
        Vec3 center = NautecSonarManager.center().subtract(camera);
        Matrix4f inverseView = new Matrix4f(event.getModelViewMatrix()).invert();
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        GpuBuffer buffer = uniforms.currentBuffer();
        try (GpuBuffer.MappedView view = encoder.mapBuffer(buffer, false, true)) {
            Std140Builder.intoBuffer(view.data())
                    .putMat4f(inverseView)
                    .putVec4((float) center.x, (float) center.y, (float) center.z, 0F)
                    .putVec4(NautecSonarManager.pulseRadius(partialTick), SonarWave.BAND_WIDTH, 0F, 0F);
        }

        // Depth is sampled, never attached for writing, so terrain and later overlays remain intact.
        try (RenderPass pass = encoder.createRenderPass(() -> "Nautec sonar surface sweep",
                target.getColorTextureView(), OptionalInt.empty())) {
            pass.setPipeline(NTRenderPipelines.SONAR_WAVE);
            pass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
            pass.setUniform("ScanInfo", buffer);
            pass.bindTexture("DepthSampler", target.getDepthTextureView(),
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            pass.draw(0, 3);
        }
        uniforms.rotate();
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
        if (uniforms != null) {
            uniforms.close();
            uniforms = null;
        }
    }
}
