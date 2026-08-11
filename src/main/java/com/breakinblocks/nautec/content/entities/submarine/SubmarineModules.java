package com.breakinblocks.nautec.content.entities.submarine;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.items.submarine.SubmarineModuleType;
import com.breakinblocks.nautec.content.items.submarine.TeleportModuleItem;
import com.breakinblocks.nautec.data.components.TeleportAnchor;
import com.breakinblocks.nautec.network.SonarPingPayload;
import com.breakinblocks.nautec.network.SubmarineCooldownPayload;
import com.breakinblocks.nautec.network.TeleportFxPayload;
import com.breakinblocks.nautec.registries.NTMobEffects;
import com.breakinblocks.nautec.registries.NTParticles;
import com.breakinblocks.nautec.registries.NTSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SubmarineModules {
    public static final int TELEPORT_CHARGE_TICKS = 50;

    private static final int SOLAR_INTERVAL = 100;
    private static final int LASER_DAMAGE_INTERVAL = 10;
    private static final double MUZZLE_SIDE = 1.0D;
    private static final double MUZZLE_FORWARD = 2.0D;
    private static final double MUZZLE_UP = 0.3D;
    private static final double SHIELD_KNOCKBACK = 1.5D;
    private static final int SONAR_GLOW_TICKS = 300;
    private static final int SONAR_NIGHT_VISION_TICKS = 900;
    private static final float SONAR_PULSE_RANGE = 48F;
    private static final double STEALTH_AGGRO_RANGE = 32D;

    private final SubmarineEntity submarine;
    private final long[] readyAt = new long[SubmarineEntity.MODULE_SLOTS];
    private final int[] cooldownTicks = new int[SubmarineEntity.MODULE_SLOTS];
    private final int[] activeTicks = new int[SubmarineEntity.MODULE_SLOTS];

    private int boostTicks;
    private int stealthTicks;
    private int teleportTicks;
    private @Nullable TeleportAnchor teleportTarget;

    public SubmarineModules(SubmarineEntity submarine) {
        this.submarine = submarine;
    }

    public void tickServer() {
        solarTick();

        if (this.boostTicks > 0 && --this.boostTicks == 0) {
            updateSpeedMultiplier();
        }

        if (this.stealthTicks > 0 && --this.stealthTicks == 0) {
            this.submarine.setStealthed(false);
            updateSpeedMultiplier();
        }

        laserTick();
        teleportTick();
    }

    public void save(ValueOutput output) {
        output.putInt("boostTicks", this.boostTicks);
        output.putInt("stealthTicks", this.stealthTicks);
    }

    public void load(ValueInput input) {
        this.boostTicks = input.getIntOr("boostTicks", 0);
        this.stealthTicks = input.getIntOr("stealthTicks", 0);
        this.submarine.setStealthed(this.stealthTicks > 0);
        updateSpeedMultiplier();
    }

    public boolean isReady(int slot) {
        return this.submarine.level().getGameTime() >= this.readyAt[slot];
    }

    public int remainingCooldown(int slot) {
        return (int) Math.max(0L, this.readyAt[slot] - this.submarine.level().getGameTime());
    }

    public boolean isBoosting() {
        return this.boostTicks > 0;
    }

    public void startCooldown(int slot, int cooldown, int active) {
        this.readyAt[slot] = this.submarine.level().getGameTime() + cooldown;
        this.cooldownTicks[slot] = cooldown;
        this.activeTicks[slot] = active;
        broadcast(new SubmarineCooldownPayload(this.submarine.getId(), slot, cooldown, active));
    }

    public void clearCooldowns() {
        for (int slot = 0; slot < SubmarineEntity.MODULE_SLOTS; slot++) {
            this.readyAt[slot] = 0L;
            this.cooldownTicks[slot] = 0;
            this.activeTicks[slot] = 0;
        }
    }

    public void sendCooldownSnapshot(ServerPlayer player) {
        for (int slot = 0; slot < SubmarineEntity.MODULE_SLOTS; slot++) {
            int remaining = remainingCooldown(slot);
            if (remaining <= 0) {
                continue;
            }

            int elapsed = this.cooldownTicks[slot] - remaining;
            int activeLeft = Math.max(0, this.activeTicks[slot] - elapsed);
            PacketDistributor.sendToPlayer(player,
                    new SubmarineCooldownPayload(this.submarine.getId(), slot, remaining, activeLeft));
        }
    }

    public void activate(int slot, Player pilot) {
        if (slot < 0 || slot >= SubmarineEntity.MODULE_SLOTS) {
            return;
        }

        SubmarineModuleType type = this.submarine.getModuleType(slot);
        if (type == null || type.isPassive()) {
            return;
        }

        if (type == SubmarineModuleType.IMPULSE_LASER && this.submarine.isLaserActive()) {
            this.submarine.setLaserActive(false);
            return;
        }

        if (this.teleportTicks > 0) {
            return;
        }

        if (!isReady(slot)) {
            refuse(pilot, "cooldown");
            return;
        }

        if (!hasPowerFor(type, pilot)) {
            refuse(pilot, "no_power");
            return;
        }

        if (!applyAbility(type, slot, pilot)) {
            return;
        }

        startCooldown(slot, type.cooldownTicks(), type.activeTicks());
    }

    public boolean hasPowerFor(SubmarineModuleType type, Player pilot) {
        return pilot.gameMode().isCreative() || this.submarine.getPowerStored() >= type.powerCost();
    }

    private boolean applyAbility(SubmarineModuleType type, int slot, Player pilot) {
        return switch (type) {
            case BOOSTER -> {
                drain(type.powerCost(), pilot);
                this.boostTicks = NTConfig.submarineBoostDurationTicks;
                updateSpeedMultiplier();
                play(NTSounds.SUBMARINE_BOOST.get(), 0.9F, 1F);
                yield true;
            }
            case STEALTH -> {
                drain(type.powerCost(), pilot);
                this.stealthTicks = NTConfig.submarineStealthDurationTicks;
                this.submarine.setStealthed(true);
                updateSpeedMultiplier();
                dropAggro();
                play(NTSounds.SUBMARINE_STEALTH.get(), 0.8F, 1F);
                yield true;
            }
            case SONAR -> {
                drain(type.powerCost(), pilot);
                sonarPing();
                yield true;
            }
            case SHIELD -> {
                drain(type.powerCost(), pilot);
                shieldDischarge(pilot);
                yield true;
            }
            case IMPULSE_LASER -> {
                this.submarine.setLaserActive(true);
                yield true;
            }
            case TELEPORT -> beginTeleport(slot, pilot);
            case SOLAR, ARMOR -> false;
        };
    }

    private void solarTick() {
        if (this.submarine.tickCount % SOLAR_INTERVAL != 0 || !this.submarine.hasModule(SubmarineModuleType.SOLAR)) {
            return;
        }

        Level level = this.submarine.level();
        if (!level.isBrightOutside() || !level.canSeeSkyFromBelowWater(this.submarine.blockPosition())) {
            return;
        }

        this.submarine.setPowerStored(this.submarine.getPowerStored() + solarGain());
    }

    public static int solarGain() {
        return Mth.ceil(NTConfig.submarinePowerCapacity * NTConfig.submarineSolarPercentPer5s / 100D);
    }

    private void updateSpeedMultiplier() {
        float multiplier = 1F;
        if (this.boostTicks > 0) {
            multiplier *= 1F + (float) NTConfig.submarineBoostSpeedBonus;
        }
        if (this.stealthTicks > 0) {
            multiplier *= 1F - (float) NTConfig.submarineStealthSpeedPenalty;
        }
        this.submarine.setSpeedMultiplier(multiplier);
    }

    private void dropAggro() {
        for (Mob mob : nearbyMobs(STEALTH_AGGRO_RANGE)) {
            LivingEntity target = mob.getTarget();
            if (target == this.submarine || (target != null && target.getVehicle() == this.submarine)) {
                mob.setTarget(null);
            }
        }
    }

    private List<Mob> nearbyMobs(double radius) {
        return this.submarine.level().getEntitiesOfClass(Mob.class, this.submarine.getBoundingBox().inflate(radius));
    }

    private void sonarPing() {
        for (Entity passenger : this.submarine.getPassengers()) {
            if (passenger instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, SONAR_NIGHT_VISION_TICKS, 0, true, false));
            }
        }

        double radius = NTConfig.submarineSonarRadius;
        List<LivingEntity> hostiles = this.submarine.level().getEntitiesOfClass(LivingEntity.class,
                this.submarine.getBoundingBox().inflate(radius), living -> living instanceof Enemy);
        for (LivingEntity hostile : hostiles) {
            hostile.addEffect(new MobEffectInstance(MobEffects.GLOWING, SONAR_GLOW_TICKS, 0, true, false));
        }

        play(NTSounds.SUBMARINE_SONAR_PING.get(), 1F, 1F);

        Vec3 center = this.submarine.position();
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(this.submarine, new SonarPingPayload(
                this.submarine.getId(), center.x, center.y, center.z, SONAR_PULSE_RANGE, NTConfig.submarineSonarCooldownTicks));
    }

    private void shieldDischarge(Player pilot) {
        double radius = NTConfig.submarineShieldRadius;
        play(NTSounds.SUBMARINE_SHIELD_PULSE.get(), 1F, 1F);
        if (this.submarine.level() instanceof ServerLevel serverLevel) {
            Vec3 center = this.submarine.position();
            serverLevel.sendParticles(NTParticles.SHIELD_RING.get(), center.x, center.y + 1D, center.z, 1, 0D, 0D, 0D, 0D);
        }

        List<LivingEntity> targets = this.submarine.level().getEntitiesOfClass(LivingEntity.class,
                this.submarine.getBoundingBox().inflate(radius),
                living -> living != this.submarine && !this.submarine.hasPassenger(living));

        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(NTMobEffects.STUNNED, NTConfig.submarineShieldStunTicks, 0, false, true));
            target.knockback(SHIELD_KNOCKBACK,
                    this.submarine.getX() - target.getX(),
                    this.submarine.getZ() - target.getZ());
            target.hurt(this.submarine.damageSources().indirectMagic(this.submarine, pilot),
                    (float) NTConfig.submarineShieldDamage);
        }
    }

    private void laserTick() {
        if (!this.submarine.isLaserActive()) {
            return;
        }

        if (!this.submarine.hasModule(SubmarineModuleType.IMPULSE_LASER) || this.submarine.getPassengers().isEmpty()) {
            this.submarine.setLaserActive(false);
            return;
        }

        Vec3 forward = this.submarine.getForward();
        Vec3 right = new Vec3(0D, 1D, 0D).cross(forward).normalize();
        boolean damageTick = this.submarine.tickCount % LASER_DAMAGE_INTERVAL == 0;

        if (damageTick && !drainForLaser()) {
            this.submarine.setLaserActive(false);
            return;
        }

        float leftLength = fireBeam(forward, right, -MUZZLE_SIDE, damageTick);
        float rightLength = fireBeam(forward, right, MUZZLE_SIDE, damageTick);
        this.submarine.setLaserLengths(leftLength, rightLength);
    }

    private boolean drainForLaser() {
        if (this.submarine.getControllingPassenger() instanceof Player pilot && pilot.gameMode().isCreative()) {
            return true;
        }

        int cost = NTConfig.submarineLaserPowerCost;
        if (this.submarine.getPowerStored() < cost) {
            return false;
        }

        this.submarine.setPowerStored(this.submarine.getPowerStored() - cost);
        return true;
    }

    private float fireBeam(Vec3 forward, Vec3 right, double side, boolean damageTick) {
        Vec3 origin = this.submarine.position()
                .add(forward.scale(MUZZLE_FORWARD))
                .add(right.scale(side))
                .add(0D, MUZZLE_UP, 0D);
        double range = NTConfig.submarineLaserRange;
        Vec3 end = origin.add(forward.scale(range));

        BlockHitResult blockHit = this.submarine.level().clip(new ClipContext(origin, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        AABB sweep = new AABB(origin, end).inflate(0.5D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(this.submarine,
                origin, end, sweep, entity -> entity.isPickable() && !this.submarine.hasPassenger(entity), range * range);

        Vec3 hit = entityHit != null ? entityHit.getLocation() : end;
        if (damageTick && entityHit != null && entityHit.getEntity() instanceof LivingEntity target) {
            float damage = (float) (NTConfig.submarineLaserDamage
                    + target.getMaxHealth() * NTConfig.submarineLaserHealthPercent);
            Player pilot = this.submarine.getControllingPassenger() instanceof Player player ? player : null;
            target.hurt(this.submarine.damageSources().indirectMagic(this.submarine, pilot), damage);
        }

        return (float) origin.distanceTo(hit);
    }

    private boolean beginTeleport(int slot, Player pilot) {
        TeleportAnchor anchor = TeleportModuleItem.anchorOf(this.submarine.getModule(slot));
        if (anchor == null) {
            refuse(pilot, "not_bound");
            return false;
        }

        int minimum = (int) (NTConfig.submarinePowerCapacity * NTConfig.submarineTeleportMinPowerPercent);
        if (!pilot.gameMode().isCreative() && this.submarine.getPowerStored() < minimum) {
            refuse(pilot, "no_power");
            return false;
        }

        ServerLevel destination = resolveDestination(anchor);
        if (destination == null || !isDestinationClear(destination, anchor)) {
            refuse(pilot, "destination_blocked");
            return false;
        }

        drain(NTConfig.submarineTeleportPowerCost, pilot);
        this.teleportTarget = anchor;
        this.teleportTicks = TELEPORT_CHARGE_TICKS;
        this.submarine.setCharging(true);

        Vec3 position = this.submarine.position();
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(this.submarine, new TeleportFxPayload(
                this.submarine.getId(), TeleportFxPayload.STAGE_CHARGE,
                position.x, position.y, position.z, this.submarine.getYRot(), TELEPORT_CHARGE_TICKS));
        return true;
    }

    private void teleportTick() {
        if (this.teleportTicks <= 0) {
            return;
        }

        if (--this.teleportTicks > 0) {
            return;
        }

        this.submarine.setCharging(false);
        TeleportAnchor anchor = this.teleportTarget;
        this.teleportTarget = null;
        if (anchor == null) {
            return;
        }

        ServerLevel destination = resolveDestination(anchor);
        if (destination == null || !isDestinationClear(destination, anchor)) {
            abortTeleport();
            return;
        }

        Vec3 target = Vec3.atCenterOf(anchor.pos().pos());
        Entity teleported = this.submarine.teleport(new TeleportTransition(destination, target, Vec3.ZERO,
                anchor.yaw(), 0F, TeleportTransition.DO_NOTHING));
        if (teleported == null) {
            abortTeleport();
            return;
        }

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(teleported, new TeleportFxPayload(
                teleported.getId(), TeleportFxPayload.STAGE_ARRIVE,
                target.x, target.y, target.z, anchor.yaw(), TELEPORT_CHARGE_TICKS));
    }

    private void abortTeleport() {
        Vec3 position = this.submarine.position();
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(this.submarine, new TeleportFxPayload(
                this.submarine.getId(), TeleportFxPayload.STAGE_ABORT,
                position.x, position.y, position.z, this.submarine.getYRot(), 0));

        if (this.submarine.getControllingPassenger() instanceof Player pilot) {
            refuse(pilot, "destination_blocked");
        }
    }

    private @Nullable ServerLevel resolveDestination(TeleportAnchor anchor) {
        return this.submarine.level().getServer() == null
                ? null
                : this.submarine.level().getServer().getLevel(anchor.pos().dimension());
    }

    private boolean isDestinationClear(ServerLevel destination, TeleportAnchor anchor) {
        BlockPos pos = anchor.pos().pos();
        if (!destination.getFluidState(pos).is(FluidTags.WATER)) {
            return false;
        }

        Vec3 target = Vec3.atCenterOf(pos);
        AABB hull = this.submarine.getBoundingBox().move(target.subtract(this.submarine.position()));
        return destination.noCollision(this.submarine, hull);
    }

    private void drain(int amount, Player pilot) {
        if (pilot.gameMode().isCreative()) {
            return;
        }
        this.submarine.setPowerStored(this.submarine.getPowerStored() - amount);
    }

    private void play(SoundEvent sound, float volume, float pitch) {
        this.submarine.level().playSound(null, this.submarine, sound, SoundSource.PLAYERS, volume, pitch);
    }

    private void refuse(Player pilot, String reason) {
        pilot.sendOverlayMessage(Component.translatable("nautec.submarine.ability." + reason)
                .withStyle(ChatFormatting.RED));
    }

    private void broadcast(SubmarineCooldownPayload payload) {
        for (Entity passenger : this.submarine.getPassengers()) {
            if (passenger instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }
}
