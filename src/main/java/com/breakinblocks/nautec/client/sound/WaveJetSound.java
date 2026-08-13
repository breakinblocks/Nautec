package com.breakinblocks.nautec.client.sound;

import com.breakinblocks.nautec.registries.NTSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class WaveJetSound extends AbstractTickableSoundInstance {
    private static final float FADE = 0.15F;
    private static final double REFERENCE_SPEED = 0.6D;

    private final Player player;

    public WaveJetSound(Player player) {
        super(NTSounds.WAVE_JET_LOOP.get(), SoundSource.PLAYERS, player.getRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0F;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public void tick() {
        if (this.player.isRemoved()) {
            stop();
            return;
        }

        if (Minecraft.getInstance().isPaused()) {
            return;
        }

        this.x = this.player.getX();
        this.y = this.player.getY();
        this.z = this.player.getZ();

        boolean running = WaveJetSoundHandler.isRunning(this.player);
        this.volume = Mth.approach(this.volume, running ? 1F : 0F, FADE);
        if (!running && this.volume <= 0.01F) {
            stop();
            return;
        }

        double speed = this.player.getDeltaMovement().length();
        this.pitch = 1.3F + 0.4F * (float) Mth.clamp(speed / REFERENCE_SPEED, 0D, 1D);
    }
}
