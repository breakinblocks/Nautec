package com.breakinblocks.nautec.capabilities.bacteria;

import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.items.PetriDishItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

public final class DishPort {
    public static final int INTERVAL = 5;
    public static final int NONE = -1;

    private DishPort() {
    }

    public static boolean isDish(ItemStack stack) {
        return stack.getItem() instanceof PetriDishItem;
    }

    public static BacteriaInstance colonyOf(ItemStack stack) {
        if (!isDish(stack)) {
            return BacteriaInstance.EMPTY;
        }
        IBacteriaStorage dish = stack.copyWithCount(1).getCapability(NTCapabilities.BacteriaStorage.ITEM);
        return dish == null ? BacteriaInstance.EMPTY : dish.getBacteria(0);
    }

    public static boolean accepts(ContainerBlockEntity machine, ItemResource resource, int emptyOutSlot, int colonyOutSlot, int[] loadSlots,
                                  IntSupplier unloadSlot, Predicate<BacteriaInstance> loadable) {
        return accepts(machine, resource, emptyOutSlot, colonyOutSlot, loadSlots, unloadSlot, loadable, () -> NONE);
    }

    public static boolean accepts(ContainerBlockEntity machine, ItemResource resource, int emptyOutSlot, int colonyOutSlot, int[] loadSlots,
                                  IntSupplier unloadSlot, Predicate<BacteriaInstance> loadable, IntSupplier swapSlot) {
        if (!(resource.getItem() instanceof PetriDishItem)) {
            return false;
        }
        ItemStackHandler items = machine.getItemStackHandler();
        IBacteriaStorage storage = machine.getBacteriaStorage();
        if (items == null || storage == null) {
            return false;
        }
        BacteriaInstance held = colonyOf(resource.toStack());
        if (!held.isEmpty()) {
            if (!loadable.test(held)) {
                return false;
            }
            if (loadTarget(storage, held, loadSlots) >= 0) {
                return emptyOutSlot == NONE || items.getStackInSlot(emptyOutSlot).isEmpty();
            }
            return colonyOutSlot != NONE && items.getStackInSlot(colonyOutSlot).isEmpty() && swapSlot.getAsInt() >= 0;
        }
        return colonyOutSlot != NONE && items.getStackInSlot(colonyOutSlot).isEmpty() && unloadSlot.getAsInt() >= 0;
    }

    public static boolean tick(ContainerBlockEntity machine, int inSlot, int emptyOutSlot, int colonyOutSlot, int[] loadSlots,
                               IntSupplier unloadSlot, IntConsumer onUnload) {
        return tick(machine, inSlot, emptyOutSlot, colonyOutSlot, loadSlots, unloadSlot, onUnload, () -> NONE);
    }

    public static boolean tick(ContainerBlockEntity machine, int inSlot, int emptyOutSlot, int colonyOutSlot, int[] loadSlots,
                               IntSupplier unloadSlot, IntConsumer onUnload, IntSupplier swapSlot) {
        ItemStackHandler items = machine.getItemStackHandler();
        IBacteriaStorage storage = machine.getBacteriaStorage();
        ItemStack in = items.getStackInSlot(inSlot);
        if (storage == null || in.isEmpty()) {
            return false;
        }
        ItemStack result = in.copyWithCount(1);
        IBacteriaStorage dish = result.getCapability(NTCapabilities.BacteriaStorage.ITEM);
        if (dish == null) {
            return false;
        }

        BacteriaInstance held = dish.getBacteria(0);
        int outSlot;
        if (!held.isEmpty()) {
            int target = loadTarget(storage, held, loadSlots);
            if (target < 0) {
                return swap(machine, inSlot, colonyOutSlot, result, dish, held, swapSlot, onUnload);
            }
            outSlot = emptyOutSlot;
            if (outSlot != NONE && !items.getStackInSlot(outSlot).isEmpty()) {
                return false;
            }
            storage.insertBacteria(target, held, false);
            dish.setBacteria(0, BacteriaInstance.EMPTY);
            if (outSlot == NONE) {
                items.setStackInSlot(inSlot, result);
                return true;
            }
        } else {
            outSlot = colonyOutSlot;
            if (outSlot == NONE || !items.getStackInSlot(outSlot).isEmpty()) {
                return false;
            }
            int source = unloadSlot.getAsInt();
            if (source < 0 || storage.getBacteria(source).isEmpty()) {
                return false;
            }
            dish.setBacteria(0, storage.getBacteria(source).copy());
            storage.setBacteria(source, BacteriaInstance.EMPTY);
            storage.onBacteriaChanged(source);
            onUnload.accept(source);
        }

        items.extractItem(inSlot, 1, false);
        machine.forceInsertItem(outSlot, result, false);
        return true;
    }

    private static boolean swap(ContainerBlockEntity machine, int inSlot, int colonyOutSlot, ItemStack result, IBacteriaStorage dish,
                                BacteriaInstance held, IntSupplier swapSlot, IntConsumer onUnload) {
        ItemStackHandler items = machine.getItemStackHandler();
        IBacteriaStorage storage = machine.getBacteriaStorage();
        if (colonyOutSlot == NONE || !items.getStackInSlot(colonyOutSlot).isEmpty()) {
            return false;
        }
        int source = swapSlot.getAsInt();
        if (source < 0 || storage.getBacteria(source).isEmpty()) {
            return false;
        }
        BacteriaInstance old = storage.getBacteria(source).copy();
        onUnload.accept(source);
        storage.setBacteria(source, held);
        storage.onBacteriaChanged(source);
        dish.setBacteria(0, old);
        items.extractItem(inSlot, 1, false);
        machine.forceInsertItem(colonyOutSlot, result, false);
        return true;
    }

    public static void reclaim(ContainerBlockEntity machine, int inSlot, int oldSlot) {
        ItemStackHandler items = machine.getItemStackHandler();
        if (items.getStackInSlot(inSlot).isEmpty() && !items.getStackInSlot(oldSlot).isEmpty()) {
            items.setStackInSlot(inSlot, items.getStackInSlot(oldSlot).copyWithCount(1));
            items.extractItem(oldSlot, 1, false);
        }
    }

    private static int loadTarget(IBacteriaStorage storage, BacteriaInstance held, int[] slots) {
        for (int slot : slots) {
            BacteriaInstance stored = storage.getBacteria(slot);
            if (!stored.isEmpty() && BacteriaInstance.isSameBacteriaAndStats(stored, held)
                    && storage.insertBacteria(slot, held, true).isEmpty()) {
                return slot;
            }
        }
        for (int slot : slots) {
            if (storage.getBacteria(slot).isEmpty() && storage.insertBacteria(slot, held, true).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }
}
