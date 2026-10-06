package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.conduits.ConduitTapBlockEntity;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class ConduitTapMenu extends NTAbstractContainerMenu<ConduitTapBlockEntity> {
    public static final int INVENTORY_Y = 174;
    public static final int UPGRADE_X = 133;
    public static final int FILTER_X = 151;
    public static final int UPGRADE_Y = 3;

    public ConduitTapMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf extraData) {
        this(containerId, inv, clientTap(inv, extraData));
    }

    public ConduitTapMenu(int containerId, @NotNull Inventory inv, @NotNull ConduitTapBlockEntity blockEntity) {
        super(NTMenuTypes.CONDUIT_TAP.get(), containerId, inv, blockEntity);
        ItemStackHandler handler = blockEntity.getItemStackHandler();
        addSlot(new ResourceHandlerSlot(handler, handler::set, ConduitTapBlockEntity.UPGRADE_SLOT, UPGRADE_X, UPGRADE_Y));
        addSlot(new ResourceHandlerSlot(handler, handler::set, ConduitTapBlockEntity.FILTER_SLOT, FILTER_X, UPGRADE_Y));
        addPlayerInventory(inv, INVENTORY_Y);
        addPlayerHotbar(inv, INVENTORY_Y + 58);
    }

    private static ConduitTapBlockEntity clientTap(Inventory inv, RegistryFriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        ConduitTapBlockEntity tap = inv.player.level().getBlockEntity(pos) instanceof ConduitTapBlockEntity found
                ? found
                : new ConduitTapBlockEntity(pos, NTBlocks.CONDUIT_TAP.get().defaultBlockState());
        tap.readFaces(extraData);
        return tap;
    }

    @Override
    protected int getMergeableSlotCount() {
        return ConduitTapBlockEntity.SLOTS;
    }
}
