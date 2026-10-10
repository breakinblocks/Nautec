package com.breakinblocks.nautec.api.augments;

import com.breakinblocks.nautec.NTRegistries;
import net.minecraft.resources.ResourceLocation;

public interface AugmentSlot {
    default String getName() {
        ResourceLocation id = NTRegistries.AUGMENT_SLOT.getKey(this);
        return id != null ? id.getPath() : "unknown";
    }
}
