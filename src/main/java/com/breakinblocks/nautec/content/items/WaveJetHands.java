package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = Nautec.MODID)
public final class WaveJetHands {
    public static boolean isWaveJet(ItemStack stack) {
        return stack.is(NTItems.WAVE_JET.get());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        enforce(player);
    }

    public static boolean enforce(Player player) {
        InteractionHand crowded = crowdedHand(player);
        if (crowded == null) {
            return false;
        }

        ItemStack waveJet = player.getItemInHand(crowded).copy();
        player.setItemInHand(crowded, ItemStack.EMPTY);
        WaveJetSpotlight.extinguish(player);

        if (!stow(player, waveJet, crowded)) {
            player.drop(waveJet, false);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendOverlayMessage(Component.translatable("nautec.wave_jet.both_hands")
                    .withStyle(ChatFormatting.AQUA));
        }
        return true;
    }

    private static @Nullable InteractionHand crowdedHand(Player player) {
        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);

        if (isWaveJet(main) && isWaveJet(off)) {
            return InteractionHand.OFF_HAND;
        }
        if (isWaveJet(main) && !off.isEmpty()) {
            return InteractionHand.MAIN_HAND;
        }
        if (isWaveJet(off) && !main.isEmpty()) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    private static boolean stow(Player player, ItemStack waveJet, InteractionHand from) {
        Inventory inventory = player.getInventory();
        int held = from == InteractionHand.MAIN_HAND ? inventory.getSelectedSlot() : -1;

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (slot != held && inventory.getItem(slot).isEmpty()) {
                inventory.setItem(slot, waveJet);
                return true;
            }
        }
        return false;
    }

    private WaveJetHands() {
    }
}
