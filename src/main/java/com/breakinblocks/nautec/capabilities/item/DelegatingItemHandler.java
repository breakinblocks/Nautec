package com.breakinblocks.nautec.capabilities.item;

import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import com.breakinblocks.nautec.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class DelegatingItemHandler implements ResourceHandler<ItemResource> {
    private final Supplier<@Nullable ResourceHandler<ItemResource>> target;

    public DelegatingItemHandler(Supplier<@Nullable ResourceHandler<ItemResource>> target) {
        this.target = target;
    }

    @Override
    public int size() {
        ResourceHandler<ItemResource> handler = target.get();
        return handler == null ? 0 : handler.size();
    }

    @Override
    public ItemResource getResource(int index) {
        ResourceHandler<ItemResource> handler = target.get();
        return handler == null || index >= handler.size() ? ItemResource.EMPTY : handler.getResource(index);
    }

    @Override
    public long getAmountAsLong(int index) {
        ResourceHandler<ItemResource> handler = target.get();
        return handler == null || index >= handler.size() ? 0 : handler.getAmountAsLong(index);
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        ResourceHandler<ItemResource> handler = target.get();
        return handler == null || index >= handler.size() ? 0 : handler.getCapacityAsLong(index, resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        ResourceHandler<ItemResource> handler = target.get();
        return handler != null && index < handler.size() && handler.isValid(index, resource);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        ResourceHandler<ItemResource> handler = target.get();
        return handler == null || index >= handler.size() ? 0 : handler.insert(index, resource, amount, transaction);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        ResourceHandler<ItemResource> handler = target.get();
        return handler == null || index >= handler.size() ? 0 : handler.extract(index, resource, amount, transaction);
    }
}
