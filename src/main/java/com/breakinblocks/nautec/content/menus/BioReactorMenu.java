package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.api.menu.slots.SlotBacteriaStorage;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.BioReactorBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class BioReactorMenu extends NTMachineMenu<BioReactorBlockEntity> {
    public BioReactorMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (BioReactorBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public BioReactorMenu(int containerId, @NotNull Inventory inv, @NotNull BioReactorBlockEntity blockEntity) {
        super(NTMenuTypes.BIO_REACTOR.get(), containerId, inv, blockEntity);

        for (int i = 0; i < 3; i++) {
            addBacteriaStorageSlot(new SlotBacteriaStorage(blockEntity.getBacteriaStorage(), i, 52, 11 + i * 22));
            addSlot(new ResourceHandlerSlot(blockEntity.getItemStackHandler(), blockEntity.getItemStackHandler()::set, i, 107, 12 + i * 22));
        }
    }

    @Override
    protected int getMergeableSlotCount() {
        return 3;
    }
}
