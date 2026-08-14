package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.api.menu.slots.SlotBacteriaStorage;
import com.breakinblocks.nautec.content.blockentities.MutatorBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class MutatorMenu extends NTMachineMenu<MutatorBlockEntity> {
    public MutatorMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (MutatorBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public MutatorMenu(int containerId, @NotNull Inventory inv, @NotNull MutatorBlockEntity blockEntity) {
        super(NTMenuTypes.MUTATOR.get(), containerId, inv, blockEntity);
        addBacteriaStorageSlot(new SlotBacteriaStorage(blockEntity.getBacteriaStorage(), 0, 32, 33));

        addBacteriaStorageSlot(new SlotBacteriaStorage(blockEntity.getBacteriaStorage(), 1, 126, 33));

        addSlot(new ResourceHandlerSlot(blockEntity.getItemStackHandler(), blockEntity.getItemStackHandler()::set, 0, 79, 61));
    }

    @Override
    protected int getMergeableSlotCount() {
        return 1;
    }
}
