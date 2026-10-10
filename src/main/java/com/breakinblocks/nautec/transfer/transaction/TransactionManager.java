package com.breakinblocks.nautec.transfer.transaction;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

final class TransactionManager {
    private static final ThreadLocal<TransactionManager> MANAGERS = ThreadLocal.withInitial(TransactionManager::new);

    final Thread thread = Thread.currentThread();
    final List<Transaction> stack = new ArrayList<>();
    final ArrayDeque<SnapshotJournal<?>> rootCommitQueue = new ArrayDeque<>();
    int currentDepth = -1;
    boolean processingRootCommitQueue;

    static TransactionManager current() {
        return MANAGERS.get();
    }

    Transaction open(@Nullable TransactionContext parent) {
        if (parent != null) {
            Transaction parentTransaction = (Transaction) parent;
            validateCurrent(parentTransaction);
            parentTransaction.validateOpen();
        } else if (currentDepth >= 0) {
            throw new IllegalStateException("A root transaction is already open on thread " + thread.getName());
        }
        currentDepth++;
        Transaction transaction;
        if (currentDepth < stack.size()) {
            transaction = stack.get(currentDepth);
        } else {
            transaction = new Transaction(this, currentDepth);
            stack.add(transaction);
        }
        transaction.open = true;
        return transaction;
    }

    @Nullable
    Transaction innermostOpen() {
        if (currentDepth == -1) {
            return null;
        }
        Transaction transaction = stack.get(currentDepth);
        return transaction.open ? transaction : null;
    }

    Transaction getOpenTransaction(int depth) {
        if (depth < 0 || depth > currentDepth) {
            throw new IndexOutOfBoundsException("No open transaction at depth " + depth);
        }
        Transaction transaction = stack.get(depth);
        transaction.validateOpen();
        return transaction;
    }

    void validateCurrent(Transaction transaction) {
        if (Thread.currentThread() != thread) {
            throw new IllegalStateException("Transactions are bound to the thread that opened them.");
        }
        if (currentDepth == -1 || stack.get(currentDepth) != transaction) {
            throw new IllegalStateException("Only the innermost open transaction can be used here.");
        }
    }

    @Nullable
    RuntimeException processRootCommitQueue(@Nullable RuntimeException pending) {
        if (processingRootCommitQueue) {
            return pending;
        }
        processingRootCommitQueue = true;
        RuntimeException exception = pending;
        while (!rootCommitQueue.isEmpty()) {
            SnapshotJournal<?> journal = rootCommitQueue.remove();
            try {
                journal.callOnRootCommit();
            } catch (RuntimeException failure) {
                if (exception == null) {
                    exception = new RuntimeException("A root commit callback failed.", failure);
                } else {
                    exception.addSuppressed(failure);
                }
            }
        }
        processingRootCommitQueue = false;
        return exception;
    }
}
