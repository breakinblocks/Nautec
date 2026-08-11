package com.breakinblocks.nautec.client.sonar;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SonarScanner {
    public record Cluster(AABB box, Identifiable ore) {
    }

    public record Identifiable(String id, int color) {
    }

    private static final int SECTIONS_PER_TICK = 4;

    private final BlockGetter level;
    private final BlockPos center;
    private final int radius;
    private final Deque<SectionPos> pending = new ArrayDeque<>();
    private final Map<BlockPos, String> found = new HashMap<>();
    private boolean scanned;

    public SonarScanner(BlockGetter level, BlockPos center, int radius) {
        this.level = level;
        this.center = center;
        this.radius = radius;

        int minSection = SectionPos.blockToSectionCoord(-radius);
        int maxSection = SectionPos.blockToSectionCoord(radius);
        for (int x = minSection; x <= maxSection; x++) {
            for (int y = minSection; y <= maxSection; y++) {
                for (int z = minSection; z <= maxSection; z++) {
                    SectionPos section = SectionPos.of(
                            SectionPos.blockToSectionCoord(center.getX()) + x,
                            SectionPos.blockToSectionCoord(center.getY()) + y,
                            SectionPos.blockToSectionCoord(center.getZ()) + z);
                    if (reaches(section)) {
                        this.pending.add(section);
                    }
                }
            }
        }
    }

    private boolean reaches(SectionPos section) {
        double dx = Math.max(0, Math.max(section.minBlockX() - this.center.getX(), this.center.getX() - section.maxBlockX()));
        double dy = Math.max(0, Math.max(section.minBlockY() - this.center.getY(), this.center.getY() - section.maxBlockY()));
        double dz = Math.max(0, Math.max(section.minBlockZ() - this.center.getZ(), this.center.getZ() - section.maxBlockZ()));
        return dx * dx + dy * dy + dz * dz <= (double) this.radius * this.radius;
    }

    public int pendingSections() {
        return this.pending.size();
    }

    public boolean isDone() {
        return this.scanned;
    }

    public void tick() {
        for (int i = 0; i < SECTIONS_PER_TICK && !this.pending.isEmpty(); i++) {
            scanSection(this.pending.poll());
        }

        this.scanned = this.pending.isEmpty();
    }

    private void scanSection(SectionPos section) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int radiusSqr = this.radius * this.radius;

        for (int x = section.minBlockX(); x <= section.maxBlockX(); x++) {
            for (int y = section.minBlockY(); y <= section.maxBlockY(); y++) {
                for (int z = section.minBlockZ(); z <= section.maxBlockZ(); z++) {
                    cursor.set(x, y, z);
                    if (this.center.distSqr(cursor) > radiusSqr) {
                        continue;
                    }

                    BlockState state = this.level.getBlockState(cursor);
                    if (state.is(Tags.Blocks.ORES)) {
                        this.found.put(cursor.immutable(), state.getBlock().getDescriptionId());
                    }
                }
            }
        }
    }

    public List<Cluster> collectClusters() {
        List<Cluster> clusters = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();

        for (Map.Entry<BlockPos, String> entry : this.found.entrySet()) {
            if (!visited.add(entry.getKey())) {
                continue;
            }

            AABB box = new AABB(entry.getKey());
            Deque<BlockPos> frontier = new ArrayDeque<>();
            frontier.add(entry.getKey());

            while (!frontier.isEmpty()) {
                BlockPos current = frontier.poll();
                for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
                    BlockPos neighbour = current.relative(direction);
                    String neighbourId = this.found.get(neighbour);
                    if (neighbourId == null || !neighbourId.equals(entry.getValue()) || !visited.add(neighbour)) {
                        continue;
                    }

                    box = box.minmax(new AABB(neighbour));
                    frontier.add(neighbour);
                }
            }

            clusters.add(new Cluster(box, new Identifiable(entry.getValue(), colorFor(entry.getValue()))));
        }

        return clusters;
    }

    public static int colorFor(String id) {
        int hash = id.hashCode();
        float hue = 0.45F + (Math.abs(hash) % 1000) / 1000F * 0.15F;
        return Mth.hsvToArgb(hue, 0.75F, 1.0F, 255);
    }
}
