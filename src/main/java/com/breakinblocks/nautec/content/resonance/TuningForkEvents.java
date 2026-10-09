package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.augments.ResonanceAugment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Nautec.MODID)
public final class TuningForkEvents {
    private TuningForkEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() instanceof TuningForkItem || event.getItemStack().getItem() instanceof ResonanceCharmItem) {
            event.setUseBlock(TriState.FALSE);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onTuneAugment(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (event.getHand() != InteractionHand.MAIN_HAND || !player.isSecondaryUseActive() || !player.getMainHandItem().isEmpty()
                || !(event.getLevel().getBlockEntity(event.getPos()) instanceof ResonanceTunable tunable)) {
            return;
        }
        ResonanceAugment augment = ResonanceAugment.installed(player);
        if (augment == null) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ResonanceNetwork network = tunable.getNetwork();
        if (network == null) {
            serverPlayer.sendOverlayMessage(Component.translatable("nautec.resonance_charm.no_network").withStyle(ChatFormatting.RED));
            return;
        }
        if (!ResonanceNetworks.canUse(serverPlayer, network)) {
            serverPlayer.sendOverlayMessage(Component.translatable("nautec.resonance.error.no_access").withStyle(ChatFormatting.RED));
            return;
        }
        augment.bind(new ResonanceBinding(network.id(), network.name()));
        serverPlayer.sendOverlayMessage(Component.translatable("nautec.resonance_augment.bound", network.name()).withStyle(ChatFormatting.AQUA));
        event.getLevel().playSound(null, event.getPos(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
    }
}
