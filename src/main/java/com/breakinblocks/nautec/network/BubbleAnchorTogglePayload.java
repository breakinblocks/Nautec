package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.bubble.BubbleAnchorBlockEntity;
import com.breakinblocks.nautec.content.menus.BubbleAnchorMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BubbleAnchorTogglePayload(int containerId, int toggle) implements CustomPacketPayload {
    public static final Type<BubbleAnchorTogglePayload> TYPE = new Type<>(Nautec.rl("bubble_anchor_toggle"));

    public static final int ENABLED = 0;
    public static final int FILL = 1;
    public static final int ABOVE = 2;

    public static final StreamCodec<RegistryFriendlyByteBuf, BubbleAnchorTogglePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BubbleAnchorTogglePayload::containerId,
            ByteBufCodecs.VAR_INT, BubbleAnchorTogglePayload::toggle,
            BubbleAnchorTogglePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BubbleAnchorTogglePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.containerMenu instanceof BubbleAnchorMenu menu)
                    || menu.containerId != payload.containerId()
                    || !menu.stillValid(player)) {
                return;
            }
            BubbleAnchorBlockEntity anchor = menu.blockEntity;
            switch (payload.toggle()) {
                case ENABLED -> anchor.setEnabled(!anchor.isEnabled());
                case FILL -> anchor.setFillWater(!anchor.fillsWater());
                case ABOVE -> anchor.setAbove(!anchor.isAbove());
                default -> {
                }
            }
        });
    }
}
