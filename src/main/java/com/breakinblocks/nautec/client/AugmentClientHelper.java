package com.breakinblocks.nautec.client;

import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.client.renderer.augments.helper.AugmentLayerRenderer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import com.breakinblocks.nautec.utils.AugmentHelper;

public final class AugmentClientHelper {
    public static void initCache(Player player) {
        Map<AugmentSlot, Augment> playerAugments = AugmentHelper.getAugments(player);
        Map<AugmentSlot, Augment> filteredAugments = new HashMap<>();
        
        for (Map.Entry<AugmentSlot, Augment> entry : playerAugments.entrySet()) {
            if (entry.getValue() != null) {
                filteredAugments.put(entry.getKey(), entry.getValue());
            }
        }
        
        AugmentLayerRenderer.AUGMENTS_CACHE = filteredAugments;
    }

    public static void invalidateCacheFor(Player player, AugmentSlot augmentSlot) {
        AugmentLayerRenderer.AUGMENTS_CACHE.remove(augmentSlot);
        var augment = AugmentHelper.getAugmentBySlot(player, augmentSlot);
        if (augment != null) {
            AugmentLayerRenderer.AUGMENTS_CACHE.put(augmentSlot, augment);
        }
    }
}
