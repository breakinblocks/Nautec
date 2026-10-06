package com.breakinblocks.nautec.content.resonantstorage;

import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class UpgradeSlot extends ItemStacksResourceHandler {
    private final ResonantStore store;

    UpgradeSlot(ResonantStore store) {
        super(1);
        this.store = store;
    }

    public int count() {
        return getAmountAsInt(0);
    }

    void load(int count) {
        stacks.set(0, count <= 0 ? ItemStack.EMPTY : new ItemStack(NTItems.RESONANT_EXPANSION.get(), Math.min(count, ResonantStore.MAX_UPGRADES)));
    }

    public int removable() {
        return Math.max(0, count() - store.requiredUpgrades());
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return resource.is(NTItems.RESONANT_EXPANSION.get());
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        return ResonantStore.MAX_UPGRADES;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        int allowed = Math.min(amount, removable());
        return allowed <= 0 ? 0 : super.extract(index, resource, allowed, transaction);
    }

    @Override
    protected void onContentsChanged(int index, ItemStack previousContents) {
        store.upgradesChanged();
    }
}
