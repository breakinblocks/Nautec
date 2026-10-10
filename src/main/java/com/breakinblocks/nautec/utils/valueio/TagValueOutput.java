package com.breakinblocks.nautec.utils.valueio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.Nullable;

public final class TagValueOutput implements ValueOutput {
    private final ProblemReporter reporter;
    private final RegistryOps<Tag> ops;
    private final CompoundTag output;

    private TagValueOutput(ProblemReporter reporter, RegistryOps<Tag> ops, CompoundTag output) {
        this.reporter = reporter;
        this.ops = ops;
        this.output = output;
    }

    public static TagValueOutput createWithContext(ProblemReporter reporter, HolderLookup.Provider lookup) {
        return new TagValueOutput(reporter, lookup.createSerializationContext(NbtOps.INSTANCE), new CompoundTag());
    }

    public static TagValueOutput wrap(ProblemReporter reporter, HolderLookup.Provider lookup, CompoundTag output) {
        return new TagValueOutput(reporter, lookup.createSerializationContext(NbtOps.INSTANCE), output);
    }

    public static TagValueOutput wrap(HolderLookup.Provider lookup, CompoundTag output) {
        return wrap(ProblemReporter.DISCARDING, lookup, output);
    }

    public CompoundTag buildResult() {
        return output;
    }

    @Override
    public CompoundTag tag() {
        return output;
    }

    @Override
    public <T> void store(String name, Codec<T> codec, T value) {
        DataResult<Tag> result = codec.encodeStart(ops, value);
        if (result.error().isPresent()) {
            reporter.report(name + ": " + result.error().get().message());
        }
        result.result().ifPresent(tag -> output.put(name, tag));
    }

    @Override
    public <T> void storeNullable(String name, Codec<T> codec, @Nullable T value) {
        if (value != null) {
            store(name, codec, value);
        }
    }

    @Override
    public <T> void store(MapCodec<T> codec, T value) {
        DataResult<Tag> result = codec.codec().encodeStart(ops, value);
        if (result.error().isPresent()) {
            reporter.report(result.error().get().message());
        }
        result.result().ifPresent(tag -> {
            if (tag instanceof CompoundTag compound) {
                output.merge(compound);
            }
        });
    }

    @Override
    public void putBoolean(String name, boolean value) {
        output.putBoolean(name, value);
    }

    @Override
    public void putByte(String name, byte value) {
        output.putByte(name, value);
    }

    @Override
    public void putShort(String name, short value) {
        output.putShort(name, value);
    }

    @Override
    public void putInt(String name, int value) {
        output.putInt(name, value);
    }

    @Override
    public void putLong(String name, long value) {
        output.putLong(name, value);
    }

    @Override
    public void putFloat(String name, float value) {
        output.putFloat(name, value);
    }

    @Override
    public void putDouble(String name, double value) {
        output.putDouble(name, value);
    }

    @Override
    public void putString(String name, String value) {
        output.putString(name, value);
    }

    @Override
    public void putIntArray(String name, int[] value) {
        output.putIntArray(name, value);
    }

    @Override
    public ValueOutput child(String name) {
        CompoundTag child = new CompoundTag();
        output.put(name, child);
        return new TagValueOutput(reporter, ops, child);
    }

    @Override
    public ValueOutputList childrenList(String name) {
        ListTag list = new ListTag();
        output.put(name, list);
        return new ValueOutputList() {
            @Override
            public ValueOutput addChild() {
                CompoundTag child = new CompoundTag();
                list.add(child);
                return new TagValueOutput(reporter, ops, child);
            }

            @Override
            public void discardLast() {
                if (!list.isEmpty()) {
                    list.remove(list.size() - 1);
                }
            }

            @Override
            public boolean isEmpty() {
                return list.isEmpty();
            }
        };
    }

    @Override
    public <T> TypedOutputList<T> list(String name, Codec<T> codec) {
        ListTag list = new ListTag();
        output.put(name, list);
        return new TypedOutputList<>() {
            @Override
            public void add(T value) {
                DataResult<Tag> result = codec.encodeStart(ops, value);
                if (result.error().isPresent()) {
                    reporter.report(name + ": " + result.error().get().message());
                }
                result.result().ifPresent(list::add);
            }

            @Override
            public boolean isEmpty() {
                return list.isEmpty();
            }
        };
    }

    @Override
    public void discard(String name) {
        output.remove(name);
    }

    @Override
    public boolean isEmpty() {
        return output.isEmpty();
    }

    @Override
    public void store(CompoundTag tag) {
        for (String key : tag.getAllKeys()) {
            Tag value = tag.get(key);
            if (value != null) {
                output.put(key, value.copy());
            }
        }
    }
}
