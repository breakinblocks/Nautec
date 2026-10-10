package com.breakinblocks.nautec.transfer.item;

import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.adapter.WrappedItemHandler;
import net.minecraft.world.Container;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public final class VanillaContainerWrapper {
    private VanillaContainerWrapper() {
    }

    public static ResourceHandler<ItemResource> of(Container container) {
        return new WrappedItemHandler(new InvWrapper(container));
    }
}
