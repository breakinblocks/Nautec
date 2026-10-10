package com.breakinblocks.nautec.client.renderer.blockentities;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import com.breakinblocks.nautec.client.render.CustomGeometry;
import com.breakinblocks.nautec.api.client.renderer.blockentities.BERenderState;
import com.breakinblocks.nautec.api.client.renderer.blockentities.NTBERenderer;
import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.content.resonantstorage.ResonantCisternBlockEntity;
import com.breakinblocks.nautec.content.resonantstorage.ResonantStorageBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class ResonantCisternRenderer extends NTBERenderer<ResonantCisternBlockEntity, ResonantCisternRenderer.CisternRenderState> {
    public static final float LIGHT_FRONT = 1.0f;
    public static final float LIGHT_BOTTOM = 14.5f;
    public static final float LIGHT_TOP = 15.5f;
    private static final float MIN = 3.25f / 16f;
    private static final float MAX = 12.75f / 16f;
    private static final float FLOOR = 2.0f / 16f;
    private static final float CEILING = 14.0f / 16f;

    public ResonantCisternRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public CisternRenderState createRenderState() {
        return new CisternRenderState();
    }

    @Override
    public void extractRenderState(ResonantCisternBlockEntity cistern, CisternRenderState state, float partialTick, Vec3 cameraPos) {
        BERenderState.extractBase(cistern, state);
        state.facing = cistern.getBlockState().hasProperty(ResonantStorageBlock.FACING) ? cistern.getBlockState().getValue(ResonantStorageBlock.FACING) : Direction.NORTH;
        state.address = cistern.channel().address();
        FluidStack fluid = cistern.fluid();
        int capacity = Math.max(1, cistern.capacity());
        state.sprite = null;
        if (fluid.isEmpty()) {
            return;
        }
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        int colour = extensions.getTintColor(fluid);
        state.sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extensions.getStillTexture(fluid));
        state.red = (colour >> 16 & 255) / 255f;
        state.green = (colour >> 8 & 255) / 255f;
        state.blue = (colour & 255) / 255f;
        state.alpha = Math.max(0.6f, (colour >> 24 & 255) / 255f);
        state.fill = Math.min(1f, (float) fluid.getAmount() / capacity);
        state.gaseous = fluid.getFluid().getFluidType().isLighterThanAir();
    }

    @Override
    public void submit(CisternRenderState state, PoseStack poseStack, MultiBufferSource buffers, Vec3 cameraPos) {
        if (state.sprite != null && state.fill > 0) {
            TextureAtlasSprite sprite = state.sprite;
            float height = Math.max(0.02f, state.fill) * (CEILING - FLOOR);
            float bottom = state.gaseous ? CEILING - height : FLOOR;
            float top = bottom + height;
            CustomGeometry.submit(poseStack, buffers, RenderType.translucentMovingBlock(), (pose, consumer) ->
                    box(pose.pose(), consumer, sprite, state, bottom, top));
        }
        ResonantLights.submit(poseStack, buffers, state.facing, state.address, LIGHT_FRONT, LIGHT_BOTTOM, LIGHT_TOP);
    }

    private static void box(Matrix4f matrix, VertexConsumer buffer, TextureAtlasSprite sprite, CisternRenderState state, float bottom, float top) {
        float r = state.red;
        float g = state.green;
        float b = state.blue;
        float a = state.alpha;
        int light = state.lightCoords;
        float u0 = sprite.getU(MIN);
        float u1 = sprite.getU(MAX);
        float v0 = sprite.getV(1 - top);
        float v1 = sprite.getV(1 - bottom);
        vertex(buffer, matrix, MIN, bottom, MIN, u0, v1, r, g, b, a, light, 0, 0, -1);
        vertex(buffer, matrix, MIN, top, MIN, u0, v0, r, g, b, a, light, 0, 0, -1);
        vertex(buffer, matrix, MAX, top, MIN, u1, v0, r, g, b, a, light, 0, 0, -1);
        vertex(buffer, matrix, MAX, bottom, MIN, u1, v1, r, g, b, a, light, 0, 0, -1);
        vertex(buffer, matrix, MIN, bottom, MAX, u0, v1, r, g, b, a, light, 0, 0, 1);
        vertex(buffer, matrix, MAX, bottom, MAX, u1, v1, r, g, b, a, light, 0, 0, 1);
        vertex(buffer, matrix, MAX, top, MAX, u1, v0, r, g, b, a, light, 0, 0, 1);
        vertex(buffer, matrix, MIN, top, MAX, u0, v0, r, g, b, a, light, 0, 0, 1);
        vertex(buffer, matrix, MIN, bottom, MIN, u0, v1, r, g, b, a, light, -1, 0, 0);
        vertex(buffer, matrix, MIN, bottom, MAX, u1, v1, r, g, b, a, light, -1, 0, 0);
        vertex(buffer, matrix, MIN, top, MAX, u1, v0, r, g, b, a, light, -1, 0, 0);
        vertex(buffer, matrix, MIN, top, MIN, u0, v0, r, g, b, a, light, -1, 0, 0);
        vertex(buffer, matrix, MAX, bottom, MIN, u0, v1, r, g, b, a, light, 1, 0, 0);
        vertex(buffer, matrix, MAX, top, MIN, u0, v0, r, g, b, a, light, 1, 0, 0);
        vertex(buffer, matrix, MAX, top, MAX, u1, v0, r, g, b, a, light, 1, 0, 0);
        vertex(buffer, matrix, MAX, bottom, MAX, u1, v1, r, g, b, a, light, 1, 0, 0);
        float w0 = sprite.getV(MIN);
        float w1 = sprite.getV(MAX);
        vertex(buffer, matrix, MIN, top, MIN, u0, w0, r, g, b, a, light, 0, 1, 0);
        vertex(buffer, matrix, MIN, top, MAX, u0, w1, r, g, b, a, light, 0, 1, 0);
        vertex(buffer, matrix, MAX, top, MAX, u1, w1, r, g, b, a, light, 0, 1, 0);
        vertex(buffer, matrix, MAX, top, MIN, u1, w0, r, g, b, a, light, 0, 1, 0);
        vertex(buffer, matrix, MIN, bottom, MIN, u0, w0, r, g, b, a, light, 0, -1, 0);
        vertex(buffer, matrix, MAX, bottom, MIN, u1, w0, r, g, b, a, light, 0, -1, 0);
        vertex(buffer, matrix, MAX, bottom, MAX, u1, w1, r, g, b, a, light, 0, -1, 0);
        vertex(buffer, matrix, MIN, bottom, MAX, u0, w1, r, g, b, a, light, 0, -1, 0);
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, float u, float v,
                               float r, float g, float b, float a, int light, float nx, float ny, float nz) {
        buffer.addVertex(matrix, x, y, z).setColor(r, g, b, a).setUv(u, v).setLight(light).setNormal(nx, ny, nz);
    }

    public static class CisternRenderState extends BERenderState {
        Direction facing = Direction.NORTH;
        GatewayAddress address = GatewayAddress.DEFAULT;
        @Nullable TextureAtlasSprite sprite;
        float red;
        float green;
        float blue;
        float alpha;
        float fill;
        boolean gaseous;
    }
}
