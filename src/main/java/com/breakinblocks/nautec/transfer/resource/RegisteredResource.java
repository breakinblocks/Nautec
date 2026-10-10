package com.breakinblocks.nautec.transfer.resource;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.function.Predicate;
import java.util.stream.Stream;

public interface RegisteredResource<T> extends Resource {
    T value();

    Holder<T> typeHolder();

    default boolean is(TagKey<T> tag) {
        return typeHolder().is(tag);
    }

    default boolean is(Holder<T> holder) {
        return typeHolder().is(holder);
    }

    default boolean is(ResourceKey<T> key) {
        return typeHolder().is(key);
    }

    default boolean is(HolderSet<T> set) {
        return set.contains(typeHolder());
    }

    default boolean is(Predicate<Holder<T>> predicate) {
        return predicate.test(typeHolder());
    }

    default Stream<TagKey<T>> tags() {
        return typeHolder().tags();
    }
}
