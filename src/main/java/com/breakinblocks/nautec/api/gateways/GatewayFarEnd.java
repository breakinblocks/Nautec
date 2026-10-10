package com.breakinblocks.nautec.api.gateways;

import com.breakinblocks.nautec.api.utils.HorizontalDirection;
import com.breakinblocks.nautec.content.blockentities.GatewayBlockEntity;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

public final class GatewayFarEnd {
    public static final int MIN_DISTANCE = 1250;
    public static final int MAX_DISTANCE = 1750;

    private static final int ATTEMPTS = 24;
    private static final int CLEAR_DEPTH = 3;

    private GatewayFarEnd() {
    }

    public static @Nullable BlockPos create(ServerLevel level, BlockPos origin, GatewayAddress address, RandomSource random) {
        int seaLevel = level.getSeaLevel();
        int[] fallback = null;
        int[] chosen = null;
        for (int attempt = 0; attempt < ATTEMPTS && chosen == null; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = MIN_DISTANCE + random.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
            int x = origin.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = origin.getZ() + (int) Math.round(Math.sin(angle) * distance);
            if (!level.getWorldBorder().isWithinBounds(new BlockPos(x, seaLevel, z))) {
                continue;
            }
            if (fallback == null) {
                fallback = new int[]{x, z};
            }
            if (level.getUncachedNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(seaLevel), QuartPos.fromBlock(z)).is(BiomeTags.IS_OCEAN)) {
                chosen = new int[]{x, z};
            }
        }
        if (chosen == null) {
            chosen = fallback;
        }
        if (chosen == null) {
            return null;
        }

        int x = chosen[0];
        int z = chosen[1];
        for (int cx = (x - 9) >> 4; cx <= (x + 9) >> 4; cx++) {
            for (int cz = (z - 9) >> 4; cz <= (z + 9) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(cx, cz), 1, new BlockPos(cx << 4, 0, cz << 4));
            }
        }

        int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
        BlockPos core = new BlockPos(x, Math.max(floor, level.getMinBuildHeight() + 2), z);
        HorizontalDirection direction = random.nextBoolean() ? HorizontalDirection.NORTH : HorizontalDirection.EAST;
        build(level, core, direction, address, seaLevel);

        if (!(level.getBlockEntity(core) instanceof GatewayBlockEntity gateway) || !gateway.isFormed()) {
            return null;
        }
        return core;
    }

    private static void build(ServerLevel level, BlockPos core, HorizontalDirection direction, GatewayAddress address, int seaLevel) {
        boolean alongX = GatewayRing.normalAxis(direction) == Direction.Axis.Z;
        for (int along = -GatewayRing.CENTRE - 1; along <= GatewayRing.CENTRE + 1; along++) {
            for (int up = 0; up <= GatewayRing.SIZE; up++) {
                for (int depth = -CLEAR_DEPTH; depth <= CLEAR_DEPTH; depth++) {
                    BlockPos pos = alongX ? core.offset(along, up, depth) : core.offset(depth, up, along);
                    BlockState state = level.getBlockState(pos);
                    if (state.hasBlockEntity()) {
                        continue;
                    }
                    level.setBlock(pos, (pos.getY() < seaLevel ? Blocks.WATER : Blocks.AIR).defaultBlockState(), 2);
                }
            }
        }

        for (int a = -2; a <= 2; a++) {
            for (int b = -2; b <= 2; b++) {
                boolean corner = Math.abs(a) == 2 && Math.abs(b) == 2;
                BlockState platform = corner ? NTBlocks.DARK_PRISMARINE_PILLAR.get().defaultBlockState() : NTBlocks.POLISHED_PRISMARINE.get().defaultBlockState();
                level.setBlock(core.offset(a, -1, b), platform, 3);
            }
        }

        level.setBlock(core, NTBlocks.GATEWAY.get().defaultBlockState(), 3);
        if (level.getBlockEntity(core) instanceof GatewayBlockEntity gateway) {
            gateway.configureFarEnd(address);
        }
        for (BlockPos cell : GatewayRing.ringCells(core, direction)) {
            level.setBlock(cell, NTBlocks.GATEWAY_RING.get().defaultBlockState(), 3);
        }
        MultiblockHelper.form(NTMultiblocks.GATEWAY.get(), core, level);
    }
}
