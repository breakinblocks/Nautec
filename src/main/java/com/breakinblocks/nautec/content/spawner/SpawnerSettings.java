package com.breakinblocks.nautec.content.spawner;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import com.breakinblocks.nautec.utils.valueio.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.nbt.Tag;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.SpawnData;
import com.breakinblocks.nautec.utils.valueio.TagValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record SpawnerSettings(int minDelay, int maxDelay, int spawnCount, SimpleWeightedRandomList<SpawnData> potentials,
                              @Nullable SpawnData next) {
    public static final int DEFAULT_MIN_DELAY = 200;
    public static final int DEFAULT_MAX_DELAY = 800;
    public static final int DEFAULT_SPAWN_COUNT = 4;

    public static SpawnerSettings read(CompoundTag spawnerTag, HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Nautec.LOGGER)) {
            ValueInput in = TagValueInput.create(reporter, registries, spawnerTag);
            SpawnData next = in.read("SpawnData", SpawnData.CODEC).orElse(null);
            SimpleWeightedRandomList<SpawnData> potentials = in.read("SpawnPotentials", SpawnData.LIST_CODEC).orElse(SimpleWeightedRandomList.empty());
            int minDelay = Math.max(1, in.getIntOr("MinSpawnDelay", DEFAULT_MIN_DELAY));
            int maxDelay = Math.max(minDelay, in.getIntOr("MaxSpawnDelay", DEFAULT_MAX_DELAY));
            int spawnCount = Math.max(0, in.getIntOr("SpawnCount", DEFAULT_SPAWN_COUNT));
            return new SpawnerSettings(minDelay, maxDelay, spawnCount, potentials, next);
        }
    }

    public @Nullable SpawnData pick(RandomSource random) {
        return potentials.getRandomValue(random).orElse(next);
    }

    public int nextDelay(RandomSource random) {
        return maxDelay <= minDelay ? minDelay : minDelay + random.nextInt(maxDelay - minDelay);
    }

    public CompoundTag displayEntity() {
        if (next != null && hasId(next)) {
            return next.getEntityToSpawn();
        }
        List<WeightedEntry.Wrapper<SpawnData>> entries = potentials.unwrap();
        for (WeightedEntry.Wrapper<SpawnData> entry : entries) {
            if (hasId(entry.data())) {
                return entry.data().getEntityToSpawn();
            }
        }
        return new CompoundTag();
    }

    public boolean hasMob() {
        return spawnCount > 0 && !displayEntity().isEmpty();
    }

    private static boolean hasId(SpawnData data) {
        return data.getEntityToSpawn().contains("id", Tag.TAG_STRING);
    }
}
