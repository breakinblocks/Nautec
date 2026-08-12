package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.concurrent.CompletableFuture;

import static com.breakinblocks.nautec.registries.NTBlocks.*;

public class BlockTagProvider extends BlockTagsProvider {

    public BlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Nautec.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_AXE,
                NTBlocks.CRATE,
                RUSTY_CRATE);
        tag(BlockTags.MINEABLE_WITH_PICKAXE,
                CRATE,
                RUSTY_CRATE,
                DARK_PRISMARINE_PILLAR,
                CHISELED_DARK_PRISMARINE,
                POLISHED_PRISMARINE,
                AQUARINE_STEEL_BLOCK,
                AQUATIC_CATALYST,
                SUBMARINE_DOCK,
                PRESSURE_FORGE,
                GATEWAY,
                RESONANCE_CHAMBER,
                PRISMATIC_MIRROR,
                BEAM_SPLITTER,
                FOCUSING_LENS,
                PRISMARINE_RELAY,
                MIXER,
                CHARGER,
                LONG_DISTANCE_LASER,
                LASER_JUNCTION,
                DRAIN,
                DRAIN_WALL,
                DRAIN_PART,
                AUGMENTATION_STATION,
                AUGMENTATION_STATION_EXTENSION,
                AUGMENTATION_STATION_PART,
                BACTERIAL_ANALYZER,
                BACTERIAL_ANALYZER_TOP,
                BACTERIAL_CONTAINMENT_SHIELD,
                BACTERIAL_FUEL_CELL,
                BIO_REACTOR,
                BIO_REACTOR_PART,
                MUTATOR,
                INCUBATOR,
                FISHING_STATION);
    }

    private void tag(TagKey<Block> blockTagKey, Block... blocks) {
        tag(blockTagKey).add(blocks);
    }

    @SafeVarargs
    private void tag(TagKey<Block> blockTagKey, DeferredBlock<? extends Block>... blocks) {
        TagAppender<Block, Block> tag = tag(blockTagKey);
        for (DeferredBlock<? extends Block> block : blocks) {
            tag.add(block.get());
        }
    }
}
