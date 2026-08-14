package com.breakinblocks.nautec.utils;

import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

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
        PlayerInventoryWrapper inventory = PlayerInventoryWrapper.of(player);
        ItemResource resource = ItemResource.of(stack);
        int inserted;
        try (Transaction tx = Transaction.openRoot()) {
            inserted = preferredSlot >= 0 && preferredSlot < inventory.size()
                    ? inventory.getSlot(preferredSlot).insert(resource, stack.getCount(), tx)
                    : 0;
            if (inserted < stack.getCount()) {
                inserted += ResourceHandlerUtil.insertStacking(inventory.getMainSlots(), resource,
                        stack.getCount() - inserted, tx);
            }
            tx.commit();
        }

        if (playSound && inserted > 0) {
            level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                    ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }

        ItemStack remainder = stack.copyWithCount(stack.getCount() - inserted);
        if (!remainder.isEmpty() && !level.isClientSide()) {
            ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), remainder);
            itemEntity.setPickUpDelay(DROPPED_PICKUP_DELAY);
            itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().multiply(0, 1, 0));

            level.addFreshEntity(itemEntity);
        }
    }

    public static InteractionResult insertHeldItem(ResourceHandler<ItemResource> handler, int slot, ItemStack stack,
                                                   Player player, InteractionHand hand) {
        try (Transaction tx = Transaction.openRoot()) {
            if (handler.insert(slot, ItemResource.of(stack), stack.getCount(), tx) != stack.getCount()) {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
            tx.commit();
        }

        player.setItemInHand(hand, ItemStack.EMPTY);
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult extractItemToPlayer(ResourceHandler<ItemResource> handler, int slot, Player player) {
        ItemResource resource = handler.getResource(slot);
        if (resource.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        int extracted;
        try (Transaction tx = Transaction.openRoot()) {
            extracted = handler.extract(slot, resource, resource.getMaxStackSize(), tx);
            tx.commit();
        }

        if (extracted <= 0) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        giveItemToPlayer(player, resource.toStack(extracted));
        return InteractionResult.SUCCESS;
    }
}
