package com.breakinblocks.nautec.content.structures;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Optional;
import java.util.function.IntBinaryOperator;

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
                 LiquidSettings liquidSettings,
                 boolean centerInChunk,
                 Optional<Integer> minCover);
    }

    protected final Holder<StructureTemplatePool> startPool;
    protected final Optional<Identifier> startJigsawName;
    protected final int size;
    protected final HeightProvider startHeight;
    protected final Optional<Heightmap.Types> projectStartToHeightmap;
    protected final int maxDistanceFromCenter;
    protected final DimensionPadding dimensionPadding;
    protected final LiquidSettings liquidSettings;
    protected final boolean centerInChunk;
    protected final Optional<Integer> minCover;

    protected NTJigsawStructure(StructureSettings config,
                                Holder<StructureTemplatePool> startPool,
                                Optional<Identifier> startJigsawName,
                                int size,
                                HeightProvider startHeight,
                                Optional<Heightmap.Types> projectStartToHeightmap,
                                int maxDistanceFromCenter,
                                DimensionPadding dimensionPadding,
                                LiquidSettings liquidSettings,
                                boolean centerInChunk,
                                Optional<Integer> minCover) {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
        this.centerInChunk = centerInChunk;
        this.minCover = minCover;
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
                LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(structure -> structure.liquidSettings),
                Codec.BOOL.optionalFieldOf("center_in_chunk", false).forGetter(structure -> structure.centerInChunk),
                Codec.intRange(0, 64).optionalFieldOf("min_cover").forGetter(structure -> structure.minCover)
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

    public static Vec3i placementOffset(BoundingBox box, int targetX, int targetZ, boolean center,
                                        Optional<Integer> minCover, IntBinaryOperator floorAt) {
        BlockPos boxCenter = box.getCenter();
        int dx = center ? targetX - boxCenter.getX() : 0;
        int dz = center ? targetZ - boxCenter.getZ() : 0;
        int dy = 0;
        if (minCover.isPresent()) {
            int floor = floorAt.applyAsInt(boxCenter.getX() + dx, boxCenter.getZ() + dz);
            dy = Math.min(0, floor - minCover.get() - box.maxY());
        }
        return new Vec3i(dx, dy, dz);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        Optional<GenerationStub> stub = placePieces(context);
        if (stub.isEmpty() || (!this.centerInChunk && this.minCover.isEmpty())) {
            return stub;
        }

        ChunkPos chunkPos = context.chunkPos();
        IntBinaryOperator floorAt = (x, z) -> context.chunkGenerator().getFirstOccupiedHeight(
                x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        StructurePiecesBuilder builder = stub.get().getPiecesBuilder();
        BoundingBox box = builder.getBoundingBox();
        Vec3i offset = placementOffset(box, chunkPos.getMiddleBlockX(), chunkPos.getMiddleBlockZ(),
                this.centerInChunk, this.minCover, floorAt);

        if (box.minY() + offset.getY() < context.heightAccessor().getMinY() + this.dimensionPadding.bottom()) {
            return Optional.empty();
        }

        StructurePiecesBuilder moved = new StructurePiecesBuilder();
        for (StructurePiece piece : builder.build().pieces()) {
            piece.move(offset.getX(), offset.getY(), offset.getZ());
            moved.addPiece(piece);
        }

        BlockPos movedCenter = box.getCenter().offset(offset);
        BlockPos biomePos = this.minCover.isPresent()
                ? new BlockPos(movedCenter.getX(), floorAt.applyAsInt(movedCenter.getX(), movedCenter.getZ()), movedCenter.getZ())
                : stub.get().position().offset(offset);
        return Optional.of(new GenerationStub(biomePos, Either.right(moved)));
    }

    private Optional<GenerationStub> placePieces(GenerationContext context) {
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
