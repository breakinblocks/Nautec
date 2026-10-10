package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.content.menus.RecipeTransfer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record RecipeTransferPayload(int containerId, List<RecipeTransfer.Entry> entries, boolean max) implements CustomPacketPayload {
    public static final Type<RecipeTransferPayload> TYPE = new Type<>(Nautec.rl("recipe_transfer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeTransferPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RecipeTransferPayload::containerId,
            RecipeTransfer.Entry.STREAM_CODEC.apply(ByteBufCodecs.list(RecipeTransfer.MAX_ENTRIES)), RecipeTransferPayload::entries,
            ByteBufCodecs.BOOL, RecipeTransferPayload::max,
            RecipeTransferPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RecipeTransferPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.containerMenu instanceof NTAbstractContainerMenu<?> menu)
                    || menu.containerId != payload.containerId()
                    || !menu.stillValid(player)) {
                return;
            }
            RecipeTransfer.transfer(player, menu, menu.blockEntity, payload.entries(), payload.max());
        });
    }
}
