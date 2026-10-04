package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.menus.EnergyConverterMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetConverterRatePayload(int containerId, int rate) implements CustomPacketPayload {
    public static final Type<SetConverterRatePayload> TYPE = new Type<>(Nautec.rl("set_converter_rate"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetConverterRatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetConverterRatePayload::containerId,
            ByteBufCodecs.VAR_INT, SetConverterRatePayload::rate,
            SetConverterRatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetConverterRatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.containerMenu instanceof EnergyConverterMenu menu)
                    || menu.containerId != payload.containerId()
                    || !menu.stillValid(player)) {
                return;
            }
            menu.blockEntity.setRate(payload.rate());
        });
    }
}
