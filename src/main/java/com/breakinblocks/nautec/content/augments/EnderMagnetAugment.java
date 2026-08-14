package com.breakinblocks.nautec.content.augments;

import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.registries.NTAugments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

public class EnderMagnetAugment extends Augment {
    private static final double MAGNET_RADIUS = 5.0;
    private static final int PICKUP_PARTICLES = 4;

    public EnderMagnetAugment(AugmentSlot augmentSlot) {
        super(NTAugments.ENDER_MAGNET_AUGMENT.get(), augmentSlot);
    }

    @Override
    public void serverTick(PlayerTickEvent.Post event) {
        if (!player.isCrouching()) {
            List<ItemEntity> nearbyItems = player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(MAGNET_RADIUS));
            for (ItemEntity itemEntity : nearbyItems) {
                if (player.addItem(itemEntity.getItem())) {
                    playPickupFx(itemEntity);
                    itemEntity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }
    }

    private void playPickupFx(ItemEntity itemEntity) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        level.playSound(null, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                0.2F, ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        level.sendParticles(ParticleTypes.PORTAL,
                itemEntity.getX(), itemEntity.getY() + 0.25, itemEntity.getZ(),
                PICKUP_PARTICLES, 0.15, 0.15, 0.15, 0.05);
    }
}
