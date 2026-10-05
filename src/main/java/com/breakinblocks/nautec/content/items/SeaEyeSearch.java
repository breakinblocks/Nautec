package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class SeaEyeSearch {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "NauTec Eye of the Sea search");
        thread.setDaemon(true);
        thread.setPriority(Thread.NORM_PRIORITY - 1);
        return thread;
    });
    private static final Set<UUID> PENDING = ConcurrentHashMap.newKeySet();

    private SeaEyeSearch() {
    }

    public static boolean isSearching(UUID player) {
        return PENDING.contains(player);
    }

    public static boolean start(ServerLevel level, UUID player, BlockPos origin, TagKey<Structure> structures, int radius,
                                Consumer<Optional<BlockPos>> onMainThread) {
        if (!PENDING.add(player)) {
            return false;
        }
        Request request = Request.capture(level, origin, structures, radius);
        MinecraftServer server = level.getServer();
        CompletableFuture.supplyAsync(request::run, EXECUTOR).whenComplete((found, error) -> server.execute(() -> {
            PENDING.remove(player);
            if (error != null) {
                Nautec.LOGGER.error("Eye of the Sea search failed", error);
            }
            onMainThread.accept(error == null ? Optional.ofNullable(found) : Optional.empty());
        }));
        return true;
    }

    public static @Nullable BlockPos searchNow(ServerLevel level, BlockPos origin, TagKey<Structure> structures, int radius) {
        return Request.capture(level, origin, structures, radius).run();
    }

    private record Request(MinecraftServer server, BlockPos origin, int radius, long seed,
                           Map<RandomSpreadStructurePlacement, List<Holder<Structure>>> placements,
                           ChunkGeneratorStructureState generatorState, RegistryAccess registryAccess, ChunkGenerator generator,
                           BiomeSource biomeSource, RandomState randomState, StructureTemplateManager templates,
                           LevelHeightAccessor height) {
        static Request capture(ServerLevel level, BlockPos origin, TagKey<Structure> structures, int radius) {
            ChunkGeneratorStructureState generatorState = level.getChunkSource().getGeneratorState();
            Map<RandomSpreadStructurePlacement, List<Holder<Structure>>> placements = new LinkedHashMap<>();
            boolean generate = level.getServer().getWorldGenSettings().options().generateStructures();
            Optional<HolderSet.Named<Structure>> tag = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).get(structures);
            if (generate && tag.isPresent()) {
                for (Holder<Structure> structure : tag.get()) {
                    for (StructurePlacement placement : generatorState.getPlacementsForStructure(structure)) {
                        if (placement instanceof RandomSpreadStructurePlacement spread) {
                            placements.computeIfAbsent(spread, key -> new ArrayList<>()).add(structure);
                        }
                    }
                }
            }
            ChunkGenerator generator = level.getChunkSource().getGenerator();
            return new Request(level.getServer(), origin.immutable(), radius, generatorState.getLevelSeed(), placements, generatorState,
                    level.registryAccess(), generator, generator.getBiomeSource(), level.getChunkSource().randomState(),
                    level.getStructureManager(), level);
        }

        @Nullable BlockPos run() {
            if (placements.isEmpty()) {
                return null;
            }
            long started = System.nanoTime();
            Stats stats = new Stats();
            BlockPos found = search(stats);
            Nautec.LOGGER.debug("Eye of the Sea search from {} found {} after {} rings, {} candidates, {} full checks in {} ms",
                    origin, found, stats.rings, stats.candidates, stats.fullChecks, (System.nanoTime() - started) / 1_000_000L);
            return found;
        }

        private @Nullable BlockPos search(Stats stats) {
            int originX = SectionPos.blockToSectionCoord(origin.getX());
            int originZ = SectionPos.blockToSectionCoord(origin.getZ());
            for (int ring = 0; ring <= radius; ring++) {
                if (!server.isRunning()) {
                    return null;
                }
                stats.rings = ring + 1;
                BlockPos nearest = null;
                double nearestDistance = Double.MAX_VALUE;
                for (Map.Entry<RandomSpreadStructurePlacement, List<Holder<Structure>>> entry : placements.entrySet()) {
                    BlockPos found = searchRing(entry.getKey(), entry.getValue(), originX, originZ, ring, stats);
                    if (found != null) {
                        double distance = origin.distSqr(found);
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearest = found;
                        }
                    }
                }
                if (nearest != null) {
                    return nearest;
                }
            }
            return null;
        }

        private @Nullable BlockPos searchRing(RandomSpreadStructurePlacement placement, List<Holder<Structure>> structures,
                                              int originX, int originZ, int ring, Stats stats) {
            int spacing = placement.spacing();
            for (int x = -ring; x <= ring; x++) {
                boolean xEdge = x == -ring || x == ring;
                for (int z = -ring; z <= ring; z++) {
                    if (!xEdge && z != -ring && z != ring) {
                        continue;
                    }
                    ChunkPos candidate = placement.getPotentialStructureChunk(seed, originX + spacing * x, originZ + spacing * z);
                    if (!placement.isStructureChunk(generatorState, candidate.x(), candidate.z())) {
                        continue;
                    }
                    stats.candidates++;
                    for (Holder<Structure> structure : structures) {
                        if (!biomeAllows(structure.value(), candidate)) {
                            continue;
                        }
                        stats.fullChecks++;
                        if (canGenerate(structure.value(), candidate)) {
                            return placement.getLocatePos(candidate);
                        }
                    }
                }
            }
            return null;
        }

        private boolean biomeAllows(Structure structure, ChunkPos chunk) {
            int y = QuartPos.fromBlock(generator.getSeaLevel());
            return structure.biomes().contains(biomeSource.getNoiseBiome(QuartPos.fromBlock(chunk.getMiddleBlockX()), y,
                            QuartPos.fromBlock(chunk.getMiddleBlockZ()), randomState.sampler()))
                    || structure.biomes().contains(biomeSource.getNoiseBiome(QuartPos.fromBlock(chunk.getMinBlockX()), y,
                            QuartPos.fromBlock(chunk.getMinBlockZ()), randomState.sampler()));
        }

        private boolean canGenerate(Structure structure, ChunkPos chunk) {
            return structure.findValidGenerationPoint(new Structure.GenerationContext(registryAccess, generator, biomeSource, randomState,
                    templates, seed, chunk, height, structure.biomes()::contains)).isPresent();
        }
    }

    private static final class Stats {
        int rings;
        int candidates;
        int fullChecks;
    }
}
