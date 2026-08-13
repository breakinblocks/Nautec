package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.api.gateways.GatewayEffects;
import com.breakinblocks.nautec.content.blockentities.GatewayBlockEntity;
import com.breakinblocks.nautec.registries.NTSounds;
import com.breakinblocks.nautec.utils.MachineSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SetGatewayAddressPayload(BlockPos pos, GatewayAddress address) implements CustomPacketPayload {
    public static final Type<SetGatewayAddressPayload> TYPE = new Type<>(Nautec.rl("set_gateway_address"));

    private static final double REACH = 8.0;

    public static final StreamCodec<RegistryFriendlyByteBuf, SetGatewayAddressPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetGatewayAddressPayload::pos,
            GatewayAddress.STREAM_CODEC, SetGatewayAddressPayload::address,
            SetGatewayAddressPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetGatewayAddressPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.level() instanceof ServerLevel level)) {
                return;
            }

            BlockPos pos = payload.pos();
            if (!player.blockPosition().closerThan(pos, REACH + 4.0)
                    || !level.isLoaded(pos)
                    || !(level.getBlockEntity(pos) instanceof GatewayBlockEntity gateway)) {
                return;
            }

            GatewayAddress current = gateway.getAddress();
            GatewayAddress wanted = payload.address();
            if (current.equals(wanted)) {
                return;
            }

            List<DyeColor> cost = costOf(current, wanted);
            if (!player.getAbilities().instabuild && !take(player.getInventory(), cost)) {
                player.sendSystemMessage(Component.translatable("nautec.gateway.missing_dye")
                        .withStyle(ChatFormatting.RED), true);
                return;
            }

            gateway.setAddress(wanted);
            MachineSounds.play(level, pos, NTSounds.GATEWAY_RECODE, 0.8f, 1.0f);
            GatewayEffects.recoded(level, pos);
            player.sendSystemMessage(wanted.describe(), true);
        });
    }

    public static List<DyeColor> costOf(GatewayAddress from, GatewayAddress to) {
        List<DyeColor> cost = new ArrayList<>();
        for (int slot = 0; slot < GatewayAddress.SLOTS; slot++) {
            if (from.slots().get(slot) != to.slots().get(slot)) {
                cost.add(to.slots().get(slot));
            }
        }
        return cost;
    }

    private static boolean take(Inventory inventory, List<DyeColor> cost) {
        List<DyeColor> outstanding = new ArrayList<>(cost);
        for (DyeColor colour : cost) {
            if (count(inventory, GatewayAddress.dyeItem(colour)) < countOf(cost, colour)) {
                return false;
            }
        }

        for (DyeColor colour : outstanding) {
            consumeOne(inventory, GatewayAddress.dyeItem(colour));
        }
        return true;
    }

    private static int countOf(List<DyeColor> cost, DyeColor colour) {
        int total = 0;
        for (DyeColor entry : cost) {
            if (entry == colour) {
                total++;
            }
        }
        return total;
    }

    public static int count(Inventory inventory, Item item) {
        int total = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void consumeOne(Inventory inventory, Item item) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                stack.shrink(1);
                return;
            }
        }
    }
}
