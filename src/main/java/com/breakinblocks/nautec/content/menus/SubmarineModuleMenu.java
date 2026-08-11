package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTEntityContainerMenu;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.entities.submarine.SubmarineModuleContainer;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import com.breakinblocks.nautec.registries.NTSounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SubmarineModuleMenu extends NTEntityContainerMenu<SubmarineEntity> {
    public static final int MODULE_ROW_Y = 26;
    public static final int INVENTORY_Y = 84;
    public static final int HOTBAR_Y = 142;

    private final Container modules;

    public SubmarineModuleMenu(int containerId, Inventory inv, SubmarineEntity submarine) {
        super(NTMenuTypes.SUBMARINE_MODULES.get(), containerId, inv, submarine);
        this.modules = new SubmarineModuleContainer(submarine);

        for (int slot = 0; slot < SubmarineEntity.MODULE_SLOTS; slot++) {
            addSlot(new ModuleSlot(this.modules, slot, 8 + slot * 18, MODULE_ROW_Y));
        }

        addPlayerInventory(inv, INVENTORY_Y);
        addPlayerHotbar(inv, HOTBAR_Y);
    }

    public SubmarineModuleMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(containerId, inv, resolveEntity(inv, buf.readVarInt(), SubmarineEntity.class));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return moved;
        }

        ItemStack stack = slot.getItem();
        moved = stack.copy();
        int moduleSlots = SubmarineEntity.MODULE_SLOTS;

        if (index < moduleSlots) {
            if (!moveItemStackTo(stack, moduleSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, moduleSlots, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return moved;
    }

    private static class ModuleSlot extends Slot {
        ModuleSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.container.canPlaceItem(getSlotIndex(), stack);
        }

        @Override
        public void setByPlayer(ItemStack stack, ItemStack previous) {
            super.setByPlayer(stack, previous);

            if (previous.isEmpty() && !stack.isEmpty()
                    && this.container instanceof SubmarineModuleContainer modules
                    && !modules.getSubmarine().level().isClientSide()) {
                SubmarineEntity submarine = modules.getSubmarine();
                submarine.level().playSound(null, submarine, NTSounds.SUBMARINE_MODULE_INSTALL.get(),
                        SoundSource.BLOCKS, 0.8F, 1.2F);
            }
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
