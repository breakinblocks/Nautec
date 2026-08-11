package com.breakinblocks.nautec.client.sound;

import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.registries.NTSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class SubmarineLaserSound extends AbstractTickableSoundInstance {
    private static final float FADE = 0.2F;

    private final SubmarineEntity submarine;

    public SubmarineLaserSound(SubmarineEntity submarine) {
        super(NTSounds.SUBMARINE_LASER_LOOP.get(), SoundSource.PLAYERS, submarine.getRandom());
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
        if (this.submarine.isRemoved() || (!this.submarine.isLaserActive() && this.volume <= 0F)) {
            stop();
            return;
        }

        this.x = this.submarine.getX();
        this.y = this.submarine.getY();
        this.z = this.submarine.getZ();
        this.volume = Mth.approach(this.volume, this.submarine.isLaserActive() ? 0.9F : 0F, FADE);
    }
}
