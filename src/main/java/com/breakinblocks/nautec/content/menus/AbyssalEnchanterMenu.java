package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.api.menu.slots.SlotFluidHandler;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.enchanter.AbyssalEnchanterBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class AbyssalEnchanterMenu extends NTMachineMenu<AbyssalEnchanterBlockEntity> {
    public static final int TANK_X = 8;
    public static final int TANK_Y = 17;
    public static final int TANK_WIDTH = 16;
    public static final int TANK_HEIGHT = 52;
    public static final int TARGET_X = 32;
    public static final int TARGET_Y = 21;
    public static final int REAGENT_X = 32;
    public static final int REAGENT_Y = 47;
    public static final int OUTPUT_X = 148;
    public static final int OUTPUT_Y = 34;

    public AbyssalEnchanterMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (AbyssalEnchanterBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public AbyssalEnchanterMenu(int containerId, @NotNull Inventory inv, @NotNull AbyssalEnchanterBlockEntity blockEntity) {
        super(NTMenuTypes.ABYSSAL_ENCHANTER.get(), containerId, inv, blockEntity);
        ItemStackHandler handler = blockEntity.getItemStackHandler();
        addSlot(new ResourceHandlerSlot(handler, handler::set, AbyssalEnchanterBlockEntity.TARGET_SLOT, TARGET_X, TARGET_Y));
        addSlot(new ResourceHandlerSlot(handler, handler::set, AbyssalEnchanterBlockEntity.REAGENT_SLOT, REAGENT_X, REAGENT_Y));
        addSlot(new ResourceHandlerSlot(handler, handler::set, AbyssalEnchanterBlockEntity.OUTPUT_SLOT, OUTPUT_X, OUTPUT_Y));
        addFluidHandlerSlot(new SlotFluidHandler(blockEntity.getFluidTank(), 0, TANK_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!player.level().isClientSide()) {
            blockEntity.pressButton(id);
        }
        return true;
    }

    @Override
    protected int getMergeableSlotCount() {
        return 2;
    }
}
