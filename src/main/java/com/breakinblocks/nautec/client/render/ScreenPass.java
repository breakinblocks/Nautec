package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.Nautec;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class ScreenPass {
    private static @Nullable TextureTarget depthCopy;
    private static @Nullable TextureTarget colorCopy;
    private static int frame;
    private static int depthFrame = -1;

    private ScreenPass() {
    }

    public enum Blend {
        NONE,
        ADDITIVE,
        TRANSLUCENT
    }

    @SubscribeEvent
    public static void onFrameStart(RenderFrameEvent.Pre event) {
        frame++;
    }

    public static boolean ready(RenderTarget main) {
        return main.width > 0 && main.height > 0 && main.frameBufferId > 0;
    }

    public static int sceneDepth(RenderTarget main) {
        if (depthCopy == null) {
            depthCopy = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
            depthFrame = -1;
        } else if (depthCopy.width != main.width || depthCopy.height != main.height) {
            depthCopy.resize(main.width, main.height, Minecraft.ON_OSX);
            depthFrame = -1;
        }
        if (main.isStencilEnabled() && !depthCopy.isStencilEnabled()) {
            depthCopy.enableStencil();
            depthFrame = -1;
        }
        if (depthFrame != frame) {
            depthCopy.copyDepthFrom(main);
            main.bindWrite(false);
            depthFrame = frame;
        }
        return depthCopy.getDepthTextureId();
    }

    public static int sceneColor(RenderTarget main) {
        if (colorCopy == null) {
            colorCopy = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
            colorCopy.setFilterMode(GL11.GL_LINEAR);
        } else if (colorCopy.width != main.width || colorCopy.height != main.height) {
            colorCopy.resize(main.width, main.height, Minecraft.ON_OSX);
            colorCopy.setFilterMode(GL11.GL_LINEAR);
        }
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, colorCopy.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, main.width, main.height, 0, 0, colorCopy.width, colorCopy.height,
                GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        main.bindWrite(false);
        return colorCopy.getColorTextureId();
    }

    public static void draw(ShaderInstance shader, Blend blend, Matrix4f projection) {
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(projection, VertexSorting.DISTANCE_TO_ORIGIN);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        switch (blend) {
            case NONE -> RenderSystem.disableBlend();
            case ADDITIVE -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            }
            case TRANSLUCENT -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }
        }
        RenderSystem.setShader(() -> shader);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        builder.addVertex(0F, 0F, 0F);
        builder.addVertex(1F, 0F, 0F);
        builder.addVertex(1F, 1F, 0F);
        builder.addVertex(0F, 1F, 0F);
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.restoreProjectionMatrix();
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
        if (depthCopy != null) {
            depthCopy.destroyBuffers();
            depthCopy = null;
        }
        if (colorCopy != null) {
            colorCopy.destroyBuffers();
            colorCopy = null;
        }
        depthFrame = -1;
    }
}
