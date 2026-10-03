package com.breakinblocks.nautec.content.entities.submarine;

import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SubmarineCargoContainer implements Container {
    private final SubmarineEntity submarine;
    private final NonNullList<ItemStack> items;
    private final int size;

    public SubmarineCargoContainer(SubmarineEntity submarine, NonNullList<ItemStack> items, int size) {
        this.submarine = submarine;
        this.items = items;
        this.size = size;
    }

    @Override
    public int getContainerSize() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < size; slot++) {
            if (!items.get(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return submarine.isAlive() && submarine.getCargoSlots() >= size && player.distanceToSqr(submarine) <= 64.0;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return SubmarineEntity.canStoreInCargo(stack);
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < size; slot++) {
            items.set(slot, ItemStack.EMPTY);
        }
    }
}
