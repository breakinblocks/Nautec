package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.menus.ColonyReplicatorMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ReplicatorModePayload(int containerId) implements CustomPacketPayload {
    public static final Type<ReplicatorModePayload> TYPE = new Type<>(Nautec.rl("replicator_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReplicatorModePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ReplicatorModePayload::containerId,
            ReplicatorModePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ReplicatorModePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof ColonyReplicatorMenu menu
                    && menu.containerId == payload.containerId()
                    && menu.stillValid(player)) {
                menu.blockEntity.setSplice(!menu.blockEntity.isSplice());
            }
        });
    }
}
