package com.breakinblocks.nautec.api.gateways;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GatewayIndex extends SavedData {
    public record Entry(BlockPos pos, GatewayAddress address) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
                GatewayAddress.CODEC.fieldOf("address").forGetter(Entry::address)
        ).apply(instance, Entry::new));
    }

    public static final Codec<GatewayIndex> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.listOf().fieldOf("gateways").forGetter(index -> List.copyOf(index.gateways.values()))
    ).apply(instance, GatewayIndex::new));

    public static final SavedDataType<GatewayIndex> TYPE =
            new SavedDataType<>(Nautec.rl("gateways"), GatewayIndex::new, CODEC);

    private final Map<BlockPos, Entry> gateways = new HashMap<>();

    public GatewayIndex() {
    }

    private GatewayIndex(List<Entry> entries) {
        for (Entry entry : entries) {
            this.gateways.put(entry.pos(), entry);
        }
    }

    public static GatewayIndex get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void put(BlockPos pos, GatewayAddress address) {
        Entry existing = gateways.get(pos);
        if (existing != null && existing.address().equals(address)) {
            return;
        }
        gateways.put(pos.immutable(), new Entry(pos.immutable(), address));
        setDirty();
    }

    public void remove(BlockPos pos) {
        if (gateways.remove(pos) != null) {
            setDirty();
        }
    }

    public int size() {
        return gateways.size();
    }

    public @Nullable GatewayAddress addressAt(BlockPos pos) {
        Entry entry = gateways.get(pos);
        return entry == null ? null : entry.address();
    }

    /**
     * Finds the closest other gateway sharing an address, dropping any entry whose block is gone.
     */
    public @Nullable BlockPos findNearest(ServerLevel level, BlockPos from, GatewayAddress address) {
        List<BlockPos> stale = new ArrayList<>();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (Entry entry : gateways.values()) {
            if (entry.pos().equals(from)) {
                continue;
            }
            if (!entry.address().equals(address)) {
                continue;
            }
            if (!level.isLoaded(entry.pos())) {
                continue;
            }
            if (!level.getBlockState(entry.pos()).is(NTBlocks.GATEWAY.get())) {
                stale.add(entry.pos());
                continue;
            }
            double distance = entry.pos().distSqr(from);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = entry.pos();
            }
        }

        for (BlockPos pos : stale) {
            remove(pos);
        }
        return best;
    }
}
