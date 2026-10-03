package com.breakinblocks.nautec.datagen.loot;

import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTLootTables;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

public class ChestLootTableProvider implements LootTableSubProvider {

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> writer) {
        writer.accept(NTLootTables.OCEAN_RUINS_SMALL, LootTable.lootTable()
                .withPool(salvage(UniformGenerator.between(1.0F, 2.0F))));
        writer.accept(NTLootTables.OCEAN_RUINS_BIG, LootTable.lootTable()
                .withPool(salvage(UniformGenerator.between(2.0F, 3.0F)))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(EmptyLootItem.emptyItem().setWeight(10))
                        .add(LootItem.lootTableItem(NTItems.AQUARINE_STEEL_INGOT.get()).setWeight(5)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                        .add(LootItem.lootTableItem(NTItems.RESONANT_SHARD.get()).setWeight(2))
                        .add(LootItem.lootTableItem(NTItems.DIVING_HELMET.get()).setWeight(1))));
        writer.accept(NTLootTables.RESEARCH_OUTPOST, LootTable.lootTable()
                .withPool(salvage(UniformGenerator.between(2.0F, 4.0F)))
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(2.0F, 3.0F))
                        .add(LootItem.lootTableItem(NTItems.PETRI_DISH.get()).setWeight(6)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                        .add(LootItem.lootTableItem(NTItems.GLASS_VIAL.get()).setWeight(5)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
                        .add(LootItem.lootTableItem(NTItems.AIR_BOTTLE.get()).setWeight(5)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                        .add(LootItem.lootTableItem(NTItems.AQUARINE_STEEL_COMPOUND.get()).setWeight(5)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
                        .add(LootItem.lootTableItem(NTItems.CAST_IRON_INGOT.get()).setWeight(4)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F))))
                        .add(LootItem.lootTableItem(NTItems.GRAFTING_TOOL.get()).setWeight(2))
                        .add(LootItem.lootTableItem(NTItems.AQUATIC_CHIP.get()).setWeight(2))
                        .add(LootItem.lootTableItem(NTItems.AQUARINE_STEEL_INGOT.get()).setWeight(3)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                        .add(LootItem.lootTableItem(Items.MAP).setWeight(2)))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(EmptyLootItem.emptyItem().setWeight(40))
                        .add(LootItem.lootTableItem(NTItems.SOLAR_MODULE.get()).setWeight(2))
                        .add(LootItem.lootTableItem(NTItems.SONAR_MODULE.get()).setWeight(2))
                        .add(LootItem.lootTableItem(NTItems.CARGO_MODULE.get()).setWeight(2))
                        .add(LootItem.lootTableItem(NTItems.ARMOR_MODULE.get()).setWeight(1))
                        .add(LootItem.lootTableItem(NTItems.DIVING_HELMET.get()).setWeight(2))
                        .add(LootItem.lootTableItem(Items.HEART_OF_THE_SEA).setWeight(1))));
        writer.accept(NTLootTables.CRATE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(4.0F, 5.0F))
                        .add(LootItem.lootTableItem(NTItems.BURNT_COIL.get()).setWeight(3)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                        .add(LootItem.lootTableItem(NTItems.BROWN_POLYMER.get()).setWeight(4)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                        .add(LootItem.lootTableItem(NTItems.CAST_IRON_INGOT.get()).setWeight(5)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 3.0F))))
                        .add(LootItem.lootTableItem(NTItems.AQUARINE_STEEL_COMPOUND.get()).setWeight(4)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 3.0F))))
                        .add(LootItem.lootTableItem(NTItems.ATLANTIC_GOLD_NUGGET.get()).setWeight(2)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                        .add(LootItem.lootTableItem(Items.PRISMARINE_CRYSTALS).setWeight(2)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F))))
                        .add(LootItem.lootTableItem(Items.PRISMARINE_SHARD).setWeight(3)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                        .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(4)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F))))
                        .add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(3)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 3.0F))))
                        .add(LootItem.lootTableItem(Items.KELP).setWeight(2)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 3.0F))))
                        .add(LootItem.lootTableItem(Items.SEA_PICKLE).setWeight(2)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 3.0F))))
                        .add(LootItem.lootTableItem(Items.BUBBLE_CORAL).setWeight(1)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                        .add(LootItem.lootTableItem(Items.DEAD_BRAIN_CORAL).setWeight(1)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                        .add(LootItem.lootTableItem(Items.DEAD_BRAIN_CORAL_FAN).setWeight(1)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                )
        );
    }

    private static LootPool.Builder salvage(NumberProvider rolls) {
        return LootPool.lootPool()
                .setRolls(rolls)
                .add(LootItem.lootTableItem(NTItems.PRISMARINE_CRYSTAL_SHARD.get()).setWeight(10)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                .add(LootItem.lootTableItem(NTItems.ATLANTIC_GOLD_NUGGET.get()).setWeight(6)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F))))
                .add(LootItem.lootTableItem(NTItems.RUSTY_GEAR.get()).setWeight(8))
                .add(LootItem.lootTableItem(NTItems.BURNT_COIL.get()).setWeight(6)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                .add(LootItem.lootTableItem(NTItems.ANCIENT_VALVE.get()).setWeight(4))
                .add(LootItem.lootTableItem(NTItems.DAMAGED_AQUATIC_CHIP.get()).setWeight(4))
                .add(LootItem.lootTableItem(NTItems.BROKEN_WHISK.get()).setWeight(3))
                .add(LootItem.lootTableItem(NTBlocks.DEEP_KELP.get()).setWeight(5)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F))))
                .add(LootItem.lootTableItem(NTItems.PRISMARINE_LENS.get()).setWeight(2));
    }
}
