package com.breakinblocks.nautec.client.renderer.items;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.renderer.entity.EmissiveGeoLayer;
import com.breakinblocks.nautec.content.resonantstorage.ResonantVaultItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ResonantVaultItemRenderer extends GeoItemRenderer<ResonantVaultItem> {
    private static final ResourceLocation GLOWMASK = Nautec.rl("textures/block/resonant_vault_glowmask.png");

    public ResonantVaultItemRenderer() {
        super(new DefaultedBlockGeoModel<>(Nautec.rl("resonant_vault")));
        addRenderLayer(new EmissiveGeoLayer<>(this, GLOWMASK));
    }
}
