package com.breakinblocks.nautec.client.renderer.blockentities;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import com.breakinblocks.nautec.client.render.CustomGeometry;
import com.breakinblocks.nautec.api.client.renderer.blockentities.BERenderState;
import com.breakinblocks.nautec.api.client.renderer.blockentities.NTBERenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.breakinblocks.nautec.api.utils.HorizontalDirection;
import com.breakinblocks.nautec.capabilities.fluid.FluidTank;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.client.model.block.WhiskModel;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.content.blocks.MixerBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class MixerBERenderer extends NTBERenderer<MixerBlockEntity, MixerBERenderer.MixerRenderState> {
    private static final HorizontalDirection[] HORIZONTAL_DIRECTIONS = HorizontalDirection.values();
    private static final float SIDE_MARGIN = (float) MixerBlock.SHAPE.min(Direction.Axis.X) + 0.075f;
    private static final float MIN_Y = 2 / 16f;
    private static final float MAX_Y = 1 - MIN_Y;
    private final WhiskModel model;

    public MixerBERenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new WhiskModel(ctx.bakeLayer(WhiskModel.LAYER_LOCATION));
        this.model.setupAnim();
    }

    @Override
    public MixerRenderState createRenderState() {
        return new MixerRenderState();
    }

    @Override
    public void extractRenderState(MixerBlockEntity blockEntity, MixerRenderState state, float partialTick, Vec3 cameraPos) {
        BERenderState.extractBase(blockEntity, state);
        state.whiskAngle = blockEntity.getIndependentAngle(partialTick);

        ItemStackHandler handler = blockEntity.getItemStackHandler();
        int itemCount = handler.getSlots() - 1;
        while (state.items.size() < itemCount) {
            state.items.add(new ItemStackRenderState());
        }
        for (int i = 0; i < itemCount; i++) {
            state.items.get(i).update(handler.getStackInSlot(i), ItemDisplayContext.NONE, blockEntity.getLevel(), 1);
        }
        state.centerItem.update(handler.getStackInSlot(handler.getSlots() - 1), ItemDisplayContext.NONE, blockEntity.getLevel(), 1);

        state.fluids.clear();
        FluidTank fluidTank = blockEntity.getFluidTank();
        FluidStack fluidStack = fluidTank.getFluid();
        int fluidCapacity = fluidTank.getCapacity();

        float fillPercentage = Math.min(1, (float) fluidStack.getAmount() / fluidCapacity) / 2;
        if (!fluidStack.isEmpty()) {
            if (fluidStack.getFluid().getFluidType().isLighterThanAir())
                extractFluid(state, fluidStack, fillPercentage, 1, MIN_Y, MAX_Y / 2);
            else
                extractFluid(state, fluidStack, 1, fillPercentage, MIN_Y, MAX_Y / 2);
        }

        FluidStack fluidInTank = blockEntity.getSecondaryFluidTank().getFluid();
        float fillPercentage1 = Math.min(1, (float) fluidInTank.getAmount() / fluidCapacity) / 2;
        if (!fluidInTank.isEmpty()) {
            extractFluid(state, fluidInTank, 1, fillPercentage1, MIN_Y + (MAX_Y / 2 - MIN_Y) * fillPercentage, MAX_Y);
        }
    }

    private static void extractFluid(MixerRenderState state, FluidStack fluidStack, float alpha, float heightPercentage, float minY, float maxY) {
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extensions.getStillTexture(fluidStack));
        int color = extensions.getTintColor(fluidStack);
        alpha *= (color >> 24 & 255) / 255f;
        float red = (color >> 16 & 255) / 255f;
        float green = (color >> 8 & 255) / 255f;
        float blue = (color & 255) / 255f;
        state.fluids.add(new FluidQuadData(sprite, red, green, blue, alpha, heightPercentage, minY, maxY));
    }

    @Override
    public void submit(MixerRenderState state, PoseStack poseStack, MultiBufferSource buffers, Vec3 cameraPos) {
        poseStack.pushPose();
        {
            poseStack.translate(0.5, 0, 0.5);
            poseStack.mulPose(Axis.YP.rotation(state.whiskAngle));
            poseStack.translate(-0.5, 0, -0.5);
            poseStack.translate(0.5, 1.425, 0.75);
            this.model.submit(poseStack, buffers, WhiskModel.RENDER_TYPE, state.lightCoords, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();

        for (int i = 0; i < state.items.size(); i++) {
            submitItem(state.items.get(i), i, poseStack, buffers, state.lightCoords);
        }

        poseStack.pushPose();
        {
            poseStack.translate(0.5, 0.25, 0.5);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            poseStack.scale(0.75f, 0.75f, 0.75f);
            state.centerItem.submit(poseStack, buffers, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        }
        poseStack.popPose();

        for (FluidQuadData fluid : state.fluids) {
            CustomGeometry.submit(poseStack, buffers, RenderType.translucentMovingBlock(), (pose, consumer) ->
                    renderQuads(pose.pose(), consumer, fluid.sprite(), fluid.red(), fluid.green(), fluid.blue(), fluid.alpha(), fluid.heightPercentage(), state.lightCoords, fluid.minY(), fluid.maxY()));
        }
    }

    private static void submitItem(ItemStackRenderState itemState, int index, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        Direction direction = HORIZONTAL_DIRECTIONS[index].toRegularDirection();

        poseStack.pushPose();
        {
            Vector3f normal = direction.step();
            poseStack.translate(normal.x / 5, normal.y / 5, normal.z / 5);
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotation((float) Math.toRadians(index * 90)));
            poseStack.scale(0.25f, 0.25f, 0.25f);
            itemState.submit(poseStack, buffers, packedLight, OverlayTexture.NO_OVERLAY, 0);
        }
        poseStack.popPose();
    }

    private static void renderQuads(Matrix4f matrix, VertexConsumer buffer, TextureAtlasSprite sprite, float r, float g, float b, float alpha, float heightPercentage, int light, float minY, float maxY) {
        float height = minY + (maxY - minY) * heightPercentage;
        float minU = sprite.getU(SIDE_MARGIN), maxU = sprite.getU((1 - SIDE_MARGIN));
        float minV = sprite.getV(minY), maxV = sprite.getV(height);
        buffer.addVertex(matrix, SIDE_MARGIN, minY, SIDE_MARGIN).setColor(r, g, b, alpha).setUv(minU, minV).setLight(light).setNormal(0, 0, -1);
        buffer.addVertex(matrix, SIDE_MARGIN, height, SIDE_MARGIN).setColor(r, g, b, alpha).setUv(minU, maxV).setLight(light).setNormal(0, 0, -1);
        buffer.addVertex(matrix, 1 - SIDE_MARGIN, height, SIDE_MARGIN).setColor(r, g, b, alpha).setUv(maxU, maxV).setLight(light).setNormal(0, 0, -1);
        buffer.addVertex(matrix, 1 - SIDE_MARGIN, minY, SIDE_MARGIN).setColor(r, g, b, alpha).setUv(maxU, minV).setLight(light).setNormal(0, 0, -1);
        buffer.addVertex(matrix, SIDE_MARGIN, minY, 1 - SIDE_MARGIN).setColor(r, g, b, alpha).setUv(minU, minV).setLight(light).setNormal(0, 0, 1);
        buffer.addVertex(matrix, 1 - SIDE_MARGIN, minY, 1 - SIDE_MARGIN).setColor(r, g, b, alpha).setUv(maxU, minV).setLight(light).setNormal(0, 0, 1);
        buffer.addVertex(matrix, 1 - SIDE_MARGIN, height, 1 - SIDE_MARGIN).setColor(r, g, b, alpha).setUv(maxU, maxV).setLight(light).setNormal(0, 0, 1);
        buffer.addVertex(matrix, SIDE_MARGIN, height, 1 - SIDE_MARGIN).setColor(r, g, b, alpha).setUv(minU, maxV).setLight(light).setNormal(0, 0, 1);
        buffer.addVertex(matrix, SIDE_MARGIN, minY, SIDE_MARGIN).setColor(r, g, b, alpha).setUv(minU, minV).setLight(light).setNormal(-1, 0, 0);
        buffer.addVertex(matrix, SIDE_MARGIN, minY, 1 - SIDE_MARGIN).setColor(r, g, b, alpha).setUv(maxU, minV).setLight(light).setNormal(-1, 0, 0);
        buffer.addVertex(matrix, SIDE_MARGIN, height, 1 - SIDE_MARGIN).setColor(r, g, b, alpha).setUv(maxU, maxV).setLight(light).setNormal(-1, 0, 0);
        buffer.addVertex(matrix, SIDE_MARGIN, height, SIDE_MARGIN).setColor(r, g, b, alpha).setUv(minU, maxV).setLight(light).setNormal(-1, 0, 0);
        buffer.addVertex(matrix, 1 - SIDE_MARGIN, minY, SIDE_MARGIN).setColor(r, g, b, alpha).setUv(minU, minV).setLight(light).setNormal(1, 0, 0);
        buffer.addVertex(matrix, 1 - SIDE_MARGIN, height, SIDE_MARGIN).setColor(r, g, b, alpha).setUv(minU, maxV).setLight(light).setNormal(1, 0, 0);
        buffer.addVertex(matrix, 1 - SIDE_MARGIN, height, 1 - SIDE_MARGIN).setColor(r, g, b, alpha).setUv(maxU, maxV).setLight(light).setNormal(1, 0, 0);
        buffer.addVertex(matrix, 1 - SIDE_MARGIN, minY, 1 - SIDE_MARGIN).setColor(r, g, b, alpha).setUv(maxU, minV).setLight(light).setNormal(1, 0, 0);
        if (heightPercentage < 1) {
            minV = sprite.getV(SIDE_MARGIN);
            maxV = sprite.getV(1 - SIDE_MARGIN);
            buffer.addVertex(matrix, SIDE_MARGIN, height, SIDE_MARGIN).setColor(r, g, b, alpha).setUv(minU, minV).setLight(light).setNormal(0, 1, 0);
            buffer.addVertex(matrix, SIDE_MARGIN, height, 1 - SIDE_MARGIN).setColor(r, g, b, alpha).setUv(minU, maxV).setLight(light).setNormal(0, 1, 0);
            buffer.addVertex(matrix, 1 - SIDE_MARGIN, height, 1 - SIDE_MARGIN).setColor(r, g, b, alpha).setUv(maxU, maxV).setLight(light).setNormal(0, 1, 0);
            buffer.addVertex(matrix, 1 - SIDE_MARGIN, height, SIDE_MARGIN).setColor(r, g, b, alpha).setUv(maxU, minV).setLight(light).setNormal(0, 1, 0);
        }
    }

    public record FluidQuadData(TextureAtlasSprite sprite, float red, float green, float blue, float alpha, float heightPercentage, float minY, float maxY) {
    }

    public static class MixerRenderState extends BERenderState {
        public float whiskAngle;
        public final List<ItemStackRenderState> items = new ArrayList<>();
        public final ItemStackRenderState centerItem = new ItemStackRenderState();
        public final List<FluidQuadData> fluids = new ArrayList<>();
    }
}
