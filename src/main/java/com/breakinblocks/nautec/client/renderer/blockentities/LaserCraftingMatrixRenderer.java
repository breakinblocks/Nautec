package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.renderer.entity.EmissiveGeoLayer;
import com.breakinblocks.nautec.content.blockentities.LaserCraftingMatrixBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class LaserCraftingMatrixRenderer extends GeoBlockRenderer<LaserCraftingMatrixBlockEntity> {
    private static final ResourceLocation GLOWMASK = Nautec.rl("textures/block/laser_crafting_matrix_glowmask.png");

    public LaserCraftingMatrixRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
        addRenderLayer(new EmissiveGeoLayer<>(this, GLOWMASK));
    }

    private static final class Model extends DefaultedBlockGeoModel<LaserCraftingMatrixBlockEntity> {
        private Model() {
            super(Nautec.rl("laser_crafting_matrix"));
        }

        @Override
        public void handleAnimations(LaserCraftingMatrixBlockEntity matrix, long instanceId,
                                     AnimationState<LaserCraftingMatrixBlockEntity> animationState, float partialTick) {
            animationState.setData(LaserCraftingMatrixBlockEntity.WORKING, matrix.isRunning());
            super.handleAnimations(matrix, instanceId, animationState, partialTick);
        }
    }
}
