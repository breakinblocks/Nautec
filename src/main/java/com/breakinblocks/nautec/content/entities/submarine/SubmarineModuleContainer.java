package com.breakinblocks.nautec.content.entities.submarine;

import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.tags.NTTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SubmarineModuleContainer implements Container {
    private final SubmarineEntity submarine;

    public SubmarineModuleContainer(SubmarineEntity submarine) {
        this.submarine = submarine;
    }

    public SubmarineEntity getSubmarine() {
        return this.submarine;
    }

    @Override
    public int getContainerSize() {
        return SubmarineEntity.MODULE_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < getContainerSize(); slot++) {
            if (!this.submarine.getModule(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.submarine.getModule(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = this.submarine.getModule(slot).copy();
        if (stack.isEmpty() || count <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack taken = stack.split(count);
        this.submarine.setModule(slot, stack);
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = this.submarine.getModule(slot);
        this.submarine.setModule(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.submarine.setModule(slot, stack);
    }

    @Override
    public void setChanged() {
        for (int slot = 0; slot < getContainerSize(); slot++) {
            this.submarine.setModule(slot, this.submarine.getModule(slot));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.submarine.isAlive() && player.isWithinEntityInteractionRange(this.submarine, 4.0);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(NTTags.Items.SUBMARINE_MODULE);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < getContainerSize(); slot++) {
            this.submarine.setModule(slot, ItemStack.EMPTY);
        }
    }
}
