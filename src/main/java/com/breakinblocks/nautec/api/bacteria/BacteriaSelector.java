package com.breakinblocks.nautec.api.bacteria;

import com.breakinblocks.nautec.NTRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.Optional;

public record BacteriaSelector(Identifier id, boolean tag) {
    public static final Codec<BacteriaSelector> CODEC = Codec.STRING.comapFlatMap(BacteriaSelector::parse, BacteriaSelector::asString);
    public static final StreamCodec<ByteBuf, BacteriaSelector> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(
            raw -> parse(raw).getOrThrow(), BacteriaSelector::asString);

    public static BacteriaSelector of(ResourceKey<Bacteria> bacteria) {
        return new BacteriaSelector(bacteria.identifier(), false);
    }

    public static BacteriaSelector of(TagKey<Bacteria> tag) {
        return new BacteriaSelector(tag.location(), true);
    }

    public static DataResult<BacteriaSelector> parse(String raw) {
        if (raw.startsWith("#")) {
            return Identifier.read(raw.substring(1)).map(id -> new BacteriaSelector(id, true));
        }
        return Identifier.read(raw).map(id -> new BacteriaSelector(id, false));
    }

    public String asString() {
        return tag ? "#" + id : id.toString();
    }

    public Optional<ResourceKey<Bacteria>> key() {
        return tag ? Optional.empty() : Optional.of(ResourceKey.create(NTRegistries.BACTERIA_KEY, id));
    }

    public TagKey<Bacteria> tagKey() {
        return TagKey.create(NTRegistries.BACTERIA_KEY, id);
    }

    public boolean matches(ResourceKey<Bacteria> bacteria, HolderLookup.Provider registries) {
        if (!tag) {
            return bacteria.identifier().equals(id);
        }
        if (registries == null) {
            return false;
        }
        return registries.lookup(NTRegistries.BACTERIA_KEY)
                .flatMap(lookup -> lookup.get(bacteria))
                .map(holder -> holder.is(tagKey()))
                .orElse(false);
    }

    public static boolean matches(Optional<BacteriaSelector> selector, ResourceKey<Bacteria> bacteria, HolderLookup.Provider registries) {
        return selector.map(value -> value.matches(bacteria, registries)).orElse(true);
    }

    public static int priority(Optional<BacteriaSelector> selector) {
        return selector.map(value -> value.tag ? 1 : 2).orElse(0);
    }
}
