package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.content.blockentities.CrystalCultivatorBlockEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.utils.ItemUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class CrystalCultivatorBlock extends LaserBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ACTIVE = BlockStateProperties.POWERED;

    public CrystalCultivatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP).setValue(ACTIVE, false));
    }

    @Override
    public boolean waterloggable() {
        return true;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(CrystalCultivatorBlock::new);
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.CRYSTAL_CULTIVATOR.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING, ACTIVE));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(FACING, context.getClickedFace());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP -> Block.box(4, 0, 4, 12, 11, 12);
            case DOWN -> Block.box(4, 5, 4, 12, 16, 12);
            case NORTH -> Block.box(4, 4, 5, 12, 12, 16);
            case SOUTH -> Block.box(4, 4, 0, 12, 12, 11);
            case EAST -> Block.box(0, 4, 4, 11, 12, 12);
            case WEST -> Block.box(5, 4, 4, 16, 12, 12);
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CrystalCultivatorBlockEntity cultivator) {
            var inventory = cultivator.getItemStackHandler();
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.extractItem(slot, 64, false);
                if (!stack.isEmpty()) {
                    ItemUtils.giveItemToPlayer(player, stack, player.getInventory().selected);
                    if (!player.isShiftKeyDown()) break;
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public List<Component> displayText(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof CrystalCultivatorBlockEntity cultivator)) return List.of();
        return List.of(Component.translatable("nautec.monocle.power", cultivator.getPower()),
                Component.translatable("nautec.cultivator.speed", String.format(Locale.ROOT, "%.2f", cultivator.beamSpeed())),
                Component.translatable("nautec.cultivator.fortune", CrystalCultivatorBlockEntity.fortune(cultivator.getPurity())),
                Component.translatable("nautec.cultivator.buffer", cultivator.storedItems()));
    }
}
