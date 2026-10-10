package com.breakinblocks.nautec.transfer.adapter;

import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import com.breakinblocks.nautec.transfer.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public record ExposedItemHandler(ResourceHandler<ItemResource> handler) implements IItemHandler {
    @Override
    public int getSlots() {
        return handler.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return handler.getResource(slot).toStack(handler.getAmountAsInt(slot));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int inserted;
        try (Transaction tx = Transaction.openNested()) {
            inserted = handler.insert(slot, ItemResource.of(stack), stack.getCount(), tx);
            if (!simulate) {
                tx.commit();
            }
        }
        return inserted >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemResource resource = handler.getResource(slot);
        if (resource.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int extracted;
        try (Transaction tx = Transaction.openNested()) {
            extracted = handler.extract(slot, resource, Math.min(amount, resource.getMaxStackSize()), tx);
            if (!simulate) {
                tx.commit();
            }
        }
        return resource.toStack(extracted);
    }

    @Override
    public int getSlotLimit(int slot) {
        return handler.getCapacityAsInt(slot, handler.getResource(slot));
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !stack.isEmpty() && handler.isValid(slot, ItemResource.of(stack));
    }
}
