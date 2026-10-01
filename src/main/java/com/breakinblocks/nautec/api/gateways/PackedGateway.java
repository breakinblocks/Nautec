package com.breakinblocks.nautec.api.gateways;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PackedGateway(boolean wild) {
    public static final PackedGateway CRAFTED = new PackedGateway(false);

    public static final Codec<PackedGateway> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("wild", false).forGetter(PackedGateway::wild)
    ).apply(instance, PackedGateway::new));

    public static final StreamCodec<ByteBuf, PackedGateway> STREAM_CODEC =
            ByteBufCodecs.BOOL.map(PackedGateway::new, PackedGateway::wild);
}
