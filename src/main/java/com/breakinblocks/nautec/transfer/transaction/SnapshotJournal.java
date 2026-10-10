package com.breakinblocks.nautec.transfer.transaction;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public abstract class SnapshotJournal<T> {
    private static final Object NO_SNAPSHOT = new Object();

    private final ArrayList<Object> snapshots = new ArrayList<>();
    private @Nullable Object originalState;
    private boolean awaitingRootCommit;

    protected abstract T createSnapshot();

    protected abstract void revertToSnapshot(T snapshot);

    protected void releaseSnapshot(T snapshot) {
    }

    protected void onRootCommit(T originalState) {
    }

    public void updateSnapshots(TransactionContext transaction) {
        int depth = transaction.depth();
        while (snapshots.size() <= depth) {
            snapshots.add(NO_SNAPSHOT);
        }
        if (snapshots.get(depth) == NO_SNAPSHOT) {
            Transaction impl = (Transaction) transaction;
            impl.validateOpen();
            snapshots.set(depth, createSnapshot());
            impl.journalsToClose.add(this);
        }
    }

    @SuppressWarnings("unchecked")
    void onClose(Transaction transaction, boolean aborted) {
        int depth = transaction.depth();
        T snapshot = (T) snapshots.remove(depth);
        if (aborted) {
            revertToSnapshot(snapshot);
            releaseSnapshot(snapshot);
        } else if (depth == 0) {
            if (!awaitingRootCommit) {
                awaitingRootCommit = true;
                originalState = snapshot;
                transaction.manager.rootCommitQueue.add(this);
            } else {
                releaseSnapshot(snapshot);
            }
        } else if (snapshots.get(depth - 1) == NO_SNAPSHOT) {
            snapshots.set(depth - 1, snapshot);
            transaction.manager.getOpenTransaction(depth - 1).journalsToClose.add(this);
        } else {
            releaseSnapshot(snapshot);
        }
    }

    @SuppressWarnings("unchecked")
    void callOnRootCommit() {
        T state = (T) originalState;
        originalState = null;
        awaitingRootCommit = false;
        onRootCommit(state);
        releaseSnapshot(state);
    }
}
