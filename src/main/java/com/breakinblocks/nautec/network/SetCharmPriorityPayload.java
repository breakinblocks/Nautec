package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.resonance.ResonanceCharmItem;
import com.breakinblocks.nautec.content.resonance.SatelliteArrayBlockEntity;
import com.breakinblocks.nautec.data.NTDataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetCharmPriorityPayload(int hand, int priority) implements CustomPacketPayload {
    public static final Type<SetCharmPriorityPayload> TYPE = new Type<>(Nautec.rl("set_charm_priority"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetCharmPriorityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetCharmPriorityPayload::hand,
            ByteBufCodecs.VAR_INT, SetCharmPriorityPayload::priority,
            SetCharmPriorityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetCharmPriorityPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || payload.hand() < 0 || payload.hand() >= InteractionHand.values().length) {
                return;
            }
            ItemStack stack = player.getItemInHand(InteractionHand.values()[payload.hand()]);
            if (stack.getItem() instanceof ResonanceCharmItem) {
                stack.set(NTDataComponents.RESONANCE_PRIORITY.get(),
                        Mth.clamp(payload.priority(), SatelliteArrayBlockEntity.MIN_PRIORITY, SatelliteArrayBlockEntity.MAX_PRIORITY));
            }
        });
    }
}
