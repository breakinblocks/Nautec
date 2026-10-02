package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.blockentities.ConfinedSpawnerBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class ConfinedSpawnerMenu extends NTAbstractContainerMenu<ConfinedSpawnerBlockEntity> {
    public static final int BUTTON_TOGGLE_MODE = 0;
    public static final int COLUMNS = 9;
    public static final int ROWS = 6;
    public static final int GRID_X = 8;
    public static final int GRID_Y = 18;
    public static final int INVENTORY_Y = 140;
    public static final int HOTBAR_Y = 198;

    private final ContainerData data;

    public ConfinedSpawnerMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (ConfinedSpawnerBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(ConfinedSpawnerBlockEntity.DATA_COUNT));
    }

    public ConfinedSpawnerMenu(int containerId, @NotNull Inventory inv, @NotNull ConfinedSpawnerBlockEntity blockEntity) {
        this(containerId, inv, blockEntity, blockEntity.getData());
    }

    private ConfinedSpawnerMenu(int containerId, Inventory inv, ConfinedSpawnerBlockEntity blockEntity, ContainerData data) {
        super(NTMenuTypes.CONFINED_SPAWNER.get(), containerId, inv, blockEntity);
        this.data = data;
        ItemStackHandler handler = blockEntity.getItemStackHandler();
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                addSlot(new OutputSlot(handler, row * COLUMNS + column, GRID_X + column * 18, GRID_Y + row * 18));
            }
        }
        addPlayerInventory(inv, INVENTORY_Y);
        addPlayerHotbar(inv, HOTBAR_Y);
        addDataSlots(data);
    }

    @Override
    protected int getMergeableSlotCount() {
        return ConfinedSpawnerBlockEntity.SLOTS;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_TOGGLE_MODE) {
            blockEntity.toggleWhitelist();
            return true;
        }
        return false;
    }

    public int getBufferedPower() {
        return data.get(ConfinedSpawnerBlockEntity.DATA_POWER);
    }

    public ConfinedSpawnerBlockEntity.Status getStatus() {
        return ConfinedSpawnerBlockEntity.Status.byId(data.get(ConfinedSpawnerBlockEntity.DATA_STATUS));
    }

    public float getCycleProgress() {
        int cycle = data.get(ConfinedSpawnerBlockEntity.DATA_CYCLE);
        return cycle <= 0 ? 0.0F : Math.min(1.0F, data.get(ConfinedSpawnerBlockEntity.DATA_PROGRESS) / (float) cycle);
    }

    private static final class OutputSlot extends ResourceHandlerSlot {
        private OutputSlot(ItemStackHandler handler, int slot, int x, int y) {
            super(handler, handler::set, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
