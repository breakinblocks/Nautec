package com.breakinblocks.nautec.datagen.loot;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.loot.AddItemModifier;
import com.breakinblocks.nautec.loot.InLuckyFishingZoneCondition;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTLootTables;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import com.breakinblocks.nautec.utils.templates.ItemStackTemplate;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.concurrent.CompletableFuture;

public class LootModifierProvider extends GlobalLootModifierProvider {
    public LootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Nautec.MODID);
    }

    @Override
    protected void start() {
        add("lucky_fishing_zone_modifier",
                new AddTableLootModifier(new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.parse("gameplay/fishing")).build(),
                        InLuckyFishingZoneCondition.builder().build()}
                        , NTLootTables.LUCKY_ZONE));

        ItemStackTemplate crate = new ItemStackTemplate(NTBlocks.CRATE.asItem(), 1, DataComponentPatch.builder()
                .set(DataComponents.CONTAINER_LOOT, new SeededContainerLoot(NTLootTables.CRATE, 0))
                .build());
        add("elder_guardian_modifier",
                new AddTableLootModifier(new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.parse("entities/elder_guardian")).build(),
                        LootItemRandomChanceCondition.randomChance(1.0f).build()}
                        , NTLootTables.ELDER_GUARDIAN));
        add("guardian_modifier",
                new AddTableLootModifier(new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.parse("entities/guardian")).build(),
                        LootItemRandomChanceCondition.randomChance(0.20f).build()}
                        , NTLootTables.GUARDIAN));
        add("shipwreck_modifier",
                new AddItemModifier(new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.parse("chests/shipwreck_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.66f).build()}
                        , crate));
        add("ocean_ruins_modifier",
                new AddItemModifier(new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.parse("chests/underwater_ruin_big")).or(LootTableIdCondition.builder(ResourceLocation.parse("chests/underwater_ruin_small"))).build(),
                        LootItemRandomChanceCondition.randomChance(0.66f).build()}
                        , crate));
        add("ocean_ruins_small_loot",
                new AddTableLootModifier(new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.parse("chests/underwater_ruin_small")).build(),
                        LootItemRandomChanceCondition.randomChance(0.25f).build()},
                        NTLootTables.OCEAN_RUINS_SMALL));
        add("ocean_ruins_big_loot",
                new AddTableLootModifier(new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.parse("chests/underwater_ruin_big")).build(),
                        LootItemRandomChanceCondition.randomChance(0.25f).build()},
                        NTLootTables.OCEAN_RUINS_BIG));
        add("suspicious_ruins_sand_modifier",
                new AddItemModifier(new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.parse("archaeology/ocean_ruin_warm")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()}
                        , crate));
        add("drowned_modifier",
                new AddTableLootModifier(new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.parse("entities/drowned")).build(),
                        LootItemRandomChanceCondition.randomChance(0.20f).build()},
                        NTLootTables.DROWNED));
        add("dolphin_modifier",
                new AddTableLootModifier(new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.parse("entities/dolphin")).build(),
                        LootItemRandomChanceCondition.randomChance(0.20f).build()},
                        NTLootTables.DOLPHIN));
        add("buried_treasure",
                new AddTableLootModifier(new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.parse("chests/buried_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.45f).build()},
                        NTLootTables.BURIED_TREASURE));
    }

}
