package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.Nautec;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.function.IntFunction;

public enum SeaEyeTarget implements StringRepresentable {
    CRYSTAL_GEODES("crystal_geodes"),
    NAUTEC_RUINS("nautec_ruins"),
    GATEWAYS("gateways"),
    OCEAN_RUINS("ocean_ruins"),
    OCEAN_MONUMENTS("ocean_monuments");

    public static final Codec<SeaEyeTarget> CODEC = StringRepresentable.fromEnum(SeaEyeTarget::values);
    private static final IntFunction<SeaEyeTarget> BY_ID =
            ByIdMap.continuous(SeaEyeTarget::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, SeaEyeTarget> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, SeaEyeTarget::ordinal);

    private final String name;
    private final TagKey<Structure> structures;

    SeaEyeTarget(String name) {
        this.name = name;
        this.structures = TagKey.create(Registries.STRUCTURE, Nautec.rl("eye_of_the_sea/" + name));
    }

    public TagKey<Structure> structures() {
        return structures;
    }

    public SeaEyeTarget next() {
        return BY_ID.apply(ordinal() + 1);
    }

    public Component displayName() {
        return Component.translatable("nautec.eye_of_the_sea.target." + name);
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
