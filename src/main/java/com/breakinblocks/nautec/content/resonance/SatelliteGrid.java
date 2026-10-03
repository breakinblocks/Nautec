package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.registries.NTCriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = Nautec.MODID)
public final class SatelliteGrid {
    public static final Link EMPTY = new Link(0, 0F, 0, 0, 0);

    private static final Map<UUID, Set<SatelliteArrayBlockEntity>> ARRAYS = new HashMap<>();
    private static final Map<Key, Cached> CACHE = new HashMap<>();
    private static final Map<UUID, CharmEntry> CHARMS = new HashMap<>();

    private SatelliteGrid() {
    }

    public record Link(int total, float purity, int share, int uplinks, int downlinks) {
    }

    private record Key(UUID network, ResourceKey<Level> dimension) {
    }

    private record Cached(long tick, Link link) {
    }

    private record CharmEntry(UUID network, long seen) {
    }

    private interface Receiver {
        int priority();

        int demand();

        int deliver(int amount);
    }

    public static void join(UUID network, SatelliteArrayBlockEntity array) {
        ARRAYS.computeIfAbsent(network, key -> new LinkedHashSet<>()).add(array);
    }

    public static void leave(UUID network, SatelliteArrayBlockEntity array) {
        Set<SatelliteArrayBlockEntity> members = ARRAYS.get(network);
        if (members != null) {
            members.remove(array);
            if (members.isEmpty()) {
                ARRAYS.remove(network);
            }
        }
    }

    public static List<SatelliteArrayBlockEntity> members(UUID network) {
        Set<SatelliteArrayBlockEntity> members = ARRAYS.get(network);
        return members == null ? List.of() : List.copyOf(members);
    }

    public static void charm(ServerPlayer player, UUID network) {
        CHARMS.put(player.getUUID(), new CharmEntry(network, player.level().getGameTime()));
    }

    public static boolean clearSky(Level level, BlockPos top) {
        return level.getHeight(Heightmap.Types.OCEAN_FLOOR, top.getX(), top.getZ()) <= top.getY() + 1;
    }

    public static Link link(UUID network, ResourceKey<Level> dimension, long tick) {
        Key key = new Key(network, dimension);
        Cached cached = CACHE.get(key);
        if (cached != null && cached.tick() == tick) {
            return cached.link();
        }
        Link link = compute(members(network), dimension);
        CACHE.put(key, new Cached(tick, link));
        return link;
    }

    public static Link compute(List<SatelliteArrayBlockEntity> members, ResourceKey<Level> dimension) {
        long total = 0;
        double weighted = 0;
        int uplinks = 0;
        int downlinks = 0;
        for (SatelliteArrayBlockEntity array : members) {
            if (array.isRemoved() || !dimension.equals(array.dimension())) {
                continue;
            }
            if (array.isUplink()) {
                if (array.transmitting()) {
                    uplinks++;
                    int power = Math.max(0, array.getPower());
                    total += power;
                    weighted += (double) power * array.getPurity();
                }
            } else if (array.hasSky()) {
                downlinks++;
            }
        }
        int capped = (int) Math.min(Integer.MAX_VALUE, total);
        float purity = total > 0 ? (float) (weighted / total) : 0F;
        int share = downlinks == 0 || uplinks == 0 ? 0 : (int) Math.floor(capped * (1.0 - NTConfig.satelliteLoss) / downlinks);
        return new Link(capped, purity, share, uplinks, downlinks);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        long tick = server.overworld().getGameTime();
        boolean charmTick = tick % ResonanceCharmItem.INTERVAL == 0;
        Map<UUID, Integer> charmDelivered = new HashMap<>();
        Map<UUID, ServerPlayer> charmPlayers = new HashMap<>();
        if (charmTick) {
            Iterator<Map.Entry<UUID, CharmEntry>> charms = CHARMS.entrySet().iterator();
            while (charms.hasNext()) {
                Map.Entry<UUID, CharmEntry> entry = charms.next();
                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                if (player == null || tick - entry.getValue().seen() > ResonanceCharmItem.INTERVAL * 2L) {
                    charms.remove();
                    continue;
                }
                charmPlayers.put(entry.getKey(), player);
            }
        }

        for (Map.Entry<UUID, Set<SatelliteArrayBlockEntity>> entry : new ArrayList<>(ARRAYS.entrySet())) {
            UUID network = entry.getKey();
            Map<ResourceKey<Level>, List<SatelliteArrayBlockEntity>> byDimension = new LinkedHashMap<>();
            for (SatelliteArrayBlockEntity array : entry.getValue()) {
                if (!array.isRemoved() && array.getLevel() != null) {
                    byDimension.computeIfAbsent(array.dimension(), key -> new ArrayList<>()).add(array);
                }
            }
            for (Map.Entry<ResourceKey<Level>, List<SatelliteArrayBlockEntity>> group : byDimension.entrySet()) {
                List<ServerPlayer> players = new ArrayList<>();
                if (charmTick) {
                    for (Map.Entry<UUID, ServerPlayer> charm : charmPlayers.entrySet()) {
                        CharmEntry charmEntry = CHARMS.get(charm.getKey());
                        if (charmEntry != null && charmEntry.network().equals(network) && charm.getValue().level().dimension().equals(group.getKey())) {
                            players.add(charm.getValue());
                        }
                    }
                }
                transfer(group.getValue(), players, tick, charmDelivered);
            }
        }

        if (charmTick) {
            for (Map.Entry<UUID, ServerPlayer> charm : charmPlayers.entrySet()) {
                ServerPlayer player = charm.getValue();
                ItemStack stack = ResonanceCharmItem.equipped(player);
                if (stack.isEmpty()) {
                    continue;
                }
                int satellite = charmDelivered.getOrDefault(charm.getKey(), 0);
                int budget = NTConfig.charmTransferRate * ResonanceCharmItem.INTERVAL - satellite;
                int pylons = budget > 0 ? ResonanceCharmItem.chargeFromPylons(player, stack, budget) : 0;
                if (satellite + pylons > 0) {
                    ResonanceCharmItem.charged(player);
                    NTCriteriaTriggers.CHARM_CHARGED.get().trigger(player);
                }
            }
        }
    }

    private static void transfer(List<SatelliteArrayBlockEntity> arrays, List<ServerPlayer> players, long tick, Map<UUID, Integer> charmDelivered) {
        List<SatelliteArrayBlockEntity> senders = new ArrayList<>();
        List<SatelliteArrayBlockEntity> downlinks = new ArrayList<>();
        for (SatelliteArrayBlockEntity array : arrays) {
            array.beginTransfer();
            if (array.transmitting()) {
                senders.add(array);
            } else if (array.receiving()) {
                downlinks.add(array);
            }
        }
        if (!senders.isEmpty()) {
            int offset = (int) Math.floorMod(tick, (long) senders.size());
            List<SatelliteArrayBlockEntity> rotated = new ArrayList<>(senders.size());
            for (int i = 0; i < senders.size(); i++) {
                rotated.add(senders.get((i + offset) % senders.size()));
            }
            moveAp(rotated, downlinks);
            moveFe(rotated, downlinks, players, charmDelivered);
        }
        for (SatelliteArrayBlockEntity array : arrays) {
            array.finishTransfer();
        }
    }

    private static double factor() {
        return 1.0 - NTConfig.satelliteLoss;
    }

    private static void moveAp(List<SatelliteArrayBlockEntity> senders, List<SatelliteArrayBlockEntity> downlinks) {
        long supply = 0;
        double weighted = 0;
        for (SatelliteArrayBlockEntity sender : senders) {
            supply += sender.getApStored();
            weighted += (double) sender.getApStored() * sender.getApPurity();
        }
        if (supply <= 0 || downlinks.isEmpty()) {
            return;
        }
        float purity = (float) (weighted / supply);
        List<Receiver> receivers = new ArrayList<>();
        for (SatelliteArrayBlockEntity downlink : downlinks) {
            receivers.add(new Receiver() {
                @Override
                public int priority() {
                    return downlink.getPriority();
                }

                @Override
                public int demand() {
                    return downlink.apDemand();
                }

                @Override
                public int deliver(int amount) {
                    downlink.giveAp(amount, purity);
                    return amount;
                }
            });
        }
        long delivered = allocate(receivers, (long) Math.floor(supply * factor()));
        long cost = Math.min(supply, (long) Math.ceil(delivered / factor()));
        for (SatelliteArrayBlockEntity sender : senders) {
            if (cost <= 0) {
                break;
            }
            cost -= sender.takeAp((int) Math.min(Integer.MAX_VALUE, cost));
        }
    }

    private static void moveFe(List<SatelliteArrayBlockEntity> senders, List<SatelliteArrayBlockEntity> downlinks, List<ServerPlayer> players,
                               Map<UUID, Integer> charmDelivered) {
        long supply = 0;
        for (SatelliteArrayBlockEntity sender : senders) {
            supply += sender.getEnergy().getAmountAsInt();
        }
        if (supply <= 0 || (downlinks.isEmpty() && players.isEmpty())) {
            return;
        }
        List<Receiver> receivers = new ArrayList<>();
        for (SatelliteArrayBlockEntity downlink : downlinks) {
            receivers.add(new Receiver() {
                @Override
                public int priority() {
                    return downlink.getPriority();
                }

                @Override
                public int demand() {
                    return downlink.feDemand();
                }

                @Override
                public int deliver(int amount) {
                    downlink.giveFe(amount);
                    return amount;
                }
            });
        }
        for (ServerPlayer player : players) {
            ItemStack charm = ResonanceCharmItem.equipped(player);
            ResonanceNetwork network = ResonanceCharmItem.network(player, charm);
            if (charm.isEmpty() || network == null) {
                continue;
            }
            int demand = ResonanceCharmItem.demand(player, charm, NTConfig.charmTransferRate * ResonanceCharmItem.INTERVAL);
            receivers.add(new Receiver() {
                @Override
                public int priority() {
                    return ResonanceCharmItem.priority(charm);
                }

                @Override
                public int demand() {
                    return demand;
                }

                @Override
                public int deliver(int amount) {
                    int used = ResonanceCharmItem.deliver(player, charm, amount);
                    charmDelivered.merge(player.getUUID(), used, Integer::sum);
                    return used;
                }
            });
        }
        long delivered = allocate(receivers, (long) Math.floor(supply * factor()));
        long cost = Math.min(supply, (long) Math.ceil(delivered / factor()));
        for (SatelliteArrayBlockEntity sender : senders) {
            if (cost <= 0) {
                break;
            }
            cost -= sender.takeFe((int) Math.min(Integer.MAX_VALUE, cost));
        }
    }

    private static long allocate(List<Receiver> receivers, long available) {
        List<Receiver> sorted = new ArrayList<>(receivers);
        sorted.sort(Comparator.comparingInt(Receiver::priority).reversed());
        long delivered = 0;
        int index = 0;
        while (index < sorted.size() && available > 0) {
            int priority = sorted.get(index).priority();
            List<Receiver> tier = new ArrayList<>();
            List<Integer> needs = new ArrayList<>();
            while (index < sorted.size() && sorted.get(index).priority() == priority) {
                Receiver receiver = sorted.get(index++);
                int need = receiver.demand();
                if (need > 0) {
                    tier.add(receiver);
                    needs.add(need);
                }
            }
            int[] shares = new int[tier.size()];
            long left = available;
            List<Integer> open = new ArrayList<>();
            for (int i = 0; i < tier.size(); i++) {
                open.add(i);
            }
            while (left > 0 && !open.isEmpty()) {
                long each = Math.max(1, left / open.size());
                Iterator<Integer> it = open.iterator();
                while (it.hasNext() && left > 0) {
                    int i = it.next();
                    int room = needs.get(i) - shares[i];
                    int give = (int) Math.min(Math.min(each, room), left);
                    shares[i] += give;
                    left -= give;
                    if (shares[i] >= needs.get(i)) {
                        it.remove();
                    }
                }
            }
            for (int i = 0; i < tier.size(); i++) {
                if (shares[i] > 0) {
                    int used = tier.get(i).deliver(shares[i]);
                    delivered += used;
                    available -= used;
                }
            }
        }
        return delivered;
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ARRAYS.clear();
        CACHE.clear();
        CHARMS.clear();
    }
}
