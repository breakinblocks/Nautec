package com.breakinblocks.nautec.content.resonantstorage;

import com.breakinblocks.nautec.NTConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;

import java.util.ArrayList;
import java.util.List;

public final class CisternStore extends FluidStacksResourceHandler implements ResonantStore {
    public static final Codec<CisternStore> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResonantChannel.CODEC.fieldOf("channel").forGetter(CisternStore::channel),
            Codec.intRange(0, MAX_UPGRADES).optionalFieldOf("upgrades", 0).forGetter(CisternStore::upgrades),
            FluidStack.OPTIONAL_CODEC.optionalFieldOf("fluid", FluidStack.EMPTY).forGetter(CisternStore::fluid)
    ).apply(instance, CisternStore::loaded));

    private final ResonantChannel channel;
    private final UpgradeSlot upgradeSlot = new UpgradeSlot(this);
    private final List<Listener> listeners = new ArrayList<>(2);
    private Runnable dirty = () -> {
    };

    public CisternStore(ResonantChannel channel) {
        super(1, tierCapacity());
        this.channel = channel;
    }

    private static CisternStore loaded(ResonantChannel channel, int upgrades, FluidStack fluid) {
        CisternStore store = new CisternStore(channel);
        store.upgradeSlot.load(upgrades);
        store.refreshCapacity();
        store.stacks.set(0, fluid.copy());
        return store;
    }

    public static int tierCapacity() {
        return NTConfig.resonantCisternTierCapacity;
    }

    public static int capacityFor(int upgrades) {
        return (int) Math.min(Integer.MAX_VALUE, (long) tierCapacity() * (upgrades + 1));
    }

    @Override
    public ResonantChannel channel() {
        return channel;
    }

    @Override
    public UpgradeSlot upgradeSlot() {
        return upgradeSlot;
    }

    public FluidStack fluid() {
        return stacks.get(0);
    }

    public int capacity() {
        return capacity;
    }

    public void setFluid(FluidStack stack) {
        set(0, FluidResource.of(stack), stack.getAmount());
    }

    private void refreshCapacity() {
        capacity = capacityFor(upgrades());
    }

    @Override
    public int requiredUpgrades() {
        int amount = fluid().getAmount();
        if (amount <= 0) {
            return 0;
        }
        return Math.max(0, (amount - 1) / tierCapacity());
    }

    @Override
    public void upgradesChanged() {
        refreshCapacity();
        changed();
    }

    @Override
    protected void onContentsChanged(int index, FluidStack previousContents) {
        changed();
    }

    private void changed() {
        dirty.run();
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).storeChanged();
        }
    }

    @Override
    public boolean isBlank() {
        return upgrades() == 0 && fluid().isEmpty();
    }

    @Override
    public int comparatorSignal() {
        int amount = fluid().getAmount();
        if (amount <= 0) {
            return 0;
        }
        return 1 + (int) ((long) amount * 14 / Math.max(1, capacity));
    }

    @Override
    public void addListener(Listener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    @Override
    public void attach(Runnable dirty) {
        this.dirty = dirty;
    }
}
