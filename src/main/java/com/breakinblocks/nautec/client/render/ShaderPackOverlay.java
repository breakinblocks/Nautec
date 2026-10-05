package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.Nautec;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class ShaderPackOverlay {
    private static final int MAX_QUEUED = 50_000;
    private static final List<Entry> POOL = new ArrayList<>();
    private static int queued;
    private static int packActiveThisFrame = -1;

    private static boolean resolved;
    private static @Nullable Object irisApi;
    private static @Nullable Method shaderPackInUse;
    private static @Nullable Method renderingShadowPass;
    private static @Nullable ByteBufferBuilder buffer;
    private static @Nullable TextureTarget opaqueDepth;
    private static boolean depthCaptured;
    private static @Nullable Vec3 anchor;

    private ShaderPackOverlay() {
    }

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer renderer) {
        if (!shaderPackActive()) {
            collector.submitCustomGeometry(poseStack, renderType, renderer);
            return;
        }
        if (queued >= MAX_QUEUED || renderingShadowPass()) {
            return;
        }
        if (queued == POOL.size()) {
            POOL.add(new Entry());
        }
        PoseStack.Pose pose = poseStack.last();
        Entry entry = POOL.get(queued++);
        entry.pose.set(pose.pose());
        entry.normal.set(pose.normal());
        entry.renderType = renderType;
        entry.renderer = renderer;
        entry.anchor = anchor;
    }

    public static void anchored(Vec3 worldAnchor, Runnable submissions) {
        Vec3 previous = anchor;
        anchor = worldAnchor;
        try {
            submissions.run();
        } finally {
            anchor = previous;
        }
    }

    public static void init() {
        resolve();
    }

    public static boolean shaderPackActive() {
        if (packActiveThisFrame < 0) {
            resolve();
            packActiveThisFrame = irisApi != null && invoke(shaderPackInUse) ? 1 : 0;
        }
        return packActiveThisFrame == 1;
    }

    @SubscribeEvent
    public static void onFrameStart(RenderFrameEvent.Pre event) {
        packActiveThisFrame = -1;
    }

    private static boolean renderingShadowPass() {
        return irisApi != null && invoke(renderingShadowPass);
    }

    private static boolean invoke(@Nullable Method method) {
        if (method == null) {
            return false;
        }
        try {
            return (boolean) method.invoke(irisApi);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    private static void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        if (!ModList.get().isLoaded("iris")) {
            return;
        }
        try {
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            irisApi = api.getMethod("getInstance").invoke(null);
            shaderPackInUse = api.getMethod("isShaderPackInUse");
            renderingShadowPass = api.getMethod("isRenderingShadowPass");
            assignItemPipelines(api);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            Nautec.LOGGER.warn("Could not reach the Iris API, NauTec effects will draw in the normal pass", e);
            irisApi = null;
        }
    }

    private static void assignItemPipelines(Class<?> api) throws ReflectiveOperationException {
        Class<?> programs = Class.forName("net.irisshaders.iris.api.v0.IrisProgram");
        Method assign = api.getMethod("assignPipeline", RenderPipeline.class, programs);
        assign.invoke(irisApi, NTRenderPipelines.CRYSTAL_SHELL, program(programs, "EMISSIVE_ENTITIES"));
        assign.invoke(irisApi, NTRenderPipelines.CRYSTAL_CORE, program(programs, "EMISSIVE_ENTITIES"));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object program(Class<?> programs, String name) {
        return Enum.valueOf((Class) programs, name);
    }

    @SubscribeEvent
    public static void captureDepth(RenderLevelStageEvent.AfterOpaqueFeatures event) {
        depthCaptured = false;
        if (queued == 0 || !shaderPackActive() || renderingShadowPass()) {
            return;
        }
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        GpuTexture depth = main.getDepthTexture();
        if (depth == null || main.width <= 0 || main.height <= 0) {
            return;
        }
        if (opaqueDepth == null) {
            opaqueDepth = new TextureTarget("Nautec opaque depth", main.width, main.height, true);
        } else if (opaqueDepth.width != main.width || opaqueDepth.height != main.height) {
            opaqueDepth.resize(main.width, main.height);
        }
        GpuTexture copy = opaqueDepth.getDepthTexture();
        if (copy == null) {
            return;
        }
        RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(depth, copy, 0, 0, 0, 0, 0, main.width, main.height);
        depthCaptured = true;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent.AfterLevel event) {
        if (queued == 0) {
            depthCaptured = false;
            return;
        }
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        if (depthCaptured && opaqueDepth != null && opaqueDepth.getDepthTexture() != null && main.getDepthTexture() != null
                && opaqueDepth.width == main.width && opaqueDepth.height == main.height) {
            RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(opaqueDepth.getDepthTexture(), main.getDepthTexture(),
                    0, 0, 0, 0, 0, main.width, main.height);
        }
        depthCaptured = false;
        if (buffer == null) {
            buffer = new ByteBufferBuilder(1 << 16);
        }
        Matrix4f modelView = new Matrix4f(event.getModelViewMatrix());
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        Matrix3f viewNormal = new Matrix3f(modelView);
        MultiBufferSource.BufferSource source = MultiBufferSource.immediate(buffer);
        PoseStack poseStack = new PoseStack();
        PoseStack.Pose pose = poseStack.last();
        for (int i = 0; i < queued; i++) {
            Entry entry = POOL.get(i);
            pose.pose().set(modelView);
            if (entry.anchor != null) {
                Vec3 offset = entry.anchor.subtract(camera);
                pose.pose().translate((float) offset.x, (float) offset.y, (float) offset.z);
            }
            pose.pose().mul(entry.pose);
            pose.normal().set(viewNormal).mul(entry.normal);
            entry.renderer.render(pose, source.getBuffer(entry.renderType));
        }
        source.endBatch();
        clearQueue();
    }

    private static void clearQueue() {
        for (int i = 0; i < queued; i++) {
            POOL.get(i).release();
        }
        queued = 0;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clearQueue();
        POOL.clear();
        close();
    }

    @SubscribeEvent
    public static void onShutdown(GameShuttingDownEvent event) {
        close();
    }

    private static void close() {
        if (buffer != null) {
            buffer.close();
            buffer = null;
        }
        if (opaqueDepth != null) {
            opaqueDepth.destroyBuffers();
            opaqueDepth = null;
        }
    }

    private static final class Entry {
        private final Matrix4f pose = new Matrix4f();
        private final Matrix3f normal = new Matrix3f();
        private @Nullable RenderType renderType;
        private @Nullable SubmitNodeCollector.CustomGeometryRenderer renderer;
        private @Nullable Vec3 anchor;

        private void release() {
            renderType = null;
            renderer = null;
            anchor = null;
        }
    }
}
