package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.content.extradimensional.ExtradimensionalStorageBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ExtradimensionalStorageMenu extends NTAbstractContainerMenu<ExtradimensionalStorageBlockEntity> {
    private final ContainerData data;

    public ExtradimensionalStorageMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (ExtradimensionalStorageBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(ExtradimensionalStorageBlockEntity.DATA_COUNT));
    }

    public ExtradimensionalStorageMenu(int containerId, @NotNull Inventory inv, @NotNull ExtradimensionalStorageBlockEntity blockEntity) {
        this(containerId, inv, blockEntity, blockEntity.getData());
    }

    private ExtradimensionalStorageMenu(int containerId, Inventory inv, ExtradimensionalStorageBlockEntity blockEntity, ContainerData data) {
        super(NTMenuTypes.EXTRADIMENSIONAL_STORAGE.get(), containerId, inv, blockEntity);
        this.data = data;
        addDataSlots(data);
    }

    @Override
    protected int getMergeableSlotCount() {
        return 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private long wide(int index, int parts) {
        int[] values = new int[parts];
        for (int i = 0; i < parts; i++) {
            values[i] = data.get(index + i);
        }
        return ExtradimensionalStorageBlockEntity.join(values);
    }

    public long getStored() {
        return wide(ExtradimensionalStorageBlockEntity.DATA_STORED, 4);
    }

    public long getCapacity() {
        return wide(ExtradimensionalStorageBlockEntity.DATA_CAPACITY, 4);
    }

    public int getTier() {
        return data.get(ExtradimensionalStorageBlockEntity.DATA_TIER);
    }

    public ExtradimensionalStorageBlockEntity.PortMode getMode(Direction side) {
        return ExtradimensionalStorageBlockEntity.unpackMode(data.get(ExtradimensionalStorageBlockEntity.DATA_PORTS), side);
    }

    public int getStatus() {
        return data.get(ExtradimensionalStorageBlockEntity.DATA_STATUS);
    }

    public int getCores() {
        return data.get(ExtradimensionalStorageBlockEntity.DATA_CORES);
    }

    public long getFlowIn() {
        return wide(ExtradimensionalStorageBlockEntity.DATA_IN, 2);
    }

    public long getFlowOut() {
        return wide(ExtradimensionalStorageBlockEntity.DATA_OUT, 2);
    }

    public int getChunkState() {
        return data.get(ExtradimensionalStorageBlockEntity.DATA_CHUNK);
    }
}
