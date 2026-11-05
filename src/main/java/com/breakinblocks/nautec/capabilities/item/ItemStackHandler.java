package com.breakinblocks.nautec.capabilities.item;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import com.breakinblocks.nautec.transfer.item.ItemStacksResourceHandler;
import com.breakinblocks.nautec.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public class ItemStackHandler extends ItemStacksResourceHandler {
    private static final Codec<List<Dynamic<?>>> RAW_STACKS = Codec.PASSTHROUGH.listOf();

    public ItemStackHandler(int slots) {
        super(slots);
    }

    public int getSlots() {
        return size();
    }

    @Override
    public void deserialize(ValueInput input) {
        input.read(VALUE_IO_KEY, RAW_STACKS).ifPresent(entries -> {
            NonNullList<ItemStack> loaded = NonNullList.withSize(Math.max(size(), entries.size()), ItemStack.EMPTY);
            for (int slot = 0; slot < entries.size(); slot++) {
                int index = slot;
                loaded.set(slot, ItemStack.OPTIONAL_CODEC.parse(entries.get(slot))
                        .resultOrPartial(error -> Nautec.LOGGER.warn("Removed an item in slot {} that could not be loaded: {}", index, error))
                        .orElse(ItemStack.EMPTY));
            }
            setStacks(loaded);
        });
    }

    public void ensureSize(int slots) {
        if (this.stacks.size() >= slots) {
            return;
        }
        NonNullList<ItemStack> resized = NonNullList.withSize(slots, ItemStack.EMPTY);
        for (int i = 0; i < this.stacks.size(); i++) {
            resized.set(i, this.stacks.get(i));
        }
        setStacks(resized);
    }

    public @NotNull ItemStack getStackInSlot(int slot) {
        return this.stacks.get(slot);
    }

    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        this.stacks.set(slot, stack);
        onContentsChanged(slot, stack);
    }

    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = insert(slot, ItemResource.of(stack), stack.getCount(), tx);
            if (!simulate) {
                tx.commit();
            }
            return inserted >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted);
        }
    }

    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemResource resource = getResource(slot);
        if (resource.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try (Transaction tx = Transaction.openRoot()) {
            int extracted = extract(slot, resource, amount, tx);
            if (!simulate) {
                tx.commit();
            }
            return extracted <= 0 ? ItemStack.EMPTY : resource.toStack(extracted);
        }
    }

    public int getSlotLimit(int slot) {
        return getCapacityAsInt(slot, ItemResource.EMPTY);
    }

    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return isValid(slot, ItemResource.of(stack));
    }
}
