package com.breakinblocks.nautec.transfer.item;

import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.adapter.WrappedItemHandler;
import com.breakinblocks.nautec.transfer.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

public final class WorldlyContainerWrapper implements ResourceHandler<ItemResource> {
    private final WrappedItemHandler delegate;

    public WorldlyContainerWrapper(WorldlyContainer container, @Nullable Direction side) {
        this.delegate = new WrappedItemHandler(new SidedInvWrapper(container, side));
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public ItemResource getResource(int index) {
        return delegate.getResource(index);
    }

    @Override
    public long getAmountAsLong(int index) {
        return delegate.getAmountAsLong(index);
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return delegate.getCapacityAsLong(index, resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return delegate.isValid(index, resource);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return delegate.insert(index, resource, amount, transaction);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return delegate.extract(index, resource, amount, transaction);
    }
}
