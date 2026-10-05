package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.augments.AugmentType;
import com.breakinblocks.nautec.client.ClientScreenHooks;
import com.breakinblocks.nautec.utils.codec.AugmentCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Optional;

public record AugmentationStationSyncPayload(BlockPos pos, boolean open, int status, int progress, Optional<AugmentType<?>> result,
                                             ItemStack preview, String description, List<Extension> extensions)
        implements CustomPacketPayload {
    public static final Type<AugmentationStationSyncPayload> TYPE = new Type<>(Nautec.rl("augmentation_station_sync"));

    public record Extension(Direction side, boolean present, boolean arm, ItemStack part, int power) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Extension> STREAM_CODEC = StreamCodec.composite(
                Direction.STREAM_CODEC, Extension::side,
                ByteBufCodecs.BOOL, Extension::present,
                ByteBufCodecs.BOOL, Extension::arm,
                ItemStack.OPTIONAL_STREAM_CODEC, Extension::part,
                ByteBufCodecs.VAR_INT, Extension::power,
                Extension::new
        );
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, AugmentationStationSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                BlockPos.STREAM_CODEC.encode(buf, payload.pos());
                buf.writeBoolean(payload.open());
                buf.writeVarInt(payload.status());
                buf.writeVarInt(payload.progress());
                ByteBufCodecs.optional(AugmentCodecs.AUGMENT_TYPE_STREAM_CODEC).encode(buf, payload.result());
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, payload.preview());
                buf.writeUtf(payload.description());
                Extension.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, payload.extensions());
            },
            buf -> new AugmentationStationSyncPayload(
                    BlockPos.STREAM_CODEC.decode(buf),
                    buf.readBoolean(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    ByteBufCodecs.optional(AugmentCodecs.AUGMENT_TYPE_STREAM_CODEC).decode(buf),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                    buf.readUtf(),
                    Extension.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AugmentationStationSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientScreenHooks.augmentationStationSync(payload));
    }
}
