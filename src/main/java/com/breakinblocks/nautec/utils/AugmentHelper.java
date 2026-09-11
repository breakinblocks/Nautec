package com.breakinblocks.nautec.utils;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.api.augments.AugmentType;
import com.breakinblocks.nautec.data.NTDataAttachments;
import com.breakinblocks.nautec.network.ClearAugmentPayload;
import com.breakinblocks.nautec.network.SyncAugmentPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import com.breakinblocks.nautec.client.AugmentClientHelper;

public final class AugmentHelper {
    public static Augment getAugmentBySlot(Player player, AugmentSlot augmentSlot) {
        return getAugments(player).get(augmentSlot);
    }

    public static Map<AugmentSlot, Augment> getAugments(Player player) {
        return player.getData(NTDataAttachments.AUGMENTS);
    }

    public static Map<AugmentSlot, CompoundTag> getAugmentsData(Player player) {
        return player.getData(NTDataAttachments.AUGMENTS_EXTRA_DATA);
    }

    public static void setAugment(Player player, AugmentSlot augmentSlot, Augment augment) {
        if (augmentSlot == null) {
            Nautec.LOGGER.warn("Refusing to write null AugmentSlot key for player {}", player.getName().getString());
            return;
        }
        Map<AugmentSlot, Augment> augments = new HashMap<>(getAugments(player));
        augments.put(augmentSlot, augment);
        player.setData(NTDataAttachments.AUGMENTS, augments);
    }

    public static void setAugmentExtraData(Player player, AugmentSlot augmentSlot, CompoundTag tag) {
        if (augmentSlot == null) {
            Nautec.LOGGER.warn("Refusing to write null AugmentSlot key (extra data) for player {}", player.getName().getString());
            return;
        }
        Map<AugmentSlot, CompoundTag> augments = new HashMap<>(getAugmentsData(player));
        augments.put(augmentSlot, tag);
        player.setData(NTDataAttachments.AUGMENTS_EXTRA_DATA, augments);
    }

    public static Augment createAugment(AugmentType<?> augmentType, Player player, AugmentSlot augmentSlot) {
        Augment previous = getAugmentBySlot(player, augmentSlot);
        if (previous != null) {
            previous.onRemoved(player);
        }
        Augment augment = augmentType.create(augmentSlot);
        augment.setPlayer(player);
        AugmentHelper.setAugment(player, augmentSlot, augment);
        reapplyEffects(player);
        syncAugment(player, augment);
        if (player.level().isClientSide()) {
            AugmentClientHelper.invalidateCacheFor(player, augmentSlot);
        }
        return augment;
    }

    public static void syncAugment(Player player, Augment augment) {
        CompoundTag data = augment.serializeNBT(player.level().registryAccess());
        setAugmentExtraData(player, augment.getAugmentSlot(), data);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncAugmentPayload(augment, data));
        }
    }

    public static void restoreAugments(Player player) {
        for (Augment augment : getAugments(player).values()) {
            augment.setPlayer(player);
            CompoundTag data = getAugmentsData(player).get(augment.getAugmentSlot());
            if (data != null) {
                augment.deserializeNBT(player.level().registryAccess(), data);
            }
            augment.onAdded(player);
            syncAugment(player, augment);
        }
    }

    private static void reapplyEffects(Player player) {
        for (Augment installed : getAugments(player).values()) {
            installed.onAdded(player);
        }
    }

    public static void removeAugment(Player player, AugmentSlot augmentSlot) {
        Augment augment = getAugmentBySlot(player, augmentSlot);
        if (augment != null) {
            augment.onRemoved(player);
        }
        Map<AugmentSlot, Augment> augments = new HashMap<>(getAugments(player));
        Map<AugmentSlot, CompoundTag> augmentsData = new HashMap<>(getAugmentsData(player));
        augments.remove(augmentSlot);
        augmentsData.remove(augmentSlot);
        player.setData(NTDataAttachments.AUGMENTS, augments);
        player.setData(NTDataAttachments.AUGMENTS_EXTRA_DATA, augmentsData);
        reapplyEffects(player);
        
        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new ClearAugmentPayload(augmentSlot));
        }
        
        if (player.level().isClientSide()) {
            AugmentClientHelper.invalidateCacheFor(player, augmentSlot);
        }
    }
}
