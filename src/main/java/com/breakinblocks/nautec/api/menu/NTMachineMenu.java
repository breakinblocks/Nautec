package com.breakinblocks.nautec.api.menu;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.menu.slots.SlotBacteriaStorage;
import com.breakinblocks.nautec.api.menu.slots.SlotFluidHandler;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class NTMachineMenu<T extends ContainerBlockEntity> extends NTAbstractContainerMenu<T> {
    private final NonNullList<SlotFluidHandler> fluidTankSlots;
    private final NonNullList<SlotBacteriaStorage> bacteriaStorageSlots;
    private @Nullable Slot dishIn;
    private @Nullable Slot dishOut;

    public NTMachineMenu(MenuType<?> menuType, int containerId, @NotNull Inventory inv, @NotNull T blockEntity) {
        this(menuType, containerId, inv, blockEntity, 92);
    }

    public NTMachineMenu(MenuType<?> menuType, int containerId, @NotNull Inventory inv, @NotNull T blockEntity, int inventoryY) {
        super(menuType, containerId, inv, blockEntity);

        addPlayerInventory(inv, inventoryY);
        addPlayerHotbar(inv, inventoryY + 58);

        this.fluidTankSlots = NonNullList.create();
        this.bacteriaStorageSlots = NonNullList.create();
    }

    public void addFluidHandlerSlot(SlotFluidHandler slot) {
        this.fluidTankSlots.add(slot);
    }

    public void addDishPort(int inIndex, int inX, int inY, int outIndex, int outX, int outY) {
        ItemStackHandler handler = blockEntity.getItemStackHandler();
        this.dishIn = addSlot(new ResourceHandlerSlot(handler, handler::set, inIndex, inX, inY));
        this.dishOut = addSlot(new ResourceHandlerSlot(handler, handler::set, outIndex, outX, outY));
    }

    public @Nullable Slot getDishIn() {
        return dishIn;
    }

    public @Nullable Slot getDishOut() {
        return dishOut;
    }

    public void addBacteriaStorageSlot(SlotBacteriaStorage slot) {
        this.bacteriaStorageSlots.add(slot);
    }

    public NonNullList<SlotFluidHandler> getFluidTankSlots() {
        return fluidTankSlots;
    }

    public NonNullList<SlotBacteriaStorage> getBacteriaStorageSlots() {
        return bacteriaStorageSlots;
    }
}
