package com.breakinblocks.nautec.capabilities.item;

import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.SidedResourceHandler;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntList;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class SidedItemHandler extends SidedResourceHandler<ItemResource> {
    public SidedItemHandler(ResourceHandler<ItemResource> inner, IOActions action, IntList slots) {
        super(inner, action, slots);
    }

    public SidedItemHandler(ResourceHandler<ItemResource> inner, Pair<IOActions, int[]> actionSlots) {
        super(inner, actionSlots);
    }
}
