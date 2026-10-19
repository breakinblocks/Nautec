package com.breakinblocks.nautec.content.reservoir;

import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import com.breakinblocks.nautec.utils.FluidInteractions;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeepSeaReservoirBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty TIER = IntegerProperty.create("tier", 1, ReservoirFluid.MAX_TIER);

    public DeepSeaReservoirBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(TIER, 1)
                .setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(DeepSeaReservoirBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TIER, BlockStateProperties.WATERLOGGED);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DeepSeaReservoirBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, NTBlockEntityTypes.DEEP_SEA_RESERVOIR.get(),
                (tickLevel, pos, tickState, reservoir) -> reservoir.serverTick());
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        Integer tier = context.getItemInHand().get(NTDataComponents.RESERVOIR_TIER.get());
        return state.setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(TIER, tier == null ? 1 : Math.clamp(tier, 1, ReservoirFluid.MAX_TIER))
                .setValue(BlockStateProperties.WATERLOGGED, ReservoirStructure.water(context.getLevel(), context.getClickedPos()));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        ReservoirStructure.build(level, pos, NTBlocks.DEEP_SEA_RESERVOIR_PART.get());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel) {
            ReservoirStructure.clear(level, pos);
        }
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
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof DeepSeaReservoirBlockEntity reservoir ? reservoir.comparatorSignal() : 0;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return open(level, pos, player);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                                       BlockHitResult hitResult) {
        return useOn(stack, level, pos, player, hand);
    }

    public static InteractionResult open(Level level, BlockPos core, Player player) {
        if (!(level.getBlockEntity(core) instanceof DeepSeaReservoirBlockEntity reservoir)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            player.openMenu(reservoir, buffer -> buffer.writeBlockPos(core));
        }
        return InteractionResult.SUCCESS;
    }

    public static ItemInteractionResult useOn(ItemStack stack, Level level, BlockPos core, Player player, InteractionHand hand) {
        if (!(level.getBlockEntity(core) instanceof DeepSeaReservoirBlockEntity reservoir)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.getItem() instanceof ReservoirUpgradeItem upgrade) {
            if (level.isClientSide()) {
                return ItemInteractionResult.SUCCESS;
            }
            if (reservoir.tier() + 1 != upgrade.tier()) {
                player.displayClientMessage(Component.translatable("nautec.deep_sea_reservoir.wrong_upgrade", ReservoirText.numeral(upgrade.tier() - 1),
                        ReservoirText.numeral(reservoir.tier()))
                        .withStyle(ChatFormatting.GOLD), true);
                return ItemInteractionResult.FAIL;
            }
            reservoir.upgradeTo(upgrade.tier());
            stack.consume(1, player);
            level.playSound(null, core, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.8f, 0.9f + 0.1f * upgrade.tier());
            player.displayClientMessage(Component.translatable("nautec.deep_sea_reservoir.upgraded", ReservoirText.numeral(upgrade.tier()),
                    ReservoirText.buckets(DeepSeaReservoirBlockEntity.capacityFor(upgrade.tier()))).withStyle(ChatFormatting.AQUA), true);
            return ItemInteractionResult.SUCCESS;
        }
        if (FluidInteractions.isFluidContainer(stack) && FluidInteractions.interact(player, hand, reservoir.tank())) {
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
