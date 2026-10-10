package com.breakinblocks.nautec.transfer.energy;

import com.breakinblocks.nautec.transfer.transaction.TransactionContext;
import com.google.common.primitives.Ints;

public interface EnergyHandler {
    long getAmountAsLong();

    default int getAmountAsInt() {
        return Ints.saturatedCast(getAmountAsLong());
    }

    long getCapacityAsLong();

    default int getCapacityAsInt() {
        return Ints.saturatedCast(getCapacityAsLong());
    }

    int insert(int amount, TransactionContext transaction);

    int extract(int amount, TransactionContext transaction);
}
