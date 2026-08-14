package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.client.screen.GatewayScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenGatewayScreenPayload(BlockPos pos, GatewayAddress address) implements CustomPacketPayload {
    public static final Type<OpenGatewayScreenPayload> TYPE = new Type<>(Nautec.rl("open_gateway_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenGatewayScreenPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenGatewayScreenPayload::pos,
            GatewayAddress.STREAM_CODEC, OpenGatewayScreenPayload::address,
            OpenGatewayScreenPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenGatewayScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> GatewayScreen.open(payload.pos(), payload.address()));
    }
}
