package com.breakinblocks.nautec.client.teleport;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.render.NTRenderPipelines;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

/** Blurs the composited world before hands and HUD, including when Iris is active. */
@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class TeleportBlurRenderer {
    private static @Nullable TextureTarget scene;
    private static @Nullable MappableRingBuffer uniforms;

    private TeleportBlurRenderer() {
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent.AfterLevel event) {
        Minecraft minecraft = Minecraft.getInstance();
        float strength = TeleportFxManager.screenStrength(minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false));
        if (strength <= 0F) return;

        RenderTarget target = minecraft.getMainRenderTarget();
        if (target.width <= 0 || target.height <= 0 || target.getColorTexture() == null
                || target.getColorTextureView() == null) return;
        if (scene == null) scene = new TextureTarget("Nautec teleport scene", target.width, target.height, false);
        else if (scene.width != target.width || scene.height != target.height) scene.resize(target.width, target.height);
        if (uniforms == null) uniforms = new MappableRingBuffer(() -> "Nautec teleport blur uniforms",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, 16);

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.copyTextureToTexture(target.getColorTexture(), scene.getColorTexture(),
                0, 0, 0, 0, 0, target.width, target.height);
        GpuBuffer buffer = uniforms.currentBuffer();
        float dimension = Math.max(target.width, target.height);
        try (GpuBuffer.MappedView view = encoder.mapBuffer(buffer, false, true)) {
            Std140Builder.intoBuffer(view.data()).putVec4(strength, target.width / dimension, target.height / dimension, 0F);
        }
        try (RenderPass pass = encoder.createRenderPass(() -> "Nautec teleport radial blur",
                target.getColorTextureView(), OptionalInt.empty())) {
            pass.setPipeline(NTRenderPipelines.TELEPORT_BLUR);
            pass.setUniform("BlurInfo", buffer);
            pass.bindTexture("SceneSampler", scene.getColorTextureView(),
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
            pass.draw(0, 3);
        }
        uniforms.rotate();
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        close();
    }

    @SubscribeEvent
    public static void onShutdown(GameShuttingDownEvent event) {
        close();
    }

    private static void close() {
        if (scene != null) {
            scene.destroyBuffers();
            scene = null;
        }
        if (uniforms != null) {
            uniforms.close();
            uniforms = null;
        }
    }
}
