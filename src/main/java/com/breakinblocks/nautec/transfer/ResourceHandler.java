package com.breakinblocks.nautec.transfer;

import com.breakinblocks.nautec.transfer.resource.Resource;
import com.breakinblocks.nautec.transfer.transaction.TransactionContext;
import com.google.common.primitives.Ints;

public interface ResourceHandler<T extends Resource> {
    int size();

    T getResource(int index);

    long getAmountAsLong(int index);

    default int getAmountAsInt(int index) {
        return Ints.saturatedCast(getAmountAsLong(index));
    }

    long getCapacityAsLong(int index, T resource);

    default int getCapacityAsInt(int index, T resource) {
        return Ints.saturatedCast(getCapacityAsLong(index, resource));
    }

    boolean isValid(int index, T resource);

    int insert(int index, T resource, int amount, TransactionContext transaction);

    default int insert(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int inserted = 0;
        int size = size();
        for (int index = 0; index < size; index++) {
            inserted += insert(index, resource, amount - inserted, transaction);
            if (inserted == amount) {
                break;
            }
        }
        return inserted;
    }

    int extract(int index, T resource, int amount, TransactionContext transaction);

    default int extract(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int extracted = 0;
        int size = size();
        for (int index = 0; index < size; index++) {
            extracted += extract(index, resource, amount - extracted, transaction);
            if (extracted == amount) {
                break;
            }
        }
        return extracted;
    }

    @SuppressWarnings("unchecked")
    static <T extends Resource> Class<ResourceHandler<T>> asClass() {
        return (Class<ResourceHandler<T>>) (Object) ResourceHandler.class;
    }
}
