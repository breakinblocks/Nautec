package com.breakinblocks.nautec.content.structures;

import com.breakinblocks.nautec.registries.NTStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Optional;

public class StoneCrystalGeode extends NTJigsawStructure {
    public static final MapCodec<StoneCrystalGeode> CODEC = codec(StoneCrystalGeode::new);

    public StoneCrystalGeode(StructureSettings config,
                  Holder<StructureTemplatePool> startPool,
                  Optional<ResourceLocation> startJigsawName,
                  int size,
                  HeightProvider startHeight,
                  Optional<Heightmap.Types> projectStartToHeightmap,
                  int maxDistanceFromCenter,
                  DimensionPadding dimensionPadding,
                  LiquidSettings liquidSettings,
                  boolean centerInChunk,
                  Optional<Integer> minCover) {
        super(config, startPool, startJigsawName, size, startHeight, projectStartToHeightmap,
                maxDistanceFromCenter, dimensionPadding, liquidSettings, centerInChunk, minCover);
    }

    @Override
    public StructureType<?> type() {
        return NTStructures.STONE_CRYSTAL_GEODE.get();
    }
}
