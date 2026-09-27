package com.breakinblocks.nautec.data.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public record ShockwaveCooldown(long readyAt, int duration) {
    public static final Codec<ShockwaveCooldown> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("ready_at").forGetter(ShockwaveCooldown::readyAt),
            Codec.INT.fieldOf("duration").forGetter(ShockwaveCooldown::duration)
    ).apply(instance, ShockwaveCooldown::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShockwaveCooldown> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, ShockwaveCooldown::readyAt,
            ByteBufCodecs.VAR_INT, ShockwaveCooldown::duration,
            ShockwaveCooldown::new);

    public boolean isReady(long gameTime) {
        return gameTime >= readyAt;
    }

    public float remainingFraction(long gameTime, float partialTick) {
        if (duration <= 0) {
            return 0.0F;
        }
        return Mth.clamp((readyAt - gameTime - partialTick) / duration, 0.0F, 1.0F);
    }
}
