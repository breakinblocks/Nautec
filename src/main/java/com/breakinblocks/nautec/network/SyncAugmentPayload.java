package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.utils.AugmentHelper;
import com.breakinblocks.nautec.utils.codec.AugmentCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncAugmentPayload(int entityId, Augment augment, CompoundTag extraData) implements CustomPacketPayload {

    public static final Type<SyncAugmentPayload> TYPE = new Type<>(Nautec.rl("augment_data_payload"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAugmentPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncAugmentPayload::entityId,
            AugmentCodecs.AUGMENT_STREAM_CODEC,
            SyncAugmentPayload::augment,
            ByteBufCodecs.COMPOUND_TAG,
            SyncAugmentPayload::extraData,
            SyncAugmentPayload::new
    );

    public SyncAugmentPayload(Augment augment, CompoundTag extraData) {
        this(-1, augment, extraData);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void setAugmentDataAction(SyncAugmentPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.player().level().isClientSide()) {
                return;
            }
            Augment augment = payload.augment();
            CompoundTag tag = payload.extraData();
            Player player = payload.entityId() == -1 ? context.player()
                    : context.player().level().getEntity(payload.entityId()) instanceof Player target ? target : null;
            if (player == null) return;
            Augment current = AugmentHelper.getAugmentBySlot(player, augment.getAugmentSlot());
            if (current != null && current.getAugmentType() == augment.getAugmentType()) augment = current;
            augment.setPlayer(player);
            augment.deserializeNBT(player.level().registryAccess(), tag);
            AugmentSlot augmentSlot = augment.getAugmentSlot();
            AugmentHelper.setAugment(player, augmentSlot, augment);
            AugmentHelper.setAugmentExtraData(player, augmentSlot, tag);
        });
    }
}
