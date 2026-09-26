package com.breakinblocks.nautec.events;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.AugmentationStationBlockEntity;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.utils.AugmentHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import com.breakinblocks.nautec.network.SyncAugmentPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;


@SuppressWarnings("unused")
@EventBusSubscriber(modid = Nautec.MODID)
public final class AugmentEvents {
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer && event.getTarget() instanceof Player target) {
            for (Augment augment : AugmentHelper.getAugments(target).values()) {
                PacketDistributor.sendToPlayer(viewer, new SyncAugmentPayload(target.getId(), augment,
                        augment.serializeNBT(target.level().registryAccess())));
            }
        }
    }

    @SubscribeEvent
    public static void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        AugmentHelper.restoreAugments(event.getEntity());
    }

    @SubscribeEvent
    public static void fallEvent(LivingFallEvent event) {
        if (event.getEntity() instanceof Player) {
            Iterable<Augment> augments = AugmentHelper.getAugments((Player) event.getEntity()).values();
            for (Augment augment : augments) {
                if (augment != null) {
                    augment.fall(event);
                }
            }
        }
    }

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            AugmentationStationBlockEntity.checkPlayer(player);
        }
        Iterable<Augment> augments = AugmentHelper.getAugments(player).values();
        for (Augment augment : augments) {
            if (augment != null) {
                AugmentSlot slot = augment.getAugmentSlot();
                augment.commonTick(event);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            AugmentationStationBlockEntity.cancelFor(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        AugmentHelper.restoreAugments(player);
    }
}
