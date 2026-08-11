package com.breakinblocks.nautec.content.entities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.EntityPowerStorage;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ComponentPowerStorage;
import com.breakinblocks.nautec.registries.NTItems;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.InstancedAnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.constant.dataticket.DataTicket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SubmarineEntity extends VehicleEntity implements GeoEntity {
    public static final int MAX_PASSENGERS = 2;
    public static final float MODEL_Y_OFFSET = 3F / 16F;
    public static final float MODEL_Z_OFFSET = 2.5F / 16F;
    public static final float MODEL_SCALE = 4.5F;

    private static final double DRIVER_SEAT_Z = -0.5D / 16D * MODEL_SCALE;
    private static final double PASSENGER_SEAT_Z = -4.5D / 16D * MODEL_SCALE;
    private static final double RIDE_HEIGHT = (1D / 16D + MODEL_Y_OFFSET) * MODEL_SCALE;

    public static final DataTicket<Boolean> DEPLOYED = DataTicket.create("nautec:submarine_deployed", Boolean.class);

    private static final RawAnimation DEPLOY = RawAnimation.begin().thenPlayAndHold("deploy");
    private static final RawAnimation STOWED = RawAnimation.begin().thenLoop("idle");
    private static final int STOW_TRANSITION_TICKS = 20;

    private static final EntityDataAccessor<Integer> DATA_POWER =
            SynchedEntityData.defineId(SubmarineEntity.class, EntityDataSerializers.INT);

    private static final float MAX_PITCH = 75F;
    private static final float PASSENGER_HEAD_YAW = 85F;
    private static final float PITCH_LEVEL_RATE = 6F;
    private static final float YAW_RATE = 2.5F;
    private static final double MOVEMENT_EPSILON = 1.0E-4;

    private final InterpolationHandler interpolation = new InterpolationHandler(this, 3);
    private final AnimatableInstanceCache animatableCache = new InstancedAnimatableInstanceCache(this);
    private final IPowerStorage powerStorage = new EntityPowerStorage(this::getPowerStored, this::setPowerStored,
            NTConfig.submarinePowerCapacity, 200, 0);

    private Input input = Input.EMPTY;
    private boolean steering;
    private boolean steeringLast;
    private boolean descending;
    private float lastDriverYaw;
    private float lastDriverPitch;
    private boolean underWay;
    private boolean posTracked;
    private double lastTickX;
    private double lastTickY;
    private double lastTickZ;

    public SubmarineEntity(EntityType<? extends SubmarineEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_POWER, 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("power", getPowerStored());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        setPowerStored(input.getIntOr("power", 0));
    }

    public int getPowerStored() {
        return this.entityData.get(DATA_POWER);
    }

    public void setPowerStored(int power) {
        this.entityData.set(DATA_POWER, Mth.clamp(power, 0, NTConfig.submarinePowerCapacity));
    }

    public IPowerStorage getPowerStorage() {
        return this.powerStorage;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public void setSteering(boolean steering) {
        this.steering = steering;
    }

    public void setDescending(boolean descending) {
        this.descending = descending;
    }

    public boolean isDeployed() {
        return !this.getPassengers().isEmpty() || this.isInWater();
    }

    public boolean isSealed() {
        return isDeployed() && getPowerStored() > 0;
    }

    @Override
    public void tick() {
        if (getHurtTime() > 0) {
            setHurtTime(getHurtTime() - 1);
        }

        if (getDamage() > 0F) {
            setDamage(getDamage() - 1F);
        }

        if (getControllingPassenger() instanceof ServerPlayer driver) {
            this.input = driver.getLastClientInput();
        }

        super.tick();
        this.interpolation.interpolate();

        if (isLocalInstanceAuthoritative()) {
            pilot();
            move(MoverType.SELF, SubmarineCollision.clampMotion(level(), this, position(), getDeltaMovement(), getYRot(), getXRot()));
        } else {
            setDeltaMovement(Vec3.ZERO);
        }

        if (!this.posTracked) {
            this.lastTickX = getX();
            this.lastTickY = getY();
            this.lastTickZ = getZ();
            this.posTracked = true;
        }
        this.underWay = distanceToSqr(this.lastTickX, this.lastTickY, this.lastTickZ) > MOVEMENT_EPSILON;
        this.lastTickX = getX();
        this.lastTickY = getY();
        this.lastTickZ = getZ();

        if (!level().isClientSide()) {
            tickServer();
        } else if (this.underWay && isUnderWater() && this.random.nextInt(3) == 0) {
            spawnWake();
        }
    }

    private void pilot() {
        LivingEntity driver = getControllingPassenger();
        boolean submerged = isInWater();
        Vec3 motion = getDeltaMovement();

        if (driver != null) {
            if (this.steering) {
                aimSteer(driver, submerged);
            }
            this.steeringLast = this.steering;
            rudder(driver);
            if (!submerged) {
                levelOut();
            }
            setYHeadRot(getYRot());
            setYBodyRot(getYRot());
        } else {
            levelOut();
        }

        if (driver != null && getPowerStored() > 0) {
            Input controls = this.input;
            float throttle = 0F;
            if (controls.forward()) {
                throttle += 1F;
            }
            if (controls.backward()) {
                throttle -= 0.5F;
            }

            if (throttle != 0F) {
                double speed = NTConfig.submarineSpeed * (controls.sprint() ? 1.6D : 1D);
                if (!submerged) {
                    speed *= 0.35D;
                }
                motion = motion.add(getForward().scale(throttle * speed));
            }

            if (controls.jump()) {
                motion = motion.add(0D, submerged ? NTConfig.submarineSpeed : NTConfig.submarineSpeed * 0.4D, 0D);
            }

            if (this.descending) {
                motion = motion.add(0D, submerged ? -NTConfig.submarineSpeed : -NTConfig.submarineSpeed * 0.4D, 0D);
            }
        }

        if (submerged) {
            motion = motion.scale(0.86D);
            double buoyancy = isUnderWater() ? 0.002D : -0.01D;
            motion = motion.add(0D, buoyancy, 0D);
        } else {
            motion = motion.multiply(0.94D, 0.98D, 0.94D);
            if (!onGround()) {
                motion = motion.add(0D, -0.04D, 0D);
            }
        }

        double maxSpeed = NTConfig.submarineMaxSpeed;
        if (motion.lengthSqr() > maxSpeed * maxSpeed) {
            motion = motion.normalize().scale(maxSpeed);
        }

        setDeltaMovement(motion);
    }

    private void levelOut() {
        if (getXRot() != 0F) {
            setXRot(getXRot() - Mth.clamp(getXRot(), -PITCH_LEVEL_RATE, PITCH_LEVEL_RATE));
        }
    }

    private void aimSteer(LivingEntity driver, boolean submerged) {
        if (!this.steeringLast) {
            this.lastDriverYaw = driver.getYRot();
            this.lastDriverPitch = driver.getXRot();
        }

        float yawDelta = Mth.wrapDegrees(driver.getYRot() - this.lastDriverYaw);
        float pitchDelta = driver.getXRot() - this.lastDriverPitch;

        float targetYaw = getYRot() + yawDelta;
        float targetPitch = submerged ? Mth.clamp(getXRot() + pitchDelta, -MAX_PITCH, MAX_PITCH) : getXRot();
        if (!SubmarineCollision.blocked(level(), this, position(), targetYaw, targetPitch)) {
            setYRot(targetYaw);
            setXRot(targetPitch);
        }

        driver.setYRot(getYRot());
        driver.setXRot(getXRot());

        this.lastDriverYaw = getYRot();
        this.lastDriverPitch = getXRot();
    }

    private void rudder(LivingEntity driver) {
        float yaw = 0F;
        if (this.input.left()) {
            yaw -= YAW_RATE;
        }
        if (this.input.right()) {
            yaw += YAW_RATE;
        }
        if (yaw != 0F && !SubmarineCollision.blocked(level(), this, position(), getYRot() + yaw, getXRot())) {
            setYRot(getYRot() + yaw);
            driver.setYRot(driver.getYRot() + yaw);
            if (this.steering) {
                this.lastDriverYaw = Mth.wrapDegrees(this.lastDriverYaw + yaw);
            }
        }
    }

    private void seatRotation(Entity passenger) {
        passenger.setYBodyRot(getYRot());
        if (passenger instanceof LivingEntity living) {
            living.yBodyRotO = this.yRotO;
        }

        if (passenger == getControllingPassenger()) {
            passenger.setYHeadRot(getYRot());
            if (passenger instanceof LivingEntity living) {
                living.yHeadRotO = this.yRotO;
            }
            return;
        }

        float offset = Mth.clamp(Mth.wrapDegrees(passenger.getYRot() - getYRot()), -PASSENGER_HEAD_YAW, PASSENGER_HEAD_YAW);
        passenger.setYHeadRot(getYRot() + offset);
    }

    private void tickServer() {
        if (getPassengers().isEmpty()) {
            return;
        }

        boolean creativePilot = getControllingPassenger() instanceof Player pilot && pilot.gameMode().isCreative();

        int drain = NTConfig.submarineIdlePowerUsage;
        if (this.underWay) {
            drain += NTConfig.submarineMovePowerUsage;
        }

        if (isSealed()) {
            drain += NTConfig.submarineOxygenPowerUsage;
            for (Entity passenger : getPassengers()) {
                if (passenger instanceof LivingEntity living) {
                    living.setAirSupply(living.getMaxAirSupply());
                }
            }
        }

        if (!creativePilot) {
            setPowerStored(getPowerStored() - drain);
        }
    }

    private void spawnWake() {
        Vec3 stern = position().subtract(getForward().scale(4.5D)).add(0D, 0.4D, 0D);
        level().addParticle(ParticleTypes.BUBBLE, stern.x, stern.y, stern.z, 0D, 0.02D, 0D);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        InteractionResult result = super.interact(player, hand, location);
        if (result != InteractionResult.PASS) {
            return result;
        }

        if (player.isSecondaryUseActive()) {
            if (!getPassengers().isEmpty()) {
                return InteractionResult.PASS;
            }
            if (level().isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            retrieve(player);
            return InteractionResult.SUCCESS;
        }

        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        return player.startRiding(this) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private void retrieve(Player player) {
        ItemStack stack = toStack();
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        gameEvent(GameEvent.ENTITY_PLACE, player);
        discard();
    }

    @Override
    public boolean hurtClient(DamageSource source) {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().size() < MAX_PASSENGERS;
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof LivingEntity driver ? driver : super.getControllingPassenger();
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        double forward = getPassengers().indexOf(passenger) == 0 ? DRIVER_SEAT_Z : PASSENGER_SEAT_Z;
        return new Vec3(0D, RIDE_HEIGHT, forward).yRot(-getYRot() * ((float) Math.PI / 180F));
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        super.positionRider(passenger, moveFunction);
        seatRotation(passenger);
    }

    @Override
    public void onPassengerTurned(Entity passenger) {
        seatRotation(passenger);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (getPassengers().isEmpty()) {
            this.input = Input.EMPTY;
            this.steering = false;
            this.descending = false;
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 sideways = getCollisionHorizontalEscapeVector(getBbWidth() * Mth.SQRT_OF_TWO, passenger.getBbWidth(), passenger.getYRot());
        Vec3 target = new Vec3(getX() + sideways.x, getBoundingBox().maxY, getZ() + sideways.z);

        for (Pose pose : passenger.getDismountPoses()) {
            if (DismountHelper.canDismountTo(level(), target, passenger, pose)) {
                passenger.setPose(pose);
                return target;
            }
        }

        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    public @Nullable InterpolationHandler getInterpolation() {
        return this.interpolation;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    public boolean isFlyingVehicle() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public float getPickRadius() {
        return 3.5F;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity other) {
        return true;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return (entity.canBeCollidedWith(this) || entity.isPushable()) && !isPassengerOfSameVehicle(entity);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canBeRiddenUnderFluidType(net.neoforged.neoforge.fluids.FluidType type, Entity rider) {
        return true;
    }

    @Override
    protected Item getDropItem() {
        return NTItems.SUBMARINE.get();
    }

    @Override
    public ItemStack getPickResult() {
        return toStack();
    }

    @Override
    protected void destroy(ServerLevel level, DamageSource source) {
        kill(level);
        if (level.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.ENTITY_DROPS)) {
            spawnAtLocation(level, toStack());
        }
    }

    public ItemStack toStack() {
        ItemStack stack = new ItemStack(NTItems.SUBMARINE.get());
        stack.set(NTDataComponents.POWER, new ComponentPowerStorage(getPowerStored(), NTConfig.submarinePowerCapacity, 1F));
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, getCustomName());
        return stack;
    }

    public void applyStack(ItemStack stack) {
        IPowerStorage stored = stack.getCapability(NTCapabilities.PowerStorage.ITEM);
        if (stored != null) {
            setPowerStored(stored.getPowerStored());
        }
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, net.minecraft.world.level.block.state.BlockState onState, net.minecraft.core.BlockPos pos) {
        if (onGround) {
            resetFallDistance();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        boolean[] posed = {false};

        controllers.add(new AnimationController<SubmarineEntity>("canopy", 0, state -> {
            state.controller().setTransitionTicks(posed[0] ? STOW_TRANSITION_TICKS : 0);
            posed[0] = true;
            return state.setAndContinue(state.getDataOrDefault(DEPLOYED, false) ? DEPLOY : STOWED);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animatableCache;
    }
}
