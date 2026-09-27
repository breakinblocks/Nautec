package com.breakinblocks.nautec.client.sonar;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SonarScanner {
    public record Cluster(AABB box, Identifiable ore) { }
    public record Identifiable(String id, int color) { }

    private static final Direction[] NEIGHBOURS = Direction.values();
    private final BlockGetter level;
    private final Vec3 center;
    private final double radiusSquared;
    private final Deque<SectionPos> pending = new ArrayDeque<>();
    private final Long2ObjectOpenHashMap<Vein> found = new Long2ObjectOpenHashMap<>();
    private final Map<Block, Identifiable> identities = new IdentityHashMap<>();
    private final Set<Vein> roots = new HashSet<>();
    private List<Cluster> clusters = List.of();
    private boolean dirty;

    public SonarScanner(BlockGetter level, BlockPos center, int radius) {
        this(level, center.getCenter(), radius);
    }

    public SonarScanner(BlockGetter level, Vec3 center, double radius) {
        this.level = level;
        this.center = center;
        this.radiusSquared = radius * radius;
        List<SectionPos> sections = new ArrayList<>();
        for (int x = SectionPos.blockToSectionCoord(Mth.floor(center.x - radius)); x <= SectionPos.blockToSectionCoord(Mth.floor(center.x + radius)); x++) {
            for (int y = SectionPos.blockToSectionCoord(Mth.floor(center.y - radius)); y <= SectionPos.blockToSectionCoord(Mth.floor(center.y + radius)); y++) {
                for (int z = SectionPos.blockToSectionCoord(Mth.floor(center.z - radius)); z <= SectionPos.blockToSectionCoord(Mth.floor(center.z + radius)); z++) {
                    SectionPos section = SectionPos.of(x, y, z);
                    if (distanceSquared(section) <= radiusSquared) sections.add(section);
                }
            }
        }
        sections.sort(Comparator.comparingDouble(this::distanceSquared));
        pending.addAll(sections);
    }

    private double distanceSquared(SectionPos section) {
        double dx = Math.max(0, Math.max(section.minBlockX() + 0.5 - center.x, center.x - section.maxBlockX() - 0.5));
        double dy = Math.max(0, Math.max(section.minBlockY() + 0.5 - center.y, center.y - section.maxBlockY() - 0.5));
        double dz = Math.max(0, Math.max(section.minBlockZ() + 0.5 - center.z, center.z - section.maxBlockZ() - 0.5));
        return dx * dx + dy * dy + dz * dz;
    }

    public int pendingSections() { return pending.size(); }
    public boolean isDone() { return pending.isEmpty(); }
    public void tick() { tick(4); }

    public void tick(int sectionBudget) {
        for (int i = 0; i < sectionBudget && !pending.isEmpty(); i++) scanSection(pending.removeFirst());
    }

    private void scanSection(SectionPos section) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = section.minBlockX(); x <= section.maxBlockX(); x++) {
            for (int y = section.minBlockY(); y <= section.maxBlockY(); y++) {
                for (int z = section.minBlockZ(); z <= section.maxBlockZ(); z++) {
                    if (center.distanceToSqr(x + 0.5, y + 0.5, z + 0.5) > radiusSquared) continue;
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (!state.is(Tags.Blocks.ORES)) continue;
                    Identifiable ore = identities.computeIfAbsent(state.getBlock(), block ->
                            new Identifiable(block.getDescriptionId(), colorFor(block.getDescriptionId())));
                    Vein vein = new Vein(new AABB(cursor), ore);
                    found.put(cursor.asLong(), vein);
                    roots.add(vein);
                    for (Direction direction : NEIGHBOURS) {
                        Vein neighbour = found.get(BlockPos.asLong(x + direction.getStepX(), y + direction.getStepY(), z + direction.getStepZ()));
                        if (neighbour != null && neighbour.ore == ore) merge(vein, neighbour);
                    }
                    dirty = true;
                }
            }
        }
    }

    // Merge when blocks arrive instead of flood-filling all previous discoveries every tick.
    private void merge(Vein first, Vein second) {
        Vein a = first.root();
        Vein b = second.root();
        if (a == b) return;
        if (a.size < b.size) { Vein swap = a; a = b; b = swap; }
        b.parent = a;
        a.size += b.size;
        a.box = a.box.minmax(b.box);
        roots.remove(b);
    }

    public List<Cluster> collectClusters() {
        if (dirty) {
            clusters = roots.stream().map(root -> new Cluster(root.box, root.ore)).toList();
            dirty = false;
        }
        return clusters;
    }

    private static final class Vein {
        private Vein parent = this;
        private int size = 1;
        private AABB box;
        private final Identifiable ore;
        private Vein(AABB box, Identifiable ore) { this.box = box; this.ore = ore; }
        private Vein root() {
            Vein root = this;
            while (root.parent != root) root = root.parent;
            Vein node = this;
            while (node.parent != node) { Vein next = node.parent; node.parent = root; node = next; }
            return root;
        }
    }

    public static int colorFor(String id) {
        float hue = 0.45F + (Math.abs((long) id.hashCode()) % 1000) / 1000F * 0.15F;
        return Mth.hsvToArgb(hue, 0.75F, 1.0F, 255);
    }
}
