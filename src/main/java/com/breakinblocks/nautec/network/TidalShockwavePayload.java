package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.shockwave.TidalShockwaves;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TidalShockwavePayload(double x, double y, double z, float radius) implements CustomPacketPayload {
    public static final Type<TidalShockwavePayload> TYPE = new Type<>(Nautec.rl("tidal_shockwave"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TidalShockwavePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, TidalShockwavePayload::x,
            ByteBufCodecs.DOUBLE, TidalShockwavePayload::y,
            ByteBufCodecs.DOUBLE, TidalShockwavePayload::z,
            ByteBufCodecs.FLOAT, TidalShockwavePayload::radius,
            TidalShockwavePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TidalShockwavePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> TidalShockwaves.begin(new Vec3(payload.x(), payload.y(), payload.z()), payload.radius()));
    }
}
