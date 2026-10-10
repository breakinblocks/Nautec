package com.breakinblocks.nautec.transfer.item;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class StackCopySlot extends Slot {
    private static final Container EMPTY_INVENTORY = new SimpleContainer(0);

    private @Nullable ItemStack cachedReturnedStack;

    public StackCopySlot(int slot, int x, int y) {
        super(EMPTY_INVENTORY, slot, x, y);
    }

    protected abstract ItemStack getStackCopy();

    protected abstract void setStackCopy(ItemStack stack);

    @Override
    public final ItemStack getItem() {
        cachedReturnedStack = getStackCopy();
        return cachedReturnedStack;
    }

    @Override
    public final void set(ItemStack stack) {
        setStackCopy(stack);
        cachedReturnedStack = stack;
    }

    @Override
    public final void setChanged() {
        if (cachedReturnedStack != null && !ItemStack.matches(cachedReturnedStack, getStackCopy())) {
            set(cachedReturnedStack);
        }
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack stack = getStackCopy().copy();
        ItemStack removed = stack.split(amount);
        set(stack);
        clearCachedReturnStack();
        return removed;
    }

    protected void clearCachedReturnStack() {
        cachedReturnedStack = null;
    }
}
