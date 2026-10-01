package com.breakinblocks.nautec.content.entities;

import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EyeOfTheSeaEntity extends Entity implements ItemSupplier {
    public static final int LIFETIME = 80;
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK =
            SynchedEntityData.defineId(EyeOfTheSeaEntity.class, EntityDataSerializers.ITEM_STACK);

    private @Nullable Vec3 target;
    private int life;

    public EyeOfTheSeaEntity(EntityType<? extends EyeOfTheSeaEntity> type, Level level) {
        super(type, level);
    }

    public EyeOfTheSeaEntity(Level level, double x, double y, double z) {
        this(NTEntities.EYE_OF_THE_SEA.get(), level);
        setPos(x, y, z);
    }

    public void setItem(ItemStack source) {
        getEntityData().set(DATA_ITEM_STACK, source.isEmpty() ? defaultItem() : source.copyWithCount(1));
    }

    @Override
    public ItemStack getItem() {
        return getEntityData().get(DATA_ITEM_STACK);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_ITEM_STACK, defaultItem());
    }

    private static ItemStack defaultItem() {
        return new ItemStack(NTItems.EYE_OF_THE_SEA.get());
    }

    public @Nullable Vec3 target() {
        return target;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        if (tickCount < 2 && distance < 12.25) {
            return false;
        }
        double size = getBoundingBox().getSize() * 4.0;
        if (Double.isNaN(size)) {
            size = 4.0;
        }
        size *= 64.0;
        return distance < size * size;
    }

    public void signalTo(Vec3 destination) {
        Vec3 delta = destination.subtract(position());
        double horizontalDistance = delta.horizontalDistance();
        if (horizontalDistance > 12.0) {
            target = position().add(delta.x / horizontalDistance * 12.0, 8.0, delta.z / horizontalDistance * 12.0);
        } else {
            target = destination;
        }
        life = 0;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 newPosition = position().add(getDeltaMovement());
        if (!level().isClientSide() && target != null) {
            setDeltaMovement(updateDeltaMovement(getDeltaMovement(), newPosition, target));
        }

        if (level().isClientSide()) {
            spawnTrail(newPosition.subtract(getDeltaMovement().scale(0.25)), getDeltaMovement());
        }

        setPos(newPosition);
        if (level() instanceof ServerLevel serverLevel) {
            if (target == null) {
                discard();
                return;
            }
            life++;
            if (life > LIFETIME) {
                vanish(serverLevel);
            }
        }
    }

    private void vanish(ServerLevel level) {
        playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.4F);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, getItem().getItem()),
                getX(), getY(), getZ(), 8, 0.1, 0.1, 0.1, 0.15);
        level.sendParticles(ParticleTypes.BUBBLE_POP, getX(), getY(), getZ(), 24, 0.4, 0.4, 0.4, 0.05);
        level.sendParticles(ParticleTypes.GLOW, getX(), getY(), getZ(), 6, 0.3, 0.3, 0.3, 0.02);
        discard();
    }

    private void spawnTrail(Vec3 origin, Vec3 movement) {
        if (isInWater()) {
            for (int i = 0; i < 4; i++) {
                level().addParticle(ParticleTypes.BUBBLE, origin.x, origin.y, origin.z, movement.x, movement.y, movement.z);
            }
            return;
        }
        level().addParticle(ParticleTypes.DOLPHIN,
                origin.x + random.nextDouble() * 0.6 - 0.3,
                origin.y - 0.3,
                origin.z + random.nextDouble() * 0.6 - 0.3,
                movement.x, movement.y, movement.z);
        if (random.nextInt(3) == 0) {
            level().addParticle(ParticleTypes.FALLING_WATER,
                    origin.x + random.nextDouble() * 0.4 - 0.2, origin.y - 0.2, origin.z + random.nextDouble() * 0.4 - 0.2,
                    0.0, 0.0, 0.0);
        }
    }

    private static Vec3 updateDeltaMovement(Vec3 oldMovement, Vec3 position, Vec3 target) {
        Vec3 horizontalDelta = new Vec3(target.x - position.x, 0.0, target.z - position.z);
        double horizontalLength = horizontalDelta.length();
        double wantedSpeed = Mth.lerp(0.0025, oldMovement.horizontalDistance(), horizontalLength);
        double movementY = oldMovement.y;
        if (horizontalLength < 1.0) {
            wantedSpeed *= 0.8;
            movementY *= 0.8;
        }
        double wantedMovementY = position.y - oldMovement.y < target.y ? 1.0 : -1.0;
        return horizontalDelta.scale(wantedSpeed / horizontalLength)
                .add(0.0, movementY + (wantedMovementY - movementY) * 0.015, 0.0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("Item", ItemStack.CODEC, getItem());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        setItem(input.read("Item", ItemStack.CODEC).orElse(defaultItem()));
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0F;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }
}
