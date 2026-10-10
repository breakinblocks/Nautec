package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class RecipeTransfer {
    public static final int MAX_ENTRIES = 16;
    private static final int MAX_SETS = 64;

    private RecipeTransfer() {
    }

    public record Entry(int slot, Ingredient ingredient, int count) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Entry::slot,
                Ingredient.CONTENTS_STREAM_CODEC, Entry::ingredient,
                ByteBufCodecs.VAR_INT, Entry::count,
                Entry::new
        );
    }

    public record Plan(Slot[] slots, ItemStack[] kinds, int sets, boolean[] missing) {
    }

    public static @Nullable Slot menuSlot(AbstractContainerMenu menu, ContainerBlockEntity machine, int index) {
        for (Slot slot : menu.slots) {
            if (slot instanceof ResourceHandlerSlot handlerSlot && handlerSlot.getResourceHandler() == machine.getItemStackHandler()
                    && slot.getContainerSlot() == index) {
                return slot;
            }
        }
        return null;
    }

    private static boolean valid(List<Entry> entries) {
        if (entries.isEmpty() || entries.size() > MAX_ENTRIES) {
            return false;
        }
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            if (entry.count() < 1 || entry.count() > 64 || entry.ingredient().isEmpty()) {
                return false;
            }
            for (int j = 0; j < i; j++) {
                if (entries.get(j).slot() == entry.slot()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static @Nullable Plan plan(Player player, AbstractContainerMenu menu, ContainerBlockEntity machine, List<Entry> entries, boolean max) {
        if (!valid(entries)) {
            return null;
        }
        Inventory inventory = player.getInventory();
        int size = Inventory.INVENTORY_SIZE;
        int[] remaining = new int[size];
        for (int i = 0; i < size; i++) {
            remaining[i] = inventory.getItem(i).getCount();
        }
        int count = entries.size();
        Slot[] slots = new Slot[count];
        ItemStack[] kinds = new ItemStack[count];
        int[] room = new int[count];
        boolean[] missing = new boolean[count];
        for (int e = 0; e < count; e++) {
            Entry entry = entries.get(e);
            Slot slot = menuSlot(menu, machine, entry.slot());
            if (slot == null) {
                return null;
            }
            slots[e] = slot;
            ItemStack held = slot.getItem();
            ItemStack kind = ItemStack.EMPTY;
            if (!held.isEmpty() && entry.ingredient().test(held)) {
                kind = held;
            } else {
                for (int i = 0; i < size; i++) {
                    ItemStack stack = inventory.getItem(i);
                    if (!stack.isEmpty() && entry.ingredient().test(stack) && slot.mayPlace(stack.copyWithCount(1))) {
                        kind = stack;
                        break;
                    }
                }
            }
            if (kind.isEmpty()) {
                missing[e] = true;
                kinds[e] = ItemStack.EMPTY;
                continue;
            }
            kinds[e] = kind.copyWithCount(1);
            int existing = kind == held ? held.getCount() : 0;
            room[e] = slot.getMaxStackSize(kinds[e]) - existing;
            int available = 0;
            for (int i = 0; i < size; i++) {
                if (ItemStack.isSameItemSameComponents(inventory.getItem(i), kinds[e])) {
                    available += remaining[i];
                }
            }
            missing[e] = available < entry.count();
        }
        for (boolean absent : missing) {
            if (absent) {
                return new Plan(slots, kinds, 0, missing);
            }
        }
        int limit = max ? MAX_SETS : 1;
        int sets = 0;
        int[] trial = new int[size];
        while (sets < limit) {
            System.arraycopy(remaining, 0, trial, 0, size);
            boolean complete = true;
            for (int e = 0; e < count && complete; e++) {
                int need = entries.get(e).count();
                if ((sets + 1) * need > room[e]) {
                    complete = false;
                    break;
                }
                for (int i = 0; i < size && need > 0; i++) {
                    if (trial[i] > 0 && ItemStack.isSameItemSameComponents(inventory.getItem(i), kinds[e])) {
                        int take = Math.min(need, trial[i]);
                        trial[i] -= take;
                        need -= take;
                    }
                }
                complete = need == 0;
            }
            if (!complete) {
                break;
            }
            System.arraycopy(trial, 0, remaining, 0, size);
            sets++;
        }
        return new Plan(slots, kinds, sets, missing);
    }

    public static int transfer(Player player, AbstractContainerMenu menu, ContainerBlockEntity machine, List<Entry> entries, boolean max) {
        Plan plan = plan(player, menu, machine, entries, max);
        if (plan == null || plan.sets() == 0) {
            return 0;
        }
        Inventory inventory = player.getInventory();
        for (int e = 0; e < entries.size(); e++) {
            Slot slot = plan.slots()[e];
            ItemStack held = slot.getItem();
            if (!held.isEmpty() && !ItemStack.isSameItemSameComponents(held, plan.kinds()[e])) {
                ItemStack removed = slot.safeTake(held.getCount(), Integer.MAX_VALUE, player);
                inventory.placeItemBackInInventory(removed);
            }
        }
        for (int e = 0; e < entries.size(); e++) {
            Slot slot = plan.slots()[e];
            int need = entries.get(e).count() * plan.sets();
            for (int i = 0; i < Inventory.INVENTORY_SIZE && need > 0; i++) {
                ItemStack stack = inventory.getItem(i);
                if (!ItemStack.isSameItemSameComponents(stack, plan.kinds()[e])) {
                    continue;
                }
                ItemStack taken = stack.split(Math.min(need, stack.getCount()));
                need -= taken.getCount();
                ItemStack left = slot.safeInsert(taken);
                if (!left.isEmpty()) {
                    inventory.placeItemBackInInventory(left);
                }
            }
        }
        inventory.setChanged();
        menu.broadcastChanges();
        return plan.sets();
    }
}
