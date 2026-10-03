package com.breakinblocks.nautec.content.resonance;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record ResonanceBinding(UUID network, String name) {
    public static final Codec<ResonanceBinding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("network").forGetter(ResonanceBinding::network),
            Codec.STRING.fieldOf("name").forGetter(ResonanceBinding::name)
    ).apply(instance, ResonanceBinding::new));

    public static final StreamCodec<ByteBuf, ResonanceBinding> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ResonanceBinding::network,
            ByteBufCodecs.STRING_UTF8, ResonanceBinding::name,
            ResonanceBinding::new
    );
}
