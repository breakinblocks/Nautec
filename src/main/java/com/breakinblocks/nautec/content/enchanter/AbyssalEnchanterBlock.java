package com.breakinblocks.nautec.content.enchanter;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.utils.FluidInteractions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AbyssalEnchanterBlock extends LaserBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public AbyssalEnchanterBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    @Override
    public boolean waterloggable() {
        return true;
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.ABYSSAL_ENCHANTER.get();
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(AbyssalEnchanterBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING, ACTIVE));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        EnchanterStructure.build(level, pos, NTBlocks.ABYSSAL_ENCHANTER_PART.get());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel) {
            EnchanterStructure.clear(level, pos);
        }
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return EnchanterStructure.outline(EnchanterStructure.CORE_CELL);
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos,
                                     BlockPos neighborPos) {
        return super.updateShape(EnchanterStructure.flood(state, direction, neighborState), direction, neighborState, level, pos, neighborPos);
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
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return open(level, pos, player);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                                       BlockHitResult hitResult) {
        return useOn(stack, level, pos, player, hand);
    }

    public static InteractionResult open(Level level, BlockPos core, Player player) {
        if (!(level.getBlockEntity(core) instanceof AbyssalEnchanterBlockEntity enchanter)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            player.openMenu(enchanter, buffer -> buffer.writeBlockPos(core));
        }
        return InteractionResult.SUCCESS;
    }

    public static ItemInteractionResult useOn(ItemStack stack, Level level, BlockPos core, Player player, InteractionHand hand) {
        if (level.getBlockEntity(core) instanceof AbyssalEnchanterBlockEntity enchanter && FluidInteractions.isFluidContainer(stack)
                && FluidInteractions.interact(player, hand, enchanter.getFluidTank())) {
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
