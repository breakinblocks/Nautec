package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.breakinblocks.nautec.client.SubmarineFxHooks;

public record TeleportFxPayload(int entityId, int stage, double x, double y, double z, float yaw, int ticks) implements CustomPacketPayload {
    public static final int STAGE_CHARGE = 0;
    public static final int STAGE_ARRIVE = 1;
    public static final int STAGE_ABORT = 2;

    public static final Type<TeleportFxPayload> TYPE = new Type<>(Nautec.rl("teleport_fx"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportFxPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TeleportFxPayload::entityId,
            ByteBufCodecs.VAR_INT, TeleportFxPayload::stage,
            ByteBufCodecs.DOUBLE, TeleportFxPayload::x,
            ByteBufCodecs.DOUBLE, TeleportFxPayload::y,
            ByteBufCodecs.DOUBLE, TeleportFxPayload::z,
            ByteBufCodecs.FLOAT, TeleportFxPayload::yaw,
            ByteBufCodecs.VAR_INT, TeleportFxPayload::ticks,
            TeleportFxPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TeleportFxPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> SubmarineFxHooks.onTeleportFx(payload));
    }
}
