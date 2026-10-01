package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.api.gateways.GatewayEffects;
import com.breakinblocks.nautec.content.blockentities.GatewayBlockEntity;
import com.breakinblocks.nautec.registries.NTSounds;
import com.breakinblocks.nautec.utils.MachineSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetGatewayAddressPayload(BlockPos pos, GatewayAddress address) implements CustomPacketPayload {
    public static final Type<SetGatewayAddressPayload> TYPE = new Type<>(Nautec.rl("set_gateway_address"));

    private static final double REACH = 16.0;

    public static final StreamCodec<RegistryFriendlyByteBuf, SetGatewayAddressPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetGatewayAddressPayload::pos,
            GatewayAddress.STREAM_CODEC, SetGatewayAddressPayload::address,
            SetGatewayAddressPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetGatewayAddressPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.level() instanceof ServerLevel level)) {
                return;
            }

            BlockPos pos = payload.pos();
            if (!player.blockPosition().closerThan(pos, REACH + 4.0)
                    || !level.isLoaded(pos)
                    || !(level.getBlockEntity(pos) instanceof GatewayBlockEntity gateway)) {
                return;
            }

            GatewayAddress current = gateway.getAddress();
            GatewayAddress wanted = payload.address();
            if (current.equals(wanted)) {
                return;
            }

            gateway.setAddress(wanted);
            MachineSounds.play(level, pos, NTSounds.GATEWAY_RECODE, 0.8f, 1.0f);
            GatewayEffects.recoded(level, pos);
            player.sendSystemMessage(wanted.describe(), true);
        });
    }
}
