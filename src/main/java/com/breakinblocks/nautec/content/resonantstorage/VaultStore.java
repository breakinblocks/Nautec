package com.breakinblocks.nautec.content.resonantstorage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

public final class VaultStore extends ItemStacksResourceHandler implements ResonantStore {
    public static final int PAGE = 27;
    public static final int MAX_SLOTS = PAGE * (MAX_UPGRADES + 1);

    public static final Codec<VaultStore> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResonantChannel.CODEC.fieldOf("channel").forGetter(VaultStore::channel),
            Codec.intRange(0, MAX_UPGRADES).optionalFieldOf("upgrades", 0).forGetter(VaultStore::upgrades),
            Entry.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(VaultStore::entries)
    ).apply(instance, VaultStore::loaded));

    private final ResonantChannel channel;
    private final UpgradeSlot upgradeSlot = new UpgradeSlot(this);
    private final List<Listener> listeners = new ArrayList<>(2);
    private final View view = new View();
    private Runnable dirty = () -> {
    };

    public VaultStore(ResonantChannel channel) {
        super(MAX_SLOTS);
        this.channel = channel;
    }

    private static VaultStore loaded(ResonantChannel channel, int upgrades, List<Entry> entries) {
        VaultStore store = new VaultStore(channel);
        store.upgradeSlot.load(upgrades);
        for (Entry entry : entries) {
            if (entry.slot() >= 0 && entry.slot() < MAX_SLOTS) {
                store.stacks.set(entry.slot(), entry.stack());
            }
        }
        return store;
    }

    private List<Entry> entries() {
        List<Entry> entries = new ArrayList<>();
        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStack stack = stacks.get(slot);
            if (!stack.isEmpty()) {
                entries.add(new Entry(slot, stack));
            }
        }
        return entries;
    }

    @Override
    public ResonantChannel channel() {
        return channel;
    }

    @Override
    public UpgradeSlot upgradeSlot() {
        return upgradeSlot;
    }

    public int capacity() {
        return PAGE * (upgrades() + 1);
    }

    public int pages() {
        return upgrades() + 1;
    }

    public ResourceHandler<ItemResource> view() {
        return view;
    }

    public ItemStack stackAt(int slot) {
        return stacks.get(slot);
    }

    public void setStackAt(int slot, ItemStack stack) {
        set(slot, ItemResource.of(stack), stack.getCount());
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        return index < capacity() ? super.getCapacity(index, resource) : 0;
    }

    @Override
    public int requiredUpgrades() {
        for (int slot = stacks.size() - 1; slot >= PAGE; slot--) {
            if (!stacks.get(slot).isEmpty()) {
                return slot / PAGE;
            }
        }
        return 0;
    }

    @Override
    public void upgradesChanged() {
        changed();
    }

    @Override
    protected void onContentsChanged(int index, ItemStack previousContents) {
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
        if (upgrades() > 0) {
            return false;
        }
        for (int slot = 0; slot < stacks.size(); slot++) {
            if (!stacks.get(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int comparatorSignal() {
        return ResourceHandlerUtil.getRedstoneSignalFromResourceHandler(view);
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

    private final class View implements ResourceHandler<ItemResource> {
        @Override
        public int size() {
            return capacity();
        }

        @Override
        public ItemResource getResource(int index) {
            return VaultStore.this.getResource(index);
        }

        @Override
        public long getAmountAsLong(int index) {
            return VaultStore.this.getAmountAsLong(index);
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return VaultStore.this.getCapacityAsLong(index, resource);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return index < capacity() && VaultStore.this.isValid(index, resource);
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return index < capacity() ? VaultStore.this.insert(index, resource, amount, transaction) : 0;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return index < capacity() ? VaultStore.this.extract(index, resource, amount, transaction) : 0;
        }
    }

    private record Entry(int slot, ItemStack stack) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("slot").forGetter(Entry::slot),
                ItemStack.CODEC.fieldOf("stack").forGetter(Entry::stack)
        ).apply(instance, Entry::new));
    }
}
