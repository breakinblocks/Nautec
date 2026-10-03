package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.content.blockentities.AdvancedBacterialAnalyzerBlockEntity;
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

public class AdvancedBacterialAnalyzerBlock extends LaserBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public AdvancedBacterialAnalyzerBlock(Properties properties) {
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
        if (!(level.getBlockEntity(blockPos) instanceof AdvancedBacterialAnalyzerBlockEntity analyzer)) {
            return List.of();
        }
        boolean running = analyzer.getStatus() == AdvancedBacterialAnalyzerBlockEntity.STATUS_RUNNING;
        return List.of(
                Component.translatable(statusKey(analyzer.getStatus())).withStyle(running ? ChatFormatting.AQUA : ChatFormatting.RED),
                Component.translatable("nautec.monocle.power", analyzer.getPower()).withStyle(ChatFormatting.WHITE),
                Component.translatable("nautec.monocle.purity", String.format("%.2f", analyzer.getPurity())).withStyle(ChatFormatting.WHITE)
        );
    }

    public static String statusKey(int status) {
        return switch (status) {
            case AdvancedBacterialAnalyzerBlockEntity.STATUS_RUNNING -> "nautec.advanced_analyzer.status.running";
            case AdvancedBacterialAnalyzerBlockEntity.STATUS_OUTPUT_FULL -> "nautec.advanced_analyzer.status.output_full";
            case AdvancedBacterialAnalyzerBlockEntity.STATUS_LOW_POWER -> "nautec.advanced_analyzer.status.low_power";
            case AdvancedBacterialAnalyzerBlockEntity.STATUS_LOW_PURITY -> "nautec.advanced_analyzer.status.low_purity";
            default -> "nautec.advanced_analyzer.status.idle";
        };
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.ADVANCED_BACTERIAL_ANALYZER.get();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(AdvancedBacterialAnalyzerBlock::new);
    }
}
