package com.breakinblocks.nautec.client.renderer.items;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.renderer.entity.EmissiveGeoLayer;
import com.breakinblocks.nautec.content.items.blocks.LaserCraftingMatrixItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class LaserCraftingMatrixItemRenderer extends GeoItemRenderer<LaserCraftingMatrixItem> {
    private static final ResourceLocation GLOWMASK = Nautec.rl("textures/block/laser_crafting_matrix_glowmask.png");

    public LaserCraftingMatrixItemRenderer() {
        super(new DefaultedBlockGeoModel<>(Nautec.rl("laser_crafting_matrix")));
        addRenderLayer(new EmissiveGeoLayer<>(this, GLOWMASK));
    }
}
