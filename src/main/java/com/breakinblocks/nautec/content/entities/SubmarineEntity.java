package com.breakinblocks.nautec.content.entities;

import net.minecraft.world.inventory.ChestMenu;
import com.breakinblocks.nautec.content.entities.submarine.SubmarineCargoContainer;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.EntityPowerStorage;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.content.entities.submarine.SubmarineInput;
import com.breakinblocks.nautec.content.entities.submarine.SubmarineModules;
import com.breakinblocks.nautec.content.items.submarine.SubmarineModuleItem;
import com.breakinblocks.nautec.content.items.submarine.SubmarineModuleType;
import com.breakinblocks.nautec.content.menus.SubmarineModuleMenu;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.SubmarineModuleState;
import com.breakinblocks.nautec.data.components.ComponentPowerStorage;
import com.breakinblocks.nautec.registries.NTCriteriaTriggers;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTParticles;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import com.breakinblocks.nautec.utils.valueio.TagValueInput;
import com.breakinblocks.nautec.utils.valueio.TagValueOutput;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.fluids.FluidType;
import java.util.Collections;

public class SubmarineEntity extends LivingEntity implements GeoEntity {
    private boolean laserHeld;
    private static final double SHOVE_STRENGTH = 2.5;
    public static final int MAX_PASSENGERS = 2;
    public static final float MODEL_Y_OFFSET = 3F / 16F;
    public static final float MODEL_Z_OFFSET = 2.5F / 16F;
    public static final double PORTAL_DISTANCE = 6D;
    public static final double PORTAL_AXIS_HEIGHT = 1D;
    public static final double EXIT_PORTAL_DISTANCE = 5.5D;
    public static final int EXIT_TICKS = 16;
    public static final float MODEL_SCALE = 4.5F;

    private static final double DRIVER_SEAT_Z = -0.5D / 16D * MODEL_SCALE;
    private static final double PASSENGER_SEAT_Z = -4.5D / 16D * MODEL_SCALE;
    private static final double RIDE_HEIGHT = (1D / 16D + MODEL_Y_OFFSET) * MODEL_SCALE;

    public static final DataTicket<Boolean> DEPLOYED = new DataTicket<>("nautec:submarine_deployed", Boolean.class);

    private static final RawAnimation DEPLOY = RawAnimation.begin().thenPlayAndHold("deploy");
    private static final RawAnimation STOWED = RawAnimation.begin().thenLoop("idle");
    private static final int STOW_TRANSITION_TICKS = 20;

    public static final int MODULE_SLOTS = 9;
    public static final int CARGO_ROWS_PER_MODULE = 3;
    public static final int CARGO_MAX_MODULES = 2;
    public static final int CARGO_CAPACITY = CARGO_ROWS_PER_MODULE * 9 * CARGO_MAX_MODULES;
    private static final double CARGO_HATCH_DEPTH = 1.0D;
    private static final double CARGO_HATCH_CONE = 0.5D;

    private static final EntityDataAccessor<Integer> DATA_POWER =
            SynchedEntityData.defineId(SubmarineEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> DATA_SPEED_MULTIPLIER =
            SynchedEntityData.defineId(SubmarineEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_STEALTHED =
            SynchedEntityData.defineId(SubmarineEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CHARGING =
            SynchedEntityData.defineId(SubmarineEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_EXITING =
            SynchedEntityData.defineId(SubmarineEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_LASER_TICKS =
            SynchedEntityData.defineId(SubmarineEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_LASER_LEFT =
            SynchedEntityData.defineId(SubmarineEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_LASER_RIGHT =
            SynchedEntityData.defineId(SubmarineEntity.class, EntityDataSerializers.FLOAT);

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
    private static final double MOVEMENT_EPSILON = 1.0E-4;

    private static final double AGGRO_TRANSFER_RANGE = 32D;
    private static final double PORTAL_PULL_PEAK = 0.36D;
    private static final double EXIT_PUSH = 0.09D;
    private static final ResourceLocation TOUGHNESS_MODIFIER = Nautec.rl("submarine_armor_module_toughness");
    private static final ResourceLocation KNOCKBACK_MODIFIER = Nautec.rl("submarine_armor_module_knockback");

    private final AnimatableInstanceCache animatableCache = GeckoLibUtil.createInstanceCache(this);
    private final SubmarineModules modules = new SubmarineModules(this);
    private final NonNullList<ItemStack> cargo = NonNullList.withSize(CARGO_CAPACITY, ItemStack.EMPTY);
    private final IPowerStorage powerStorage = new EntityPowerStorage(this::getPowerStored, this::setPowerStored,
            NTConfig.submarinePowerCapacity, 200, 0);

    private SubmarineInput input = SubmarineInput.EMPTY;
    private boolean freeLook;
    private boolean steeringLast;
    private @Nullable Vec3 portalTarget;
    private int chargeAge;
    private int exitAge;
    private static final double FLIGHT_DRAG = 0.86D;
    private static final double FLIGHT_SETTLE = 0.01D;
    private static final double FLIGHT_SETTLE_SPEED = 0.12D;
    private static final double MAX_SAFE_SPEED = 1.5D;
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

    private void rebaseAttribute(Holder<Attribute> attribute, double value) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_POWER, 0);
        entityData.define(DATA_SPEED_MULTIPLIER, 1F);
        entityData.define(DATA_STEALTHED, false);
        entityData.define(DATA_CHARGING, false);
        entityData.define(DATA_EXITING, false);
        entityData.define(DATA_LASER_TICKS, -1);
        entityData.define(DATA_LASER_LEFT, 0F);
        entityData.define(DATA_LASER_RIGHT, 0F);
        for (EntityDataAccessor<ItemStack> accessor : DATA_MODULES) {
            entityData.define(accessor, ItemStack.EMPTY);
        }
    }

    public float getSpeedMultiplier() {
        return this.entityData.get(DATA_SPEED_MULTIPLIER);
    }

    public void setSpeedMultiplier(float multiplier) {
        this.entityData.set(DATA_SPEED_MULTIPLIER, multiplier);
    }

    public boolean isStealthed() {
        return this.entityData.get(DATA_STEALTHED);
    }

    public void setStealthed(boolean stealthed) {
        this.entityData.set(DATA_STEALTHED, stealthed);
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_CHARGING);
    }

    public void setCharging(boolean charging) {
        this.entityData.set(DATA_CHARGING, charging);
    }

    public boolean isExiting() {
        return this.entityData.get(DATA_EXITING);
    }

    public void setExiting(boolean exiting) {
        this.entityData.set(DATA_EXITING, exiting);
    }

    public static Vec3 portalCenter(Vec3 position, float yaw, float pitch) {
        return position.add(0D, PORTAL_AXIS_HEIGHT, 0D).add(Vec3.directionFromRotation(pitch, yaw).scale(PORTAL_DISTANCE));
    }

    public static Vec3 exitPortalCenter(Vec3 position, float yaw, float pitch) {
        return position.add(0D, PORTAL_AXIS_HEIGHT, 0D).add(Vec3.directionFromRotation(pitch, yaw).scale(-EXIT_PORTAL_DISTANCE));
    }

    public void setPortalTarget(Vec3 target) {
        this.portalTarget = target;
    }

    public int getLaserTicks() {
        return this.entityData.get(DATA_LASER_TICKS);
    }

    public void setLaserTicks(int ticks) {
        this.entityData.set(DATA_LASER_TICKS, ticks);
    }

    public boolean isLaserEngaged() {
        return getLaserTicks() >= 0;
    }

    public boolean isLaserCharging() {
        int ticks = getLaserTicks();
        return ticks >= 0 && ticks < NTConfig.submarineLaserChargeTicks;
    }

    public boolean isLaserActive() {
        return getLaserTicks() >= NTConfig.submarineLaserChargeTicks;
    }

    public float getLaserFiringTicks(float partialTick) {
        return isLaserEngaged() ? getLaserTicks() + partialTick - NTConfig.submarineLaserChargeTicks : Float.NEGATIVE_INFINITY;
    }

    public static float laserRamp(float firingTicks) {
        return Mth.clamp(firingTicks / Math.max(1, NTConfig.submarineLaserRampTicks), 0F, 1F);
    }

    public boolean isLaserHeld() {
        return this.laserHeld;
    }

    public void setLaserHeld(boolean held) {
        this.laserHeld = held;
    }

    public void stopLaser() {
        this.laserHeld = false;
        if (getLaserTicks() != -1) {
            setLaserTicks(-1);
        }
        setLaserLengths(0F, 0F);
    }

    public float getLaserLength(boolean left) {
        return this.entityData.get(left ? DATA_LASER_LEFT : DATA_LASER_RIGHT);
    }

    public void setLaserLengths(float left, float right) {
        this.entityData.set(DATA_LASER_LEFT, left);
        this.entityData.set(DATA_LASER_RIGHT, right);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ValueOutput output = TagValueOutput.wrap(registryAccess(), tag);
        output.putInt("power", getPowerStored());
        output.store("modules", ItemContainerContents.CODEC, ItemContainerContents.fromItems(getModuleStacks()));
        output.store("cargo", ItemContainerContents.CODEC, ItemContainerContents.fromItems(cargo));
        this.modules.save(output);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ValueInput input = TagValueInput.create(registryAccess(), tag);
        setPowerStored(input.getIntOr("power", 0));
        setModules(input.read("modules", ItemContainerContents.CODEC).orElse(ItemContainerContents.EMPTY));
        setCargo(input.read("cargo", ItemContainerContents.CODEC).orElse(ItemContainerContents.EMPTY));
        this.modules.load(input);
    }

    public ItemStack getModule(int slot) {
        return this.entityData.get(DATA_MODULES.get(slot));
    }

    public void setModule(int slot, ItemStack stack) {
        this.entityData.set(DATA_MODULES.get(slot), stack, true);
        refreshModuleAttributes();
    }

    public void refreshModuleAttributes() {
        if (level().isClientSide()) {
            return;
        }

        boolean armored = hasModule(SubmarineModuleType.ARMOR);
        applyModuleModifier(Attributes.ARMOR_TOUGHNESS, TOUGHNESS_MODIFIER,
                armored ? NTConfig.submarineArmorModuleToughness - NTConfig.submarineArmorToughness : 0D);
        applyModuleModifier(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_MODIFIER, armored ? 1D : 0D);
    }

    private void applyModuleModifier(Holder<Attribute> attribute, ResourceLocation id, double amount) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance == null) {
            return;
        }

        instance.removeModifier(id);
        if (amount != 0D) {
            instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
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

    public int getCargoSlots() {
        int modules = 0;
        for (int slot = 0; slot < MODULE_SLOTS; slot++) {
            if (getModuleType(slot) == SubmarineModuleType.CARGO) {
                modules++;
            }
        }
        return Math.min(CARGO_MAX_MODULES, modules) * CARGO_ROWS_PER_MODULE * 9;
    }

    public NonNullList<ItemStack> getCargo() {
        return cargo;
    }

    public void setCargo(ItemContainerContents contents) {
        for (int slot = 0; slot < CARGO_CAPACITY; slot++) {
            cargo.set(slot, ItemStack.EMPTY);
        }
        contents.copyInto(cargo);
    }

    public static boolean canStoreInCargo(ItemStack stack) {
        return !stack.is(NTItems.SUBMARINE.get());
    }

    public boolean openCargo(ServerPlayer player) {
        int slots = getCargoSlots();
        if (slots <= 0) {
            return false;
        }
        SubmarineCargoContainer container = new SubmarineCargoContainer(this, cargo, slots);
        player.openMenu(new SimpleMenuProvider((containerId, inventory, opener) -> slots > CARGO_ROWS_PER_MODULE * 9
                ? ChestMenu.sixRows(containerId, inventory, container)
                : ChestMenu.threeRows(containerId, inventory, container),
                Component.translatable("nautec.submarine.cargo")));
        return true;
    }

    public boolean isBehind(Vec3 offset) {
        Vec3 forward = Vec3.directionFromRotation(0F, getYRot());
        double flat = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
        return flat > CARGO_HATCH_DEPTH && offset.x * forward.x + offset.z * forward.z < -flat * CARGO_HATCH_CONE;
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

    public void setInput(SubmarineInput input) {
        this.input = input;
    }

    public void setFreeLook(boolean freeLook) {
        this.freeLook = freeLook;
    }

    public void setDescending(boolean descending) {
        this.descending = descending;
    }

    public boolean isDeployed() {
        return !this.getPassengers().isEmpty() || this.isInWater();
    }

    public boolean isUnderWay() {
        return this.underWay;
    }

    public boolean isSealed() {
        return isDeployed() && getPowerStored() > 0;
    }

    @Override
    public void tick() {
        if (getControllingPassenger() instanceof ServerPlayer driver) {
            this.input = new SubmarineInput(driver.zza > 0F, driver.zza < 0F, driver.xxa > 0F, driver.xxa < 0F,
                    false, driver.isShiftKeyDown(), driver.isSprinting());
        }

        if (isCharging()) {
            this.chargeAge++;
        } else {
            this.chargeAge = 0;
            this.portalTarget = null;
        }

        if (isExiting()) {
            this.exitAge++;
            if (!level().isClientSide() && this.exitAge >= EXIT_TICKS) {
                setExiting(false);
            }
        } else {
            this.exitAge = 0;
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
        } else if (this.underWay && isInWater()) {
            spawnWake();
        } else if (isFlying()) {
            spawnLiftJets();
        }
    }

    @Override
    public void travel(Vec3 relative) {
        pilot();
        move(MoverType.SELF, SubmarineCollision.clampMotion(level(), this, position(), getDeltaMovement(), getYRot(), getXRot()));
        shoveAside();
    }

    private void shoveAside() {
        if (level().isClientSide()) {
            return;
        }

        Vec3 motion = getDeltaMovement();
        double speed = motion.length();
        if (speed < 1.0E-3) {
            return;
        }

        List<Entity> caught = level().getEntities(this, getBoundingBox().inflate(0.2),
                entity -> !entity.isPassengerOfSameVehicle(this) && !hasPassenger(entity) && entity.isPushable());

        for (Entity entity : caught) {
            Vec3 away = entity.position().subtract(position());
            Vec3 push = away.horizontalDistanceSqr() < 1.0E-4
                    ? motion.normalize()
                    : new Vec3(away.x, away.y * 0.4, away.z).normalize();

            entity.push(push.x * speed * SHOVE_STRENGTH,
                    push.y * speed * SHOVE_STRENGTH + 0.05,
                    push.z * speed * SHOVE_STRENGTH);
            entity.hurtMarked = true;
        }
    }

    public static double maxSpeed(boolean flying) {
        return flying
                ? Math.min(MAX_SAFE_SPEED, NTConfig.submarineMaxSpeed * NTConfig.submarineFlightSpeedMultiplier)
                : NTConfig.submarineMaxSpeed;
    }

    public boolean canFly() {
        return hasModule(SubmarineModuleType.FLIGHT) && getPowerStored() > 0;
    }

    public boolean isFlying() {
        return !isInWater() && !onGround() && canFly();
    }

    private void pilot() {
        LivingEntity driver = getControllingPassenger();
        boolean submerged = isInWater();
        boolean flying = !submerged && canFly();
        Vec3 motion = getDeltaMovement();

        boolean charging = isCharging();
        if (driver != null) {
            boolean steering = !this.freeLook && !charging;
            if (steering) {
                aimSteer(driver, submerged || flying);
            }
            this.steeringLast = steering;
            if (!submerged && !flying && !charging) {
                levelOut();
            }
            setYHeadRot(getYRot());
            setYBodyRot(getYRot());
        } else if (!charging) {
            levelOut();
        }

        if (charging) {
            setDeltaMovement(portalPull());
            return;
        }

        if (isExiting() && this.exitAge < EXIT_TICKS) {
            motion = motion.add(getForward().scale(EXIT_PUSH * (1D - (double) this.exitAge / EXIT_TICKS)));
        }

        if (driver != null && getPowerStored() > 0) {
            SubmarineInput controls = this.input;
            float throttle = 0F;
            if (controls.forward()) {
                throttle += 1F;
            }
            if (controls.backward()) {
                throttle -= 0.5F;
            }

            if (throttle != 0F) {
                double speed = NTConfig.submarineSpeed * getSpeedMultiplier() * (controls.sprint() ? 1.6D : 1D);
                if (flying) {
                    speed *= NTConfig.submarineFlightSpeedMultiplier;
                } else if (!submerged) {
                    speed *= 0.35D;
                }
                motion = motion.add(getForward().scale(throttle * speed));
            }

            double climb = submerged || flying ? NTConfig.submarineSpeed : NTConfig.submarineSpeed * 0.4D;
            if (controls.jump()) {
                motion = motion.add(0D, climb, 0D);
            }

            if (this.descending) {
                motion = motion.add(0D, -climb, 0D);
            }
        }

        if (submerged) {
            motion = motion.scale(0.86D);
            double buoyancy = isUnderWater() ? 0.002D : -0.01D;
            motion = motion.add(0D, buoyancy, 0D);
        } else if (flying) {
            motion = motion.scale(FLIGHT_DRAG);
            if (driver == null) {
                motion = new Vec3(motion.x, Math.max(motion.y - FLIGHT_SETTLE, -FLIGHT_SETTLE_SPEED), motion.z);
            }
        } else {
            motion = motion.multiply(0.94D, 0.98D, 0.94D);
            if (!onGround()) {
                motion = motion.add(0D, -0.04D, 0D);
            }
        }

        double maxSpeed = maxSpeed(flying);
        if (motion.lengthSqr() > maxSpeed * maxSpeed) {
            motion = motion.normalize().scale(maxSpeed);
        }

        setDeltaMovement(motion);
    }

    private Vec3 portalPull() {
        if (this.portalTarget == null) {
            this.portalTarget = portalCenter(position(), getYRot(), getXRot());
        }

        float progress = Mth.clamp((float) this.chargeAge / SubmarineModules.TELEPORT_CHARGE_TICKS, 0F, 1F);
        Vec3 toPortal = this.portalTarget.subtract(position().add(0D, PORTAL_AXIS_HEIGHT, 0D));
        double distance = toPortal.length();
        if (distance < 1.0E-3D) {
            return Vec3.ZERO;
        }
        return toPortal.scale(Math.min(distance, PORTAL_PULL_PEAK * progress * progress) / distance);
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

        if (this.tickCount % 20 == 0 && isFlying() && getControllingPassenger() instanceof ServerPlayer pilot) {
            NTCriteriaTriggers.SUBMARINE_FLIGHT.get().trigger(pilot);
        }

        if (getPassengers().isEmpty()) {
            return;
        }

        boolean creativePilot = getControllingPassenger() instanceof Player pilot && pilot.isCreative();

        int drain = NTConfig.submarineIdlePowerUsage;
        if (this.underWay) {
            drain += NTConfig.submarineMovePowerUsage;
        }
        if (isFlying()) {
            drain += NTConfig.submarineFlightPowerUsage;
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

    private void spawnLiftJets() {
        if (this.random.nextInt(2) != 0) {
            return;
        }
        Vec3 forward = getForward();
        Vec3 side = new Vec3(-forward.z, 0D, forward.x).normalize().scale(1.6D);
        for (Vec3 offset : new Vec3[]{side, side.reverse()}) {
            Vec3 at = position().add(offset).add(forward.scale(-1.5D)).add(0D, 0.2D, 0D);
            level().addParticle(NTParticles.BOOST_TRAIL.get(), at.x, at.y, at.z,
                    this.random.nextGaussian() * 0.01D, -0.18D, this.random.nextGaussian() * 0.01D);
        }
    }

    private void spawnWake() {
        Vec3 stern = position().subtract(getForward().scale(4.5D)).add(0D, 0.4D, 0D);
        boolean boosting = getSpeedMultiplier() > 1F;

        if (this.random.nextInt(boosting ? 1 : 3) == 0) {
            level().addParticle(NTParticles.THRUSTER_WAKE.get(), stern.x, stern.y, stern.z, 0D, 0.02D, 0D);
        }

        if (!boosting) {
            return;
        }

        Vec3 back = getForward().scale(-1D);
        for (int i = 0; i < 2; i++) {
            Vec3 spread = new Vec3(this.random.nextGaussian(), this.random.nextGaussian(), this.random.nextGaussian()).scale(0.25D);
            Vec3 at = stern.add(spread);
            level().addParticle(NTParticles.BOOST_TRAIL.get(), at.x, at.y, at.z,
                    back.x * 0.12D, back.y * 0.12D, back.z * 0.12D);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        InteractionResult result = super.interact(player, hand);
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
                serverPlayer.openMenu(moduleMenu(), buf -> buf.writeVarInt(getId()));
            }
            return InteractionResult.SUCCESS;
        }

        if (getCargoSlots() > 0 && isBehind(player.position().subtract(position()))) {
            if (player instanceof ServerPlayer serverPlayer) {
                openCargo(serverPlayer);
            }
            return InteractionResult.SUCCESS;
        }

        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        return player.startRiding(this) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private MenuProvider moduleMenu() {
        return new SimpleMenuProvider(
                (containerId, inventory, player) -> new SubmarineModuleMenu(containerId, inventory, this),
                Component.translatable("nautec.submarine.modules"));
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
    public boolean hurt(DamageSource source, float damage) {
        if (level().isClientSide()) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (isRemoved() || (attacker != null && hasPassenger(attacker))) {
            return false;
        }

        if (source.isCreativePlayer()) {
            ejectPassengers();
            spawnAtLocation(toStack());
            gameEvent(GameEvent.ENTITY_PLACE, attacker);
            discard();
            return true;
        }

        return super.hurt(source, damage);
    }

    @Override
    public void die(DamageSource source) {
        if (this.dead || isRemoved()) {
            return;
        }

        this.dead = true;
        ejectPassengers();

        if (level() instanceof ServerLevel serverLevel && !source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            spawnAtLocation(toStack());
        }

        gameEvent(GameEvent.ENTITY_DIE);
        discard();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return super.isInvulnerableTo(source)
                || source.is(DamageTypeTags.IS_DROWNING)
                || source.is(DamageTypeTags.IS_FREEZING)
                || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.CRAMMING);
    }

    @Override
    public void heal(float amount) {
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageModifier, DamageSource source) {
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
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
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
        syncPassengers();
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
        syncPassengers();
        if (getPassengers().isEmpty()) {
            this.input = SubmarineInput.EMPTY;
            this.freeLook = false;
            this.descending = false;
        }
    }

    private void syncPassengers() {
        if (level() instanceof ServerLevel server) {
            // Vanilla filters newly mounted/dismounted players from the tick's update.
            // Multiple seat changes in one tick can otherwise leave their view stale.
            server.getChunkSource().broadcast(this, new ClientboundSetPassengersPacket(this));
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
    public boolean isNoGravity() {
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
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return (entity.canBeCollidedWith() || entity.isPushable()) && !isPassengerOfSameVehicle(entity);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canBeRiddenUnderFluidType(FluidType type, Entity rider) {
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
        stack.set(NTDataComponents.SUBMARINE_MODULE_STATE, modules.snapshot());
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(getModuleStacks()));
        boolean anyCargo = cargo.stream().anyMatch(item -> !item.isEmpty());
        if (anyCargo) {
            stack.set(NTDataComponents.SUBMARINE_CARGO, ItemContainerContents.fromItems(cargo));
        }
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
        setCargo(stack.getOrDefault(NTDataComponents.SUBMARINE_CARGO, ItemContainerContents.EMPTY));
        modules.restore(stack.getOrDefault(NTDataComponents.SUBMARINE_MODULE_STATE, SubmarineModuleState.EMPTY));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        boolean[] posed = {false};

        controllers.add(new AnimationController<SubmarineEntity>(this, "canopy", 0, state -> {
            state.getController().transitionLength(posed[0] ? STOW_TRANSITION_TICKS : 0);
            posed[0] = true;
            return state.setAndContinue(state.getAnimatable().isDeployed() ? DEPLOY : STOWED);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animatableCache;
    }
}
