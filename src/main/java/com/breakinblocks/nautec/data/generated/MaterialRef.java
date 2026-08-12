package com.breakinblocks.nautec.data.generated;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Optional;

public sealed interface MaterialRef {
    Codec<MaterialRef> CODEC = Codec.STRING.comapFlatMap(MaterialRef::parse, MaterialRef::asString);

    Optional<HolderSet<Item>> resolve();

    String asString();

    static DataResult<MaterialRef> parse(String raw) {
        if (raw.startsWith("#")) {
            return Identifier.read(raw.substring(1)).map(id -> new Tag(TagKey.create(Registries.ITEM, id)));
        }
        return Identifier.read(raw).map(id -> new Single(ResourceKey.create(Registries.ITEM, id)));
    }

    static MaterialRef tag(String id) {
        return new Tag(TagKey.create(Registries.ITEM, Identifier.parse(id)));
    }

    static MaterialRef item(String id) {
        return new Single(ResourceKey.create(Registries.ITEM, Identifier.parse(id)));
    }

    record Tag(TagKey<Item> tag) implements MaterialRef {
        @Override
        public Optional<HolderSet<Item>> resolve() {
            return BuiltInRegistries.ITEM.get(tag)
                    .filter(holders -> holders.size() > 0)
                    .map(holders -> holders);
        }

        @Override
        public String asString() {
            return "#" + tag.location();
        }
    }

    record Single(ResourceKey<Item> item) implements MaterialRef {
        @Override
        public Optional<HolderSet<Item>> resolve() {
            return BuiltInRegistries.ITEM.get(item).map(HolderSet::direct);
        }

        @Override
        public String asString() {
            return item.identifier().toString();
        }
    }
}
