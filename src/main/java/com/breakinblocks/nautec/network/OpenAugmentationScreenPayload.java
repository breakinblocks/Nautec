package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.augments.AugmentType;
import com.breakinblocks.nautec.client.ClientScreenHooks;
import com.breakinblocks.nautec.utils.codec.AugmentCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record OpenAugmentationScreenPayload(BlockPos pos, Optional<AugmentType<?>> augmentType,
                                             ItemStack preview) implements CustomPacketPayload {
    public static final Type<OpenAugmentationScreenPayload> TYPE = new Type<>(Nautec.rl("open_augmentation_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenAugmentationScreenPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenAugmentationScreenPayload::pos,
            ByteBufCodecs.optional(AugmentCodecs.AUGMENT_TYPE_STREAM_CODEC), OpenAugmentationScreenPayload::augmentType,
            ItemStack.OPTIONAL_STREAM_CODEC, OpenAugmentationScreenPayload::preview,
            OpenAugmentationScreenPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenAugmentationScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientScreenHooks.openAugmentationStationScreen(payload));
    }
}
