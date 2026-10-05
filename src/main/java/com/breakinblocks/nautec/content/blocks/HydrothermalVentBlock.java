package com.breakinblocks.nautec.content.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HydrothermalVentBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<HydrothermalVentBlock> CODEC = simpleCodec(HydrothermalVentBlock::new);
    public static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 3, 16),
            Block.box(1, 3, 1, 15, 6, 15),
            Block.box(2, 6, 2, 14, 9, 14),
            Block.box(3, 9, 3, 13, 12, 13),
            Block.box(4, 12, 4, 12, 16, 12));

    public HydrothermalVentBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                                           @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean water = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, water);
    }

    @Override
    protected @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull LevelReader level, @NotNull ScheduledTickAccess tickAccess,
                                              @NotNull BlockPos pos, @NotNull Direction direction, @NotNull BlockPos neighborPos,
                                              @NotNull BlockState neighborState, @NotNull RandomSource random) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.isSteppingCarefully() && entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().hotFloor(), 1.0F);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        FluidState fluid = level.getFluidState(above);
        boolean underwater = fluid.is(FluidTags.WATER);
        if (!underwater && !level.getBlockState(above).isAir()) {
            return;
        }
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        for (int i = 0; i < 3; i++) {
            double ox = (random.nextDouble() - 0.5) * 0.3;
            double oz = (random.nextDouble() - 0.5) * 0.3;
            level.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, x + ox, y + random.nextDouble() * 0.3, z + oz,
                    0.0, 0.05 + random.nextDouble() * 0.04, 0.0);
        }
        if (underwater) {
            for (int i = 0; i < 2; i++) {
                level.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, x + (random.nextDouble() - 0.5) * 0.35, y, z + (random.nextDouble() - 0.5) * 0.35,
                        0.0, 0.04, 0.0);
            }
            if (random.nextInt(60) == 0) {
                level.playLocalSound(x, y, z, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS, 0.4F, 0.6F + random.nextFloat() * 0.2F, false);
            }
        }
    }
}
