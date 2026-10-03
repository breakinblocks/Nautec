package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.content.distributor.DistributorBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DistributorMenu extends NTAbstractContainerMenu<DistributorBlockEntity> {
    public static final int INVENTORY_Y = 140;

    public DistributorMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (DistributorBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public DistributorMenu(int containerId, @NotNull Inventory inv, @NotNull DistributorBlockEntity blockEntity) {
        super(NTMenuTypes.DISTRIBUTOR.get(), containerId, inv, blockEntity);
        addPlayerInventory(inv, INVENTORY_Y);
        addPlayerHotbar(inv, INVENTORY_Y + 58);
    }

    @Override
    protected int getMergeableSlotCount() {
        return 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
