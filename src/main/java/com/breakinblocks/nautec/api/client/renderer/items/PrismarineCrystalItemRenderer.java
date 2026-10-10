package com.breakinblocks.nautec.api.client.renderer.items;

import com.breakinblocks.nautec.client.render.PrismarineCrystalRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

public class PrismarineCrystalItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float ITEM_LIFT = 2.0F;
    private static @Nullable PrismarineCrystalItemRenderer instance;

    public static final IClientItemExtensions EXTENSIONS = new IClientItemExtensions() {
        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return get();
        }
    };

    public PrismarineCrystalItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static PrismarineCrystalItemRenderer get() {
        if (instance == null) {
            instance = new PrismarineCrystalItemRenderer();
        }
        return instance;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, ITEM_LIFT, 0.5F);
        long millis = Util.getMillis();
        float ticks = (millis / 50L % PrismarineCrystalRenderer.TICK_WRAP) + (millis % 50L) / 50F;
        PrismarineCrystalRenderer.submit(poseStack, buffers, ticks, 0L, 0F, false);
        poseStack.popPose();
    }
}
