package com.breakinblocks.nautec.content.augments;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.network.KeyPressedPayload;
import com.breakinblocks.nautec.registries.NTAugments;
import com.breakinblocks.nautec.registries.NTKeybinds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import com.breakinblocks.nautec.utils.AugmentHelper;

public class GuardianEyeAugment extends Augment {
    public Vec3 laserFiredPos = null;
    public int timeLeft = 0;

    private Entity targetEntity;
    private int beamTicks;

    private float clientLaserTime;

    public GuardianEyeAugment(AugmentSlot augmentSlot) {
        super(NTAugments.GUARDIAN_EYE.get(), augmentSlot);
    }

    @Override
    public void clientTick(PlayerTickEvent.Post event) {
        if (player.isLocalPlayer() && NTKeybinds.ACTIVATE_LASER_KEYBIND.get().isDown()) {
            ClientPacketDistributor.sendToServer(new KeyPressedPayload(augmentSlot));
            handleKeybindPress();
        }

        if (targetEntity != null && !targetEntity.isAlive()) {
            targetEntity = null;
        }

        if (targetEntity != null) {
            if (clientLaserTime < getLaserAnimTimeDuration()) {
                this.clientLaserTime += 0.5f;
            } else {
                this.clientLaserTime = 0;
            }

            double laserScale = this.getLaserScale(0.0F);
            double dx = targetEntity.getX() - player.getX();
            double dy = targetEntity.getY(0.5) - player.getEyeY();
            double dz = targetEntity.getZ() - player.getZ();

            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double nx = dx / dist;
            double ny = dy / dist;
            double nz = dz / dist;

            double rOffset = player.getRandom().nextDouble();
            double particleDist = 0.0;

            while (particleDist < dist) {
                particleDist += 1.8 - laserScale + rOffset * (1.7-laserScale);
                player.level().addParticle(ParticleTypes.BUBBLE,
                        player.getX() + nx * particleDist,
                        player.getEyeY() + ny * particleDist,
                        player.getZ() + nz * particleDist,
                        0.0,0.0,0.0);
            }

        }
    }

    public int getLaserAnimTimeDuration() {
        return 80;
    }

    public float getClientLaserTime() {
        return clientLaserTime;
    }

    public float getLaserScale(float partialTick) {
        return (this.clientLaserTime + partialTick) / (float) this.getLaserAnimTimeDuration();
    }

    @Override
    public void handleKeybindPress() {
        Vec3 look = player.getLookAngle();
        Vec3 startPos = player.getEyePosition(1.0f);
        double maxDistance = 15.0;
        double step = 0.1;
        for (double t = 0; t <= maxDistance; t += step) {
            Vec3 checkPos = startPos.add(look.scale(t));
            List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, new AABB(checkPos.add(-0.5, -0.5, -0.5), checkPos.add(0.5, 0.5, 0.5)));

            for (LivingEntity entity : entities) {
                if (entity != player) {
                    if (!player.level().isClientSide()) {
                        entity.hurt(entity.damageSources().magic(), NTConfig.guardianAugmentDamage);
                        timeLeft = 1000;
                        laserFiredPos = entity.getEyePosition();
                    }
                    this.targetEntity = entity;
                    this.beamTicks = 3;
                    return;
                }
            }
        }

        this.targetEntity = null;
    }

    @Override
    public void serverTick(PlayerTickEvent.Post event) {
        if (beamTicks > 0 && --beamTicks == 0 && targetEntity != null) {
            targetEntity = null;
            AugmentHelper.syncAugment(player, this);
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = super.serializeNBT(provider);
        tag.putInt("beam_target", targetEntity == null ? -1 : targetEntity.getId());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        super.deserializeNBT(provider, tag);
        if (player != null && player.level().isClientSide()) {
            Entity target = player.level().getEntity(tag.getIntOr("beam_target", -1));
            if (target != targetEntity) clientLaserTime = 0;
            targetEntity = target;
        }
    }

    @Override
    public GuardianEyeAugment copyForRender() {
        GuardianEyeAugment copy = (GuardianEyeAugment) super.copyForRender();
        copy.targetEntity = targetEntity;
        copy.clientLaserTime = clientLaserTime;
        return copy;
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }
}
