package com.breakinblocks.nautec.content.conduits;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CurrentConduitBlock extends ConduitPartBlock {
    public static final MapCodec<CurrentConduitBlock> CODEC = simpleCodec(CurrentConduitBlock::new);
    public static final EnumProperty<ConduitArm>[] ARMS = arms(ConduitArm.class);

    public CurrentConduitBlock(Properties properties) {
        super(properties);
        registerDefaultState(withArms(getStateDefinition().any(), ConduitArm.NONE));
    }

    @Override
    protected @NotNull MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected EnumProperty<?>[] armProperties() {
        return ARMS;
    }

    @Override
    protected VoxelShape shapeFor(BlockState state) {
        int mask = 0;
        for (Direction direction : DIRECTIONS) {
            if (state.getValue(ARMS[direction.ordinal()]) == ConduitArm.CONNECTED) {
                mask |= 1 << direction.ordinal();
            }
        }
        return ConduitShapes.conduit(mask);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return refresh(level, pos, waterloggedFor(defaultBlockState(), level, pos));
    }

    public static BlockState refresh(LevelReader level, BlockPos pos, BlockState state) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction direction : DIRECTIONS) {
            cursor.setWithOffset(pos, direction);
            state = state.setValue(ARMS[direction.ordinal()], arm(level, cursor, direction, state, level.getBlockState(cursor)));
        }
        return state;
    }

    private static ConduitArm arm(LevelReader level, BlockPos neighbourPos, Direction direction, BlockState state, BlockState neighbour) {
        if (state.getValue(ARMS[direction.ordinal()]) == ConduitArm.BLOCKED) {
            return ConduitArm.BLOCKED;
        }
        return joins(level, neighbourPos, direction.getOpposite(), neighbour) ? ConduitArm.CONNECTED : ConduitArm.NONE;
    }

    public static boolean joins(LevelReader level, BlockPos neighbourPos, Direction back, BlockState neighbour) {
        if (neighbour.getBlock() instanceof CurrentConduitBlock) {
            return neighbour.getValue(ARMS[back.ordinal()]) != ConduitArm.BLOCKED;
        }
        if (neighbour.getBlock() instanceof ConduitTapBlock) {
            return !(level.getBlockEntity(neighbourPos) instanceof ConduitTapBlockEntity tap) || !tap.face(back).disabled();
        }
        return false;
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction,
                                              BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        scheduleWater(state, level, tickAccess, pos);
        return state.setValue(ARMS[direction.ordinal()], arm(level, neighbourPos, direction, state, neighbour));
    }

    public static void toggleArm(Level level, BlockPos pos, BlockState state, Direction direction) {
        EnumProperty<ConduitArm> property = ARMS[direction.ordinal()];
        BlockState next;
        if (state.getValue(property) == ConduitArm.BLOCKED) {
            next = state.setValue(property, ConduitArm.NONE);
            BlockPos neighbourPos = pos.relative(direction);
            next = next.setValue(property, arm(level, neighbourPos, direction, next, level.getBlockState(neighbourPos)));
        } else {
            next = state.setValue(property, ConduitArm.BLOCKED);
        }
        level.setBlock(pos, next, Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 0.6F, next.getValue(property) == ConduitArm.BLOCKED ? 0.8F : 1.4F);
    }
}
