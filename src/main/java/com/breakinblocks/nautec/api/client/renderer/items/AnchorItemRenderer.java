package com.breakinblocks.nautec.api.client.renderer.items;

import com.breakinblocks.nautec.client.model.block.AnchorModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

public class AnchorItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static @Nullable AnchorItemRenderer instance;

    public static final IClientItemExtensions EXTENSIONS = new IClientItemExtensions() {
        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return get();
        }
    };

    private @Nullable AnchorModel model;

    public AnchorItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static AnchorItemRenderer get() {
        if (instance == null) {
            instance = new AnchorItemRenderer();
        }
        return instance;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.model = null;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (this.model == null) {
            this.model = new AnchorModel(Minecraft.getInstance().getEntityModels().bakeLayer(AnchorModel.LAYER_LOCATION));
            this.model.setupAnim();
        }
        this.model.renderToBuffer(poseStack, buffers.getBuffer(AnchorModel.RENDER_TYPE), packedLight, packedOverlay);
    }
}
