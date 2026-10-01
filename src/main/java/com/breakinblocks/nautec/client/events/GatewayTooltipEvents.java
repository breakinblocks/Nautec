package com.breakinblocks.nautec.client.events;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.api.gateways.PackedGateway;
import com.breakinblocks.nautec.data.NTDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class GatewayTooltipEvents {
    private GatewayTooltipEvents() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        GatewayAddress address = stack.get(NTDataComponents.GATEWAY_ADDRESS.get());
        PackedGateway packed = stack.get(NTDataComponents.GATEWAY_PACKED.get());
        if (address != null) {
            event.getToolTip().add(Component.translatable("nautec.monocle.address").withStyle(ChatFormatting.GRAY).append(address.describe()));
        }
        if (packed != null) {
            event.getToolTip().add(Component.translatable("nautec.gateway.packed").withStyle(ChatFormatting.AQUA));
            if (packed.wild()) {
                event.getToolTip().add(Component.translatable("nautec.gateway.packed_wild").withStyle(ChatFormatting.DARK_AQUA));
            }
        }
    }
}
