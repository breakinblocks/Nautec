package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = Nautec.MODID)
public final class ResonanceGrid {
    private static final Map<UUID, Set<ResonancePylonBlockEntity>> PYLONS = new HashMap<>();

    private ResonanceGrid() {
    }

    public static void join(UUID network, ResonancePylonBlockEntity pylon) {
        PYLONS.computeIfAbsent(network, key -> new LinkedHashSet<>()).add(pylon);
    }

    public static void leave(UUID network, ResonancePylonBlockEntity pylon) {
        Set<ResonancePylonBlockEntity> members = PYLONS.get(network);
        if (members != null) {
            members.remove(pylon);
            if (members.isEmpty()) {
                PYLONS.remove(network);
            }
        }
    }

    public static List<ResonancePylonBlockEntity> members(UUID network) {
        Set<ResonancePylonBlockEntity> members = PYLONS.get(network);
        return members == null ? List.of() : List.copyOf(members);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        ResonanceNetworks networks = ResonanceNetworks.get(server);
        long tick = server.getTickCount();
        Iterator<Map.Entry<UUID, Set<ResonancePylonBlockEntity>>> entries = PYLONS.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<UUID, Set<ResonancePylonBlockEntity>> entry = entries.next();
            entry.getValue().removeIf(pylon -> pylon.isRemoved() || !entry.getKey().equals(pylon.getNetworkId()));
            if (entry.getValue().isEmpty()) {
                entries.remove();
                continue;
            }
            if (networks.get(entry.getKey()) == null) {
                continue;
            }
            List<ResonanceEndpoint> endpoints = new ArrayList<>(entry.getValue());
            transfer(endpoints, tick);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PYLONS.clear();
    }

    public static double factor(ResonanceEndpoint from, ResonanceEndpoint to) {
        if (from.dimension().equals(to.dimension())) {
            return 1.0 - NTConfig.resonanceSameDimensionLoss;
        }
        if (from.interdimensional() && to.interdimensional()) {
            return 1.0 - NTConfig.resonanceCrossDimensionLoss;
        }
        return 0.0;
    }

    public static void transfer(List<? extends ResonanceEndpoint> endpoints, long tick) {
        List<ResonanceEndpoint> senders = new ArrayList<>();
        List<ResonanceEndpoint> receivers = new ArrayList<>();
        for (ResonanceEndpoint endpoint : endpoints) {
            (endpoint.sending() ? senders : receivers).add(endpoint);
        }
        if (senders.isEmpty() || receivers.isEmpty()) {
            return;
        }
        int offset = (int) Math.floorMod(tick, (long) receivers.size());
        for (int pass = 0; pass < 2; pass++) {
            boolean sameDimension = pass == 0;
            for (int i = 0; i < receivers.size(); i++) {
                ResonanceEndpoint receiver = receivers.get((i + offset) % receivers.size());
                for (ResonanceEndpoint sender : senders) {
                    if (sender.dimension().equals(receiver.dimension()) != sameDimension) {
                        continue;
                    }
                    int demand = receiver.receivable();
                    if (demand <= 0) {
                        break;
                    }
                    double factor = factor(sender, receiver);
                    if (factor <= 0.0) {
                        continue;
                    }
                    int needed = (int) Math.min(Integer.MAX_VALUE, (long) Math.ceil(demand / factor));
                    int taken = Math.min(needed, sender.sendable());
                    if (taken <= 0) {
                        continue;
                    }
                    int delivered = Math.min(demand, (int) Math.floor(taken * factor));
                    sender.send(taken);
                    receiver.receive(delivered);
                }
            }
        }
    }
}
