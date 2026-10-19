package com.breakinblocks.nautec.content.blocks;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuddingPrismarineBlock extends Block {
    public static final MapCodec<BuddingPrismarineBlock> CODEC = simpleCodec(BuddingPrismarineBlock::new);
    public static final int GROWTH_CHANCE = 5;
    public static final int LIGHT = 4;
    private static final Direction[] DIRECTIONS = Direction.values();

    public BuddingPrismarineBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<BuddingPrismarineBlock> codec() {
        return CODEC;
    }

    @Override
    protected void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (random.nextInt(GROWTH_CHANCE) != 0) {
            return;
        }

        Direction growDirection = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos growPos = pos.relative(growDirection);
        BlockState relativeState = level.getBlockState(growPos);
        Block nextStage = nextStage(relativeState, growDirection);

        if (nextStage != null) {
            level.setBlockAndUpdate(growPos, nextStage.defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, growDirection)
                    .setValue(AmethystClusterBlock.WATERLOGGED, relativeState.getFluidState().is(Fluids.WATER)));
        }
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (random.nextInt(4) != 0) {
            return;
        }
        Direction face = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos outside = pos.relative(face);
        if (level.getBlockState(outside).isSolidRender()) {
            return;
        }
        double x = pos.getX() + 0.5 + face.getStepX() * 0.6 + (face.getStepX() == 0 ? random.nextDouble() - 0.5 : 0.0);
        double y = pos.getY() + 0.5 + face.getStepY() * 0.6 + (face.getStepY() == 0 ? random.nextDouble() - 0.5 : 0.0);
        double z = pos.getZ() + 0.5 + face.getStepZ() * 0.6 + (face.getStepZ() == 0 ? random.nextDouble() - 0.5 : 0.0);
        level.addParticle(NTParticles.CRYSTAL_MOTE.get(), x, y, z, 0.0, 0.0, 0.0);
    }

    private static @Nullable Block nextStage(BlockState state, Direction growDirection) {
        if (canClusterGrowAtState(state)) {
            return NTBlocks.SMALL_PRISMARINE_BUD.get();
        }
        if (!facesSameWay(state, growDirection)) {
            return null;
        }
        if (state.is(NTBlocks.SMALL_PRISMARINE_BUD.get())) {
            return NTBlocks.MEDIUM_PRISMARINE_BUD.get();
        }
        if (state.is(NTBlocks.MEDIUM_PRISMARINE_BUD.get())) {
            return NTBlocks.LARGE_PRISMARINE_BUD.get();
        }
        if (state.is(NTBlocks.LARGE_PRISMARINE_BUD.get())) {
            return NTBlocks.PRISMARINE_CLUSTER.get();
        }
        return null;
    }

    private static boolean facesSameWay(BlockState state, Direction growDirection) {
        return state.hasProperty(AmethystClusterBlock.FACING) && state.getValue(AmethystClusterBlock.FACING) == growDirection;
    }

    public static boolean canClusterGrowAtState(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) && state.getFluidState().isFull();
    }
}
