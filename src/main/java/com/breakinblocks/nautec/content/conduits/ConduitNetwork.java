package com.breakinblocks.nautec.content.conduits;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public final class ConduitNetwork {
    private static int nextVersion = 1;

    private final long[] taps;
    private final long[] members;
    private final long[] chunks;
    private final boolean truncated;
    private final Routes[] routes = new Routes[ConduitChannel.ALL.length];
    private boolean dirty;

    public static final class Routes {
        public final int version;
        public final int size;
        public final ConduitTapBlockEntity[] taps;
        public final Direction[] faces;
        public final int[] priorities;
        public final int[] groupStart;
        public final int[] groupEnd;

        Routes(int size, ConduitTapBlockEntity[] taps, Direction[] faces, int[] priorities) {
            this.version = nextVersion++;
            this.size = size;
            this.taps = taps;
            this.faces = faces;
            this.priorities = priorities;
            this.groupStart = new int[size];
            this.groupEnd = new int[size];
            int start = 0;
            for (int i = 1; i <= size; i++) {
                if (i == size || priorities[i] != priorities[start]) {
                    for (int j = start; j < i; j++) {
                        groupStart[j] = start;
                        groupEnd[j] = i;
                    }
                    start = i;
                }
            }
        }
    }

    ConduitNetwork(long[] taps, long[] members, long[] chunks, boolean truncated) {
        this.taps = taps;
        this.members = members;
        this.chunks = chunks;
        this.truncated = truncated;
    }

    public long[] taps() {
        return taps;
    }

    long[] members() {
        return members;
    }

    long[] chunks() {
        return chunks;
    }

    public int size() {
        return members.length;
    }

    public boolean truncated() {
        return truncated;
    }

    public boolean dirty() {
        return dirty;
    }

    void markDirty() {
        dirty = true;
        markRoutesDirty();
    }

    public void markRoutesDirty() {
        for (int i = 0; i < routes.length; i++) {
            routes[i] = null;
        }
    }

    public Routes routes(ServerLevel level, ConduitChannel channel) {
        Routes cached = routes[channel.ordinal()];
        if (cached == null) {
            cached = buildRoutes(level, channel);
            routes[channel.ordinal()] = cached;
        }
        return cached;
    }

    private Routes buildRoutes(ServerLevel level, ConduitChannel channel) {
        int capacity = taps.length * ConduitPartBlock.DIRECTIONS.length;
        ConduitTapBlockEntity[] routeTaps = new ConduitTapBlockEntity[capacity];
        Direction[] routeFaces = new Direction[capacity];
        int[] routePriorities = new int[capacity];
        int size = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (long packed : taps) {
            cursor.set(packed);
            if (!level.isLoaded(cursor) || !(level.getBlockEntity(cursor) instanceof ConduitTapBlockEntity tap)) {
                continue;
            }
            for (Direction direction : ConduitPartBlock.DIRECTIONS) {
                if (!tap.receives(channel, direction)) {
                    continue;
                }
                int priority = tap.face(direction).priority();
                int at = size;
                while (at > 0 && routePriorities[at - 1] < priority) {
                    routeTaps[at] = routeTaps[at - 1];
                    routeFaces[at] = routeFaces[at - 1];
                    routePriorities[at] = routePriorities[at - 1];
                    at--;
                }
                routeTaps[at] = tap;
                routeFaces[at] = direction;
                routePriorities[at] = priority;
                size++;
            }
        }
        return new Routes(size, routeTaps, routeFaces, routePriorities);
    }
}
