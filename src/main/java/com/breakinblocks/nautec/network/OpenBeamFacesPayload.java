package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.screen.BeamFacesScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenBeamFacesPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<OpenBeamFacesPayload> TYPE = new Type<>(Nautec.rl("open_beam_faces"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenBeamFacesPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenBeamFacesPayload::pos,
            OpenBeamFacesPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenBeamFacesPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> BeamFacesScreen.open(payload.pos()));
    }
}
