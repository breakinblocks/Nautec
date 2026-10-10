package com.breakinblocks.nautec.transfer.adapter;

import com.breakinblocks.nautec.transfer.energy.EnergyHandler;
import com.breakinblocks.nautec.transfer.energy.SimpleEnergyHandler;
import com.breakinblocks.nautec.transfer.transaction.Transaction;
import net.neoforged.neoforge.energy.IEnergyStorage;

public record ExposedEnergyStorage(EnergyHandler handler) implements IEnergyStorage {
    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        if (toReceive <= 0) {
            return 0;
        }
        try (Transaction tx = Transaction.openNested()) {
            int inserted = handler.insert(toReceive, tx);
            if (!simulate) {
                tx.commit();
            }
            return inserted;
        }
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (toExtract <= 0) {
            return 0;
        }
        try (Transaction tx = Transaction.openNested()) {
            int extracted = handler.extract(toExtract, tx);
            if (!simulate) {
                tx.commit();
            }
            return extracted;
        }
    }

    @Override
    public int getEnergyStored() {
        return handler.getAmountAsInt();
    }

    @Override
    public int getMaxEnergyStored() {
        return handler.getCapacityAsInt();
    }

    @Override
    public boolean canExtract() {
        return !(handler instanceof SimpleEnergyHandler simple) || simple.getMaxExtract() > 0;
    }

    @Override
    public boolean canReceive() {
        return !(handler instanceof SimpleEnergyHandler simple) || simple.getMaxInsert() > 0;
    }
}
