package com.breakinblocks.nautec.utils.valueio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public final class TagValueInput implements ValueInput {
    private final ProblemReporter reporter;
    private final HolderLookup.Provider lookup;
    private final RegistryOps<Tag> ops;
    private final CompoundTag input;

    private TagValueInput(ProblemReporter reporter, HolderLookup.Provider lookup, RegistryOps<Tag> ops, CompoundTag input) {
        this.reporter = reporter;
        this.lookup = lookup;
        this.ops = ops;
        this.input = input;
    }

    public static ValueInput create(ProblemReporter reporter, HolderLookup.Provider lookup, CompoundTag tag) {
        return new TagValueInput(reporter, lookup, lookup.createSerializationContext(NbtOps.INSTANCE), tag);
    }

    public static ValueInput create(HolderLookup.Provider lookup, CompoundTag tag) {
        return create(ProblemReporter.DISCARDING, lookup, tag);
    }

    private ValueInput wrap(CompoundTag tag) {
        return new TagValueInput(reporter, lookup, ops, tag);
    }

    @Override
    public <T> Optional<T> read(String name, Codec<T> codec) {
        Tag tag = input.get(name);
        if (tag == null) {
            return Optional.empty();
        }
        DataResult<T> result = codec.parse(ops, tag);
        if (result.error().isPresent()) {
            reporter.report(name + ": " + result.error().get().message());
        }
        return result.result();
    }

    @Override
    public <T> Optional<T> read(MapCodec<T> codec) {
        DataResult<T> result = codec.codec().parse(ops, input);
        if (result.error().isPresent()) {
            reporter.report(result.error().get().message());
        }
        return result.result();
    }

    @Override
    public Optional<ValueInput> child(String name) {
        return input.get(name) instanceof CompoundTag compound ? Optional.of(wrap(compound)) : Optional.empty();
    }

    @Override
    public ValueInput childOrEmpty(String name) {
        return input.get(name) instanceof CompoundTag compound ? wrap(compound) : wrap(new CompoundTag());
    }

    @Override
    public Optional<ValueInputList> childrenList(String name) {
        if (!(input.get(name) instanceof ListTag list)) {
            return Optional.empty();
        }
        return Optional.of(compounds(list));
    }

    @Override
    public ValueInputList childrenListOrEmpty(String name) {
        return input.get(name) instanceof ListTag list ? compounds(list) : compounds(new ListTag());
    }

    private ValueInputList compounds(ListTag list) {
        List<ValueInput> children = new ArrayList<>(list.size());
        for (Tag tag : list) {
            if (tag instanceof CompoundTag compound) {
                children.add(wrap(compound));
            }
        }
        return new ValueInputList() {
            @Override
            public boolean isEmpty() {
                return children.isEmpty();
            }

            @Override
            public Stream<ValueInput> stream() {
                return children.stream();
            }

            @Override
            public Iterator<ValueInput> iterator() {
                return children.iterator();
            }
        };
    }

    @Override
    public <T> Optional<TypedInputList<T>> list(String name, Codec<T> codec) {
        if (!(input.get(name) instanceof ListTag list)) {
            return Optional.empty();
        }
        return Optional.of(typed(list, codec));
    }

    @Override
    public <T> TypedInputList<T> listOrEmpty(String name, Codec<T> codec) {
        return input.get(name) instanceof ListTag list ? typed(list, codec) : typed(new ListTag(), codec);
    }

    private <T> TypedInputList<T> typed(ListTag list, Codec<T> codec) {
        List<T> values = new ArrayList<>(list.size());
        for (Tag tag : list) {
            DataResult<T> result = codec.parse(ops, tag);
            if (result.error().isPresent()) {
                reporter.report(result.error().get().message());
            }
            result.result().ifPresent(values::add);
        }
        return new TypedInputList<>() {
            @Override
            public boolean isEmpty() {
                return values.isEmpty();
            }

            @Override
            public Stream<T> stream() {
                return values.stream();
            }

            @Override
            public Iterator<T> iterator() {
                return values.iterator();
            }
        };
    }

    private @Nullable NumericTag numeric(String name) {
        return input.get(name) instanceof NumericTag numeric ? numeric : null;
    }

    @Override
    public boolean getBooleanOr(String name, boolean defaultValue) {
        NumericTag tag = numeric(name);
        return tag == null ? defaultValue : tag.getAsByte() != 0;
    }

    @Override
    public byte getByteOr(String name, byte defaultValue) {
        NumericTag tag = numeric(name);
        return tag == null ? defaultValue : tag.getAsByte();
    }

    @Override
    public int getShortOr(String name, short defaultValue) {
        NumericTag tag = numeric(name);
        return tag == null ? defaultValue : tag.getAsShort();
    }

    @Override
    public Optional<Integer> getInt(String name) {
        NumericTag tag = numeric(name);
        return tag == null ? Optional.empty() : Optional.of(tag.getAsInt());
    }

    @Override
    public int getIntOr(String name, int defaultValue) {
        NumericTag tag = numeric(name);
        return tag == null ? defaultValue : tag.getAsInt();
    }

    @Override
    public long getLongOr(String name, long defaultValue) {
        NumericTag tag = numeric(name);
        return tag == null ? defaultValue : tag.getAsLong();
    }

    @Override
    public Optional<Long> getLong(String name) {
        NumericTag tag = numeric(name);
        return tag == null ? Optional.empty() : Optional.of(tag.getAsLong());
    }

    @Override
    public float getFloatOr(String name, float defaultValue) {
        NumericTag tag = numeric(name);
        return tag == null ? defaultValue : tag.getAsFloat();
    }

    @Override
    public double getDoubleOr(String name, double defaultValue) {
        NumericTag tag = numeric(name);
        return tag == null ? defaultValue : tag.getAsDouble();
    }

    @Override
    public Optional<String> getString(String name) {
        return input.get(name) instanceof StringTag string ? Optional.of(string.getAsString()) : Optional.empty();
    }

    @Override
    public String getStringOr(String name, String defaultValue) {
        return input.get(name) instanceof StringTag string ? string.getAsString() : defaultValue;
    }

    @Override
    public Optional<int[]> getIntArray(String name) {
        return input.get(name) instanceof IntArrayTag array ? Optional.of(array.getAsIntArray()) : Optional.empty();
    }

    @Override
    public HolderLookup.Provider lookup() {
        return lookup;
    }

    @Override
    public Set<String> keySet() {
        return input.getAllKeys();
    }

    @Override
    public CompoundTag tag() {
        return input;
    }
}
