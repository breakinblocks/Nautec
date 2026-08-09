package com.breakinblocks.nautec.api.items;

import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import net.minecraft.world.item.ItemStack;

public interface IBacteriaItem {
    default IBacteriaStorage getStorage(ItemStack stack) {
        return stack.getCapability(NTCapabilities.BacteriaStorage.ITEM);
    }
}
