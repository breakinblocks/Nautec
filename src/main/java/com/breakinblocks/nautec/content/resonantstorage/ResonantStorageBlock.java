package com.breakinblocks.nautec.content.resonantstorage;

import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ResonantStorageBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected ResonantStorageBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }

    @Override
    protected @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull LevelReader level, @NotNull ScheduledTickAccess tickAccess,
                                              @NotNull BlockPos pos, @NotNull Direction direction, @NotNull BlockPos neighborPos,
                                              @NotNull BlockState neighborState, @NotNull RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof ResonantStorageBlockEntity storage)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!storage.canUse(player)) {
            denied(player, storage);
            return InteractionResult.FAIL;
        }
        player.openMenu(storage, buffer -> buffer.writeBlockPos(pos));
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                                   BlockHitResult hitResult) {
        if (!stack.is(NTItems.RESONANT_EXPANSION.get()) || !(level.getBlockEntity(pos) instanceof ResonantStorageBlockEntity storage)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!storage.canUse(player)) {
            denied(player, storage);
            return InteractionResult.FAIL;
        }
        ResonantStore store = storage.store();
        if (store == null) {
            return InteractionResult.FAIL;
        }
        int inserted;
        try (Transaction transaction = Transaction.openRoot()) {
            inserted = store.upgradeSlot().insert(0, ItemResource.of(stack), 1, transaction);
            transaction.commit();
        }
        if (inserted <= 0) {
            player.sendOverlayMessage(Component.translatable("nautec.resonant_storage.expansions_full").withStyle(ChatFormatting.GOLD));
            return InteractionResult.FAIL;
        }
        stack.consume(1, player);
        player.sendOverlayMessage(Component.translatable("nautec.resonant_storage.expansions", store.upgrades(), ResonantStore.MAX_UPGRADES)
                .withStyle(ChatFormatting.AQUA));
        return InteractionResult.SUCCESS_SERVER;
    }

    protected static void denied(Player player, ResonantStorageBlockEntity storage) {
        player.sendOverlayMessage(Component.translatable("nautec.resonant_storage.denied", storage.ownerName()).withStyle(ChatFormatting.RED));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(placer instanceof ServerPlayer player) || !(level.getBlockEntity(pos) instanceof ResonantStorageBlockEntity storage)) {
            return;
        }
        ResonantLink carried = stack.get(NTDataComponents.RESONANT_LINK.get());
        if (carried != null && ResonantAccess.canUse(player, carried.channel())) {
            return;
        }
        GatewayAddress address = carried != null ? carried.channel().address() : GatewayAddress.DEFAULT;
        ResonantLink own = ResonantAccess.key(player, ChannelAccess.PRIVATE, address);
        if (own != null) {
            storage.link(own);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof ResonantStorageBlockEntity storage) {
            storage.serverTick(level);
        }
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ResonantStorageBlockEntity storage && !storage.canUse(player)) {
            return 0.0f;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof ResonantStorageBlockEntity storage ? storage.comparatorSignal() : 0;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(id, param);
    }
}
