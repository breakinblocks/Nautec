package com.breakinblocks.nautec.compat.curio;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class PrismMonocleCurioRenderer implements ICurioRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/models/armor/prismarine_layer_1.png");

    private HumanoidModel<LivingEntity> model;

    public HumanoidModel<LivingEntity> getModel() {
        if (model == null) {
            model = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
            model.setAllVisible(false);
            model.head.visible = true;
            model.hat.visible = true;
        }
        return model;
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack,
                                                                          RenderLayerParent<T, M> renderLayerParent, MultiBufferSource bufferSource,
                                                                          int light, float limbSwing, float limbSwingAmount, float partialTicks,
                                                                          float ageInTicks, float netHeadYaw, float headPitch) {
        HumanoidModel<LivingEntity> humanoid = getModel();
        ICurioRenderer.followBodyRotations(slotContext.entity(), humanoid);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(TEXTURE));
        humanoid.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, -1);
    }
}
