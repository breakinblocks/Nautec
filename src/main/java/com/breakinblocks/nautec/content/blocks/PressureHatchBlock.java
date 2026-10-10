package com.breakinblocks.nautec.content.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

public class PressureHatchBlock extends DoorBlock {
    public static final MapCodec<PressureHatchBlock> CODEC = simpleCodec(PressureHatchBlock::new);
    public static final int POCKET_LIMIT = 64;

    public PressureHatchBlock(Properties properties) {
        super(BlockSetType.COPPER, properties);
    }

    @Override
    public MapCodec<? extends DoorBlock> codec() {
        return CODEC;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
        super.setPlacedBy(level, pos, state, by, itemStack);
        drainIfClosed(level, pos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        InteractionResult result = super.useWithoutItem(state, level, pos, player, hitResult);
        drainIfClosed(level, pos);
        return result;
    }

    @Override
    public void setOpen(@Nullable Entity sourceEntity, Level level, BlockState state, BlockPos pos, boolean shouldOpen) {
        super.setOpen(sourceEntity, level, state, pos, shouldOpen);
        drainIfClosed(level, pos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        boolean wasOpen = state.getValue(OPEN);
        super.neighborChanged(state, level, pos, block, fromPos, movedByPiston);
        if (wasOpen) {
            drainIfClosed(level, pos);
        }
    }

    public static int drainIfClosed(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return 0;
        }
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof PressureHatchBlock) || state.getValue(OPEN)) {
            return 0;
        }
        BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        Direction facing = state.getValue(FACING);
        int drained = 0;
        for (Direction side : new Direction[]{facing, facing.getOpposite()}) {
            Set<BlockPos> pocket = pocket(level, lower.relative(side));
            if (pocket == null) {
                continue;
            }
            for (BlockPos water : pocket) {
                level.setBlock(water, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                serverLevel.sendParticles(ParticleTypes.BUBBLE_POP, water.getX() + 0.5, water.getY() + 0.5, water.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.0);
            }
            drained += pocket.size();
        }
        if (drained > 0) {
            level.playSound(null, lower, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.8F, 0.7F);
        }
        return drained;
    }

    private static boolean water(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.WATER);
    }

    private static @Nullable Set<BlockPos> pocket(Level level, BlockPos cell) {
        Set<BlockPos> found = new LinkedHashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        for (BlockPos start : new BlockPos[]{cell, cell.above()}) {
            if (water(level, start) && found.add(start)) {
                queue.add(start);
            }
        }
        while (!queue.isEmpty()) {
            BlockPos next = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos neighbour = next.relative(direction);
                if (!found.contains(neighbour) && water(level, neighbour)) {
                    if (found.size() >= POCKET_LIMIT) {
                        return null;
                    }
                    found.add(neighbour);
                    queue.add(neighbour);
                }
            }
        }
        return found.isEmpty() ? null : found;
    }
}
