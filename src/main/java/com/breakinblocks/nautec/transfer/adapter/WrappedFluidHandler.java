package com.breakinblocks.nautec.transfer.adapter;

import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.TransferPreconditions;
import com.breakinblocks.nautec.transfer.fluid.FluidResource;
import com.breakinblocks.nautec.transfer.transaction.SnapshotJournal;
import com.breakinblocks.nautec.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

public final class WrappedFluidHandler implements ResourceHandler<FluidResource> {
    private final IFluidHandler handler;
    private final UndoJournal journal = new UndoJournal();

    public WrappedFluidHandler(IFluidHandler handler) {
        this.handler = handler;
    }

    public IFluidHandler handler() {
        return handler;
    }

    @Override
    public int size() {
        return handler.getTanks();
    }

    @Override
    public FluidResource getResource(int index) {
        return FluidResource.of(handler.getFluidInTank(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return handler.getFluidInTank(index).getAmount();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return handler.getTankCapacity(index);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return !resource.isEmpty() && handler.isFluidValid(index, resource.toStack(1000));
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return insert(resource, amount, transaction);
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            return 0;
        }
        FluidStack stack = resource.toStack(amount);
        int simulated = handler.fill(stack, IFluidHandler.FluidAction.SIMULATE);
        if (simulated <= 0) {
            return 0;
        }
        journal.updateSnapshots(transaction);
        int filled = handler.fill(resource.toStack(simulated), IFluidHandler.FluidAction.EXECUTE);
        if (filled > 0) {
            journal.record(true, resource, filled);
        }
        return filled;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return extract(resource, amount, transaction);
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            return 0;
        }
        FluidStack simulated = handler.drain(resource.toStack(amount), IFluidHandler.FluidAction.SIMULATE);
        if (simulated.isEmpty() || !resource.matches(simulated)) {
            return 0;
        }
        journal.updateSnapshots(transaction);
        FluidStack drained = handler.drain(resource.toStack(simulated.getAmount()), IFluidHandler.FluidAction.EXECUTE);
        if (!drained.isEmpty()) {
            journal.record(false, resource, drained.getAmount());
        }
        return drained.getAmount();
    }

    private final class UndoJournal extends SnapshotJournal<Integer> {
        private final List<Boolean> fills = new ArrayList<>();
        private final List<FluidResource> resources = new ArrayList<>();
        private final List<Integer> amounts = new ArrayList<>();

        void record(boolean fill, FluidResource resource, int amount) {
            fills.add(fill);
            resources.add(resource);
            amounts.add(amount);
        }

        @Override
        protected Integer createSnapshot() {
            return fills.size();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            for (int i = fills.size() - 1; i >= snapshot; i--) {
                FluidStack stack = resources.get(i).toStack(amounts.get(i));
                if (fills.get(i)) {
                    handler.drain(stack, IFluidHandler.FluidAction.EXECUTE);
                } else {
                    handler.fill(stack, IFluidHandler.FluidAction.EXECUTE);
                }
            }
            truncate(snapshot);
        }

        @Override
        protected void onRootCommit(Integer originalState) {
            truncate(0);
        }

        private void truncate(int size) {
            while (fills.size() > size) {
                int last = fills.size() - 1;
                fills.remove(last);
                resources.remove(last);
                amounts.remove(last);
            }
        }
    }
}
