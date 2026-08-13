package com.breakinblocks.nautec.content.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Optional;

public abstract class NTJigsawStructure extends Structure {
    @FunctionalInterface
    public interface Factory<S extends NTJigsawStructure> {
        S create(StructureSettings settings,
                 Holder<StructureTemplatePool> startPool,
                 Optional<Identifier> startJigsawName,
                 int size,
                 HeightProvider startHeight,
                 Optional<Heightmap.Types> projectStartToHeightmap,
                 int maxDistanceFromCenter,
                 DimensionPadding dimensionPadding,
                 LiquidSettings liquidSettings);
    }

    protected final Holder<StructureTemplatePool> startPool;
    protected final Optional<Identifier> startJigsawName;
    protected final int size;
    protected final HeightProvider startHeight;
    protected final Optional<Heightmap.Types> projectStartToHeightmap;
    protected final int maxDistanceFromCenter;
    protected final DimensionPadding dimensionPadding;
    protected final LiquidSettings liquidSettings;

    protected NTJigsawStructure(StructureSettings config,
                                Holder<StructureTemplatePool> startPool,
                                Optional<Identifier> startJigsawName,
                                int size,
                                HeightProvider startHeight,
                                Optional<Heightmap.Types> projectStartToHeightmap,
                                int maxDistanceFromCenter,
                                DimensionPadding dimensionPadding,
                                LiquidSettings liquidSettings) {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
    }

    protected static <S extends NTJigsawStructure> MapCodec<S> codec(Factory<S> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                settingsCodec(instance),
                StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
                Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
                HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
                Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter),
                DimensionPadding.CODEC.optionalFieldOf("dimension_padding", JigsawStructure.DEFAULT_DIMENSION_PADDING).forGetter(structure -> structure.dimensionPadding),
                LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(structure -> structure.liquidSettings)
        ).apply(instance, factory::create));
    }

    protected boolean canPlace(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        return context.chunkGenerator().getFirstOccupiedHeight(
                chunkPos.getMinBlockX(),
                chunkPos.getMinBlockZ(),
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                context.heightAccessor(),
                context.randomState()) < context.chunkGenerator().getSeaLevel();
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!canPlace(context)) {
            return Optional.empty();
        }

        int startY = this.startHeight.sample(context.random(),
                new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));

        ChunkPos chunkPos = context.chunkPos();
        BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), startY, chunkPos.getMinBlockZ());

        return JigsawPlacement.addPieces(
                context,
                this.startPool,
                this.startJigsawName,
                this.size,
                blockPos,
                false,
                this.projectStartToHeightmap,
                new JigsawStructure.MaxDistance(this.maxDistanceFromCenter),
                PoolAliasLookup.EMPTY,
                this.dimensionPadding,
                this.liquidSettings);
    }
}
