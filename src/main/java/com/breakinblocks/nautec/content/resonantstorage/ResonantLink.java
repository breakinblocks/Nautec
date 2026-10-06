package com.breakinblocks.nautec.content.resonantstorage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ResonantLink(ResonantChannel channel, String ownerName) {
    public static final Codec<ResonantLink> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResonantChannel.CODEC.fieldOf("channel").forGetter(ResonantLink::channel),
            Codec.STRING.optionalFieldOf("owner_name", "").forGetter(ResonantLink::ownerName)
    ).apply(instance, ResonantLink::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResonantLink> STREAM_CODEC = StreamCodec.composite(
            ResonantChannel.STREAM_CODEC, ResonantLink::channel,
            ByteBufCodecs.STRING_UTF8, ResonantLink::ownerName,
            ResonantLink::new
    );
}
