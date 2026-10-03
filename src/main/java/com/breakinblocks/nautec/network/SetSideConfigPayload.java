package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.api.sides.RelativeFace;
import com.breakinblocks.nautec.api.sides.SideKind;
import com.breakinblocks.nautec.api.sides.SideMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetSideConfigPayload(int containerId, int kind, int face, int mode) implements CustomPacketPayload {
    public static final Type<SetSideConfigPayload> TYPE = new Type<>(Nautec.rl("set_side_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetSideConfigPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetSideConfigPayload::containerId,
            ByteBufCodecs.VAR_INT, SetSideConfigPayload::kind,
            ByteBufCodecs.VAR_INT, SetSideConfigPayload::face,
            ByteBufCodecs.VAR_INT, SetSideConfigPayload::mode,
            SetSideConfigPayload::new
    );

    public SetSideConfigPayload(int containerId, SideKind kind, RelativeFace face, SideMode mode) {
        this(containerId, kind.ordinal(), face.ordinal(), mode.ordinal());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetSideConfigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.containerMenu instanceof NTAbstractContainerMenu<?> menu)
                    || menu.containerId != payload.containerId()
                    || !menu.stillValid(player)
                    || payload.kind() < 0 || payload.kind() >= SideKind.values().length
                    || payload.face() < 0 || payload.face() >= RelativeFace.values().length
                    || payload.mode() < 0 || payload.mode() >= SideMode.values().length) {
                return;
            }
            SideKind kind = SideKind.values()[payload.kind()];
            if (!menu.blockEntity.hasSideConfig(kind)) {
                return;
            }
            menu.blockEntity.setSideMode(kind, RelativeFace.values()[payload.face()], SideMode.values()[payload.mode()]);
        });
    }
}
