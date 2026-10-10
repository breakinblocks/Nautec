package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.api.menu.slots.SlotBacteriaStorage;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.AbstractBioReactorBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import com.breakinblocks.nautec.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractBioReactorMenu<T extends AbstractBioReactorBlockEntity> extends NTMachineMenu<T> {
    private final BioReactorLayout layout;
    private final int machineSlots;

    protected AbstractBioReactorMenu(MenuType<?> menuType, int containerId, @NotNull Inventory inv, @NotNull T blockEntity, BioReactorLayout layout) {
        super(menuType, containerId, inv, blockEntity, layout.inventoryY());
        this.layout = layout;

        ItemStackHandler handler = blockEntity.getItemStackHandler();
        int added = 0;
        for (int i = 0; i < blockEntity.getColonySlots() && i < layout.colonies().length; i++) {
            addBacteriaStorageSlot(new SlotBacteriaStorage(blockEntity.getBacteriaStorage(), i, layout.colonies()[i][0], layout.colonies()[i][1]));
        }
        for (int i = 0; i < blockEntity.getColonySlots() && i < layout.outputs().length; i++) {
            addSlot(new ResourceHandlerSlot(handler, handler::set, blockEntity.outputSlot(i), layout.outputs()[i][0], layout.outputs()[i][1]));
            added++;
        }
        for (int i = 0; i < blockEntity.getNutrientSlotCount() && i < layout.nutrients().length; i++) {
            addSlot(new ResourceHandlerSlot(handler, handler::set, blockEntity.nutrientSlot(i), layout.nutrients()[i][0], layout.nutrients()[i][1]));
            added++;
        }
        for (int i = 0; i < blockEntity.getUpgradeSlotCount() && i < layout.upgrades().length; i++) {
            addSlot(new ResourceHandlerSlot(handler, handler::set, blockEntity.upgradeSlot(i), layout.upgrades()[i][0], layout.upgrades()[i][1]));
            added++;
        }
        addDishPort(blockEntity.dishInSlot(), layout.dishIn()[0], layout.dishIn()[1],
                blockEntity.dishOutSlot(), layout.dishOut()[0], layout.dishOut()[1],
                blockEntity.dishEmptyOutSlot(), layout.dishEmptyOut()[0], layout.dishEmptyOut()[1]);
        this.machineSlots = added + 1;
    }

    public BioReactorLayout getLayout() {
        return layout;
    }

    @Override
    protected int getMergeableSlotCount() {
        return machineSlots;
    }
}
