package com.breakinblocks.nautec.client.sound;

import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.registries.NTSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class SubmarineAmbientSound extends AbstractTickableSoundInstance {
    private static final float FADE = 0.05F;

    private final SubmarineEntity submarine;

    public SubmarineAmbientSound(SubmarineEntity submarine) {
        super(NTSounds.SUBMARINE_AMBIENT.get(), SoundSource.AMBIENT, submarine.getRandom());
        this.submarine = submarine;
        this.looping = true;
        this.delay = 0;
        this.volume = 0F;
        this.relative = true;
        this.attenuation = Attenuation.NONE;
    }

    @Override
    public void tick() {
        if (this.submarine.isRemoved()) {
            stop();
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        boolean aboard = minecraft.player != null && minecraft.player.getVehicle() == this.submarine;
        float target = aboard && this.submarine.isSealed() ? 1F : 0F;
        this.volume = Mth.approach(this.volume, target, FADE);
    }
}
