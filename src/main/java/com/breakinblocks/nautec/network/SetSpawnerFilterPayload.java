package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.menus.ConfinedSpawnerMenu;
import com.breakinblocks.nautec.content.spawner.SpawnerFilter;
import com.breakinblocks.nautec.content.spawner.SpawnerFilterEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record SetSpawnerFilterPayload(int containerId, int slot, Optional<SpawnerFilterEntry> entry) implements CustomPacketPayload {
    public static final Type<SetSpawnerFilterPayload> TYPE = new Type<>(Nautec.rl("set_spawner_filter"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetSpawnerFilterPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetSpawnerFilterPayload::containerId,
            ByteBufCodecs.VAR_INT, SetSpawnerFilterPayload::slot,
            ByteBufCodecs.optional(SpawnerFilterEntry.STREAM_CODEC), SetSpawnerFilterPayload::entry,
            SetSpawnerFilterPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetSpawnerFilterPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.containerMenu instanceof ConfinedSpawnerMenu menu)
                    || menu.containerId != payload.containerId()
                    || payload.slot() < 0
                    || payload.slot() >= SpawnerFilter.SIZE
                    || !menu.stillValid(player)) {
                return;
            }
            menu.getBlockEntity().setFilterEntry(payload.slot(), payload.entry().orElse(null));
        });
    }
}
