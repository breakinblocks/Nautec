package com.breakinblocks.nautec.capabilities.bacteria;

import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.items.PetriDishItem;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class DishPort {
    public static final int INTERVAL = 5;

    private DishPort() {
    }

    public static boolean isDish(ItemStack stack) {
        return stack.getItem() instanceof PetriDishItem;
    }

    public static boolean tick(ContainerBlockEntity machine, int inSlot, int outSlot, int[] loadSlots, IntSupplier unloadSlot, IntConsumer onUnload) {
        ItemStackHandler items = machine.getItemStackHandler();
        IBacteriaStorage storage = machine.getBacteriaStorage();
        ItemStack in = items.getStackInSlot(inSlot);
        if (storage == null || in.isEmpty() || !items.getStackInSlot(outSlot).isEmpty()) {
            return false;
        }
        ItemStack result = in.copyWithCount(1);
        IBacteriaStorage dish = result.getCapability(NTCapabilities.BacteriaStorage.ITEM);
        if (dish == null) {
            return false;
        }

        BacteriaInstance held = dish.getBacteria(0);
        if (!held.isEmpty()) {
            int target = loadTarget(storage, held, loadSlots);
            if (target < 0) {
                return false;
            }
            storage.insertBacteria(target, held, false);
            dish.setBacteria(0, BacteriaInstance.EMPTY);
        } else {
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
