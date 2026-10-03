package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SubmarineLaserPayload(int entityId, boolean held) implements CustomPacketPayload {
    public static final Type<SubmarineLaserPayload> TYPE = new Type<>(Nautec.rl("submarine_laser"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SubmarineLaserPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SubmarineLaserPayload::entityId,
            ByteBufCodecs.BOOL, SubmarineLaserPayload::held,
            SubmarineLaserPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SubmarineLaserPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            if (player.getVehicle() instanceof SubmarineEntity submarine
                    && submarine.getId() == payload.entityId()
                    && submarine.getControllingPassenger() == player) {
                submarine.setLaserHeld(payload.held());
            }
        });
    }
}
