package com.breakinblocks.nautec.transfer.item;

import com.breakinblocks.nautec.transfer.IndexModifier;
import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.transaction.Transaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ResourceHandlerSlot extends StackCopySlot {
    private final ResourceHandler<ItemResource> handler;
    private final IndexModifier<ItemResource> slotModifier;

    public ResourceHandlerSlot(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> slotModifier, int handlerSlot, int xPosition, int yPosition) {
        super(handlerSlot, xPosition, yPosition);
        this.handler = handler;
        this.slotModifier = slotModifier;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return handler.isValid(getSlotIndex(), ItemResource.of(stack));
    }

    @Override
    protected ItemStack getStackCopy() {
        return handler.getResource(getSlotIndex()).toStack(handler.getAmountAsInt(getSlotIndex()));
    }

    @Override
    protected void setStackCopy(ItemStack stack) {
        slotModifier.set(getSlotIndex(), ItemResource.of(stack), stack.getCount());
    }

    @Override
    public void onQuickCraft(ItemStack oldStack, ItemStack newStack) {
    }

    @Override
    public int getMaxStackSize() {
        return handler.getCapacityAsInt(getSlotIndex(), ItemResource.EMPTY);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return handler.getCapacityAsInt(getSlotIndex(), ItemResource.of(stack));
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemResource resource = handler.getResource(getSlotIndex());
        if (resource.isEmpty()) {
            return false;
        }
        try (Transaction simulation = Transaction.openRoot()) {
            return handler.extract(getSlotIndex(), resource, 1, simulation) == 1;
        }
    }

    public ResourceHandler<ItemResource> getResourceHandler() {
        return handler;
    }

    @Override
    public boolean isSameInventory(Slot other) {
        return other instanceof ResourceHandlerSlot slot && slot.handler == handler;
    }
}
