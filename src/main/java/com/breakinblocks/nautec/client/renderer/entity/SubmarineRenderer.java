package com.breakinblocks.nautec.client.renderer.entity;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.model.entity.SubmarineModel;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SubmarineRenderer extends GeoEntityRenderer<SubmarineEntity> {
    private static final ResourceLocation EMISSIVE = Nautec.rl("textures/entity/submarine_e.png");

    private static final float PITCH_PIVOT = 4.5F / 16F;

    public SubmarineRenderer(EntityRendererProvider.Context context) {
        super(context, new SubmarineModel());
        this.shadowRadius = 1.1F * SubmarineEntity.MODEL_SCALE;
        withScale(SubmarineEntity.MODEL_SCALE);
        addRenderLayer(new EmissiveGeoLayer<>(this, EMISSIVE));
    }

    @Override
    public boolean shouldShowName(SubmarineEntity submarine) {
        return false;
    }

    @Override
    public @Nullable RenderType getRenderType(SubmarineEntity submarine, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        if (submarine.isInvisible()) {
            return super.getRenderType(submarine, texture, bufferSource, partialTick);
        }
        return RenderType.entityTranslucent(texture);
    }

    @Override
    protected void applyRotations(SubmarineEntity submarine, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        float yaw = Mth.rotLerp(partialTick, submarine.yRotO, submarine.getYRot());
        float pitch = Mth.rotLerp(partialTick, submarine.xRotO, submarine.getXRot());
        poseStack.translate(0F, PITCH_PIVOT + SubmarineEntity.MODEL_Y_OFFSET, 0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180F - yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        poseStack.translate(0F, -PITCH_PIVOT, -SubmarineEntity.MODEL_Z_OFFSET);
    }
}
