package com.breakinblocks.nautec.client.sound;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.registries.NTSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class SubmarineEngineSound extends AbstractTickableSoundInstance {
    private static final float FADE = 0.08F;

    private final SubmarineEntity submarine;

    public SubmarineEngineSound(SubmarineEntity submarine) {
        super(NTSounds.SUBMARINE_ENGINE.get(), SoundSource.NEUTRAL, submarine.getRandom());
        this.submarine = submarine;
        this.looping = true;
        this.delay = 0;
        this.volume = 0F;
        this.x = submarine.getX();
        this.y = submarine.getY();
        this.z = submarine.getZ();
    }

    @Override
    public void tick() {
        if (this.submarine.isRemoved() || Minecraft.getInstance().isPaused()) {
            if (this.submarine.isRemoved()) {
                stop();
            }
            return;
        }

        this.x = this.submarine.getX();
        this.y = this.submarine.getY();
        this.z = this.submarine.getZ();

        double speed = this.submarine.getDeltaMovement().length();
        float target = this.submarine.isUnderWay() && this.submarine.getPowerStored() > 0 ? 1F : 0F;
        this.volume = Mth.approach(this.volume, target, FADE);
        this.pitch = 0.65F + 0.5F * (float) Mth.clamp(speed / NTConfig.submarineMaxSpeed, 0D, 1D);
    }
}
