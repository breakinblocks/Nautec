package com.breakinblocks.nautec.transfer.energy;

import com.breakinblocks.nautec.transfer.TransferPreconditions;
import com.breakinblocks.nautec.transfer.transaction.SnapshotJournal;
import com.breakinblocks.nautec.transfer.transaction.TransactionContext;
import com.breakinblocks.nautec.utils.valueio.ValueIOSerializable;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;

public class SimpleEnergyHandler implements EnergyHandler, ValueIOSerializable {
    protected int energy;
    protected int capacity;
    protected int maxInsert;
    protected int maxExtract;
    private final EnergyJournal journal = new EnergyJournal();

    public SimpleEnergyHandler(int capacity) {
        this(capacity, capacity);
    }

    public SimpleEnergyHandler(int capacity, int maxTransfer) {
        this(capacity, maxTransfer, maxTransfer);
    }

    public SimpleEnergyHandler(int capacity, int maxInsert, int maxExtract) {
        this(capacity, maxInsert, maxExtract, 0);
    }

    public SimpleEnergyHandler(int capacity, int maxInsert, int maxExtract, int energy) {
        TransferPreconditions.checkNonNegative(capacity);
        TransferPreconditions.checkNonNegative(maxInsert);
        TransferPreconditions.checkNonNegative(maxExtract);
        TransferPreconditions.checkNonNegative(energy);
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
        this.energy = energy;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("energy", energy);
    }

    @Override
    public void deserialize(ValueInput input) {
        energy = Math.max(0, input.getIntOr("energy", 0));
    }

    public void set(int amount) {
        TransferPreconditions.checkNonNegative(amount);
        if (energy != amount) {
            int previous = energy;
            energy = amount;
            onEnergyChanged(previous);
        }
    }

    protected void onEnergyChanged(int previousAmount) {
    }

    public int getMaxInsert() {
        return maxInsert;
    }

    public int getMaxExtract() {
        return maxExtract;
    }

    @Override
    public long getAmountAsLong() {
        return energy;
    }

    @Override
    public long getCapacityAsLong() {
        return capacity;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        int inserted = Math.min(capacity - energy, Math.min(amount, maxInsert));
        if (inserted > 0) {
            journal.updateSnapshots(transaction);
            energy += inserted;
            return inserted;
        }
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        int extracted = Math.min(energy, Math.min(amount, maxExtract));
        if (extracted > 0) {
            journal.updateSnapshots(transaction);
            energy -= extracted;
            return extracted;
        }
        return 0;
    }

    private final class EnergyJournal extends SnapshotJournal<Integer> {
        @Override
        protected Integer createSnapshot() {
            return energy;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            energy = snapshot;
        }

        @Override
        protected void onRootCommit(Integer originalState) {
            if (energy != originalState) {
                onEnergyChanged(originalState);
            }
        }
    }
}
