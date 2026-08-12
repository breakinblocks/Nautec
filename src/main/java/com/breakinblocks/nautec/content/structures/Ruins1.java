package com.breakinblocks.nautec.content.structures;

import com.breakinblocks.nautec.registries.NTStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Optional;

public class Ruins1 extends NTJigsawStructure {
    public static final MapCodec<Ruins1> CODEC = codec(Ruins1::new);

    public Ruins1(StructureSettings config,
                  Holder<StructureTemplatePool> startPool,
                  Optional<Identifier> startJigsawName,
                  int size,
                  HeightProvider startHeight,
                  Optional<Heightmap.Types> projectStartToHeightmap,
                  int maxDistanceFromCenter,
                  DimensionPadding dimensionPadding,
                  LiquidSettings liquidSettings) {
        super(config, startPool, startJigsawName, size, startHeight, projectStartToHeightmap,
                maxDistanceFromCenter, dimensionPadding, liquidSettings);
    }

    @Override
    public StructureType<?> type() {
        return NTStructures.RUINS_1.get();
    }
}
