package com.breakinblocks.nautec.content.entities;

import com.breakinblocks.nautec.content.items.NeptunesTridentItem;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.TagValueInput;
import com.breakinblocks.nautec.utils.valueio.TagValueOutput;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ThrownNeptunesTrident extends AbstractArrow {
    private static final EntityDataAccessor<Boolean> ID_FOIL = SynchedEntityData.defineId(ThrownNeptunesTrident.class, EntityDataSerializers.BOOLEAN);
    private static final float WATER_INERTIA = 0.99F;
    private static final double DRIPSTONE_BREAK_SPEED = 0.6;
    private static final float THUNDER_VOLUME = 5.0F;
    private boolean dealtDamage;
    private boolean shockwaveSpent;
    public int clientSideReturnTridentTickCount;

    public ThrownNeptunesTrident(EntityType<? extends ThrownNeptunesTrident> type, Level level) {
        super(type, level);
    }

    public ThrownNeptunesTrident(Level level, LivingEntity owner, ItemStack tridentItem) {
        super(NTEntities.NEPTUNES_TRIDENT.get(), owner, level, tridentItem, null);
        this.entityData.set(ID_FOIL, tridentItem.hasFoil());
    }

    public ThrownNeptunesTrident(Level level, double x, double y, double z, ItemStack tridentItem) {
        super(NTEntities.NEPTUNES_TRIDENT.get(), x, y, z, level, tridentItem, tridentItem);
        this.entityData.set(ID_FOIL, tridentItem.hasFoil());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(ID_FOIL, false);
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity currentOwner = this.getOwner();
        int loyalty = NeptunesTridentItem.BUILT_IN_LOYALTY;
        if ((this.dealtDamage || this.isNoPhysics()) && currentOwner != null) {
            if (!this.isAcceptableReturnOwner()) {
                if (this.level() instanceof ServerLevel level && this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.discard();
            } else {
                if (!(currentOwner instanceof Player) && this.position().distanceTo(currentOwner.getEyePosition()) < currentOwner.getBbWidth() + 1.0) {
                    this.discard();
                    return;
                }

                this.setNoPhysics(true);
                Vec3 toOwner = currentOwner.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + toOwner.y * 0.015 * loyalty, this.getZ());
                double acceleration = 0.05 * loyalty;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(toOwner.normalize().scale(acceleration)));
                if (this.clientSideReturnTridentTickCount == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }

                this.clientSideReturnTridentTickCount++;
            }
        }

        super.tick();
    }

    private boolean isAcceptableReturnOwner() {
        Entity currentOwner = this.getOwner();
        if (currentOwner == null || !currentOwner.isAlive()) {
            return false;
        }
        return !(currentOwner instanceof ServerPlayer) || !currentOwner.isSpectator();
    }

    public boolean isFoil() {
        return this.entityData.get(ID_FOIL);
    }

    @Override
    protected @Nullable EntityHitResult findHitEntity(Vec3 from, Vec3 to) {
        return this.dealtDamage ? null : super.findHitEntity(from, to);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        Entity entity = hitResult.getEntity();
        float damage = NeptunesTridentItem.DAMAGE;
        Entity currentOwner = this.getOwner();
        DamageSource damageSource = this.damageSources().trident(this, currentOwner == null ? this : currentOwner);
        if (this.level() instanceof ServerLevel serverLevel) {
            damage = EnchantmentHelper.modifyDamage(serverLevel, this.getWeaponItem(), entity, damageSource, damage);
        }

        this.dealtDamage = true;
        if (entity.hurt(damageSource, damage)) {
            if (entity.getType() == EntityType.ENDERMAN) {
                this.releaseShockwave(entity.position());
                return;
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, entity, damageSource, this.getWeaponItem());
                this.channelLightning(serverLevel, entity.position(), entity);
            }

            if (entity instanceof LivingEntity mob) {
                this.doKnockback(mob, damageSource);
                this.doPostHurtEffects(mob);
            }
        }

        this.deflect(ProjectileDeflection.REVERSE, entity, this.getOwner(), false);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.02, 0.2, 0.02));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
        this.releaseShockwave(entity.position());
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = this.level().getBlockState(pos);
        double impactSpeed = this.getDeltaMovement().length();
        super.onHitBlock(hitResult);
        if (this.level() instanceof ServerLevel serverLevel
                && state.is(Blocks.POINTED_DRIPSTONE)
                && impactSpeed > DRIPSTONE_BREAK_SPEED
                && this.mayInteract(serverLevel, pos)
                && this.mayBreak(serverLevel)) {
            serverLevel.destroyBlock(pos, true);
        }
        this.releaseShockwave(hitResult.getLocation());
    }

    private void releaseShockwave(Vec3 center) {
        if (this.shockwaveSpent || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        this.shockwaveSpent = true;
        ItemStack weapon = this.getWeaponItem();
        if (TidalShockwave.isReady(weapon, serverLevel.getGameTime())) {
            TidalShockwave.release(serverLevel, this, this.getOwner(), weapon, center);
        }
    }

    @Override
    protected void hitBlockEnchantmentEffects(ServerLevel level, BlockHitResult hitResult, ItemStack weapon) {
        Vec3 compensatedHitPosition = hitResult.getBlockPos().clampLocationWithin(hitResult.getLocation());
        EnchantmentHelper.onHitBlock(
                level,
                weapon,
                this.getOwner() instanceof LivingEntity livingOwner ? livingOwner : null,
                this,
                null,
                compensatedHitPosition,
                level.getBlockState(hitResult.getBlockPos()),
                item -> this.kill()
        );
        if (level.getBlockState(BlockPos.containing(compensatedHitPosition)).is(Blocks.LIGHTNING_ROD)) {
            this.channelLightning(level, compensatedHitPosition, this);
        }
    }

    private void channelLightning(ServerLevel level, Vec3 position, Entity soundSource) {
        BlockPos pos = BlockPos.containing(position);
        if (!level.isThundering() || !level.canSeeSky(pos) || !this.hasChanneling(level)) {
            return;
        }

        LightningBolt bolt = Level.isInSpawnableBounds(pos) ? EntityType.LIGHTNING_BOLT.spawn(level, pos, MobSpawnType.TRIGGERED) : null;
        if (bolt != null) {
            if (this.getOwner() instanceof ServerPlayer player) {
                bolt.setCause(player);
            }
            bolt.moveTo(position.x, position.y, position.z, bolt.getYRot(), bolt.getXRot());
        }
        if (!soundSource.isSilent()) {
            level.playSound(null, position.x, position.y, position.z, SoundEvents.TRIDENT_THUNDER, soundSource.getSoundSource(), THUNDER_VOLUME, 1.0F);
        }
    }

    private boolean hasChanneling(ServerLevel level) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.CHANNELING)
                .map(channeling -> EnchantmentHelper.getItemEnchantmentLevel(channeling, this.getWeaponItem()) > 0)
                .orElse(false);
    }

    @Override
    public ItemStack getWeaponItem() {
        return this.getPickupItemStackOrigin();
    }

    @Override
    protected boolean tryPickup(Player player) {
        return super.tryPickup(player) || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(NTItems.NEPTUNES_TRIDENT.get());
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(Player player) {
        if (this.ownedBy(player) || this.getOwner() == null) {
            super.playerTouch(player);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ValueInput input = TagValueInput.create(this.registryAccess(), tag);
        this.dealtDamage = input.getBooleanOr("DealtDamage", false);
        this.shockwaveSpent = input.getBooleanOr("ShockwaveSpent", false);
        this.entityData.set(ID_FOIL, this.getPickupItemStackOrigin().hasFoil());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ValueOutput output = TagValueOutput.wrap(this.registryAccess(), tag);
        output.putBoolean("DealtDamage", this.dealtDamage);
        output.putBoolean("ShockwaveSpent", this.shockwaveSpent);
    }

    @Override
    public void tickDespawn() {
        if (this.pickup != AbstractArrow.Pickup.ALLOWED) {
            super.tickDespawn();
        }
    }

    @Override
    protected float getWaterInertia() {
        return WATER_INERTIA;
    }

    @Override
    public boolean shouldRender(double camX, double camY, double camZ) {
        return true;
    }
}
