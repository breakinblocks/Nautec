package com.breakinblocks.nautec.client.renderer.items;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.renderer.entity.EmissiveGeoLayer;
import com.breakinblocks.nautec.content.items.SubmarineItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SubmarineItemRenderer extends GeoItemRenderer<SubmarineItem> {
    private static final ResourceLocation EMISSIVE = Nautec.rl("textures/entity/submarine_e.png");

    private static final float CENTRE_Y = 7.5F / 16F;
    private static final float CENTRE_Z = -2.5F / 16F;

    public SubmarineItemRenderer() {
        super(new DefaultedEntityGeoModel<>(Nautec.rl("submarine")));
        addRenderLayer(new EmissiveGeoLayer<>(this, EMISSIVE));
    }

    private static float scaleFor(@Nullable ItemDisplayContext context) {
        if (context == null) {
            return 1.1F;
        }
        return switch (context) {
            case GUI, FIXED -> 1.1F;
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> 2.6F;
            default -> 1.0F;
        };
    }

    @Override
    public RenderType getRenderType(SubmarineItem item, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void preRender(PoseStack poseStack, SubmarineItem item, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, item, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (isReRender) {
            return;
        }
        float scale = scaleFor(this.renderPerspective);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0F, -CENTRE_Y, CENTRE_Z);
    }
}
