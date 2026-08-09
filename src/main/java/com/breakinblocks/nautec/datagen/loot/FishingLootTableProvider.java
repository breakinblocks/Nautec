package com.breakinblocks.nautec.datagen.loot;

import com.breakinblocks.nautec.datagen.BiomeTagProvider;
import com.breakinblocks.nautec.loot.CatchAsEntityFunction;
import com.breakinblocks.nautec.registries.NTEntities;
import net.minecraft.world.entity.EntityType;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTLootTables;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.level.ItemLike;

import java.util.function.BiConsumer;

public record FishingLootTableProvider(HolderLookup.Provider registries) implements LootTableSubProvider {

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(NTLootTables.LUCKY_ZONE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(NestedLootTable.lootTableReference(NTLootTables.LUCKY_ZONE_CATCH).setWeight(85).setQuality(-1))
                        .add(NestedLootTable.lootTableReference(NTLootTables.LUCKY_ZONE_TREASURE).setWeight(15).setQuality(2))));

        output.accept(NTLootTables.LUCKY_ZONE_CATCH, catchTable());
        output.accept(NTLootTables.LUCKY_ZONE_TREASURE, treasureTable());
    }

    private LootTable.Builder catchTable() {
        return LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))

                .add(anywhere(Items.COD, 12))
                .add(anywhere(Items.SALMON, 10))
                .add(anywhere(NTItems.SILT_SKIPPER.get(), 10))
                .add(anywhere(NTItems.SALT.get(), 8, 1, 3))
                .add(anywhere(NTItems.PRISMARINE_CRYSTAL_SHARD.get(), 4))

                .add(inBiome(Items.CLAY_BALL, 12, 1, 3, BiomeTags.IS_RIVER))
                .add(inBiome(Items.LILY_PAD, 8, BiomeTags.IS_RIVER))
                .add(inBiome(Items.TROPICAL_FISH, 10, BiomeTags.IS_OCEAN))
                .add(inBiome(Items.KELP, 8, 1, 4, BiomeTags.IS_OCEAN))

                .add(inBiome(NTItems.LUMINOUS_MEMBRANE.get(), 10, BiomeTagProvider.BIOLUMINESCENT))
                .add(inBiome(NTBlocks.LUMINESCENT_ALGAE.asItem(), 12, 1, 3, BiomeTagProvider.BIOLUMINESCENT))
                .add(inBiome(NTBlocks.DEEP_KELP.asItem(), 10, 1, 2, BiomeTagProvider.BIOLUMINESCENT))

                .add(inBiome(NTItems.CHITIN_PLATE.get(), 10, BiomeTagProvider.VENTS))
                .add(inBiome(NTBlocks.VENT_TUBEWORM.asItem(), 12, 1, 3, BiomeTagProvider.VENTS))
                .add(inBiome(Items.MAGMA_BLOCK, 8, BiomeTagProvider.VENTS))

                .add(inBiome(NTBlocks.PRISMARINE_FROND.asItem(), 12, 1, 3, BiomeTagProvider.REEF))
                .add(inBiome(Items.PRISMARINE_SHARD, 10, 1, 3, BiomeTagProvider.REEF))
                .add(inBiome(Items.PUFFERFISH, 8, BiomeTagProvider.REEF))

                .add(inBiome(NTBlocks.ABYSSAL_CORAL.asItem(), 10, BiomeTagProvider.ABYSSAL))
                .add(inBiome(NTItems.DAMAGED_AQUATIC_CHIP.get(), 8, BiomeTagProvider.ABYSSAL))
                .add(inBiome(Items.INK_SAC, 8, 1, 3, BiomeTagProvider.ABYSSAL))

                .add(anywhere(Items.COD, 6).apply(CatchAsEntityFunction.catchAsEntity(EntityType.COD)))
                .add(inBiome(Items.TROPICAL_FISH, 6, BiomeTagProvider.REEF)
                        .apply(CatchAsEntityFunction.catchAsEntity(EntityType.TROPICAL_FISH)))
                .add(inBiome(NTItems.SILT_SKIPPER.get(), 8, BiomeTagProvider.BIOLUMINESCENT)
                        .apply(CatchAsEntityFunction.catchAsEntity(NTEntities.SILT_SKIPPER.get()))));
    }

    private LootTable.Builder treasureTable() {
        return LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))

                .add(anywhere(NTItems.RUSTY_GEAR.get(), 14))
                .add(anywhere(NTItems.ANCIENT_VALVE.get(), 12))
                .add(anywhere(NTItems.BROKEN_WHISK.get(), 12))
                .add(anywhere(NTItems.PRISMARINE_LENS.get(), 6))
                .add(anywhere(NTBlocks.CRATE.asItem(), 4))

                .add(inBiome(Items.NAUTILUS_SHELL, 10, BiomeTags.IS_OCEAN))
                .add(inBiome(NTItems.ATLANTIC_GOLD_NUGGET.get(), 10, 1, 4, BiomeTags.IS_OCEAN))

                .add(inBiome(NTItems.ABYSSAL_ORGAN.get(), 4, BiomeTagProvider.ABYSSAL))
                .add(inBiome(NTBlocks.BUDDING_PRISMARINE.asItem(), 2, BiomeTagProvider.ABYSSAL))

                .add(inBiome(NTItems.PRISMARINE_CRYSTAL_SHARD.get(), 10, 2, 4, BiomeTagProvider.REEF))
                .add(inBiome(Items.HEART_OF_THE_SEA, 2, BiomeTagProvider.REEF))

                .add(inBiome(NTBlocks.GLOW_POLYP.asItem(), 10, 1, 2, BiomeTagProvider.BIOLUMINESCENT))
                .add(inBiome(NTItems.AQUATIC_CHIP.get(), 4, BiomeTagProvider.BIOLUMINESCENT))

                .add(inBiome(NTItems.LASER_CHANNELING_COIL.get(), 6, BiomeTagProvider.VENTS))
                .add(inBiome(Items.BLAZE_POWDER, 8, BiomeTagProvider.VENTS))

                .add(inBiome(Items.INK_SAC, 3, BiomeTagProvider.ABYSSAL)
                        .apply(CatchAsEntityFunction.catchAsEntity(EntityType.SQUID)))
                .add(inBiome(NTItems.LUMINOUS_MEMBRANE.get(), 3, BiomeTagProvider.BIOLUMINESCENT)
                        .apply(CatchAsEntityFunction.catchAsEntity(NTEntities.LANTERN_JELLY.get()))));
    }

    private static LootPoolSingletonContainer.Builder<?> anywhere(ItemLike item, int weight) {
        return LootItem.lootTableItem(item).setWeight(weight);
    }

    private static LootPoolSingletonContainer.Builder<?> anywhere(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item).setWeight(weight)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
    }

    private LootPoolSingletonContainer.Builder<?> inBiome(ItemLike item, int weight, TagKey<Biome> biomes) {
        return anywhere(item, weight).when(biomeCheck(biomes));
    }

    private LootPoolSingletonContainer.Builder<?> inBiome(ItemLike item, int weight, int min, int max, TagKey<Biome> biomes) {
        return anywhere(item, weight, min, max).when(biomeCheck(biomes));
    }

    private LocationCheck.Builder biomeCheck(TagKey<Biome> biomes) {
        return (LocationCheck.Builder) LocationCheck.checkLocation(LocationPredicate.Builder.location()
                .setBiomes(this.registries.lookupOrThrow(Registries.BIOME).getOrThrow(biomes)));
    }
}
