package com.breakinblocks.nautec.content.distributor;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Nautec.MODID)
public final class DistributorLinking {
    private static final long TIMEOUT = 20L * 60;
    private static final Map<UUID, Pending> PENDING = new HashMap<>();

    private record Pending(GlobalPos distributor, long expires) {
    }

    private DistributorLinking() {
    }

    public static boolean toggle(ServerPlayer player, BlockPos distributor) {
        GlobalPos target = GlobalPos.of(player.level().dimension(), distributor);
        Pending current = PENDING.get(player.getUUID());
        if (current != null && current.distributor().equals(target)) {
            PENDING.remove(player.getUUID());
            player.sendOverlayMessage(Component.translatable("nautec.distributor.link.stopped").withStyle(ChatFormatting.GRAY));
            return false;
        }
        PENDING.put(player.getUUID(), new Pending(target, player.level().getGameTime() + TIMEOUT));
        player.sendOverlayMessage(Component.translatable("nautec.distributor.link.started", NTConfig.distributorRange).withStyle(ChatFormatting.AQUA));
        return true;
    }

    public static @Nullable GlobalPos pending(ServerPlayer player) {
        Pending pending = PENDING.get(player.getUUID());
        if (pending == null || pending.expires() < player.level().getGameTime()) {
            PENDING.remove(player.getUUID());
            return null;
        }
        return pending.distributor();
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != InteractionHand.MAIN_HAND
                || !player.isSecondaryUseActive() || !player.getMainHandItem().isEmpty()) {
            return;
        }
        GlobalPos source = pending(player);
        if (source == null || source.pos().equals(event.getPos())) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        ServerLevel level = player.level();
        if (!source.dimension().equals(level.dimension())) {
            player.sendOverlayMessage(Component.translatable("nautec.distributor.link.other_dimension").withStyle(ChatFormatting.RED));
            return;
        }
        if (!(level.getBlockEntity(source.pos()) instanceof DistributorBlockEntity distributor)) {
            PENDING.remove(player.getUUID());
            player.sendOverlayMessage(Component.translatable("nautec.distributor.link.missing").withStyle(ChatFormatting.RED));
            return;
        }
        Component name = level.getBlockState(event.getPos()).getBlock().getName();
        DistributorBlockEntity.LinkResult result = distributor.toggle(event.getPos(), event.getFace() == null ? Direction.UP : event.getFace());
        switch (result) {
            case LINKED -> {
                player.sendOverlayMessage(Component.translatable("nautec.distributor.link.linked", name).withStyle(ChatFormatting.AQUA));
                level.playSound(null, event.getPos(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8F, 1.4F);
            }
            case UNLINKED -> {
                player.sendOverlayMessage(Component.translatable("nautec.distributor.link.unlinked", name).withStyle(ChatFormatting.GRAY));
                level.playSound(null, event.getPos(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 0.8F, 0.8F);
            }
            case TOO_FAR -> player.sendOverlayMessage(Component.translatable("nautec.distributor.link.too_far", NTConfig.distributorRange)
                    .withStyle(ChatFormatting.RED));
            case FULL -> player.sendOverlayMessage(Component.translatable("nautec.distributor.link.full", NTConfig.distributorMaxLinks)
                    .withStyle(ChatFormatting.RED));
            case SELF -> {
            }
        }
        PENDING.put(player.getUUID(), new Pending(source, level.getGameTime() + TIMEOUT));
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PENDING.clear();
    }
}
