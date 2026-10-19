package com.breakinblocks.nautec.content.reservoir;

import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.fluid.FluidResource;
import com.breakinblocks.nautec.transfer.transaction.SnapshotJournal;
import com.breakinblocks.nautec.transfer.transaction.TransactionContext;

import java.util.function.LongSupplier;

public final class ReservoirTank implements ResourceHandler<FluidResource> {
    private final LongSupplier capacity;
    private final Runnable onChange;
    private final Journal journal = new Journal();
    private FluidResource resource = FluidResource.EMPTY;
    private long amount;

    public ReservoirTank(LongSupplier capacity, Runnable onChange) {
        this.capacity = capacity;
        this.onChange = onChange;
    }

    public FluidResource resource() {
        return resource;
    }

    public long amount() {
        return amount;
    }

    public long capacity() {
        return Math.max(0, capacity.getAsLong());
    }

    public ReservoirFluid contents() {
        return ReservoirFluid.of(resource, amount);
    }

    public void load(ReservoirFluid fluid) {
        this.resource = fluid.isEmpty() ? FluidResource.EMPTY : fluid.resource();
        this.amount = fluid.isEmpty() ? 0 : fluid.amount();
    }

    public long excessOver(long limit) {
        return Math.max(0, amount - Math.max(0, limit));
    }

    public long trimTo(long limit) {
        long voided = excessOver(limit);
        if (voided > 0) {
            amount -= voided;
            if (amount <= 0) {
                amount = 0;
                resource = FluidResource.EMPTY;
            }
            onChange.run();
        }
        return voided;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int index) {
        return index == 0 ? resource : FluidResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
        return index == 0 ? amount : 0;
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return index == 0 ? capacity() : 0;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return index == 0;
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (index != 0 || resource.isEmpty() || amount <= 0) {
            return 0;
        }
        if (!this.resource.isEmpty() && !this.resource.equals(resource)) {
            return 0;
        }
        long room = capacity() - this.amount;
        int accepted = (int) Math.max(0, Math.min(amount, room));
        if (accepted > 0) {
            journal.updateSnapshots(transaction);
            this.resource = resource;
            this.amount += accepted;
        }
        return accepted;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (index != 0 || resource.isEmpty() || amount <= 0 || this.resource.isEmpty() || !this.resource.equals(resource)) {
            return 0;
        }
        int given = (int) Math.min(amount, this.amount);
        if (given > 0) {
            journal.updateSnapshots(transaction);
            this.amount -= given;
            if (this.amount <= 0) {
                this.amount = 0;
                this.resource = FluidResource.EMPTY;
            }
        }
        return given;
    }

    private record Snapshot(FluidResource resource, long amount) {
    }

    private final class Journal extends SnapshotJournal<Snapshot> {
        @Override
        protected Snapshot createSnapshot() {
            return new Snapshot(resource, amount);
        }

        @Override
        protected void revertToSnapshot(Snapshot snapshot) {
            resource = snapshot.resource();
            amount = snapshot.amount();
        }

        @Override
        protected void onRootCommit(Snapshot originalState) {
            onChange.run();
        }
    }
}
