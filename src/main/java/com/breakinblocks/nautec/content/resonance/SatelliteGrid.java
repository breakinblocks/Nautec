package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.HashMap;
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

    private SatelliteGrid() {
    }

    public record Link(int total, float purity, int share, int uplinks, int downlinks) {
    }

    private record Key(UUID network, ResourceKey<Level> dimension) {
    }

    private record Cached(long tick, Link link) {
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
    public static void onServerStopped(ServerStoppedEvent event) {
        ARRAYS.clear();
        CACHE.clear();
    }
}
