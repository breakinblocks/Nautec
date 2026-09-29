package com.breakinblocks.nautec.client.teleport;

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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class ArrivalWaveRenderer {
    private static final int UNIFORM_SIZE = new Std140SizeCalculator().putMat4f().putVec4().putVec4().get();
    private static final List<MappableRingBuffer> uniformBuffers = new ArrayList<>();

    private ArrivalWaveRenderer() {
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent.AfterLevel event) {
        List<ArrivalWaves.Wave> waves = ArrivalWaves.waves();
        if (waves.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget target = minecraft.getMainRenderTarget();
        if (target.getColorTextureView() == null || target.getDepthTextureView() == null) {
            return;
        }

        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Matrix4f inverseView = new Matrix4f(event.getModelViewMatrix()).invert();
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        for (int index = 0; index < waves.size(); index++) {
            renderWave(encoder, target, camera, inverseView, partialTick, waves.get(index), index);
        }
    }

    private static void renderWave(CommandEncoder encoder, RenderTarget target, Vec3 camera, Matrix4f inverseView,
                                   float partialTick, ArrivalWaves.Wave wave, int index) {
        if (index == uniformBuffers.size()) {
            uniformBuffers.add(new MappableRingBuffer(() -> "Nautec portal arrival uniforms",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, UNIFORM_SIZE));
        }
        MappableRingBuffer uniforms = uniformBuffers.get(index);

        Vec3 center = wave.center().subtract(camera);
        GpuBuffer buffer = uniforms.currentBuffer();
        try (GpuBuffer.MappedView view = encoder.mapBuffer(buffer, false, true)) {
            Std140Builder.intoBuffer(view.data())
                    .putMat4f(inverseView)
                    .putVec4((float) center.x, (float) center.y, (float) center.z, wave.seconds(partialTick))
                    .putVec4(wave.front(partialTick), wave.radius(), wave.fade(partialTick), wave.seed());
        }

        try (RenderPass pass = encoder.createRenderPass(() -> "Nautec portal arrival",
                target.getColorTextureView(), OptionalInt.empty())) {
            pass.setPipeline(NTRenderPipelines.PORTAL_ARRIVAL);
            pass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
            pass.setUniform("ArrivalInfo", buffer);
            pass.bindTexture("DepthSampler", target.getDepthTextureView(),
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            pass.draw(0, 3);
        }
        uniforms.rotate();
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ArrivalWaves.clear();
        close();
    }

    @SubscribeEvent
    public static void onShutdown(GameShuttingDownEvent event) {
        close();
    }

    private static void close() {
        uniformBuffers.forEach(MappableRingBuffer::close);
        uniformBuffers.clear();
    }
}
