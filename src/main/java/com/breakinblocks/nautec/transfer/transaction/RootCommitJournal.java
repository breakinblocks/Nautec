package com.breakinblocks.nautec.transfer.transaction;

public final class RootCommitJournal extends SnapshotJournal<Boolean> {
    private final Runnable rootCommitCallback;

    public RootCommitJournal(Runnable rootCommitCallback) {
        this.rootCommitCallback = rootCommitCallback;
    }

    @Override
    protected Boolean createSnapshot() {
        return Boolean.TRUE;
    }

    @Override
    protected void revertToSnapshot(Boolean snapshot) {
    }

    @Override
    protected void onRootCommit(Boolean originalState) {
        rootCommitCallback.run();
    }
}
