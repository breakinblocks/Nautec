package com.breakinblocks.nautec.transfer.transaction;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class Transaction implements AutoCloseable, TransactionContext {
    final TransactionManager manager;
    private final int depth;
    boolean open;
    final List<SnapshotJournal<?>> journalsToClose = new ArrayList<>();

    Transaction(TransactionManager manager, int depth) {
        this.manager = manager;
        this.depth = depth;
    }

    public static Transaction openRoot() {
        return TransactionManager.current().open(null);
    }

    public static Transaction open(@Nullable TransactionContext parent) {
        return TransactionManager.current().open(parent);
    }

    public static Transaction openNested() {
        TransactionManager manager = TransactionManager.current();
        return manager.open(manager.innermostOpen());
    }

    @Nullable
    public static TransactionContext getCurrentOpenedTransaction() {
        return TransactionManager.current().innermostOpen();
    }

    public static Lifecycle getLifecycle() {
        TransactionManager manager = TransactionManager.current();
        if (manager.currentDepth == -1) {
            return manager.processingRootCommitQueue ? Lifecycle.ROOT_CLOSING : Lifecycle.NONE;
        }
        return manager.stack.get(manager.currentDepth).open ? Lifecycle.OPEN : Lifecycle.CLOSING;
    }

    public void commit() {
        close(false);
    }

    @Override
    public void close() {
        if (open && manager.currentDepth >= depth) {
            close(true);
        }
    }

    @Override
    public int depth() {
        return depth;
    }

    void validateOpen() {
        if (!open) {
            throw new IllegalStateException("The transaction is closed or closing.");
        }
    }

    private void close(boolean aborted) {
        manager.validateCurrent(this);
        validateOpen();
        open = false;
        RuntimeException exception = null;
        for (int i = 0; i < journalsToClose.size(); i++) {
            try {
                journalsToClose.get(i).onClose(this, aborted);
            } catch (RuntimeException failure) {
                if (exception == null) {
                    exception = new RuntimeException("A transaction close callback failed.", failure);
                } else {
                    exception.addSuppressed(failure);
                }
            }
        }
        journalsToClose.clear();
        manager.currentDepth--;
        if (manager.currentDepth == -1) {
            exception = manager.processRootCommitQueue(exception);
        }
        if (exception != null) {
            throw exception;
        }
    }

    @Override
    public String toString() {
        return "Transaction[depth=" + depth + ", open=" + open + "]";
    }

    public enum Lifecycle {
        NONE,
        OPEN,
        CLOSING,
        ROOT_CLOSING
    }
}
