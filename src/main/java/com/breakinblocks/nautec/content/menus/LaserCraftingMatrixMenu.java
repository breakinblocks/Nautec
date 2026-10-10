package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.api.menu.slots.SlotFluidHandler;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.blockentities.LaserCraftingMatrixBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import com.breakinblocks.nautec.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class LaserCraftingMatrixMenu extends NTMachineMenu<LaserCraftingMatrixBlockEntity> {
    public static final int TANK_Y = 17;
    public static final int TANK_HEIGHT = 52;
    public static final int TANK_WIDTH = 16;
    public static final int[] INPUT_TANK_X = {8, 27};
    public static final int INPUT_SLOT_X = 48;
    public static final int OUTPUT_SLOT_X = 112;
    public static final int[] OUTPUT_TANK_X = {133, 152};
    public static final int SLOT_TOP = 17;
    public static final int SLOT_STEP = 18;

    public LaserCraftingMatrixMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (LaserCraftingMatrixBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public LaserCraftingMatrixMenu(int containerId, @NotNull Inventory inv, @NotNull LaserCraftingMatrixBlockEntity blockEntity) {
        super(NTMenuTypes.LASER_CRAFTING_MATRIX.get(), containerId, inv, blockEntity);
        ItemStackHandler handler = blockEntity.getItemStackHandler();

        for (int i = 0; i < LaserCraftingMatrixBlockEntity.INPUT_SLOTS; i++) {
            addSlot(new ResourceHandlerSlot(handler, handler::set, i, INPUT_SLOT_X, SLOT_TOP + i * SLOT_STEP));
        }
        for (int i = 0; i < LaserCraftingMatrixBlockEntity.SLOTS - LaserCraftingMatrixBlockEntity.OUTPUT_START; i++) {
            addSlot(new ResourceHandlerSlot(handler, handler::set, LaserCraftingMatrixBlockEntity.OUTPUT_START + i,
                    OUTPUT_SLOT_X, SLOT_TOP + i * SLOT_STEP));
        }

        for (int i = 0; i < INPUT_TANK_X.length; i++) {
            addFluidHandlerSlot(new SlotFluidHandler(blockEntity.inputTank(i), 0, INPUT_TANK_X[i], TANK_Y, TANK_WIDTH, TANK_HEIGHT));
        }
        for (int i = 0; i < OUTPUT_TANK_X.length; i++) {
            addFluidHandlerSlot(new SlotFluidHandler(blockEntity.outputTank(i), 0, OUTPUT_TANK_X[i], TANK_Y, TANK_WIDTH, TANK_HEIGHT));
        }
    }

    @Override
    protected int getMergeableSlotCount() {
        return LaserCraftingMatrixBlockEntity.INPUT_SLOTS;
    }
}
