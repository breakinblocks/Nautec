package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetGhostInputPayload(int containerId, int slot, ItemStack stack) implements CustomPacketPayload {
    public static final Type<SetGhostInputPayload> TYPE = new Type<>(Nautec.rl("set_ghost_input"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetGhostInputPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetGhostInputPayload::containerId,
            ByteBufCodecs.VAR_INT, SetGhostInputPayload::slot,
            ItemStack.OPTIONAL_STREAM_CODEC, SetGhostInputPayload::stack,
            SetGhostInputPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetGhostInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.containerMenu instanceof NTAbstractContainerMenu<?> menu)
                    || menu.containerId != payload.containerId()
                    || !menu.stillValid(player)) {
                return;
            }
            if (!menu.blockEntity.setGhost(payload.slot(), payload.stack()) && !payload.stack().isEmpty()) {
                player.displayClientMessage(Component.translatable("nautec.ghost_input.refused", payload.stack().getHoverName()).withStyle(ChatFormatting.RED), true);
            }
        });
    }
}
