package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blocks.BeamFaces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetBeamFacePayload(BlockPos pos, int face, boolean forward) implements CustomPacketPayload {
    private static final int THROTTLE_TICKS = 2;
    public static final Type<SetBeamFacePayload> TYPE = new Type<>(Nautec.rl("set_beam_face"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetBeamFacePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetBeamFacePayload::pos,
            ByteBufCodecs.VAR_INT, SetBeamFacePayload::face,
            ByteBufCodecs.BOOL, SetBeamFacePayload::forward,
            SetBeamFacePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetBeamFacePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)
                    || payload.face() < 0 || payload.face() >= Direction.values().length) {
                return;
            }
            BlockPos pos = payload.pos();
            if (!level.isLoaded(pos) || !player.isWithinBlockInteractionRange(pos, 1.0D) || !level.mayInteract(player, pos)
                    || !BeamFaces.configurable(level.getBlockState(pos)) || !ServerPacketGuards.allow(player, "beam_faces", THROTTLE_TICKS)) {
                return;
            }
            BeamFaces.apply(level, pos, Direction.from3DDataValue(payload.face()), payload.forward());
        });
    }
}
