package com.breakinblocks.nautec.transfer;

import com.breakinblocks.nautec.transfer.resource.Resource;
import com.breakinblocks.nautec.transfer.resource.ResourceStack;
import com.breakinblocks.nautec.transfer.transaction.Transaction;
import com.breakinblocks.nautec.transfer.transaction.TransactionContext;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

public final class ResourceHandlerUtil {
    private ResourceHandlerUtil() {
    }

    public static boolean isEmpty(Resource resource, int amount) {
        return amount <= 0 || resource.isEmpty();
    }

    public static boolean isEmpty(ResourceHandler<? extends Resource> handler) {
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            if (handler.getAmountAsLong(i) > 0) {
                return false;
            }
        }
        return true;
    }

    public static <T extends Resource> boolean isFull(ResourceHandler<T> handler) {
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            if (handler.getAmountAsLong(i) < handler.getCapacityAsLong(i, handler.getResource(i))) {
                return false;
            }
        }
        return true;
    }

    public static <T extends Resource> boolean isValid(ResourceHandler<T> handler, T resource) {
        TransferPreconditions.checkNonEmpty(resource);
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            if (handler.isValid(i, resource)) {
                return true;
            }
        }
        return false;
    }

    public static <T extends Resource> int getRedstoneSignalFromResourceHandler(ResourceHandler<T> handler) {
        float proportion = 0.0F;
        int samples = 0;
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            long fill = handler.getAmountAsLong(index);
            if (fill > 0) {
                long capacity = handler.getCapacityAsLong(index, handler.getResource(index));
                if (capacity > 0) {
                    proportion += Math.min(1.0F, (float) fill / capacity);
                    samples++;
                }
            }
        }
        if (samples == 0) {
            return 0;
        }
        return Mth.lerpDiscrete(proportion / samples, 0, 15);
    }

    public static <T extends Resource> int insertStacking(@Nullable ResourceHandler<T> handler, T resource, int amount, @Nullable TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (handler == null || amount == 0) {
            return 0;
        }
        try (Transaction tx = Transaction.open(transaction)) {
            int inserted = 0;
            int size = handler.size();
            for (int index = 0; index < size && inserted < amount; index++) {
                if (!handler.getResource(index).isEmpty()) {
                    inserted += handler.insert(index, resource, amount - inserted, tx);
                }
            }
            for (int index = 0; index < size && inserted < amount; index++) {
                if (handler.getResource(index).isEmpty()) {
                    inserted += handler.insert(index, resource, amount - inserted, tx);
                }
            }
            tx.commit();
            return inserted;
        }
    }

    @Nullable
    public static <T extends Resource> ResourceStack<T> extractFirst(@Nullable ResourceHandler<T> handler, Predicate<T> filter, int amount, @Nullable TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (handler == null || amount == 0) {
            return null;
        }
        T resource = findExtractableResource(handler, filter, transaction);
        if (resource == null) {
            return null;
        }
        try (Transaction tx = Transaction.open(transaction)) {
            int extracted = handler.extract(resource, amount, tx);
            if (extracted <= 0) {
                return null;
            }
            tx.commit();
            return new ResourceStack<>(resource, extracted);
        }
    }

    public static <T extends Resource> int move(@Nullable ResourceHandler<T> from, @Nullable ResourceHandler<T> to, Predicate<T> filter, int amount, @Nullable TransactionContext transaction) {
        return moveInternal(from, to, filter, amount, false, transaction);
    }

    public static <T extends Resource> int moveStacking(@Nullable ResourceHandler<T> from, @Nullable ResourceHandler<T> to, Predicate<T> filter, int amount, @Nullable TransactionContext transaction) {
        return moveInternal(from, to, filter, amount, true, transaction);
    }

    private static <T extends Resource> int moveInternal(@Nullable ResourceHandler<T> from, @Nullable ResourceHandler<T> to, Predicate<T> filter, int amount, boolean stacking, @Nullable TransactionContext transaction) {
        Objects.requireNonNull(filter, "filter");
        TransferPreconditions.checkNonNegative(amount);
        if (from == null || to == null || amount == 0) {
            return 0;
        }
        try (Transaction outer = Transaction.open(transaction)) {
            int moved = 0;
            int size = from.size();
            for (int index = 0; index < size && moved < amount; index++) {
                T resource = from.getResource(index);
                if (resource.isEmpty() || !filter.test(resource)) {
                    continue;
                }
                int available;
                try (Transaction simulation = Transaction.open(outer)) {
                    available = from.extract(index, resource, amount - moved, simulation);
                }
                if (available == 0) {
                    continue;
                }
                try (Transaction transfer = Transaction.open(outer)) {
                    int inserted = stacking ? insertStacking(to, resource, available, transfer) : to.insert(resource, available, transfer);
                    if (inserted == 0 || inserted != from.extract(index, resource, inserted, transfer)) {
                        continue;
                    }
                    moved += inserted;
                    transfer.commit();
                }
            }
            outer.commit();
            return moved;
        }
    }

    @Nullable
    public static <T extends Resource> ResourceStack<T> moveFirst(@Nullable ResourceHandler<T> from, @Nullable ResourceHandler<T> to, Predicate<T> filter, int amount, @Nullable TransactionContext transaction) {
        return moveFirstInternal(from, to, filter, amount, false, transaction);
    }

    @Nullable
    public static <T extends Resource> ResourceStack<T> moveFirstStacking(@Nullable ResourceHandler<T> from, @Nullable ResourceHandler<T> to, Predicate<T> filter, int amount, @Nullable TransactionContext transaction) {
        return moveFirstInternal(from, to, filter, amount, true, transaction);
    }

    @Nullable
    private static <T extends Resource> ResourceStack<T> moveFirstInternal(@Nullable ResourceHandler<T> from, @Nullable ResourceHandler<T> to, Predicate<T> filter, int amount, boolean stacking, @Nullable TransactionContext transaction) {
        Objects.requireNonNull(filter, "filter");
        TransferPreconditions.checkNonNegative(amount);
        if (from == null || to == null || amount == 0) {
            return null;
        }
        int moved = 0;
        T selected = null;
        int size = from.size();
        for (int index = 0; index < size && moved < amount; index++) {
            T resource = from.getResource(index);
            if (selected == null ? resource.isEmpty() || !filter.test(resource) : !selected.equals(resource)) {
                continue;
            }
            int available;
            try (Transaction simulation = Transaction.open(transaction)) {
                available = from.extract(index, resource, amount - moved, simulation);
            }
            if (available == 0) {
                continue;
            }
            try (Transaction transfer = Transaction.open(transaction)) {
                int inserted = stacking ? insertStacking(to, resource, available, transfer) : to.insert(resource, available, transfer);
                if (inserted == 0 || inserted != from.extract(index, resource, inserted, transfer)) {
                    continue;
                }
                moved += inserted;
                transfer.commit();
                selected = resource;
            }
        }
        return moved > 0 ? new ResourceStack<>(selected, moved) : null;
    }

    public static <T extends Resource> boolean contains(ResourceHandler<T> handler, T resource) {
        return indexOf(handler, resource) != -1;
    }

    public static <T extends Resource> int indexOf(ResourceHandler<T> handler, T resource) {
        TransferPreconditions.checkNonEmpty(resource);
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            if (resource.equals(handler.getResource(index))) {
                return index;
            }
        }
        return -1;
    }

    @Nullable
    public static <T extends Resource> T findExtractableResource(ResourceHandler<T> handler, Predicate<T> filter, @Nullable TransactionContext transaction) {
        try (Transaction simulation = Transaction.open(transaction)) {
            int size = handler.size();
            for (int index = 0; index < size; index++) {
                T resource = handler.getResource(index);
                if (!resource.isEmpty() && filter.test(resource) && handler.extract(index, resource, handler.getAmountAsInt(index), simulation) > 0) {
                    return resource;
                }
            }
            return null;
        }
    }
}
