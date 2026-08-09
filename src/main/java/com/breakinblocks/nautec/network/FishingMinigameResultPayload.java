package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.entities.NautecFishingHook;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FishingMinigameResultPayload(long nonce, List<Integer> ticks) implements CustomPacketPayload {
    public static final Type<FishingMinigameResultPayload> TYPE = new Type<>(Nautec.rl("fishing_minigame_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FishingMinigameResultPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, FishingMinigameResultPayload::nonce,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list(4)), FishingMinigameResultPayload::ticks,
            FishingMinigameResultPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FishingMinigameResultPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.fishing instanceof NautecFishingHook hook) {
                hook.onMinigameReport(payload.nonce(), payload.ticks());
            }
        });
    }
}
