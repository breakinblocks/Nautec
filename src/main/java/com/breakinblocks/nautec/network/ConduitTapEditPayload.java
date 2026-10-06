package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.conduits.ConduitChannel;
import com.breakinblocks.nautec.content.conduits.ConduitPartBlock;
import com.breakinblocks.nautec.content.conduits.ConduitTapBlockEntity;
import com.breakinblocks.nautec.content.conduits.DistributionMode;
import com.breakinblocks.nautec.content.conduits.FlowMode;
import com.breakinblocks.nautec.content.conduits.RedstoneMode;
import com.breakinblocks.nautec.content.conduits.TapFace;
import com.breakinblocks.nautec.content.conduits.TapFilter;
import com.breakinblocks.nautec.content.conduits.TapSide;
import com.breakinblocks.nautec.content.menus.ConduitTapMenu;
import com.breakinblocks.nautec.utils.TemplateSanitizer;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ConduitTapEditPayload(int containerId, int face, int side, int action, int index, int value, ItemStack item, FluidStack fluid)
        implements CustomPacketPayload {
    public static final Type<ConduitTapEditPayload> TYPE = new Type<>(Nautec.rl("conduit_tap_edit"));

    public static final int MODE = 0;
    public static final int PRIORITY = 1;
    public static final int REDSTONE = 2;
    public static final int DISTRIBUTION = 3;
    public static final int WHITELIST = 4;
    public static final int COMPONENTS = 5;
    public static final int ITEM = 6;
    public static final int FLUID = 7;
    public static final int DISABLED = 8;

    public static final StreamCodec<RegistryFriendlyByteBuf, ConduitTapEditPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ConduitTapEditPayload::containerId,
            ByteBufCodecs.VAR_INT, ConduitTapEditPayload::face,
            ByteBufCodecs.VAR_INT, ConduitTapEditPayload::side,
            ByteBufCodecs.VAR_INT, ConduitTapEditPayload::action,
            ByteBufCodecs.VAR_INT, ConduitTapEditPayload::index,
            ByteBufCodecs.VAR_INT, ConduitTapEditPayload::value,
            ItemStack.OPTIONAL_STREAM_CODEC, ConduitTapEditPayload::item,
            FluidStack.OPTIONAL_STREAM_CODEC, ConduitTapEditPayload::fluid,
            ConduitTapEditPayload::new
    );

    public static ConduitTapEditPayload value(int containerId, Direction face, int action, int index, int value) {
        return sided(containerId, face, TapSide.INPUT, action, index, value);
    }

    public static ConduitTapEditPayload sided(int containerId, Direction face, TapSide side, int action, int index, int value) {
        return new ConduitTapEditPayload(containerId, face.ordinal(), side.ordinal(), action, index, value, ItemStack.EMPTY, FluidStack.EMPTY);
    }

    public static ConduitTapEditPayload item(int containerId, Direction face, TapSide side, int slot, ItemStack stack) {
        return new ConduitTapEditPayload(containerId, face.ordinal(), side.ordinal(), ITEM, slot, 0, stack, FluidStack.EMPTY);
    }

    public static ConduitTapEditPayload fluid(int containerId, Direction face, TapSide side, int slot, FluidStack stack) {
        return new ConduitTapEditPayload(containerId, face.ordinal(), side.ordinal(), FLUID, slot, 0, ItemStack.EMPTY, stack);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ConduitTapEditPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.containerMenu instanceof ConduitTapMenu menu)
                    || menu.containerId != payload.containerId()
                    || !menu.stillValid(player)
                    || payload.face() < 0 || payload.face() >= ConduitPartBlock.DIRECTIONS.length
                    || !TapFace.validId(TapSide.ALL, payload.side())) {
                return;
            }
            ConduitTapBlockEntity tap = menu.blockEntity;
            Direction direction = ConduitPartBlock.DIRECTIONS[payload.face()];
            if (apply(tap, tap.face(direction), payload, player.registryAccess())) {
                tap.configChanged(direction);
            } else {
                tap.resync(player, menu.containerId, direction);
            }
        });
    }

    public static boolean apply(ConduitTapBlockEntity tap, TapFace face, ConduitTapEditPayload payload, HolderLookup.Provider registries) {
        int index = payload.index();
        int value = payload.value();
        TapSide side = TapSide.ALL[payload.side()];
        TapFilter filter = face.filter(side);
        switch (payload.action()) {
            case MODE -> {
                if (!TapFace.validId(ConduitChannel.ALL, index) || !TapFace.validId(FlowMode.ALL, value)) {
                    return false;
                }
                face.setMode(ConduitChannel.ALL[index], FlowMode.ALL[value]);
            }
            case PRIORITY -> face.setPriority(value);
            case REDSTONE -> {
                if (!TapFace.validId(RedstoneMode.ALL, value)) {
                    return false;
                }
                face.setRedstone(side, RedstoneMode.ALL[value]);
            }
            case DISTRIBUTION -> {
                if (!TapFace.validId(DistributionMode.ALL, value)) {
                    return false;
                }
                face.setDistribution(DistributionMode.ALL[value]);
            }
            case WHITELIST -> {
                if (!tap.hasFilterUpgrade()) {
                    return false;
                }
                filter.setWhitelist(value != 0);
            }
            case COMPONENTS -> {
                if (index < 0 || index >= tap.itemFilterSlots() || filter.item(index).isEmpty()) {
                    return false;
                }
                filter.setExact(index, value != 0);
            }
            case ITEM -> {
                if (index < 0 || index >= tap.itemFilterSlots()) {
                    return false;
                }
                filter.setItem(index, TemplateSanitizer.item(payload.item(), registries));
            }
            case FLUID -> {
                if (!tap.hasFilterUpgrade() || index < 0 || index >= TapFilter.FLUID_SLOTS) {
                    return false;
                }
                filter.setFluid(index, payload.fluid());
            }
            case DISABLED -> face.setDisabled(value != 0);
            default -> {
                return false;
            }
        }
        return true;
    }
}
