package com.breakinblocks.nautec.content.spawner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record SpawnerFilterEntry(Holder<Item> item, Optional<TagKey<Item>> tag) {
    public static final Codec<SpawnerFilterEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Item.CODEC.fieldOf("item").forGetter(SpawnerFilterEntry::item),
            TagKey.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(SpawnerFilterEntry::tag)
    ).apply(instance, SpawnerFilterEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpawnerFilterEntry> STREAM_CODEC = StreamCodec.composite(
            Item.STREAM_CODEC, SpawnerFilterEntry::item,
            ByteBufCodecs.optional(TagKey.streamCodec(Registries.ITEM)), SpawnerFilterEntry::tag,
            SpawnerFilterEntry::new
    );

    public static SpawnerFilterEntry of(ItemStack stack) {
        return new SpawnerFilterEntry(stack.typeHolder(), Optional.empty());
    }

    public boolean matches(ItemStack stack) {
        return tag.map(stack::is).orElseGet(() -> stack.is(item));
    }

    public ItemStack displayStack() {
        return new ItemStack(item);
    }

    public SpawnerFilterEntry nextTag() {
        List<TagKey<Item>> tags = sortedTags(item);
        if (tags.isEmpty()) {
            return new SpawnerFilterEntry(item, Optional.empty());
        }
        if (tag.isEmpty()) {
            return new SpawnerFilterEntry(item, Optional.of(tags.getFirst()));
        }
        int index = tags.indexOf(tag.get());
        if (index < 0 || index + 1 >= tags.size()) {
            return new SpawnerFilterEntry(item, Optional.empty());
        }
        return new SpawnerFilterEntry(item, Optional.of(tags.get(index + 1)));
    }

    private static List<TagKey<Item>> sortedTags(Holder<Item> item) {
        List<TagKey<Item>> tags = new ArrayList<>(item.tags().toList());
        tags.sort(Comparator.comparing(key -> key.location().toString()));
        return tags;
    }
}
