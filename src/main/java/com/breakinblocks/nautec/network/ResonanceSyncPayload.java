package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.resonance.ResonanceClientState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record ResonanceSyncPayload(BlockPos pos, Optional<UUID> current, List<NetworkView> networks, boolean teamsAvailable)
        implements CustomPacketPayload {
    public static final Type<ResonanceSyncPayload> TYPE = new Type<>(Nautec.rl("resonance_sync"));

    public record Member(UUID id, String name) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Member> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, Member::id,
                ByteBufCodecs.STRING_UTF8, Member::name,
                Member::new
        );
    }

    public record NetworkView(UUID id, String name, String ownerName, boolean owner, boolean manage, List<Member> trusted,
                              boolean teamAccess, int pylons) {
        public static final StreamCodec<RegistryFriendlyByteBuf, NetworkView> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, NetworkView::id,
                ByteBufCodecs.STRING_UTF8, NetworkView::name,
                ByteBufCodecs.STRING_UTF8, NetworkView::ownerName,
                ByteBufCodecs.BOOL, NetworkView::owner,
                ByteBufCodecs.BOOL, NetworkView::manage,
                Member.STREAM_CODEC.apply(ByteBufCodecs.list()), NetworkView::trusted,
                ByteBufCodecs.BOOL, NetworkView::teamAccess,
                ByteBufCodecs.VAR_INT, NetworkView::pylons,
                NetworkView::new
        );
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ResonanceSyncPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ResonanceSyncPayload::pos,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), ResonanceSyncPayload::current,
            NetworkView.STREAM_CODEC.apply(ByteBufCodecs.list()), ResonanceSyncPayload::networks,
            ByteBufCodecs.BOOL, ResonanceSyncPayload::teamsAvailable,
            ResonanceSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ResonanceSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ResonanceClientState.update(payload));
    }
}
