package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.breakinblocks.nautec.client.SubmarineFxHooks;

public record SonarPingPayload(int entityId, double x, double y, double z, float range, int highlightTicks) implements CustomPacketPayload {
    public static final Type<SonarPingPayload> TYPE = new Type<>(Nautec.rl("sonar_ping"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SonarPingPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SonarPingPayload::entityId,
            ByteBufCodecs.DOUBLE, SonarPingPayload::x,
            ByteBufCodecs.DOUBLE, SonarPingPayload::y,
            ByteBufCodecs.DOUBLE, SonarPingPayload::z,
            ByteBufCodecs.FLOAT, SonarPingPayload::range,
            ByteBufCodecs.VAR_INT, SonarPingPayload::highlightTicks,
            SonarPingPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SonarPingPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> SubmarineFxHooks.onSonarPing(payload));
    }
}
