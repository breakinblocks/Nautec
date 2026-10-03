package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.resonance.ResonanceActions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;
import java.util.UUID;

public record ResonanceActionPayload(BlockPos pos, int action, Optional<UUID> network, String text) implements CustomPacketPayload {
    public static final Type<ResonanceActionPayload> TYPE = new Type<>(Nautec.rl("resonance_action"));

    public static final int SELECT = 0;
    public static final int CREATE = 1;
    public static final int MODE = 2;
    public static final int TRUST = 3;
    public static final int UNTRUST = 4;
    public static final int TEAM = 5;
    public static final int RENAME = 6;
    public static final int DELETE = 7;
    public static final int PRIORITY = 8;
    public static final int LIMIT = 9;

    public static final StreamCodec<RegistryFriendlyByteBuf, ResonanceActionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ResonanceActionPayload::pos,
            ByteBufCodecs.VAR_INT, ResonanceActionPayload::action,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), ResonanceActionPayload::network,
            ByteBufCodecs.stringUtf8(64), ResonanceActionPayload::text,
            ResonanceActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ResonanceActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ResonanceActions.apply(player, payload);
            }
        });
    }
}
