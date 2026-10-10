package com.breakinblocks.nautec.capabilities;

import it.unimi.dsi.fastutil.ints.IntSet;
import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.resource.Resource;
import com.breakinblocks.nautec.transfer.transaction.TransactionContext;

public final class RoleResourceHandler<R extends Resource> implements ResourceHandler<R> {
    private final ResourceHandler<R> inner;
    private final IntSet insertSlots;
    private final IntSet extractSlots;
    private final R empty;

    public RoleResourceHandler(ResourceHandler<R> inner, IntSet insertSlots, IntSet extractSlots, R empty) {
        this.inner = inner;
        this.insertSlots = insertSlots;
        this.extractSlots = extractSlots;
        this.empty = empty;
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public R getResource(int index) {
        return index < inner.size() ? inner.getResource(index) : empty;
    }

    @Override
    public long getAmountAsLong(int index) {
        return index < inner.size() ? inner.getAmountAsLong(index) : 0;
    }

    @Override
    public long getCapacityAsLong(int index, R resource) {
        return index < inner.size() ? inner.getCapacityAsLong(index, resource) : 0;
    }

    @Override
    public boolean isValid(int index, R resource) {
        return insertSlots.contains(index) && index < inner.size() && inner.isValid(index, resource);
    }

    @Override
    public int insert(int index, R resource, int amount, TransactionContext transaction) {
        return insertSlots.contains(index) && index < inner.size() ? inner.insert(index, resource, amount, transaction) : 0;
    }

    @Override
    public int extract(int index, R resource, int amount, TransactionContext transaction) {
        return extractSlots.contains(index) && index < inner.size() ? inner.extract(index, resource, amount, transaction) : 0;
    }
}
