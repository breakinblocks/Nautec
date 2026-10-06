package com.breakinblocks.nautec.content.conduits;

import com.breakinblocks.nautec.NTConfig;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class ConduitNetworks {
    private static final Reference2ObjectOpenHashMap<ResourceKey<Level>, LevelNetworks> LEVELS = new Reference2ObjectOpenHashMap<>();

    private static final class LevelNetworks {
        private final Long2ObjectOpenHashMap<ConduitNetwork> index = new Long2ObjectOpenHashMap<>();
        private final Long2ObjectOpenHashMap<ObjectArrayList<ConduitNetwork>> byChunk = new Long2ObjectOpenHashMap<>();
        private final ObjectArrayList<ConduitNetwork> scratch = new ObjectArrayList<>();

        private void register(ConduitNetwork network) {
            for (long member : network.members()) {
                ConduitNetwork previous = index.put(member, network);
                if (previous != null && previous != network) {
                    retire(previous);
                }
            }
            for (long chunk : network.chunks()) {
                ObjectArrayList<ConduitNetwork> list = byChunk.get(chunk);
                if (list == null) {
                    list = new ObjectArrayList<>(2);
                    byChunk.put(chunk, list);
                }
                list.add(network);
            }
        }

        private void retire(@Nullable ConduitNetwork network) {
            if (network == null || network.dirty()) {
                return;
            }
            network.markDirty();
            for (long member : network.members()) {
                if (index.get(member) == network) {
                    index.remove(member);
                }
            }
            for (long chunk : network.chunks()) {
                ObjectArrayList<ConduitNetwork> list = byChunk.get(chunk);
                if (list != null) {
                    list.remove(network);
                    if (list.isEmpty()) {
                        byChunk.remove(chunk);
                    }
                }
            }
        }

        private void retireChunk(long chunk) {
            ObjectArrayList<ConduitNetwork> list = byChunk.get(chunk);
            if (list == null || list.isEmpty()) {
                return;
            }
            scratch.clear();
            scratch.addAll(list);
            for (int i = 0; i < scratch.size(); i++) {
                retire(scratch.get(i));
            }
            scratch.clear();
        }
    }

    private ConduitNetworks() {
    }

    private static LevelNetworks of(Level level) {
        LevelNetworks networks = LEVELS.get(level.dimension());
        if (networks == null) {
            networks = new LevelNetworks();
            LEVELS.put(level.dimension(), networks);
        }
        return networks;
    }

    public static ConduitNetwork get(ServerLevel level, BlockPos tap) {
        LevelNetworks networks = of(level);
        ConduitNetwork network = networks.index.get(tap.asLong());
        if (network != null && !network.dirty()) {
            return network;
        }
        ConduitNetwork built = build(level, tap);
        networks.register(built);
        return built;
    }

    public static @Nullable ConduitNetwork peek(Level level, BlockPos pos) {
        LevelNetworks networks = LEVELS.get(level.dimension());
        return networks == null ? null : networks.index.get(pos.asLong());
    }

    public static void routesChanged(Level level, BlockPos tap) {
        ConduitNetwork network = peek(level, tap);
        if (network != null) {
            network.markRoutesDirty();
        }
    }

    public static void invalidate(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        LevelNetworks networks = LEVELS.get(level.dimension());
        if (networks == null) {
            return;
        }
        long packed = pos.asLong();
        networks.retire(networks.index.get(packed));
        for (Direction direction : ConduitPartBlock.DIRECTIONS) {
            networks.retire(networks.index.get(BlockPos.offset(packed, direction)));
        }
    }

    public static void chunkLoaded(Level level, ChunkPos chunk) {
        LevelNetworks networks = level.isClientSide() ? null : LEVELS.get(level.dimension());
        if (networks == null || networks.byChunk.isEmpty()) {
            return;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                networks.retireChunk(ChunkPos.pack(chunk.x() + dx, chunk.z() + dz));
            }
        }
    }

    public static void chunkUnloaded(Level level, ChunkPos chunk) {
        LevelNetworks networks = level.isClientSide() ? null : LEVELS.get(level.dimension());
        if (networks != null && !networks.byChunk.isEmpty()) {
            networks.retireChunk(ChunkPos.pack(chunk.x(), chunk.z()));
        }
    }

    public static void clear(Level level) {
        LEVELS.remove(level.dimension());
    }

    public static void clear() {
        LEVELS.clear();
    }

    private static boolean links(BlockState state, Direction direction) {
        if (state.getBlock() instanceof CurrentConduitBlock) {
            return state.getValue(CurrentConduitBlock.ARMS[direction.ordinal()]) == ConduitArm.CONNECTED;
        }
        if (state.getBlock() instanceof ConduitTapBlock) {
            return state.getValue(ConduitTapBlock.ARMS[direction.ordinal()]) == TapArm.CONDUIT;
        }
        return false;
    }

    private static ConduitNetwork build(ServerLevel level, BlockPos start) {
        int limit = Math.max(2, NTConfig.conduitMaxNetworkSize);
        LongOpenHashSet visited = new LongOpenHashSet();
        LongArrayList members = new LongArrayList();
        LongArrayList taps = new LongArrayList();
        LongOpenHashSet chunks = new LongOpenHashSet();
        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos next = new BlockPos.MutableBlockPos();
        boolean truncated = false;
        long origin = start.asLong();
        visited.add(origin);
        queue.enqueue(origin);
        while (!queue.isEmpty()) {
            long packed = queue.dequeueLong();
            cursor.set(packed);
            BlockState state = level.getBlockState(cursor);
            if (!ConduitTapBlock.isConduitPart(state)) {
                continue;
            }
            members.add(packed);
            chunks.add(ChunkPos.pack(cursor));
            if (state.getBlock() instanceof ConduitTapBlock) {
                taps.add(packed);
            }
            for (Direction direction : ConduitPartBlock.DIRECTIONS) {
                if (!links(state, direction)) {
                    continue;
                }
                long nextPacked = BlockPos.offset(packed, direction);
                if (visited.contains(nextPacked)) {
                    continue;
                }
                next.set(nextPacked);
                if (!level.isLoaded(next)) {
                    continue;
                }
                BlockState nextState = level.getBlockState(next);
                if (!links(nextState, direction.getOpposite())) {
                    continue;
                }
                if (visited.size() >= limit) {
                    truncated = true;
                    continue;
                }
                visited.add(nextPacked);
                queue.enqueue(nextPacked);
            }
        }
        return new ConduitNetwork(taps.toLongArray(), members.toLongArray(), chunks.toLongArray(), truncated);
    }
}
