package com.breakinblocks.nautec.data.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TeleportAnchor(GlobalPos pos, float yaw) {
    public static final Codec<TeleportAnchor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(TeleportAnchor::pos),
            Codec.FLOAT.optionalFieldOf("yaw", 0F).forGetter(TeleportAnchor::yaw)
    ).apply(instance, TeleportAnchor::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportAnchor> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, TeleportAnchor::pos,
            ByteBufCodecs.FLOAT, TeleportAnchor::yaw,
            TeleportAnchor::new);
}
