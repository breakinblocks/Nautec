package com.breakinblocks.nautec.content.blocks;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.nautec.content.blockentities.LuckyFishingZoneBlockEntity;
import com.breakinblocks.nautec.registries.NTParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuckyFishingZoneBlock extends BaseEntityBlock {
    public static final MapCodec<LuckyFishingZoneBlock> CODEC = simpleCodec(LuckyFishingZoneBlock::new);

    public LuckyFishingZoneBlock(BlockBehaviour.Properties properties) {
        super(properties.replaceable().noLootTable());
    }

    @Override
    protected @NotNull MapCodec<LuckyFishingZoneBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        return isWaterSurface(level, pos);
    }

    public static boolean isWaterSurface(LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getFluidState(below).is(FluidTags.WATER)
                && level.getFluidState(below).isSource()
                && level.getBlockState(pos).canBeReplaced();
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull LevelReader level, @NotNull ScheduledTickAccess scheduledTickAccess,
                                              @NotNull BlockPos pos, @NotNull Direction direction, @NotNull BlockPos neighborPos,
                                              @NotNull BlockState neighborState, @NotNull RandomSource random) {
        return state.canSurvive(level, pos)
                ? super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random)
                : Blocks.AIR.defaultBlockState();
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        int radius = level.getBlockEntity(pos) instanceof LuckyFishingZoneBlockEntity zone ? zone.getRadius() : 1;

        for (int i = 0; i < 1 + radius; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = Math.sqrt(random.nextDouble()) * (radius + 0.5);
            double x = pos.getX() + 0.5 + Math.cos(angle) * distance;
            double z = pos.getZ() + 0.5 + Math.sin(angle) * distance;
            level.addAlwaysVisibleParticle(NTParticles.GLOW_SPORE.get(), x, pos.getY() + 0.05, z, 0.0, 0.0, 0.0);
        }

        if (random.nextInt(6) == 0) {
            level.addAlwaysVisibleParticle(NTParticles.VENT_BUBBLE.get(),
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * radius,
                    pos.getY() + 0.1,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * radius,
                    0.0, 0.0, 0.0);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new LuckyFishingZoneBlockEntity(pos, state);
    }
}
