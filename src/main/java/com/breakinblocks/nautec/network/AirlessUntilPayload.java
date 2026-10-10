package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.data.NTDataAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AirlessUntilPayload(long until) implements CustomPacketPayload {
    public static final Type<AirlessUntilPayload> TYPE = new Type<>(Nautec.rl("airless_until"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AirlessUntilPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, AirlessUntilPayload::until,
            AirlessUntilPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AirlessUntilPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> context.player().setData(NTDataAttachments.AIRLESS_UNTIL, payload.until()));
    }
}
