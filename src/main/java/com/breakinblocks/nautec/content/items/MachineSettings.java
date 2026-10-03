package com.breakinblocks.nautec.content.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record MachineSettings(Identifier block, Identifier blockEntityType, CompoundTag data) {
    public static final Codec<MachineSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("block").forGetter(MachineSettings::block),
            Identifier.CODEC.fieldOf("block_entity_type").forGetter(MachineSettings::blockEntityType),
            CompoundTag.CODEC.fieldOf("data").forGetter(MachineSettings::data)
    ).apply(instance, MachineSettings::new));

    public static final StreamCodec<ByteBuf, MachineSettings> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, MachineSettings::block,
            Identifier.STREAM_CODEC, MachineSettings::blockEntityType,
            ByteBufCodecs.COMPOUND_TAG, MachineSettings::data,
            MachineSettings::new
    );
}
