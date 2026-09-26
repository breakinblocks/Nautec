package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.api.menu.slots.SlotFluidHandler;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class MixerMenu extends NTMachineMenu<MixerBlockEntity> {
    public MixerMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (MixerBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public MixerMenu(int containerId, @NotNull Inventory inv, @NotNull MixerBlockEntity blockEntity) {
        super(NTMenuTypes.MIXER.get(), containerId, inv, blockEntity);

        for (int i = 0; i < 4; i++) {
            addSlot(new ResourceHandlerSlot(blockEntity.getItemStackHandler(), blockEntity.getItemStackHandler()::set, i, 29 + i * (4 + 18), 12));
        }

        addSlot(new ResourceHandlerSlot(blockEntity.getItemStackHandler(), blockEntity.getItemStackHandler()::set, 4, 29 + 2 * (4 + 18) - 11, 67));

        addFluidHandlerSlot(new SlotFluidHandler(blockEntity.getFluidTank(), 0, 122, 11, 18, 18));
        addFluidHandlerSlot(new SlotFluidHandler(blockEntity.getSecondaryFluidTank(), 0, 122, 66, 18, 18));
    }

    @Override
    protected int getMergeableSlotCount() {
        return 4;
    }

}
