package com.breakinblocks.nautec.api.bacteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.utils.ranges.LongRange;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Comparator;

public interface Bacteria {
    Codec<ResourceKey<Bacteria>> BACTERIA_TYPE_CODEC = ResourceKey.codec(NTRegistries.BACTERIA_KEY);
    StreamCodec<ByteBuf, ResourceKey<Bacteria>> BACTERIA_TYPE_STREAM_CODEC = ResourceKey.streamCodec(NTRegistries.BACTERIA_KEY);

    Codec<Bacteria> CODEC = NTRegistries.BACTERIA_SERIALIZER.byNameCodec().dispatch(Bacteria::getSerializer, BacteriaSerializer::mapCodec);
    StreamCodec<RegistryFriendlyByteBuf, Bacteria> STREAM_CODEC = ByteBufCodecs.registry(NTRegistries.BACTERIA_SERIALIZER_KEY).dispatch(Bacteria::getSerializer, BacteriaSerializer::streamCodec);

    LongRange initialSize();

    Resource resource();

    BacteriaStats<?> stats();

    BacteriaSerializer<?> getSerializer();

    long rollSize();

    default long maxInitialSize() {
        return initialSize().getMax();
    }

    interface Resource {
        Resource EMPTY = new ItemResource(Items.AIR);

        Codec<Resource> CODEC = Codec.STRING.comapFlatMap(Resource::parse, Resource::asString);
        StreamCodec<ByteBuf, Resource> STREAM_CODEC =
                ByteBufCodecs.STRING_UTF8.map(raw -> parse(raw).result().orElse(EMPTY), Resource::asString);

        Item resolve();

        String asString();

        default boolean isEmpty() {
            Item item = resolve();
            return item == null || item == Items.AIR;
        }

        static DataResult<Resource> parse(String raw) {
            if (raw.startsWith("#")) {
                return Identifier.read(raw.substring(1))
                        .map(id -> new ItemTagResource(TagKey.create(Registries.ITEM, id)));
            }
            return Identifier.read(raw).map(id -> new ItemResource(BuiltInRegistries.ITEM.getValue(id)));
        }

        record ItemResource(Item item) implements Resource {
            @Override
            public Item resolve() {
                return item;
            }

            @Override
            public String asString() {
                return BuiltInRegistries.ITEM.getKey(item).toString();
            }
        }

        record ItemTagResource(TagKey<Item> tag) implements Resource {
            @Override
            public Item resolve() {
                return BuiltInRegistries.ITEM.get(tag)
                        .map(holders -> holders.stream()
                                .map(Holder::value)
                                .filter(item -> item != Items.AIR)
                                .min(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()))
                                .orElse(Items.AIR))
                        .orElse(Items.AIR);
            }

            @Override
            public String asString() {
                return "#" + tag.location();
            }
        }
    }

    interface Builder<T extends Bacteria> {
        T build();
    }
}
