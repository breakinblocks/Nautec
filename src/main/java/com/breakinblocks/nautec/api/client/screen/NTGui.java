package com.breakinblocks.nautec.api.client.screen;

import com.breakinblocks.nautec.utils.ARGB;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;

public final class NTGui {
    public static final float OVER_ITEM_Z = 200F;

    private NTGui() {
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        blit(graphics, texture, x, y, u, v, width, height, width, height, textureWidth, textureHeight, -1);
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color) {
        blit(graphics, texture, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color);
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int srcWidth, int srcHeight, int textureWidth, int textureHeight) {
        blit(graphics, texture, x, y, u, v, width, height, srcWidth, srcHeight, textureWidth, textureHeight, -1);
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int srcWidth, int srcHeight, int textureWidth, int textureHeight, int color) {
        innerBlit(graphics, texture, x, x + width, y, y + height,
                u / textureWidth, (u + srcWidth) / textureWidth,
                v / textureHeight, (v + srcHeight) / textureHeight, color);
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1) {
        innerBlit(graphics, texture, x0, x1, y0, y1, u0, u1, v0, v1, -1);
    }

    public static void blitSprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        blitSprite(graphics, sprite, x, y, width, height, -1);
    }

    public static void blitSprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height, float alpha) {
        blitSprite(graphics, sprite, x, y, width, height, ARGB.white(alpha));
    }

    public static void blitSprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (color != -1) {
            graphics.setColor(ARGB.red(color) / 255F, ARGB.green(color) / 255F, ARGB.blue(color) / 255F, ARGB.alpha(color) / 255F);
        }
        graphics.blitSprite(sprite, x, y, width, height);
        if (color != -1) {
            graphics.setColor(1F, 1F, 1F, 1F);
        }
        RenderSystem.disableBlend();
    }

    public static void blitSprite(GuiGraphics graphics, ResourceLocation sprite, int spriteWidth, int spriteHeight, int textureX, int textureY, int x, int y, int width, int height) {
        blitSprite(graphics, sprite, spriteWidth, spriteHeight, textureX, textureY, x, y, width, height, -1);
    }

    public static void blitSprite(GuiGraphics graphics, ResourceLocation sprite, int spriteWidth, int spriteHeight, int textureX, int textureY, int x, int y, int width, int height, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (color != -1) {
            graphics.setColor(ARGB.red(color) / 255F, ARGB.green(color) / 255F, ARGB.blue(color) / 255F, ARGB.alpha(color) / 255F);
        }
        graphics.blitSprite(sprite, spriteWidth, spriteHeight, textureX, textureY, x, y, width, height);
        if (color != -1) {
            graphics.setColor(1F, 1F, 1F, 1F);
        }
        RenderSystem.disableBlend();
    }

    public static void blitSprite(GuiGraphics graphics, TextureAtlasSprite sprite, int x, int y, int width, int height) {
        blitSprite(graphics, sprite, x, y, width, height, -1);
    }

    public static void blitSprite(GuiGraphics graphics, TextureAtlasSprite sprite, int x, int y, int width, int height, int color) {
        if (width != 0 && height != 0) {
            innerBlit(graphics, sprite.atlasLocation(), x, x + width, y, y + height, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), color);
        }
    }

    public static void blitSpriteRegion(GuiGraphics graphics, TextureAtlasSprite sprite, int x, int y, int width, int height, float uFrom, float uTo, float vFrom, float vTo, int color) {
        if (width != 0 && height != 0) {
            innerBlit(graphics, sprite.atlasLocation(), x, x + width, y, y + height,
                    sprite.getU(uFrom), sprite.getU(uTo), sprite.getV(vFrom), sprite.getV(vTo), color);
        }
    }

    public static void innerBlit(GuiGraphics graphics, ResourceLocation texture, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.addVertex(matrix, x0, y0, 0F).setUv(u0, v0).setColor(color);
        builder.addVertex(matrix, x0, y1, 0F).setUv(u0, v1).setColor(color);
        builder.addVertex(matrix, x1, y1, 0F).setUv(u1, v1).setColor(color);
        builder.addVertex(matrix, x1, y0, 0F).setUv(u1, v0).setColor(color);
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void pushOverItems(GuiGraphics graphics) {
        graphics.pose().pushPose();
        graphics.pose().translate(0F, 0F, OVER_ITEM_Z);
    }

    public static void popOverItems(GuiGraphics graphics) {
        graphics.pose().popPose();
    }

    public static TextureAtlasSprite fluidStillSprite(FluidStack stack) {
        Fluid fluid = stack.getFluid();
        ResourceLocation still = IClientFluidTypeExtensions.of(fluid).getStillTexture(stack);
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
    }

    public static int fluidTint(FluidStack stack) {
        return IClientFluidTypeExtensions.of(stack.getFluid()).getTintColor(stack);
    }

    public static TextureAtlasSprite blockSprite(ResourceLocation location) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(location);
    }
}
