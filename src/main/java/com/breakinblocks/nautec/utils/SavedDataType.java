package com.breakinblocks.nautec.utils;

import com.breakinblocks.nautec.Nautec;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.function.Supplier;

public record SavedDataType<T extends SavedData>(ResourceLocation id, Supplier<T> constructor, Codec<T> codec) {
    private static final String DATA_KEY = "data";

    public SavedData.Factory<T> factory() {
        return new SavedData.Factory<>(constructor, this::load, null);
    }

    public String fileName() {
        return id.getNamespace() + "_" + id.getPath().replace('/', '_');
    }

    public T get(DimensionDataStorage storage) {
        return storage.computeIfAbsent(factory(), fileName());
    }

    private T load(CompoundTag tag, HolderLookup.Provider registries) {
        Tag data = tag.get(DATA_KEY);
        if (data == null) {
            return constructor.get();
        }
        return codec.parse(registries.createSerializationContext(NbtOps.INSTANCE), data)
                .resultOrPartial(error -> Nautec.LOGGER.error("Could not load {}: {}", id, error))
                .orElseGet(constructor);
    }

    public CompoundTag save(T value, CompoundTag tag, HolderLookup.Provider registries) {
        codec.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), value)
                .resultOrPartial(error -> Nautec.LOGGER.error("Could not save {}: {}", id, error))
                .ifPresent(data -> tag.put(DATA_KEY, data));
        return tag;
    }
}
