package com.breakinblocks.nautec.client.sound;

import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.registries.NTSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;

public class SubmarineLaserChargeSound extends AbstractTickableSoundInstance {
    public static final int DURATION_TICKS = 60;

    private final SubmarineEntity submarine;

    public SubmarineLaserChargeSound(SubmarineEntity submarine) {
        super(NTSounds.ATLANTEAN_RIFLE_CHARGE.get(), SoundSource.PLAYERS, submarine.getRandom());
        this.submarine = submarine;
        this.looping = false;
        this.delay = 0;
        this.volume = 1.0F;
        this.x = submarine.getX();
        this.y = submarine.getY();
        this.z = submarine.getZ();
    }

    @Override
    public void tick() {
        if (this.submarine.isRemoved()
                || !this.submarine.isLaserEngaged()
                || this.submarine.getLaserTicks() >= DURATION_TICKS) {
            stop();
            return;
        }

        this.x = this.submarine.getX();
        this.y = this.submarine.getY();
        this.z = this.submarine.getZ();
    }
}
