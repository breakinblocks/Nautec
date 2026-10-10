package com.breakinblocks.nautec.transfer.adapter;

import com.breakinblocks.nautec.transfer.TransferPreconditions;
import com.breakinblocks.nautec.transfer.energy.EnergyHandler;
import com.breakinblocks.nautec.transfer.transaction.SnapshotJournal;
import com.breakinblocks.nautec.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class WrappedEnergyStorage implements EnergyHandler {
    private final IEnergyStorage storage;
    private final UndoJournal journal = new UndoJournal();

    public WrappedEnergyStorage(IEnergyStorage storage) {
        this.storage = storage;
    }

    public IEnergyStorage storage() {
        return storage;
    }

    @Override
    public long getAmountAsLong() {
        return storage.getEnergyStored();
    }

    @Override
    public long getCapacityAsLong() {
        return storage.getMaxEnergyStored();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (amount == 0 || !storage.canReceive() || storage.receiveEnergy(amount, true) <= 0) {
            return 0;
        }
        journal.updateSnapshots(transaction);
        int inserted = storage.receiveEnergy(amount, false);
        journal.balance += inserted;
        return inserted;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (amount == 0 || !storage.canExtract() || storage.extractEnergy(amount, true) <= 0) {
            return 0;
        }
        journal.updateSnapshots(transaction);
        int extracted = storage.extractEnergy(amount, false);
        journal.balance -= extracted;
        return extracted;
    }

    private final class UndoJournal extends SnapshotJournal<Long> {
        private long balance;

        @Override
        protected Long createSnapshot() {
            return balance;
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            long delta = balance - snapshot;
            if (delta > 0) {
                storage.extractEnergy((int) Math.min(Integer.MAX_VALUE, delta), false);
            } else if (delta < 0) {
                storage.receiveEnergy((int) Math.min(Integer.MAX_VALUE, -delta), false);
            }
            balance = snapshot;
        }

        @Override
        protected void onRootCommit(Long originalState) {
            balance = 0;
        }
    }
}
