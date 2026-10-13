package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.Nautec;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class SingularityLens {
    public static final int MAX = 4;
    private static final int UNIFORM_SIZE = uniformSize();
    private static final int RELEASE_AFTER_IDLE_FRAMES = 120;

    private record Singularity(Vec3 centre, float radius, float strength, float glow) {
    }

    private static final List<Singularity> PENDING = new ArrayList<>();
    private static @Nullable TextureTarget scene;
    private static @Nullable MappableRingBuffer uniforms;
    private static int idleFrames;

    private SingularityLens() {
    }

    private static int uniformSize() {
        Std140SizeCalculator calculator = new Std140SizeCalculator().putVec4();
        for (int i = 0; i < MAX * 2; i++) {
            calculator.putVec4();
        }
        return calculator.get();
    }

    public static void submit(Vec3 centre, float radius, float strength, float glow) {
        if (PENDING.size() < 64) {
            PENDING.add(new Singularity(centre, radius, strength, glow));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void render(RenderLevelStageEvent.AfterLevel event) {
        if (PENDING.isEmpty()) {
            if (scene != null && ++idleFrames > RELEASE_AFTER_IDLE_FRAMES) {
                close();
            }
            return;
        }
        idleFrames = 0;
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget target = minecraft.getMainRenderTarget();
        if (target.width <= 0 || target.height <= 0 || target.getColorTexture() == null
                || target.getColorTextureView() == null || target.getDepthTextureView() == null) {
            PENDING.clear();
            return;
        }
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        List<Singularity> chosen = new ArrayList<>(PENDING);
        PENDING.clear();
        chosen.sort(Comparator.comparingDouble(singularity -> singularity.centre().distanceToSqr(camera)));
        int count = Math.min(MAX, chosen.size());

        if (scene == null) {
            scene = new TextureTarget("Nautec singularity scene", target.width, target.height, false);
        } else if (scene.width != target.width || scene.height != target.height) {
            scene.resize(target.width, target.height);
        }
        if (uniforms == null) {
            uniforms = new MappableRingBuffer(() -> "Nautec singularity lens uniforms",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, UNIFORM_SIZE);
        }

        Matrix4f modelView = new Matrix4f(event.getModelViewMatrix());
        float seconds = (minecraft.level == null ? 0 : minecraft.level.getGameTime() % 72_000L) / 20F;
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.copyTextureToTexture(target.getColorTexture(), scene.getColorTexture(), 0, 0, 0, 0, 0, target.width, target.height);
        GpuBuffer buffer = uniforms.currentBuffer();
        try (GpuBuffer.MappedView view = encoder.mapBuffer(buffer, false, true)) {
            Std140Builder builder = Std140Builder.intoBuffer(view.data()).putVec4(target.width, target.height, count, seconds);
            for (int i = 0; i < MAX; i++) {
                if (i < count) {
                    Singularity singularity = chosen.get(i);
                    Vec3 relative = singularity.centre().subtract(camera);
                    Vector3f inView = modelView.transformPosition(new Vector3f((float) relative.x, (float) relative.y, (float) relative.z));
                    builder.putVec4(inView.x, inView.y, inView.z, singularity.radius());
                } else {
                    builder.putVec4(0F, 0F, 0F, 0F);
                }
            }
            for (int i = 0; i < MAX; i++) {
                if (i < count) {
                    Singularity singularity = chosen.get(i);
                    builder.putVec4(singularity.strength(), singularity.glow(), 0F, 0F);
                } else {
                    builder.putVec4(0F, 0F, 0F, 0F);
                }
            }
        }
        try (RenderPass pass = encoder.createRenderPass(() -> "Nautec singularity lens", target.getColorTextureView(), OptionalInt.empty())) {
            pass.setPipeline(NTRenderPipelines.SINGULARITY_LENS);
            pass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
            pass.setUniform("LensInfo", buffer);
            pass.bindTexture("SceneSampler", scene.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
            pass.bindTexture("DepthSampler", target.getDepthTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            pass.draw(0, 3);
        }
        uniforms.rotate();
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        PENDING.clear();
        close();
    }

    @SubscribeEvent
    public static void onShutdown(GameShuttingDownEvent event) {
        close();
    }

    private static void close() {
        idleFrames = 0;
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
