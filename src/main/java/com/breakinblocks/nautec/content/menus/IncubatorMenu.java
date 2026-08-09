package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.api.menu.slots.SlotBacteriaStorage;
import com.breakinblocks.nautec.content.blockentities.IncubatorBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class IncubatorMenu extends NTMachineMenu<IncubatorBlockEntity> {
    public IncubatorMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (IncubatorBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public IncubatorMenu(int containerId, @NotNull Inventory inv, @NotNull IncubatorBlockEntity blockEntity) {
        super(NTMenuTypes.INCUBATOR.get(), containerId, inv, blockEntity);

        // Nutrient
        addSlot(new ResourceHandlerSlot(blockEntity.getItemStackHandler(), blockEntity.getItemStackHandler()::set, 0, 80, 49));

        addBacteriaStorageSlot(new SlotBacteriaStorage(blockEntity.getBacteriaStorage(), 0, 79, 26));
    }

    @Override
    protected int getMergeableSlotCount() {
        return 1;
    }
}
