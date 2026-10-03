package com.breakinblocks.nautec.content.bubble;

import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.LiquidBlockContainer;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HeldWaterBlock extends Block implements LiquidBlockContainer {
    public static final MapCodec<HeldWaterBlock> CODEC = simpleCodec(HeldWaterBlock::new);

    public HeldWaterBlock(Properties properties) {
        super(properties.noCollision().noOcclusion().noLootTable().replaceable().pushReaction(PushReaction.DESTROY));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return true;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean canPlaceLiquid(@Nullable LivingEntity entity, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return ItemStack.EMPTY;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(6) != 0) {
            return;
        }
        Direction side = Direction.getRandom(random);
        FluidState beyond = level.getFluidState(pos.relative(side));
        if (beyond.getType() != Fluids.EMPTY) {
            double x = pos.getX() + 0.5 + side.getStepX() * 0.45 + (random.nextDouble() - 0.5) * (side.getStepX() == 0 ? 0.9 : 0.0);
            double y = pos.getY() + 0.5 + side.getStepY() * 0.45 + (random.nextDouble() - 0.5) * (side.getStepY() == 0 ? 0.9 : 0.0);
            double z = pos.getZ() + 0.5 + side.getStepZ() * 0.45 + (random.nextDouble() - 0.5) * (side.getStepZ() == 0 ? 0.9 : 0.0);
            level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0.0, 0.02, 0.0);
        }
    }
}
