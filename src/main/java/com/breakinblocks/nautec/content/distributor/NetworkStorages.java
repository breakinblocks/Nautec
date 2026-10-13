package com.breakinblocks.nautec.content.distributor;

import com.breakinblocks.nautec.NTConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;
import java.util.function.LongUnaryOperator;

public final class NetworkStorages {
    @FunctionalInterface
    public interface Finder {
        @Nullable NetworkStorage find(ServerLevel level, BlockPos pos, Direction side);
    }

    private static final List<Finder> FINDERS = new ArrayList<>();

    private NetworkStorages() {
    }

    public static synchronized void register(Finder finder) {
        FINDERS.add(finder);
    }

    public static boolean enabled() {
        return NTConfig.distributorStorageNetworks && !FINDERS.isEmpty();
    }

    public static @Nullable NetworkStorage find(ServerLevel level, BlockPos pos, Direction side) {
        for (Finder finder : FINDERS) {
            NetworkStorage storage = finder.find(level, pos, side);
            if (storage != null) {
                return storage;
            }
        }
        return null;
    }

    public static <R extends Resource> int fill(ResourceHandler<R> destination, R resource, long available, int need,
                                                LongUnaryOperator extract, LongUnaryOperator giveBack, LongConsumer stranded) {
        int offered = (int) Math.min(need, available);
        if (offered <= 0 || Transaction.getLifecycle() != Transaction.Lifecycle.NONE) {
            return 0;
        }
        int accepted;
        try (Transaction simulation = Transaction.openRoot()) {
            accepted = destination.insert(resource, offered, simulation);
        }
        if (accepted <= 0) {
            return 0;
        }
        long taken = extract.applyAsLong(accepted);
        if (taken <= 0) {
            return 0;
        }
        int placed;
        try (Transaction transaction = Transaction.openRoot()) {
            placed = destination.insert(resource, (int) Math.min(taken, accepted), transaction);
            transaction.commit();
        }
        long rest = taken - placed;
        if (rest > 0) {
            long returned = giveBack.applyAsLong(rest);
            if (returned < rest) {
                stranded.accept(rest - returned);
            }
        }
        return placed;
    }
}
