package com.breakinblocks.nautec.content.entities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.EntityPowerStorage;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.content.entities.submarine.SubmarineModules;
import com.breakinblocks.nautec.content.items.submarine.SubmarineModuleItem;
import com.breakinblocks.nautec.content.items.submarine.SubmarineModuleType;
import com.breakinblocks.nautec.content.menus.SubmarineModuleMenu;
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
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SubmarineEntity extends LivingEntity implements GeoEntity, MenuProvider {
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

    public static final int MODULE_SLOTS = 9;

    private static final EntityDataAccessor<Integer> DATA_POWER =
            SynchedEntityData.defineId(SubmarineEntity.class, EntityDataSerializers.INT);

    private static final List<EntityDataAccessor<ItemStack>> DATA_MODULES = defineModuleSlots();

    private static List<EntityDataAccessor<ItemStack>> defineModuleSlots() {
        List<EntityDataAccessor<ItemStack>> accessors = new ArrayList<>(MODULE_SLOTS);
        for (int i = 0; i < MODULE_SLOTS; i++) {
            accessors.add(SynchedEntityData.defineId(SubmarineEntity.class, EntityDataSerializers.ITEM_STACK));
        }
        return List.copyOf(accessors);
    }

    private static final float MAX_PITCH = 75F;
    private static final float PASSENGER_HEAD_YAW = 85F;
    private static final float PITCH_LEVEL_RATE = 6F;
    private static final float YAW_RATE = 2.5F;
    private static final double MOVEMENT_EPSILON = 1.0E-4;

    private static final double AGGRO_TRANSFER_RANGE = 32D;

    private final AnimatableInstanceCache animatableCache = new InstancedAnimatableInstanceCache(this);
    private final SubmarineModules modules = new SubmarineModules(this);
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
        rebaseAttribute(Attributes.MAX_HEALTH, NTConfig.submarineMaxHealth);
        rebaseAttribute(Attributes.ARMOR, NTConfig.submarineArmor);
        rebaseAttribute(Attributes.ARMOR_TOUGHNESS, NTConfig.submarineArmorToughness);
        rebaseAttribute(Attributes.KNOCKBACK_RESISTANCE, NTConfig.submarineKnockbackResistance);
        setHealth(getMaxHealth());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, NTConfig.submarineMaxHealth)
                .add(Attributes.ARMOR, NTConfig.submarineArmor)
                .add(Attributes.ARMOR_TOUGHNESS, NTConfig.submarineArmorToughness)
                .add(Attributes.KNOCKBACK_RESISTANCE, NTConfig.submarineKnockbackResistance)
                .add(Attributes.MOVEMENT_SPEED, 0D);
    }

    private void rebaseAttribute(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double value) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_POWER, 0);
        for (EntityDataAccessor<ItemStack> accessor : DATA_MODULES) {
            entityData.define(accessor, ItemStack.EMPTY);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("power", getPowerStored());
        output.store("modules", ItemContainerContents.CODEC, ItemContainerContents.fromItems(getModuleStacks()));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setPowerStored(input.getIntOr("power", 0));
        setModules(input.read("modules", ItemContainerContents.CODEC).orElse(ItemContainerContents.EMPTY));
    }

    public ItemStack getModule(int slot) {
        return this.entityData.get(DATA_MODULES.get(slot));
    }

    public void setModule(int slot, ItemStack stack) {
        this.entityData.set(DATA_MODULES.get(slot), stack, true);
    }

    public List<ItemStack> getModuleStacks() {
        List<ItemStack> stacks = new ArrayList<>(MODULE_SLOTS);
        for (int slot = 0; slot < MODULE_SLOTS; slot++) {
            stacks.add(getModule(slot));
        }
        return stacks;
    }

    public void setModules(ItemContainerContents contents) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(MODULE_SLOTS, ItemStack.EMPTY);
        contents.copyInto(stacks);
        for (int slot = 0; slot < MODULE_SLOTS; slot++) {
            setModule(slot, stacks.get(slot));
        }
    }

    public SubmarineModules getModules() {
        return this.modules;
    }

    public @Nullable SubmarineModuleType getModuleType(int slot) {
        return SubmarineModuleItem.typeOf(getModule(slot));
    }

    public boolean hasModule(SubmarineModuleType type) {
        for (int slot = 0; slot < MODULE_SLOTS; slot++) {
            if (getModuleType(slot) == type) {
                return true;
            }
        }
        return false;
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
        if (getControllingPassenger() instanceof ServerPlayer driver) {
            this.input = driver.getLastClientInput();
        }

        super.tick();

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

    @Override
    public void travel(Vec3 relative) {
        pilot();
        move(MoverType.SELF, SubmarineCollision.clampMotion(level(), this, position(), getDeltaMovement(), getYRot(), getXRot()));
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
        autorepair();
        this.modules.tickServer();

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

    private void autorepair() {
        int interval = NTConfig.submarineAutorepairIntervalTicks;
        if (interval <= 0 || this.tickCount % interval != 0) {
            return;
        }

        float health = getHealth();
        if (health <= 0F || health >= getMaxHealth()) {
            return;
        }

        setHealth(health + (float) (getMaxHealth() * NTConfig.submarineAutorepairPercent));
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

        if (player.getItemInHand(hand).is(Tags.Items.TOOLS_WRENCH)) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(this, buf -> buf.writeVarInt(getId()));
            }
            return InteractionResult.SUCCESS;
        }

        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        return player.startRiding(this) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public Component getDisplayName() {
        return hasCustomName() ? super.getDisplayName() : Component.translatable("nautec.submarine.modules");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SubmarineModuleMenu(containerId, inventory, this);
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
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        Entity attacker = source.getEntity();
        if (isRemoved() || (attacker != null && hasPassenger(attacker))) {
            return false;
        }

        if (source.isCreativePlayer()) {
            ejectPassengers();
            spawnAtLocation(level, toStack());
            gameEvent(GameEvent.ENTITY_PLACE, attacker);
            discard();
            return true;
        }

        return super.hurtServer(level, source, damage);
    }

    @Override
    public void die(DamageSource source) {
        if (this.dead || isRemoved()) {
            return;
        }

        this.dead = true;
        ejectPassengers();

        if (level() instanceof ServerLevel serverLevel && !source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            spawnAtLocation(serverLevel, toStack());
        }

        gameEvent(GameEvent.ENTITY_DIE);
        discard();
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return super.isInvulnerableTo(level, source)
                || source.is(DamageTypeTags.IS_DROWNING)
                || source.is(DamageTypeTags.IS_FREEZING)
                || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.CRAMMING);
    }

    @Override
    public void heal(float amount) {
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource source) {
        resetFallDistance();
        return false;
    }

    @Override
    public boolean isAffectedByPotions() {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return false;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().size() < MAX_PASSENGERS;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (level().isClientSide() || !(passenger instanceof LivingEntity boarded)) {
            return;
        }

        if (passenger instanceof ServerPlayer player) {
            this.modules.sendCooldownSnapshot(player);
        }

        List<Mob> nearby = level().getEntitiesOfClass(Mob.class, getBoundingBox().inflate(AGGRO_TRANSFER_RANGE),
                mob -> mob.getTarget() == boarded);
        for (Mob mob : nearby) {
            mob.setTarget(this);
        }
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
    public ItemStack getPickResult() {
        return toStack();
    }

    public ItemStack toStack() {
        ItemStack stack = new ItemStack(NTItems.SUBMARINE.get());
        stack.set(NTDataComponents.POWER, new ComponentPowerStorage(getPowerStored(), NTConfig.submarinePowerCapacity, 1F));
        stack.set(NTDataComponents.SUBMARINE_HEALTH, getHealth());
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(getModuleStacks()));
        stack.set(DataComponents.CUSTOM_NAME, getCustomName());
        return stack;
    }

    public void applyStack(ItemStack stack) {
        IPowerStorage stored = stack.getCapability(NTCapabilities.PowerStorage.ITEM);
        if (stored != null) {
            setPowerStored(stored.getPowerStored());
        }

        Float health = stack.get(NTDataComponents.SUBMARINE_HEALTH);
        setHealth(health == null ? getMaxHealth() : Math.max(1F, health));

        setModules(stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
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
