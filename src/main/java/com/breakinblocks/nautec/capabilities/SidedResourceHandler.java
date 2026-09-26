package com.breakinblocks.nautec.capabilities;

import com.breakinblocks.nautec.utils.Utils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntList;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class SidedResourceHandler<R extends Resource> implements ResourceHandler<R> {
    private final ResourceHandler<R> innerHandler;
    private final IOActions action;
    private final IntList slots;

    public SidedResourceHandler(ResourceHandler<R> innerHandler, IOActions action, IntList slots) {
        this.innerHandler = innerHandler;
        this.action = action;
        this.slots = IntList.of(slots.toIntArray());
    }

    public SidedResourceHandler(ResourceHandler<R> innerHandler, Pair<IOActions, int[]> actionSlotsPair) {
        this(innerHandler, actionSlotsPair != null ? actionSlotsPair.left() : IOActions.NONE,
                actionSlotsPair != null ? Utils.intArrayToList(actionSlotsPair.right()) : IntList.of());
    }

    @Override
    public int size() {
        return innerHandler.size();
    }

    @Override
    public R getResource(int index) {
        return innerHandler.getResource(index);
    }

    @Override
    public long getAmountAsLong(int index) {
        return innerHandler.getAmountAsLong(index);
    }

    @Override
    public long getCapacityAsLong(int index, R resource) {
        return innerHandler.getCapacityAsLong(index, resource);
    }

    @Override
    public boolean isValid(int index, R resource) {
        return (action == IOActions.INSERT || action == IOActions.BOTH) && slots.contains(index) && innerHandler.isValid(index, resource);
    }

    @Override
    public int insert(int index, R resource, int amount, TransactionContext transaction) {
        return (action == IOActions.INSERT || action == IOActions.BOTH) && slots.contains(index) ? innerHandler.insert(index, resource, amount, transaction) : 0;
    }

    @Override
    public int extract(int index, R resource, int amount, TransactionContext transaction) {
        return (action == IOActions.EXTRACT || action == IOActions.BOTH) && slots.contains(index) ? innerHandler.extract(index, resource, amount, transaction) : 0;
    }
}
