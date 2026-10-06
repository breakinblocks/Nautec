package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.conduits.ConduitPartBlock;
import com.breakinblocks.nautec.content.conduits.TapFace;
import com.breakinblocks.nautec.content.menus.ConduitTapMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ConduitTapSyncPayload(int containerId, int face, TapFace data) implements CustomPacketPayload {
    public static final Type<ConduitTapSyncPayload> TYPE = new Type<>(Nautec.rl("conduit_tap_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConduitTapSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ConduitTapSyncPayload::containerId,
            ByteBufCodecs.VAR_INT, ConduitTapSyncPayload::face,
            TapFace.STREAM_CODEC, ConduitTapSyncPayload::data,
            ConduitTapSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ConduitTapSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof ConduitTapMenu menu
                    && menu.containerId == payload.containerId()
                    && payload.face() >= 0 && payload.face() < ConduitPartBlock.DIRECTIONS.length) {
                menu.blockEntity.face(ConduitPartBlock.DIRECTIONS[payload.face()]).copyFrom(payload.data());
            }
        });
    }
}
