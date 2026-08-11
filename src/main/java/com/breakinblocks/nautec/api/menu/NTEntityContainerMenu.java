package com.breakinblocks.nautec.api.menu;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public abstract class NTEntityContainerMenu<T extends Entity> extends AbstractContainerMenu {
    protected static final double INTERACTION_RANGE = 4.0;

    protected final @NotNull T entity;
    protected final @NotNull Inventory inv;

    protected NTEntityContainerMenu(MenuType<?> menuType, int containerId, @NotNull Inventory inv, @NotNull T entity) {
        super(menuType, containerId);
        this.entity = entity;
        this.inv = inv;
    }

    public @NotNull T getEntity() {
        return this.entity;
    }

    protected static <T extends Entity> T resolveEntity(Inventory inv, int entityId, Class<T> type) {
        Entity found = inv.player.level().getEntity(entityId);
        if (!type.isInstance(found)) {
            throw new IllegalStateException("No " + type.getSimpleName() + " with id " + entityId + " to open a menu for");
        }
        return type.cast(found);
    }

    protected void addPlayerInventory(Inventory playerInventory, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, y + row * 18));
            }
        }
    }

    protected void addPlayerHotbar(Inventory playerInventory, int y) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, y));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.entity.isAlive() && player.isWithinEntityInteractionRange(this.entity, INTERACTION_RANGE);
    }
}
