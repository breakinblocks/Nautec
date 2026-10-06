package com.breakinblocks.nautec.content.resonantstorage;

import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record ResonantChannel(ChannelAccess access, UUID owner, GatewayAddress address) {
    public static final UUID NOBODY = new UUID(0L, 0L);
    public static final ResonantChannel PUBLIC_DEFAULT = new ResonantChannel(ChannelAccess.PUBLIC, NOBODY, GatewayAddress.DEFAULT);

    public static final Codec<ResonantChannel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ChannelAccess.CODEC.fieldOf("access").forGetter(ResonantChannel::access),
            UUIDUtil.CODEC.fieldOf("owner").forGetter(ResonantChannel::owner),
            GatewayAddress.CODEC.fieldOf("address").forGetter(ResonantChannel::address)
    ).apply(instance, ResonantChannel::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResonantChannel> STREAM_CODEC = StreamCodec.composite(
            ChannelAccess.STREAM_CODEC, ResonantChannel::access,
            UUIDUtil.STREAM_CODEC, ResonantChannel::owner,
            GatewayAddress.STREAM_CODEC, ResonantChannel::address,
            ResonantChannel::new
    );

    public ResonantChannel {
        if (access == ChannelAccess.PUBLIC) {
            owner = NOBODY;
        }
    }

    public static ResonantChannel privateTo(UUID player) {
        return new ResonantChannel(ChannelAccess.PRIVATE, player, GatewayAddress.DEFAULT);
    }

    public ResonantChannel withAddress(GatewayAddress address) {
        return new ResonantChannel(access, owner, address);
    }
}
