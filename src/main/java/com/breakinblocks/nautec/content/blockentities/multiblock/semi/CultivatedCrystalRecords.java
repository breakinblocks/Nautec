package com.breakinblocks.nautec.content.blockentities.multiblock.semi;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.utils.SavedDataType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

public class CultivatedCrystalRecords extends SavedData {
    private record Entry(BlockPos pos, float purity) {
        private static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
                Codec.FLOAT.fieldOf("purity").forGetter(Entry::purity)
        ).apply(instance, Entry::new));
    }

    public static final Codec<CultivatedCrystalRecords> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.listOf().fieldOf("crystals").forGetter(CultivatedCrystalRecords::entries)
    ).apply(instance, CultivatedCrystalRecords::new));

    public static final SavedDataType<CultivatedCrystalRecords> TYPE =
            new SavedDataType<>(Nautec.rl("cultivated_crystals"), CultivatedCrystalRecords::new, CODEC);

    private final Long2FloatOpenHashMap purities = new Long2FloatOpenHashMap();

    public CultivatedCrystalRecords() {
        purities.defaultReturnValue(Float.NaN);
    }

    private CultivatedCrystalRecords(List<Entry> entries) {
        this();
        for (Entry entry : entries) {
            purities.put(entry.pos().asLong(), entry.purity());
        }
    }

    public static CultivatedCrystalRecords get(ServerLevel level) {
        return TYPE.get(level.getDataStorage());
    }

    public void remember(BlockPos pos, float purity) {
        purities.put(pos.asLong(), purity);
        setDirty();
    }

    public float take(BlockPos pos) {
        if (purities.isEmpty()) {
            return Float.NaN;
        }
        float purity = purities.remove(pos.asLong());
        if (!Float.isNaN(purity)) {
            setDirty();
        }
        return purity;
    }

    private List<Entry> entries() {
        List<Entry> entries = new ArrayList<>(purities.size());
        for (Long2FloatMap.Entry entry : purities.long2FloatEntrySet()) {
            entries.add(new Entry(BlockPos.of(entry.getLongKey()), entry.getFloatValue()));
        }
        return entries;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        return TYPE.save(this, tag, registries);
    }
}
