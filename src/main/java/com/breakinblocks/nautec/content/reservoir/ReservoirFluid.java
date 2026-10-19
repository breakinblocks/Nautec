package com.breakinblocks.nautec.content.reservoir;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public record ReservoirFluid(FluidResource resource, long amount) {
    public static final int MAX_TIER = 4;
    public static final ReservoirFluid EMPTY = new ReservoirFluid(FluidResource.EMPTY, 0);

    public static final Codec<ReservoirFluid> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidResource.OPTIONAL_CODEC.fieldOf("fluid").forGetter(ReservoirFluid::resource),
            Codec.LONG.fieldOf("amount").forGetter(ReservoirFluid::amount)
    ).apply(instance, ReservoirFluid::of));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReservoirFluid> STREAM_CODEC = StreamCodec.composite(
            FluidResource.STREAM_CODEC, ReservoirFluid::resource,
            ByteBufCodecs.VAR_LONG, ReservoirFluid::amount,
            ReservoirFluid::of);

    public static ReservoirFluid of(FluidResource resource, long amount) {
        return resource.isEmpty() || amount <= 0 ? EMPTY : new ReservoirFluid(resource, amount);
    }

    public boolean isEmpty() {
        return resource.isEmpty() || amount <= 0;
    }
}
