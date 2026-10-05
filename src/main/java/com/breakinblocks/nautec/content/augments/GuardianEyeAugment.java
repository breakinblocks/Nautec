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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;
import java.util.Optional;
import net.minecraft.world.level.Level;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import com.breakinblocks.nautec.utils.AugmentHelper;

public class GuardianEyeAugment extends Augment {
    private static final double MAX_DISTANCE = 15.0;
    private static final double HIT_PADDING = 0.5;
    private static final int FIRE_INTERVAL = 2;
    private static final int BEAM_TIMEOUT = 5;

    public Vec3 laserFiredPos = null;
    public int timeLeft = 0;

    private Entity targetEntity;
    private int beamTicks;

    private float clientLaserTime;
    private long lastFireTime = -FIRE_INTERVAL;
    private long lastSendTime = -FIRE_INTERVAL;

    public GuardianEyeAugment(AugmentSlot augmentSlot) {
        super(NTAugments.GUARDIAN_EYE.get(), augmentSlot);
    }

    @Override
    public void clientTick(PlayerTickEvent.Post event) {
        if (player.isLocalPlayer() && NTKeybinds.ACTIVATE_LASER_KEYBIND.get().isDown()
                && intervalPassed(lastSendTime)) {
            lastSendTime = player.level().getGameTime();
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
    public boolean canActivate() {
        return super.canActivate() && intervalPassed(lastFireTime);
    }

    private boolean intervalPassed(long last) {
        long elapsed = player.level().getGameTime() - last;
        return elapsed >= FIRE_INTERVAL || elapsed < 0;
    }

    @Override
    public void handleKeybindPress() {
        Level level = player.level();
        if (!level.isClientSide()) {
            lastFireTime = level.getGameTime();
        }
        Vec3 look = player.getLookAngle();
        Vec3 startPos = player.getEyePosition(1.0f);
        Vec3 endPos = startPos.add(look.scale(MAX_DISTANCE));
        BlockHitResult blockHit = level.clip(new ClipContext(startPos, endPos,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blockHit.getType() != HitResult.Type.MISS) {
            endPos = blockHit.getLocation();
        }

        LivingEntity closest = null;
        double closestDistance = Double.MAX_VALUE;
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(startPos, endPos).inflate(HIT_PADDING),
                entity -> entity != player && entity.isAlive());
        for (LivingEntity entity : entities) {
            AABB box = entity.getBoundingBox().inflate(HIT_PADDING);
            double distance;
            if (box.contains(startPos)) {
                distance = 0;
            } else {
                Optional<Vec3> hit = box.clip(startPos, endPos);
                if (hit.isEmpty()) {
                    continue;
                }
                distance = startPos.distanceToSqr(hit.get());
            }
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = entity;
            }
        }

        if (closest == null) {
            this.targetEntity = null;
            return;
        }
        if (!level.isClientSide()) {
            closest.hurt(player.damageSources().indirectMagic(player, player), NTConfig.guardianAugmentDamage);
            timeLeft = 1000;
            laserFiredPos = closest.getEyePosition();
        }
        this.targetEntity = closest;
        this.beamTicks = BEAM_TIMEOUT;
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
