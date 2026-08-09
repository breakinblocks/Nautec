package com.breakinblocks.nautec.content.fishing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LuckyZoneIndex extends SavedData {
    public record Zone(BlockPos pos, int radius, long expiresAt) {
        public static final Codec<Zone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Zone::pos),
                Codec.INT.fieldOf("radius").forGetter(Zone::radius),
                Codec.LONG.fieldOf("expires_at").forGetter(Zone::expiresAt)
        ).apply(instance, Zone::new));

        public boolean covers(BlockPos target) {
            int dx = target.getX() - pos.getX();
            int dz = target.getZ() - pos.getZ();
            return Math.abs(target.getY() - pos.getY()) <= 2 && dx * dx + dz * dz <= radius * radius;
        }
    }

    public static final Codec<LuckyZoneIndex> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Zone.CODEC.listOf().fieldOf("zones").forGetter(index -> List.copyOf(index.zones.values()))
    ).apply(instance, LuckyZoneIndex::new));

    public static final SavedDataType<LuckyZoneIndex> TYPE =
            new SavedDataType<>(Nautec.rl("lucky_fishing_zones"), LuckyZoneIndex::new, CODEC);

    private final Map<BlockPos, Zone> zones = new HashMap<>();

    public LuckyZoneIndex() {
    }

    private LuckyZoneIndex(List<Zone> zones) {
        for (Zone zone : zones) {
            this.zones.put(zone.pos(), zone);
        }
    }

    public static LuckyZoneIndex get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void add(Zone zone) {
        zones.put(zone.pos(), zone);
        setDirty();
    }

    public void remove(BlockPos pos) {
        if (zones.remove(pos) != null) {
            setDirty();
        }
    }

    public boolean isEmpty() {
        return zones.isEmpty();
    }

    public int size() {
        return zones.size();
    }

    public boolean hasZoneWithin(BlockPos pos, int distance) {
        int squared = distance * distance;
        for (Zone zone : zones.values()) {
            if (zone.pos().distSqr(pos) <= squared) {
                return true;
            }
        }
        return false;
    }

    public int countInChunk(ChunkPos chunk) {
        int count = 0;
        for (Zone zone : zones.values()) {
            if (chunk.equals(new ChunkPos(SectionPos.blockToSectionCoord(zone.pos().getX()),
                    SectionPos.blockToSectionCoord(zone.pos().getZ())))) {
                count++;
            }
        }
        return count;
    }

    public Zone zoneAt(ServerLevel level, BlockPos pos) {
        for (Zone zone : zones.values()) {
            if (zone.covers(pos)) {
                if (level.getBlockState(zone.pos()).is(NTBlocks.LUCKY_FISHING_ZONE.get())) {
                    return zone;
                }
                remove(zone.pos());
                return null;
            }
        }
        return null;
    }

    public List<Zone> expired(long gameTime) {
        List<Zone> stale = new ArrayList<>();
        for (Zone zone : zones.values()) {
            if (gameTime >= zone.expiresAt()) {
                stale.add(zone);
            }
        }
        return stale;
    }
}
