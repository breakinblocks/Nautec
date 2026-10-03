package com.breakinblocks.nautec.content.structures;

import com.breakinblocks.nautec.registries.NTStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class ResearchOutpost extends Structure {
    public static final MapCodec<ResearchOutpost> CODEC = simpleCodec(ResearchOutpost::new);
    public static final int MIN_WATER_ABOVE = 12;
    public static final int MAX_FLOOR_SPREAD = 6;

    public ResearchOutpost(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int minX = context.chunkPos().getMinBlockX();
        int minZ = context.chunkPos().getMinBlockZ();
        ChunkGenerator generator = context.chunkGenerator();
        LevelHeightAccessor heights = context.heightAccessor();
        RandomState random = context.randomState();
        int size = ResearchOutpostPiece.SIZE - 1;
        int[][] corners = {{minX, minZ}, {minX + size, minZ}, {minX, minZ + size}, {minX + size, minZ + size}, {minX + size / 2, minZ + size / 2}};
        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        for (int[] corner : corners) {
            int floor = generator.getFirstOccupiedHeight(corner[0], corner[1], Heightmap.Types.OCEAN_FLOOR_WG, heights, random);
            int surface = generator.getFirstOccupiedHeight(corner[0], corner[1], Heightmap.Types.WORLD_SURFACE_WG, heights, random);
            if (surface - floor < MIN_WATER_ABOVE) {
                return Optional.empty();
            }
            lowest = Math.min(lowest, floor);
            highest = Math.max(highest, floor);
        }
        if (highest - lowest > MAX_FLOOR_SPREAD) {
            return Optional.empty();
        }
        BlockPos origin = new BlockPos(minX, lowest, minZ);
        Rotation rotation = Rotation.getRandom(context.random());
        long seed = context.random().nextLong();
        return Optional.of(new GenerationStub(origin.offset(size / 2, 0, size / 2),
                builder -> builder.addPiece(new ResearchOutpostPiece(origin, rotation, seed))));
    }

    @Override
    public StructureType<?> type() {
        return NTStructures.RESEARCH_OUTPOST.get();
    }
}
