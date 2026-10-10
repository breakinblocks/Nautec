package com.breakinblocks.nautec.content.entities.mobs;

import com.breakinblocks.nautec.registries.NTSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public class AbyssalMaw extends Monster {
    public AbyssalMaw(EntityType<? extends AbyssalMaw> type, Level level) {
        super(type, level);
        this.moveControl = new SwimmingMoveControl(this);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.9)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BiteGoal(this));
        this.goalSelector.addGoal(3, new RandomSwimmingGoal(this, 0.8, 20));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
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
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return NTSounds.ABYSSAL_MAW_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NTSounds.ABYSSAL_MAW_DEATH.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource source) {
        return NTSounds.ABYSSAL_MAW_HURT.get();
    }

    @Override
    protected void playAttackSound() {
        this.playSound(NTSounds.ABYSSAL_MAW_ATTACK.get(), 0.8F, this.getVoicePitch());
    }

    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    private static class BiteGoal extends MeleeAttackGoal {
        BiteGoal(AbyssalMaw maw) {
            super(maw, 1.0, true);
        }

        @Override
        public void tick() {
            super.tick();
            LivingEntity target = this.mob.getTarget();
            if (target != null && this.mob.getNavigation().isDone() && !this.mob.isWithinMeleeAttackRange(target)) {
                this.mob.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0);
            }
        }
    }
}
