package com.breakinblocks.nautec.capabilities.item;

import it.unimi.dsi.fastutil.ints.IntList;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public final class OutputSlotsItemHandler implements ResourceHandler<ItemResource> {
    private final ResourceHandler<ItemResource> inner;
    private final @Nullable ResourceHandler<ItemResource> sided;
    private final IntList outputs;

    public OutputSlotsItemHandler(ResourceHandler<ItemResource> inner, @Nullable ResourceHandler<ItemResource> sided, int[] outputs) {
        this.inner = inner;
        this.sided = sided;
        this.outputs = IntList.of(outputs);
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public ItemResource getResource(int index) {
        return inner.getResource(index);
    }

    @Override
    public long getAmountAsLong(int index) {
        return inner.getAmountAsLong(index);
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return inner.getCapacityAsLong(index, resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return sided != null && !outputs.contains(index) && sided.isValid(index, resource);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return sided != null && !outputs.contains(index) ? sided.insert(index, resource, amount, transaction) : 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (outputs.contains(index)) {
            return inner.extract(index, resource, amount, transaction);
        }
        return sided != null ? sided.extract(index, resource, amount, transaction) : 0;
    }
}
