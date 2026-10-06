package com.breakinblocks.nautec.content.conduits;

import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConduitTapBlock extends ConduitPartBlock implements EntityBlock {
    public static final MapCodec<ConduitTapBlock> CODEC = simpleCodec(ConduitTapBlock::new);
    public static final EnumProperty<TapArm>[] ARMS = arms(TapArm.class);

    public ConduitTapBlock(Properties properties) {
        super(properties);
        registerDefaultState(withArms(getStateDefinition().any(), TapArm.NONE));
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
        int conduits = 0;
        int machines = 0;
        for (Direction direction : DIRECTIONS) {
            TapArm arm = state.getValue(ARMS[direction.ordinal()]);
            if (arm == TapArm.CONDUIT) {
                conduits |= 1 << direction.ordinal();
            } else if (arm == TapArm.MACHINE) {
                machines |= 1 << direction.ordinal();
            }
        }
        return ConduitShapes.tap(conduits, machines);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConduitTapBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != NTBlockEntityTypes.CONDUIT_TAP.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<ConduitTapBlockEntity>) (tickLevel, pos, tickState, tap) -> tap.serverTick((ServerLevel) tickLevel);
    }

    static boolean isConduitPart(BlockState state) {
        return state.getBlock() instanceof ConduitPartBlock;
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction,
                                              BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        scheduleWater(state, level, tickAccess, pos);
        boolean disabled = level.getBlockEntity(pos) instanceof ConduitTapBlockEntity tap && tap.face(direction).disabled();
        if (disabled || neighbour.isAir()) {
            return state.setValue(ARMS[direction.ordinal()], TapArm.NONE);
        }
        if (isConduitPart(neighbour)) {
            boolean joined = CurrentConduitBlock.joins(level, neighbourPos, direction.getOpposite(), neighbour);
            return state.setValue(ARMS[direction.ordinal()], joined ? TapArm.CONDUIT : TapArm.NONE);
        }
        tickAccess.scheduleTick(pos, this, 1);
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof ConduitTapBlockEntity tap) {
            tap.refreshArms();
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ConduitTapBlockEntity tap) {
            tap.setPowered(level.hasNeighborSignal(pos));
        }
    }

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                                   BlockHitResult hitResult) {
        if (stack.is(Tags.Items.TOOLS_WRENCH)) {
            return InteractionResult.PASS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ConduitTapBlockEntity tap) {
            player.openMenu(tap, buffer -> {
                buffer.writeBlockPos(pos);
                tap.writeFaces(buffer);
            });
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(NTBlocks.CURRENT_CONDUIT.get());
    }
}
