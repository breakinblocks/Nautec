package com.breakinblocks.nautec.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class MachineSounds {
    private MachineSounds() {
    }

    public static void interval(@Nullable Level level, BlockPos pos, Holder<SoundEvent> sound, int period,
                                float volume, float pitch) {
        if (level == null || level.isClientSide() || period <= 0) {
            return;
        }
        if (Math.floorMod(level.getGameTime(), period) != Math.floorMod(pos.asLong(), period)) {
            return;
        }
        play(level, pos, sound, volume, pitch);
    }

    public static void play(@Nullable Level level, BlockPos pos, Holder<SoundEvent> sound, float volume, float pitch) {
        if (level == null || level.isClientSide()) {
            return;
        }
        level.playSound(null, pos, sound.value(), SoundSource.BLOCKS, volume, pitch);
    }
}
