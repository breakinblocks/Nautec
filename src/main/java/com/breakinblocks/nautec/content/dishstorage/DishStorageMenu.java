package com.breakinblocks.nautec.content.dishstorage;

import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.items.PetriDishItem;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DishStorageMenu extends NTMachineMenu<DishStorageBlockEntity> {
    public static final int COLUMNS = 8;
    public static final int VISIBLE_ROWS = 6;
    public static final int GRID_X = 8;
    public static final int GRID_Y = 18;
    public static final int INVENTORY_Y = 146;

    private int offset;

    public DishStorageMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (DishStorageBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public DishStorageMenu(int containerId, @NotNull Inventory inv, @NotNull DishStorageBlockEntity blockEntity) {
        super(NTMenuTypes.DISH_STORAGE.get(), containerId, inv, blockEntity, INVENTORY_Y);
        SimpleContainer dummy = new SimpleContainer(COLUMNS * VISIBLE_ROWS);
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                addSlot(new DishSlot(dummy, row * COLUMNS + column, GRID_X + column * 18, GRID_Y + row * 18));
            }
        }
    }

    public int rows() {
        return Mth.positiveCeilDiv(blockEntity.getCapacity(), COLUMNS);
    }

    public int maxOffset() {
        return Math.max(0, rows() - VISIBLE_ROWS);
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = Mth.clamp(offset, 0, maxOffset());
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        setOffset(id);
        sendAllDataToRemote();
        return true;
    }

    @Override
    protected int getMergeableSlotCount() {
        return COLUMNS * VISIBLE_ROWS;
    }

    @Override
    protected boolean performMerge(int index, ItemStack stack) {
        int playerEnd = playerSlotStart + 36;
        if (index < playerSlotStart || index >= playerEnd) {
            return super.performMerge(index, stack);
        }
        if (!(stack.getItem() instanceof PetriDishItem)) {
            return false;
        }
        ItemStackHandler handler = blockEntity.getItemStackHandler();
        boolean moved = false;
        for (int slot = 0; slot < blockEntity.getCapacity() && !stack.isEmpty(); slot++) {
            if (handler.getStackInSlot(slot).isEmpty()) {
                handler.setStackInSlot(slot, stack.split(1));
                moved = true;
            }
        }
        return moved;
    }

    private class DishSlot extends Slot {
        private final int local;

        DishSlot(SimpleContainer dummy, int local, int x, int y) {
            super(dummy, local, x, y);
            this.local = local;
        }

        private int storageIndex() {
            return offset * COLUMNS + local;
        }

        private boolean inRange() {
            return storageIndex() < blockEntity.getCapacity();
        }

        @Override
        public ItemStack getItem() {
            return inRange() ? blockEntity.getItemStackHandler().getStackInSlot(storageIndex()) : ItemStack.EMPTY;
        }

        @Override
        public void set(ItemStack stack) {
            if (inRange()) {
                blockEntity.getItemStackHandler().setStackInSlot(storageIndex(), stack);
            }
        }

        @Override
        public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
            set(newStack);
        }

        @Override
        public ItemStack remove(int amount) {
            return inRange() ? blockEntity.forceExtractItem(storageIndex(), amount, false) : ItemStack.EMPTY;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return inRange() && stack.getItem() instanceof PetriDishItem;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }

        @Override
        public boolean isActive() {
            return inRange();
        }

        @Override
        public void setChanged() {
            blockEntity.setChanged();
        }
    }
}
