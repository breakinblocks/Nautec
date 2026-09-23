package com.breakinblocks.nautec.content.entities.mobs;

import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.fish.AbstractSchoolingFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SiltSkipper extends AbstractSchoolingFish {
    public SiltSkipper(EntityType<? extends SiltSkipper> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSchoolingFish.createAttributes()
                .add(Attributes.MAX_HEALTH, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 1.0);
    }

    @Override
    public @NotNull ItemStack getBucketItemStack() {
        return new ItemStack(NTItems.SILT_SKIPPER_BUCKET.get());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return NTSounds.SILT_SKIPPER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NTSounds.SILT_SKIPPER_DEATH.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource source) {
        return NTSounds.SILT_SKIPPER_HURT.get();
    }

    @Override
    protected @NotNull SoundEvent getFlopSound() {
        return NTSounds.SILT_SKIPPER_FLOP.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.35F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    public int getMaxSchoolSize() {
        return 8;
    }
}
