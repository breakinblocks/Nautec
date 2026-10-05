package com.breakinblocks.nautec.content.conduit;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Nautec.MODID)
public final class ConduitBeaconTracker {
    private static final Map<ResourceKey<Level>, Set<BlockPos>> ACTIVE = new ConcurrentHashMap<>();
    private static final Set<EntitySpawnReason> BLOCKED_REASONS = Set.of(EntitySpawnReason.NATURAL, EntitySpawnReason.CHUNK_GENERATION,
            EntitySpawnReason.PATROL);
    private static final Set<MobCategory> BLOCKED_CATEGORIES = Set.of(MobCategory.MONSTER, MobCategory.CREATURE, MobCategory.AMBIENT);

    private ConduitBeaconTracker() {
    }

    public static void add(ServerLevel level, BlockPos pos) {
        ACTIVE.computeIfAbsent(level.dimension(), key -> ConcurrentHashMap.newKeySet()).add(pos.immutable());
    }

    public static void remove(ServerLevel level, BlockPos pos) {
        Set<BlockPos> beacons = ACTIVE.get(level.dimension());
        if (beacons != null) {
            beacons.remove(pos);
        }
    }

    public static boolean protects(ResourceKey<Level> dimension, BlockPos pos) {
        Set<BlockPos> beacons = ACTIVE.get(dimension);
        if (beacons == null || beacons.isEmpty()) {
            return false;
        }
        long radius = NTConfig.conduitBeaconSpawnRadius;
        long reach = radius * radius;
        for (BlockPos beacon : beacons) {
            long dx = beacon.getX() - pos.getX();
            long dz = beacon.getZ() - pos.getZ();
            if (dx * dx + dz * dz <= reach) {
                return true;
            }
        }
        return false;
    }

    public static boolean blocks(ResourceKey<Level> dimension, MobCategory category, EntitySpawnReason reason, BlockPos pos) {
        return BLOCKED_CATEGORIES.contains(category) && BLOCKED_REASONS.contains(reason) && protects(dimension, pos);
    }

    @SubscribeEvent
    public static void onSpawnPlacementCheck(MobSpawnEvent.SpawnPlacementCheck event) {
        if (blocks(event.getLevel().getLevel().dimension(), event.getEntityType().getCategory(), event.getSpawnType(), event.getPos())) {
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
        }
    }

    @SubscribeEvent
    public static void onPositionCheck(MobSpawnEvent.PositionCheck event) {
        if (blocks(event.getLevel().getLevel().dimension(), event.getEntity().getType().getCategory(), event.getSpawnType(),
                event.getEntity().blockPosition())) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ACTIVE.clear();
    }
}
