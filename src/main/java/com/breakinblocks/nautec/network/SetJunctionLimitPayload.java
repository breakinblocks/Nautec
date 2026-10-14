package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.LaserJunctionBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetJunctionLimitPayload(BlockPos pos, int face, int limit) implements CustomPacketPayload {
    public static final Type<SetJunctionLimitPayload> TYPE = new Type<>(Nautec.rl("set_junction_limit"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetJunctionLimitPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetJunctionLimitPayload::pos,
            ByteBufCodecs.VAR_INT, SetJunctionLimitPayload::face,
            ByteBufCodecs.VAR_INT, SetJunctionLimitPayload::limit,
            SetJunctionLimitPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetJunctionLimitPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)
                    || payload.face() < 0 || payload.face() >= Direction.values().length || payload.limit() < 0) {
                return;
            }
            BlockPos pos = payload.pos();
            if (!level.isLoaded(pos) || !player.canInteractWithBlock(pos, 1.0D) || !level.mayInteract(player, pos)
                    || !(level.getBlockEntity(pos) instanceof LaserJunctionBlockEntity junction)) {
                return;
            }
            junction.setOutputLimit(Direction.from3DDataValue(payload.face()), payload.limit());
        });
    }
}
