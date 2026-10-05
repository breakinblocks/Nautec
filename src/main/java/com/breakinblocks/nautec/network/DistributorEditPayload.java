package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.distributor.DistributorLink;
import com.breakinblocks.nautec.content.menus.DistributorMenu;
import com.breakinblocks.nautec.utils.TemplateSanitizer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DistributorEditPayload(int containerId, int action, int link, int slot, int amount, ItemStack item, FluidStack fluid)
        implements CustomPacketPayload {
    public static final Type<DistributorEditPayload> TYPE = new Type<>(Nautec.rl("distributor_edit"));

    public static final int SET_ITEM = 0;
    public static final int SET_FLUID = 1;
    public static final int ITEM_AMOUNT = 2;
    public static final int FLUID_AMOUNT = 3;
    public static final int UNLINK = 4;

    public static final StreamCodec<RegistryFriendlyByteBuf, DistributorEditPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DistributorEditPayload::containerId,
            ByteBufCodecs.VAR_INT, DistributorEditPayload::action,
            ByteBufCodecs.VAR_INT, DistributorEditPayload::link,
            ByteBufCodecs.VAR_INT, DistributorEditPayload::slot,
            ByteBufCodecs.VAR_INT, DistributorEditPayload::amount,
            ItemStack.OPTIONAL_STREAM_CODEC, DistributorEditPayload::item,
            FluidStack.OPTIONAL_STREAM_CODEC, DistributorEditPayload::fluid,
            DistributorEditPayload::new
    );

    public static DistributorEditPayload item(int containerId, int link, int slot, ItemStack stack) {
        return new DistributorEditPayload(containerId, SET_ITEM, link, slot, 0, stack, FluidStack.EMPTY);
    }

    public static DistributorEditPayload fluid(int containerId, int link, int slot, FluidStack stack) {
        return new DistributorEditPayload(containerId, SET_FLUID, link, slot, 0, ItemStack.EMPTY, stack);
    }

    public static DistributorEditPayload amount(int containerId, boolean fluid, int link, int slot, int amount) {
        return new DistributorEditPayload(containerId, fluid ? FLUID_AMOUNT : ITEM_AMOUNT, link, slot, amount, ItemStack.EMPTY, FluidStack.EMPTY);
    }

    public static DistributorEditPayload unlink(int containerId, int link) {
        return new DistributorEditPayload(containerId, UNLINK, link, 0, 0, ItemStack.EMPTY, FluidStack.EMPTY);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DistributorEditPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.containerMenu instanceof DistributorMenu menu)
                    || menu.containerId != payload.containerId()
                    || !menu.stillValid(player)) {
                return;
            }
            if (payload.action() == UNLINK) {
                menu.blockEntity.unlink(payload.link());
                return;
            }
            DistributorLink link = menu.blockEntity.link(payload.link());
            if (link == null) {
                return;
            }
            boolean fluid = payload.action() == SET_FLUID || payload.action() == FLUID_AMOUNT;
            int max = fluid ? DistributorLink.FLUID_REQUESTS : DistributorLink.ITEM_REQUESTS;
            if (payload.slot() < 0 || payload.slot() >= max) {
                return;
            }
            switch (payload.action()) {
                case SET_ITEM -> link.setItem(payload.slot(), TemplateSanitizer.item(payload.item(), player.registryAccess()));
                case SET_FLUID -> link.setFluid(payload.slot(), payload.fluid());
                case ITEM_AMOUNT -> link.setItemAmount(payload.slot(), payload.amount());
                case FLUID_AMOUNT -> link.setFluidAmount(payload.slot(), payload.amount());
                default -> {
                    return;
                }
            }
            menu.blockEntity.changed();
        });
    }
}
