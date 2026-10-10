package com.breakinblocks.nautec.transfer.adapter;

import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.TransferPreconditions;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import com.breakinblocks.nautec.transfer.transaction.SnapshotJournal;
import com.breakinblocks.nautec.transfer.transaction.TransactionContext;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.List;

public final class WrappedItemHandler implements ResourceHandler<ItemResource> {
    private final IItemHandler handler;
    private final UndoJournal journal = new UndoJournal();

    public WrappedItemHandler(IItemHandler handler) {
        this.handler = handler;
    }

    public IItemHandler handler() {
        return handler;
    }

    @Override
    public int size() {
        return handler.getSlots();
    }

    @Override
    public ItemResource getResource(int index) {
        return ItemResource.of(handler.getStackInSlot(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return handler.getStackInSlot(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        int limit = handler.getSlotLimit(index);
        return resource.isEmpty() ? limit : Math.min(limit, resource.getMaxStackSize());
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return !resource.isEmpty() && handler.isItemValid(index, resource.toStack());
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            return 0;
        }
        ItemStack remainder = handler.insertItem(index, resource.toStack(amount), true);
        int inserted = amount - remainder.getCount();
        if (inserted <= 0) {
            return 0;
        }
        journal.updateSnapshots(transaction);
        ItemStack previous = handler.getStackInSlot(index).copy();
        ItemStack actual = handler.insertItem(index, resource.toStack(inserted), false);
        int done = inserted - actual.getCount();
        if (done > 0) {
            journal.record(index, previous, true, resource, done);
        }
        return done;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || !resource.matches(handler.getStackInSlot(index))) {
            return 0;
        }
        ItemStack simulated = handler.extractItem(index, amount, true);
        if (simulated.isEmpty() || !resource.matches(simulated)) {
            return 0;
        }
        journal.updateSnapshots(transaction);
        ItemStack previous = handler.getStackInSlot(index).copy();
        ItemStack extracted = handler.extractItem(index, simulated.getCount(), false);
        if (!extracted.isEmpty()) {
            journal.record(index, previous, false, resource, extracted.getCount());
        }
        return extracted.getCount();
    }

    private final class UndoJournal extends SnapshotJournal<Integer> {
        private final IntArrayList slots = new IntArrayList();
        private final List<ItemStack> previousStacks = new ArrayList<>();
        private final List<Boolean> inserts = new ArrayList<>();
        private final List<ItemResource> resources = new ArrayList<>();
        private final IntArrayList amounts = new IntArrayList();

        void record(int slot, ItemStack previous, boolean insert, ItemResource resource, int amount) {
            slots.add(slot);
            previousStacks.add(previous);
            inserts.add(insert);
            resources.add(resource);
            amounts.add(amount);
        }

        @Override
        protected Integer createSnapshot() {
            return slots.size();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            for (int i = slots.size() - 1; i >= snapshot; i--) {
                int slot = slots.getInt(i);
                if (handler instanceof IItemHandlerModifiable modifiable) {
                    modifiable.setStackInSlot(slot, previousStacks.get(i));
                } else if (inserts.get(i)) {
                    handler.extractItem(slot, amounts.getInt(i), false);
                } else {
                    handler.insertItem(slot, resources.get(i).toStack(amounts.getInt(i)), false);
                }
            }
            truncate(snapshot);
        }

        @Override
        protected void onRootCommit(Integer originalState) {
            truncate(0);
        }

        private void truncate(int size) {
            slots.size(size);
            amounts.size(size);
            while (inserts.size() > size) {
                int last = inserts.size() - 1;
                inserts.remove(last);
                resources.remove(last);
                previousStacks.remove(last);
            }
        }
    }
}
