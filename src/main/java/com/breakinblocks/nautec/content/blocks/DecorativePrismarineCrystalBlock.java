package com.breakinblocks.nautec.content.blocks;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.nautec.content.blockentities.DecorativePrismarineCrystalBlockEntity;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
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

public class DecorativePrismarineCrystalBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final int HEIGHT = 6;
    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(4, 0, 4, 12, 16, 12),
        Block.box(2, 2, 2, 14, 14, 14)
    );

    public DecorativePrismarineCrystalBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    protected @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(DecorativePrismarineCrystalBlock::new);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DecorativePrismarineCrystalBlockEntity(pos, state);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        for (int i = 1; i < HEIGHT; i++) {
            BlockPos curPos = pos.above(i);
            if (level.isOutsideBuildHeight(curPos) || !level.getBlockState(curPos).canBeReplaced()) {
                return null;
            }
        }
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(BlockStateProperties.WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        for (int i = 0; i < HEIGHT; i++) {
            BlockPos curPos = pos.above(i);
            boolean water = level.getFluidState(curPos).getType() == Fluids.WATER;
            if (i == 0) {
                level.setBlockAndUpdate(curPos, NTBlocks.DECORATIVE_PRISMARINE_CRYSTAL.get().defaultBlockState()
                        .setValue(BlockStateProperties.WATERLOGGED, water));
            } else {
                level.setBlockAndUpdate(curPos, NTBlocks.DECORATIVE_PRISMARINE_CRYSTAL_PART.get().defaultBlockState()
                        .setValue(DecorativePrismarineCrystalPartBlock.INDEX, i)
                        .setValue(DecorativePrismarineCrystalPartBlock.WATERLOGGED, water));
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) {
            return;
        }
        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = 0.5 + random.nextDouble() * 0.9;
        level.addParticle(NTParticles.CRYSTAL_MOTE.get(),
                pos.getX() + 0.5 + Math.cos(angle) * distance,
                pos.getY() + 0.3 + random.nextDouble() * 4.5,
                pos.getZ() + 0.5 + Math.sin(angle) * distance,
                0.0, 0.0, 0.0);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        removeCrystal(level, player, pos);
        return true;
    }

    public static void removeCrystal(Level level, Player player, BlockPos thisPos) {
        if (thisPos != null) {
            for (int i = 0; i < HEIGHT; i++) {
                BlockPos curPos = thisPos.above(i);
                Block block = level.getBlockState(curPos).getBlock();
                boolean ours = i == 0
                        ? block instanceof DecorativePrismarineCrystalBlock
                        : block instanceof DecorativePrismarineCrystalPartBlock;
                if (ours) {
                    level.removeBlock(curPos, false);
                }
            }
        }
    }
}