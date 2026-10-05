package com.breakinblocks.nautec.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class ServerPacketGuards {
    private static final Map<Player, Map<String, Long>> LAST_ACCEPTED = new WeakHashMap<>();
    private static final Map<Player, BlockPos> OPEN_GATEWAYS = new WeakHashMap<>();

    private ServerPacketGuards() {
    }

    public static boolean allow(ServerPlayer player, String key, int intervalTicks) {
        long now = player.level().getGameTime();
        Map<String, Long> times = LAST_ACCEPTED.computeIfAbsent(player, p -> new HashMap<>());
        Long last = times.get(key);
        if (last != null && now - last < intervalTicks && now >= last) {
            return false;
        }
        times.put(key, now);
        return true;
    }

    public static void openGateway(ServerPlayer player, BlockPos pos) {
        OPEN_GATEWAYS.put(player, pos.immutable());
    }

    public static @Nullable BlockPos openGateway(ServerPlayer player) {
        return OPEN_GATEWAYS.get(player);
    }
}
