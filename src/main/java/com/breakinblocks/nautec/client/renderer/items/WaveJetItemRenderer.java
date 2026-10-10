package com.breakinblocks.nautec.client.renderer.items;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.renderer.entity.EmissiveGeoLayer;
import com.breakinblocks.nautec.content.items.WaveJetItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.function.BiConsumer;

public class WaveJetItemRenderer extends GeoItemRenderer<WaveJetItem> {
    private static final ResourceLocation EMISSIVE = Nautec.rl("textures/entity/wave_jet_e.png");

    private static final float CENTRE_Y = 5.5625F / 16F;
    private static final float CENTRE_Z = 5.125F / 16F;

    public WaveJetItemRenderer() {
        super(new Model());
        ((Model) this.model).renderer = this;
        addRenderLayer(new EmissiveGeoLayer<>(this, EMISSIVE));
    }

    private static float scaleFor(@Nullable ItemDisplayContext context) {
        if (context == null) {
            return 0.85F;
        }
        return switch (context) {
            case GUI, FIXED, GROUND -> 0.85F;
            default -> 1.0F;
        };
    }

    private @Nullable LivingEntity holder() {
        ItemStack stack = this.currentItemStack;
        return stack == null ? null : HeldItemOwner.of(stack, this.renderPerspective);
    }

    private boolean isThrusting() {
        LivingEntity holder = holder();
        return holder != null && holder.isUsingItem() && holder.getUseItem() == this.currentItemStack;
    }

    @Override
    public void preRender(PoseStack poseStack, WaveJetItem item, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, item, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (isReRender) {
            return;
        }
        ItemDisplayContext context = this.renderPerspective;
        float scale = scaleFor(context);
        poseStack.scale(scale, scale, scale);
        if (context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            LivingEntity holder = holder();
            boolean swimming = holder != null && holder.getPose() == Pose.SWIMMING;
            float gripOffset = swimming ? 0.45F : 0.22F;
            poseStack.translate(context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND ? gripOffset : -gripOffset, -0.6F, 0.0F);
            if (swimming) {
                poseStack.mulPose(Axis.XP.rotationDegrees(53.0F));
            }
        }
        poseStack.translate(0F, -CENTRE_Y, -CENTRE_Z);
    }

    private static final class Model extends DefaultedEntityGeoModel<WaveJetItem> {
        private @Nullable WaveJetItemRenderer renderer;

        private Model() {
            super(Nautec.rl("wave_jet"));
        }

        @Override
        public void addAdditionalStateData(WaveJetItem animatable, long instanceId, BiConsumer<DataTicket<WaveJetItem>, WaveJetItem> dataConsumer) {
            super.addAdditionalStateData(animatable, instanceId, dataConsumer);
            GeoStateData.put(dataConsumer, WaveJetItem.THRUSTING, this.renderer != null && this.renderer.isThrusting());
        }
    }
}
