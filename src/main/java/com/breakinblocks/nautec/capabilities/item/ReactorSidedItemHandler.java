package com.breakinblocks.nautec.capabilities.item;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class ReactorSidedItemHandler implements ResourceHandler<ItemResource> {
    private final ResourceHandler<ItemResource> inner;
    private final IntSet insertSlots;
    private final IntSet extractSlots;

    public ReactorSidedItemHandler(ResourceHandler<ItemResource> inner, IntSet insertSlots, IntSet extractSlots) {
        this.inner = inner;
        this.insertSlots = insertSlots;
        this.extractSlots = extractSlots;
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public ItemResource getResource(int index) {
        return index < inner.size() ? inner.getResource(index) : ItemResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
        return index < inner.size() ? inner.getAmountAsLong(index) : 0;
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return index < inner.size() ? inner.getCapacityAsLong(index, resource) : 0;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return insertSlots.contains(index) && index < inner.size() && inner.isValid(index, resource);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return insertSlots.contains(index) && index < inner.size() ? inner.insert(index, resource, amount, transaction) : 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return extractSlots.contains(index) && index < inner.size() ? inner.extract(index, resource, amount, transaction) : 0;
    }
}
