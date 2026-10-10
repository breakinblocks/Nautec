package com.breakinblocks.nautec.transfer.energy;

import com.breakinblocks.nautec.transfer.TransferPreconditions;
import com.breakinblocks.nautec.transfer.transaction.Transaction;
import com.breakinblocks.nautec.transfer.transaction.TransactionContext;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public final class EnergyHandlerUtil {
    private EnergyHandlerUtil() {
    }

    public static boolean isFull(EnergyHandler handler) {
        return handler.getAmountAsLong() >= handler.getCapacityAsLong();
    }

    public static int getRedstoneSignalFromEnergyHandler(EnergyHandler handler) {
        long amount = handler.getAmountAsLong();
        long capacity = handler.getCapacityAsLong();
        if (amount == 0 || capacity == 0) {
            return 0;
        }
        return Mth.lerpDiscrete(Math.min(1.0F, (float) amount / capacity), 0, 15);
    }

    public static int move(@Nullable EnergyHandler from, @Nullable EnergyHandler to, int amount, @Nullable TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (from == null || to == null || amount == 0) {
            return 0;
        }
        try (Transaction outer = Transaction.open(transaction)) {
            int available;
            try (Transaction simulation = Transaction.open(outer)) {
                available = from.extract(amount, simulation);
            }
            if (available == 0) {
                return 0;
            }
            int inserted = to.insert(available, outer);
            if (inserted != from.extract(inserted, outer)) {
                return 0;
            }
            outer.commit();
            return inserted;
        }
    }
}
