package com.breakinblocks.nautec.utils;

import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import com.breakinblocks.nautec.transfer.transaction.Transaction;

public final class ItemUtils {
    public static final int ITEM_POWER_INPUT = 128;

    public static final int POWER_BAR_COLOR = ARGB.color(94, 133, 164);

    private static final int DROPPED_PICKUP_DELAY = 40;
    private static final int NO_PREFERRED_SLOT = -1;

    public static int powerForDurabilityBar(ItemStack itemStack) {
        IPowerStorage powerStorage = itemStack.getCapability(NTCapabilities.PowerStorage.ITEM);
        if (powerStorage != null) {
            int powerStored = powerStorage.getPowerStored();
            int powerCapacity = powerStorage.getPowerCapacity();
            float chargeRatio = (float) powerStored / powerCapacity;
            return Math.round(13.0F - ((1 - chargeRatio) * 13.0F));
        }
        return 0;
    }

    public static void giveItemToPlayer(Player player, ItemStack stack) {
        giveItemToPlayer(player, stack, NO_PREFERRED_SLOT, true);
    }

    public static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot) {
        giveItemToPlayer(player, stack, preferredSlot, true);
    }

    public static void giveItemToPlayerNoSound(Player player, ItemStack stack) {
        giveItemToPlayer(player, stack, NO_PREFERRED_SLOT, false);
    }

    private static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot, boolean playSound) {
        if (stack.isEmpty()) {
            return;
        }

        Level level = player.level();
        IItemHandler inventory = new PlayerMainInvWrapper(player.getInventory());
        ItemStack remainder = stack.copy();
        if (preferredSlot >= 0 && preferredSlot < inventory.getSlots()) {
            remainder = inventory.insertItem(preferredSlot, remainder, false);
        }
        if (!remainder.isEmpty()) {
            remainder = ItemHandlerHelper.insertItemStacked(inventory, remainder, false);
        }
        int inserted = stack.getCount() - remainder.getCount();

        if (playSound && inserted > 0) {
            level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                    ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }

        if (!remainder.isEmpty() && !level.isClientSide()) {
            ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), remainder);
            itemEntity.setPickUpDelay(DROPPED_PICKUP_DELAY);
            itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().multiply(0, 1, 0));

            level.addFreshEntity(itemEntity);
        }
    }

    public static ItemInteractionResult insertHeldItem(ResourceHandler<ItemResource> handler, int slot, ItemStack stack,
                                                   Player player, InteractionHand hand) {
        try (Transaction tx = Transaction.openRoot()) {
            if (handler.insert(slot, ItemResource.of(stack), stack.getCount(), tx) != stack.getCount()) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            tx.commit();
        }

        player.setItemInHand(hand, ItemStack.EMPTY);
        return ItemInteractionResult.SUCCESS;
    }

    public static ItemInteractionResult extractItemToPlayer(ResourceHandler<ItemResource> handler, int slot, Player player) {
        ItemResource resource = handler.getResource(slot);
        if (resource.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        int extracted;
        try (Transaction tx = Transaction.openRoot()) {
            extracted = handler.extract(slot, resource, resource.getMaxStackSize(), tx);
            tx.commit();
        }

        if (extracted <= 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        giveItemToPlayer(player, resource.toStack(extracted));
        return ItemInteractionResult.SUCCESS;
    }
}
