package com.breakinblocks.nautec.api.augments;

import com.breakinblocks.nautec.NTRegistries;
import net.minecraft.resources.Identifier;

public interface AugmentSlot {
    default String getName() {
        Identifier id = NTRegistries.AUGMENT_SLOT.getKey(this);
        return id != null ? id.getPath() : "unknown";
    }
}
