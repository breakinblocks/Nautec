package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.items.WaveJetSpotlight;
import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import com.breakinblocks.nautec.registries.NTSounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ToggleWaveJetLightPayload() implements CustomPacketPayload {
    private static final int THROTTLE_TICKS = 4;
    public static final Type<ToggleWaveJetLightPayload> TYPE = new Type<>(Nautec.rl("toggle_wave_jet_light"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleWaveJetLightPayload> STREAM_CODEC =
            StreamCodec.unit(new ToggleWaveJetLightPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleWaveJetLightPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !ServerPacketGuards.allow(player, "wave_jet_light", THROTTLE_TICKS)) {
                return;
            }
            ItemStack stack = WaveJetSpotlight.heldWaveJet(player);
            if (stack == null) {
                return;
            }

            boolean lit = !NTDataComponentsUtils.isAbilityEnabled(stack);
            NTDataComponentsUtils.setAbilityStatus(stack, lit);
            if (!lit) {
                WaveJetSpotlight.extinguish(player);
            }

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    lit ? NTSounds.WAVE_JET_START.get() : NTSounds.WAVE_JET_STOP.get(),
                    SoundSource.PLAYERS, 0.35f, lit ? 1.8f : 1.6f);
        });
    }
}
