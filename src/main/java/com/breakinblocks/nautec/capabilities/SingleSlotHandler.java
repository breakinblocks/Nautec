package com.breakinblocks.nautec.capabilities;

import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.resource.Resource;
import com.breakinblocks.nautec.transfer.transaction.TransactionContext;

public final class SingleSlotHandler<R extends Resource> implements ResourceHandler<R> {
    private final ResourceHandler<R> inner;
    private final int slot;

    public SingleSlotHandler(ResourceHandler<R> inner, int slot) {
        this.inner = inner;
        this.slot = slot;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public R getResource(int index) {
        return inner.getResource(slot);
    }

    @Override
    public long getAmountAsLong(int index) {
        return inner.getAmountAsLong(slot);
    }

    @Override
    public long getCapacityAsLong(int index, R resource) {
        return inner.getCapacityAsLong(slot, resource);
    }

    @Override
    public boolean isValid(int index, R resource) {
        return inner.isValid(slot, resource);
    }

    @Override
    public int insert(int index, R resource, int amount, TransactionContext transaction) {
        return inner.insert(slot, resource, amount, transaction);
    }

    @Override
    public int extract(int index, R resource, int amount, TransactionContext transaction) {
        return inner.extract(slot, resource, amount, transaction);
    }
}
