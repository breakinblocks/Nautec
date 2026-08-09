package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenFishingMinigamePayload(int kind, long nonce, long seed) implements CustomPacketPayload {
    public static final Type<OpenFishingMinigamePayload> TYPE = new Type<>(Nautec.rl("open_fishing_minigame"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenFishingMinigamePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OpenFishingMinigamePayload::kind,
            ByteBufCodecs.VAR_LONG, OpenFishingMinigamePayload::nonce,
            ByteBufCodecs.VAR_LONG, OpenFishingMinigamePayload::seed,
            OpenFishingMinigamePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenFishingMinigamePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.breakinblocks.nautec.client.screen.FishingMinigameScreen.open(payload.kind(), payload.nonce(), payload.seed()));
    }
}
