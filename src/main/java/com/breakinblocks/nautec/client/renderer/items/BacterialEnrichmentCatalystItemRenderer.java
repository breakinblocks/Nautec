package com.breakinblocks.nautec.client.renderer.items;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.renderer.entity.EmissiveGeoLayer;
import com.breakinblocks.nautec.content.items.blocks.BacterialEnrichmentCatalystItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class BacterialEnrichmentCatalystItemRenderer extends GeoItemRenderer<BacterialEnrichmentCatalystItem> {
    private static final ResourceLocation GLOWMASK = Nautec.rl("textures/block/bacterial_enrichment_catalyst_glowmask.png");

    public BacterialEnrichmentCatalystItemRenderer() {
        super(new DefaultedBlockGeoModel<>(Nautec.rl("bacterial_enrichment_catalyst")));
        addRenderLayer(new EmissiveGeoLayer<>(this, GLOWMASK));
    }
}
