package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.content.blockentities.ColonyReplicatorBlockEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ColonyReplicatorBlock extends LaserBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public ColonyReplicatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    @Override
    public boolean waterloggable() {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING, ACTIVE));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state != null ? state.setValue(FACING, context.getHorizontalDirection().getOpposite()) : null;
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
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        if (!(level.getBlockEntity(blockPos) instanceof ColonyReplicatorBlockEntity replicator)) {
            return List.of();
        }
        boolean running = replicator.getStatus() == ColonyReplicatorBlockEntity.STATUS_RUNNING;
        return List.of(
                Component.translatable(statusKey(replicator.getStatus())).withStyle(running ? ChatFormatting.AQUA : ChatFormatting.RED),
                Component.translatable(replicator.isSplice() ? "nautec.replicator.mode.splice" : "nautec.replicator.mode.replicate")
                        .withStyle(ChatFormatting.WHITE),
                Component.translatable("nautec.replicator.biomass", replicator.getBiomass()).withStyle(ChatFormatting.WHITE)
        );
    }

    public static String statusKey(int status) {
        return switch (status) {
            case ColonyReplicatorBlockEntity.STATUS_RUNNING -> "nautec.replicator.status.running";
            case ColonyReplicatorBlockEntity.STATUS_NOT_ANALYZED -> "nautec.replicator.status.not_analyzed";
            case ColonyReplicatorBlockEntity.STATUS_NO_PARTNER -> "nautec.replicator.status.no_partner";
            case ColonyReplicatorBlockEntity.STATUS_WRONG_STRAIN -> "nautec.replicator.status.wrong_strain";
            case ColonyReplicatorBlockEntity.STATUS_NO_BIOMASS -> "nautec.replicator.status.no_biomass";
            case ColonyReplicatorBlockEntity.STATUS_OUTPUT_FULL -> "nautec.replicator.status.output_full";
            case ColonyReplicatorBlockEntity.STATUS_LOW_POWER -> "nautec.replicator.status.low_power";
            case ColonyReplicatorBlockEntity.STATUS_LOW_PURITY -> "nautec.replicator.status.low_purity";
            case ColonyReplicatorBlockEntity.STATUS_UNSUPPORTED -> "nautec.replicator.status.unsupported";
            default -> "nautec.replicator.status.no_template";
        };
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.COLONY_REPLICATOR.get();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(ColonyReplicatorBlock::new);
    }
}
