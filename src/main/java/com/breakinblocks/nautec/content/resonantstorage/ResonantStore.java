package com.breakinblocks.nautec.content.resonantstorage;

public interface ResonantStore {
    int MAX_UPGRADES = 4;

    ResonantChannel channel();

    UpgradeSlot upgradeSlot();

    default int upgrades() {
        return upgradeSlot().count();
    }

    int requiredUpgrades();

    void upgradesChanged();

    boolean isBlank();

    int comparatorSignal();

    void addListener(Listener listener);

    void removeListener(Listener listener);

    void attach(Runnable dirty);

    interface Listener {
        void storeChanged();
    }
}
