package com.breakinblocks.nautec.data.generated;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.List;

public record BacteriaPreset(String name, List<MaterialRef> nutrient, List<MaterialRef> resource,
                             BacteriaBalance.Rarity rarity, boolean enabled) {
    public static final Codec<BacteriaBalance.Rarity> RARITY_CODEC = Codec.stringResolver(
            BacteriaBalance.Rarity::lowerName,
            name -> BacteriaBalance.Rarity.byName(name).orElse(null));

    private static final Codec<List<MaterialRef>> REF_LIST_CODEC = Codec.withAlternative(
            MaterialRef.CODEC.listOf(),
            MaterialRef.CODEC,
            List::of);

    public static final Codec<BacteriaPreset> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("name").forGetter(BacteriaPreset::name),
            REF_LIST_CODEC.fieldOf("nutrient").forGetter(BacteriaPreset::nutrient),
            REF_LIST_CODEC.fieldOf("resource").forGetter(BacteriaPreset::resource),
            RARITY_CODEC.optionalFieldOf("rarity", BacteriaBalance.Rarity.UNCOMMON).forGetter(BacteriaPreset::rarity),
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(BacteriaPreset::enabled)
    ).apply(inst, BacteriaPreset::new));

    public static BacteriaPreset of(String name, List<String> nutrient, BacteriaBalance.Rarity rarity, List<String> resource) {
        return new BacteriaPreset(
                name,
                nutrient.stream().map(raw -> MaterialRef.parse(raw).getOrThrow()).toList(),
                resource.stream().map(raw -> MaterialRef.parse(raw).getOrThrow()).toList(),
                rarity,
                true
        );
    }
}
