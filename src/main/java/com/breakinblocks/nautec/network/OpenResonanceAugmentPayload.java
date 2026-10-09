package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.augments.ResonanceAugment;
import com.breakinblocks.nautec.content.resonance.ResonanceCharmItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record OpenResonanceAugmentPayload() implements CustomPacketPayload {
    private static final int THROTTLE_TICKS = 4;
    public static final Type<OpenResonanceAugmentPayload> TYPE = new Type<>(Nautec.rl("open_resonance_augment"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenResonanceAugmentPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenResonanceAugmentPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenResonanceAugmentPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !ServerPacketGuards.allow(player, "open_resonance_augment", THROTTLE_TICKS)) {
                return;
            }
            ResonanceAugment augment = ResonanceAugment.installed(player);
            if (augment == null) {
                return;
            }
            PacketDistributor.sendToPlayer(player, new OpenCharmScreenPayload(OpenCharmScreenPayload.AUGMENT,
                    ResonanceCharmItem.info(player, augment.getBinding(), augment.getPriority())));
        });
    }
}
