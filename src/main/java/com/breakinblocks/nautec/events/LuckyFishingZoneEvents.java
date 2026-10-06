package com.breakinblocks.nautec.events;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.compat.starcatcher.StarcatcherCompat;
import com.breakinblocks.nautec.content.fishing.FishingMinigame;
import com.breakinblocks.nautec.content.blockentities.LuckyFishingZoneBlockEntity;
import com.breakinblocks.nautec.content.blocks.LuckyFishingZoneBlock;
import com.breakinblocks.nautec.content.entities.NautecFishingHook;
import com.breakinblocks.nautec.content.fishing.LuckyZoneIndex;
import com.breakinblocks.nautec.mixin.FishingHookAccessor;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Nautec.MODID)
public final class LuckyFishingZoneEvents {
    private static final Map<UUID, Integer> COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Long> BOOSTS = new HashMap<>();
    private static final RandomSource RANDOM = RandomSource.create();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!NTConfig.luckyZonesEnabled || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        int cooldown = COOLDOWNS.getOrDefault(player.getUUID(), 0);
        if (cooldown > 0) {
            COOLDOWNS.put(player.getUUID(), cooldown - zoneRateMultiplier(player));
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

    public static void boost(Player player, int ticks) {
        if (ticks <= 0) {
            return;
        }
        BOOSTS.merge(player.getUUID(), player.level().getGameTime() + ticks, Math::max);
    }

    public static int zoneRateMultiplier(Player player) {
        Long until = BOOSTS.get(player.getUUID());
        if (until == null) {
            return 1;
        }
        if (player.level().getGameTime() >= until) {
            BOOSTS.remove(player.getUUID());
            return 1;
        }
        return Math.max(1, NTConfig.eyeOfTheSeaLuckyZoneMultiplier);
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
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        COOLDOWNS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        COOLDOWNS.clear();
        BOOSTS.clear();
    }

    @SubscribeEvent
    public static void onHookTick(EntityTickEvent.Post event) {
        if (!NTConfig.luckyZonesEnabled
                || !(event.getEntity() instanceof FishingHook hook)
                || !(hook.level() instanceof ServerLevel level)) {
            return;
        }

        FishingHookAccessor accessor = (FishingHookAccessor) hook;
        int lured = accessor.nautec$getTimeUntilLured();
        int hooked = accessor.nautec$getTimeUntilHooked();
        boolean counting = lured > 1 || hooked > 1;
        boolean floating = hook.isInWater() && hook.getHookedIn() == null;
        if (!counting && !floating) {
            return;
        }

        if (LuckyZoneIndex.get(level).zoneAt(level, hook.blockPosition()) == null) {
            return;
        }

        if (floating) {
            hook.setPos(hook.xo, hook.getY(), hook.zo);
            hook.setDeltaMovement(0.0, hook.getDeltaMovement().y, 0.0);
        }

        if (counting && NTConfig.luckyZoneBiteSpeed > 1) {
            int extra = NTConfig.luckyZoneBiteSpeed - 1;
            if (lured > 1) {
                accessor.nautec$setTimeUntilLured(Math.max(1, lured - extra));
            } else {
                accessor.nautec$setTimeUntilHooked(Math.max(1, hooked - extra));
            }
        }
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        FishingHook hookEntity = event.getHookEntity();
        BlockPos hook = hookEntity.blockPosition();
        LuckyZoneIndex index = LuckyZoneIndex.get(level);
        LuckyZoneIndex.Zone zone = index.zoneAt(level, hook);
        if (zone == null) {
            return;
        }

        if (!hookEntity.isAddedToLevel()) {
            awardDetachedHookBonus(level, event.getEntity(), hookEntity);
        }

        if (!NTConfig.luckyZoneConsumedOnCatch) {
            return;
        }

        level.sendParticles(ParticleTypes.SPLASH,
                zone.pos().getX() + 0.5, zone.pos().getY() + 0.1, zone.pos().getZ() + 0.5,
                24, zone.radius() * 0.6, 0.1, zone.radius() * 0.6, 0.1);
        level.removeBlock(zone.pos(), false);
        index.remove(zone.pos());
    }

    private static void awardDetachedHookBonus(ServerLevel level, Player player, FishingHook hook) {
        ItemStack rod = player.getMainHandItem().isEmpty() ? player.getOffhandItem() : player.getMainHandItem();
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, hook.position())
                .withParameter(LootContextParams.TOOL, rod)
                .withParameter(LootContextParams.THIS_ENTITY, hook)
                .withLuck(player.getLuck())
                .create(LootContextParamSets.FISHING);
        List<ResourceKey<LootTable>> tables = StarcatcherCompat.isStarcatcherCatch(player)
                ? FishingMinigame.rewardTables(true, RANDOM.nextInt(100) < FishingMinigame.TREASURE_CHANCE)
                : List.of(NTLootTables.LUCKY_ZONE);
        for (ResourceKey<LootTable> key : tables) {
            LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
            NautecFishingHook.deliverCatch(level, player, hook, table.getRandomItems(params),
                    hook.getX(), hook.getY() + 1.2, hook.getZ());
        }
    }
}
