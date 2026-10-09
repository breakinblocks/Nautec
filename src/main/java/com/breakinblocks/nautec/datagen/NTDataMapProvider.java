package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.data.NTDataMaps;
import com.breakinblocks.nautec.data.maps.BacteriaObtainValue;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.concurrent.CompletableFuture;

public class NTDataMapProvider extends DataMapProvider {
    protected NTDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        obtainBacteria(Blocks.STONE, NTBacterias.CYANOBACTERIA, BiomeTags.IS_OCEAN, 0.4f);
        obtainBacteria(Blocks.PODZOL, NTBacterias.METHANOGENS, BiomeTags.IS_FOREST, 0.4f);
        obtainBacteria(Blocks.SAND, NTBacterias.HALOBACTERIA, BiomeTags.IS_BEACH, 0.4f);
        obtainBacteria(Blocks.NETHERRACK, NTBacterias.THERMOPHILES, BiomeTags.IS_NETHER, 0.4f);

        obtainBacteria(NTBlocks.DEEP_KELP.get(), NTBacterias.HALOTROPHS, BiomeTagProvider.BIOLUMINESCENT, 0.30f);
        obtainBacteria(NTBlocks.DEEP_KELP_PLANT.get(), NTBacterias.HALOTROPHS, BiomeTagProvider.BIOLUMINESCENT, 0.30f);
        obtainBacteria(NTBlocks.LUMINESCENT_ALGAE.get(), NTBacterias.ALGAEFORMERS, BiomeTagProvider.BIOLUMINESCENT, 0.30f);
        obtainBacteria(NTBlocks.GLOW_POLYP.get(), NTBacterias.CYANOBACTERIA, BiomeTagProvider.BIOLUMINESCENT, 0.50f);
        obtainBacteria(NTBlocks.VENT_TUBEWORM.get(), NTBacterias.SULFUROPHILES, BiomeTagProvider.VENTS, 0.25f);
        obtainBacteria(NTBlocks.ABYSSAL_CORAL.get(), NTBacterias.CALCIOPHILES, BiomeTagProvider.ABYSSAL, 0.30f);
        obtainBacteria(NTBlocks.PRISMARINE_FROND.get(), NTBacterias.PHOTOTROPHS, BiomeTagProvider.REEF, 0.35f);
        obtainBacteria(NTBlocks.BUDDING_PRISMARINE.get(), NTBacterias.LITHOPHILES, BiomeTagProvider.REEF, 0.30f);

        storageBlock(Items.COAL, Items.COAL_BLOCK);
        storageBlock(Items.BONE_MEAL, Items.BONE_BLOCK);
        storageBlock(Items.WHEAT, Items.HAY_BLOCK);
        storageBlock(Items.MELON_SLICE, Items.MELON);
        storageBlock(Items.BAMBOO, Items.BAMBOO_BLOCK);
        storageBlock(Items.ICE, Items.PACKED_ICE);
        storageBlock(Items.DRIED_KELP, Items.DRIED_KELP_BLOCK);
        storageBlock(Items.SLIME_BALL, Items.SLIME_BLOCK);
        storageBlock(Items.HONEYCOMB, Items.HONEYCOMB_BLOCK);
    }

    private void storageBlock(Item product, Item block) {
        builder(NTDataMaps.STORAGE_BLOCKS).add(product.builtInRegistryHolder(), block, false);
    }

    private void obtainBacteria(Block block, ResourceKey<Bacteria> bacteria, TagKey<Biome> biome, float chance) {
        builder(NTDataMaps.BACTERIA_OBTAINING)
                .add(block.builtInRegistryHolder(), new BacteriaObtainValue(bacteria, biome, chance), false);
    }
}
