package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.breakinblocks.nautec.client.hud.SubmarineAbilityBarState;

public record SubmarineCooldownPayload(int entityId, int slot, int cooldownTicks, int activeTicks) implements CustomPacketPayload {
    public static final Type<SubmarineCooldownPayload> TYPE = new Type<>(Nautec.rl("submarine_cooldown"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SubmarineCooldownPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SubmarineCooldownPayload::entityId,
            ByteBufCodecs.VAR_INT, SubmarineCooldownPayload::slot,
            ByteBufCodecs.VAR_INT, SubmarineCooldownPayload::cooldownTicks,
            ByteBufCodecs.VAR_INT, SubmarineCooldownPayload::activeTicks,
            SubmarineCooldownPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SubmarineCooldownPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> SubmarineAbilityBarState.onCooldown(
                payload.entityId(), payload.slot(), payload.cooldownTicks(), payload.activeTicks()));
    }
}
