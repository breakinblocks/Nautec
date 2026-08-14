package com.breakinblocks.nautec.events;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.LuckyFishingZoneBlockEntity;
import com.breakinblocks.nautec.content.blocks.LuckyFishingZoneBlock;
import com.breakinblocks.nautec.content.fishing.LuckyZoneIndex;
import com.breakinblocks.nautec.mixin.FishingHookAccessor;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Nautec.MODID)
public final class LuckyFishingZoneEvents {
    private static final Map<UUID, Integer> COOLDOWNS = new HashMap<>();
    private static final RandomSource RANDOM = RandomSource.create();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!NTConfig.luckyZonesEnabled || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        int cooldown = COOLDOWNS.getOrDefault(player.getUUID(), 0);
        if (cooldown > 0) {
            COOLDOWNS.put(player.getUUID(), cooldown - 1);
            return;
        }

        ServerLevel level = player.level();
        expireZones(level);

        if (!trySpawn(level, player)) {
            COOLDOWNS.put(player.getUUID(), 40);
            return;
        }
        COOLDOWNS.put(player.getUUID(), NTConfig.luckyZoneIntervalSeconds * 20);
    }

    private static boolean trySpawn(ServerLevel level, ServerPlayer player) {
        int range = NTConfig.luckyZoneSpawnDistance;
        int offsetX = RANDOM.nextInt(range * 2 + 1) - range;
        int offsetZ = RANDOM.nextInt(range * 2 + 1) - range;
        BlockPos candidate = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE,
                player.blockPosition().offset(offsetX, 0, offsetZ));

        if (!level.getBiome(candidate).is(BiomeTags.IS_OCEAN) && !level.getBiome(candidate).is(BiomeTags.IS_RIVER)) {
            return false;
        }

        LuckyZoneIndex index = LuckyZoneIndex.get(level);
        if (index.hasZoneWithin(candidate, NTConfig.luckyZoneMinSeparation)) {
            return false;
        }

        ChunkPos chunk = new ChunkPos(SectionPos.blockToSectionCoord(candidate.getX()),
                SectionPos.blockToSectionCoord(candidate.getZ()));
        if (index.countInChunk(chunk) >= NTConfig.luckyZonesPerChunk) {
            return false;
        }

        int radius = largestOpenRadius(level, candidate);
        if (radius < NTConfig.luckyZoneMinRadius) {
            return false;
        }

        place(level, candidate, radius, index);
        return true;
    }

    public static int largestOpenRadius(ServerLevel level, BlockPos centre) {
        if (!LuckyFishingZoneBlock.isWaterSurface(level, centre)) {
            return 0;
        }

        int best = 0;
        for (int radius = NTConfig.luckyZoneMinRadius; radius <= NTConfig.luckyZoneMaxRadius; radius++) {
            if (!ringIsOpenWater(level, centre, radius)) {
                break;
            }
            best = radius;
        }
        return best;
    }

    private static boolean ringIsOpenWater(ServerLevel level, BlockPos centre, int radius) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) {
                    continue;
                }
                cursor.set(centre.getX() + dx, centre.getY(), centre.getZ() + dz);
                if (!LuckyFishingZoneBlock.isWaterSurface(level, cursor)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void place(ServerLevel level, BlockPos pos, int radius, LuckyZoneIndex index) {
        level.setBlock(pos, NTBlocks.LUCKY_FISHING_ZONE.get().defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof LuckyFishingZoneBlockEntity zone) {
            zone.setRadius(radius);
        }
        index.add(new LuckyZoneIndex.Zone(pos, radius,
                level.getGameTime() + NTConfig.luckyZoneLifetimeSeconds * 20L));
    }

    private static void expireZones(ServerLevel level) {
        LuckyZoneIndex index = LuckyZoneIndex.get(level);
        if (index.isEmpty()) {
            return;
        }
        for (LuckyZoneIndex.Zone zone : index.expired(level.getGameTime())) {
            if (level.isLoaded(zone.pos()) && level.getBlockState(zone.pos()).is(NTBlocks.LUCKY_FISHING_ZONE.get())) {
                level.removeBlock(zone.pos(), false);
            }
            index.remove(zone.pos());
        }
    }

    @SubscribeEvent
    public static void onHookTick(EntityTickEvent.Post event) {
        if (!NTConfig.luckyZonesEnabled || NTConfig.luckyZoneBiteSpeed <= 1
                || !(event.getEntity() instanceof FishingHook hook)
                || !(hook.level() instanceof ServerLevel level)) {
            return;
        }

        FishingHookAccessor accessor = (FishingHookAccessor) hook;
        int lured = accessor.nautec$getTimeUntilLured();
        int hooked = accessor.nautec$getTimeUntilHooked();
        if (lured <= 1 && hooked <= 1) {
            return;
        }

        if (LuckyZoneIndex.get(level).zoneAt(level, hook.blockPosition()) == null) {
            return;
        }

        int extra = NTConfig.luckyZoneBiteSpeed - 1;
        if (lured > 1) {
            accessor.nautec$setTimeUntilLured(Math.max(1, lured - extra));
        } else {
            accessor.nautec$setTimeUntilHooked(Math.max(1, hooked - extra));
        }
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        if (!NTConfig.luckyZoneConsumedOnCatch || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        BlockPos hook = event.getHookEntity().blockPosition();
        LuckyZoneIndex index = LuckyZoneIndex.get(level);
        LuckyZoneIndex.Zone zone = index.zoneAt(level, hook);
        if (zone == null) {
            return;
        }

        level.sendParticles(ParticleTypes.SPLASH,
                zone.pos().getX() + 0.5, zone.pos().getY() + 0.1, zone.pos().getZ() + 0.5,
                24, zone.radius() * 0.6, 0.1, zone.radius() * 0.6, 0.1);
        level.removeBlock(zone.pos(), false);
        index.remove(zone.pos());
    }
}
