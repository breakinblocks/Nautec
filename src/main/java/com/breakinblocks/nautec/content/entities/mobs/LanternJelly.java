package com.breakinblocks.nautec.content.entities.mobs;

import com.breakinblocks.nautec.registries.NTSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public class LanternJelly extends WaterAnimal {
    public static final float SWIM_PULSE_SPEED = 0.09F;

    public LanternJelly(EntityType<? extends LanternJelly> type, Level level) {
        super(type, level);
        this.moveControl = new SwimmingMoveControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RandomSwimmingGoal(this, 0.6, 12));
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return type != NeoForgeMod.WATER_TYPE.value() && super.canDrownInFluidType(type);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return NTSounds.LANTERN_JELLY_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NTSounds.LANTERN_JELLY_DEATH.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource source) {
        return NTSounds.LANTERN_JELLY_HURT.get();
    }

    public static float swimPulse(float ageInTicks) {
        return Mth.sin(SWIM_PULSE_SPEED * ageInTicks);
    }

    @Override
    public void tick() {
        super.tick();
        // Use the same client tick clock as the bell and emissive layer, once per stroke.
        if (this.level().isClientSide() && this.isAlive() && this.isInWater() && !this.isSilent()
                && swimPulse(this.tickCount - 1) < 0.8F && swimPulse(this.tickCount) >= 0.8F) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), NTSounds.LANTERN_JELLY_PULSE.get(),
                    this.getSoundSource(), 0.16F, this.getVoicePitch(), false);
        }
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 280;
    }
}
