package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SubmarineAbilityPayload(int entityId, int slot) implements CustomPacketPayload {
    public static final Type<SubmarineAbilityPayload> TYPE = new Type<>(Nautec.rl("submarine_ability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SubmarineAbilityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SubmarineAbilityPayload::entityId,
            ByteBufCodecs.VAR_INT, SubmarineAbilityPayload::slot,
            SubmarineAbilityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SubmarineAbilityPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            if (player.getVehicle() instanceof SubmarineEntity submarine
                    && submarine.getId() == payload.entityId()
                    && submarine.getControllingPassenger() == player) {
                submarine.getModules().activate(payload.slot(), player);
            }
        });
    }
}
