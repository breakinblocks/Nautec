package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.data.NTDataAttachments;
import com.breakinblocks.nautec.client.AugmentClientHelper;
import com.breakinblocks.nautec.utils.AugmentHelper;
import com.breakinblocks.nautec.utils.codec.AugmentCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record ClearAugmentPayload(AugmentSlot augmentSlot) implements CustomPacketPayload {

    public static final Type<ClearAugmentPayload> TYPE = new Type<>(Nautec.rl("clear_augment_payload"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClearAugmentPayload> STREAM_CODEC = StreamCodec.composite(
            AugmentCodecs.AUGMENT_SLOT_STREAM_CODEC,
            ClearAugmentPayload::augmentSlot,
            ClearAugmentPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void clearAugmentAction(ClearAugmentPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            AugmentSlot slot = payload.augmentSlot();
            
            Map<AugmentSlot, Augment> augments = new HashMap<>(AugmentHelper.getAugments(player));
            Map<AugmentSlot, CompoundTag> augmentsData = new HashMap<>(AugmentHelper.getAugmentsData(player));
            
            augments.remove(slot);
            augmentsData.remove(slot);
            
            player.setData(NTDataAttachments.AUGMENTS, augments);
            player.setData(NTDataAttachments.AUGMENTS_EXTRA_DATA, augmentsData);
            
            if (player.level().isClientSide()) {
                AugmentClientHelper.invalidateCacheFor(player, slot);
            }
        });
    }
}