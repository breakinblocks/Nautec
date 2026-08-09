package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.content.blockentities.BacterialAnalyzerBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class BacterialAnalyzerMenu extends NTAbstractContainerMenu<BacterialAnalyzerBlockEntity> {
    public BacterialAnalyzerMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (BacterialAnalyzerBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public BacterialAnalyzerMenu(int containerId, @NotNull Inventory inv, @NotNull BacterialAnalyzerBlockEntity blockEntity) {
        super(NTMenuTypes.BACTERIAL_ANALYZER.get(), containerId, inv, blockEntity);

        addSlot(new ResourceHandlerSlot(blockEntity.getItemStackHandler(), blockEntity.getItemStackHandler()::set, 0, 53, 34));
        addSlot(new ResourceHandlerSlot(blockEntity.getItemStackHandler(), blockEntity.getItemStackHandler()::set, 1, 107, 34));

        addPlayerInventory(inv, 92);
        addPlayerHotbar(inv, 150);
    }

    @Override
    protected int getMergeableSlotCount() {
        return 2;
    }
}
