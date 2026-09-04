package com.breakinblocks.nautec.client.sound;

import com.breakinblocks.nautec.content.items.AtlanteanRifleItem;
import com.breakinblocks.nautec.registries.NTSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class AtlanteanRifleChargeSound extends AbstractTickableSoundInstance {
    public static final int DURATION_TICKS = 60;

    private final Player shooter;

    public AtlanteanRifleChargeSound(Player shooter) {
        super(NTSounds.ATLANTEAN_RIFLE_CHARGE.get(), SoundSource.PLAYERS, shooter.getRandom());
        this.shooter = shooter;
        this.looping = false;
        this.delay = 0;
        this.volume = 1.0F;
        this.x = shooter.getX();
        this.y = shooter.getY();
        this.z = shooter.getZ();
    }

    @Override
    public void tick() {
        if (this.shooter.isRemoved()
                || !AtlanteanRifleItem.isUsing(this.shooter)
                || this.shooter.getTicksUsingItem() >= DURATION_TICKS) {
            stop();
            return;
        }

        this.x = this.shooter.getX();
        this.y = this.shooter.getY();
        this.z = this.shooter.getZ();
    }
}
