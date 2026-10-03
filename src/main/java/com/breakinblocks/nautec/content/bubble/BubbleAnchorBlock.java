package com.breakinblocks.nautec.content.bubble;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.List;

public class BubbleAnchorBlock extends LaserBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public BubbleAnchorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
    }

    @Override
    public boolean waterloggable() {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(ACTIVE));
    }

    @Override
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        if (!(level.getBlockEntity(blockPos) instanceof BubbleAnchorBlockEntity anchor)) {
            return List.of();
        }
        int size = anchor.currentRadius() * 2 + 1;
        boolean running = anchor.getStatus() == BubbleAnchorBlockEntity.STATUS_FUEL || anchor.getStatus() == BubbleAnchorBlockEntity.STATUS_LASER;
        return List.of(
                Component.translatable(statusKey(anchor.getStatus())).withStyle(running ? ChatFormatting.AQUA : ChatFormatting.RED),
                Component.translatable("nautec.bubble_anchor.size", size, size, size).withStyle(ChatFormatting.WHITE),
                anchor.getStatus() == BubbleAnchorBlockEntity.STATUS_FUEL
                        ? Component.translatable("nautec.bubble_anchor.time", anchor.getBurn() / 20).withStyle(ChatFormatting.WHITE)
                        : Component.translatable(anchor.fillsWater() ? "nautec.bubble_anchor.mode.water" : "nautec.bubble_anchor.mode.air")
                        .withStyle(ChatFormatting.GRAY)
        );
    }

    public static String statusKey(int status) {
        return switch (status) {
            case BubbleAnchorBlockEntity.STATUS_FUEL -> "nautec.bubble_anchor.status.fuel";
            case BubbleAnchorBlockEntity.STATUS_LASER -> "nautec.bubble_anchor.status.laser";
            case BubbleAnchorBlockEntity.STATUS_NO_FUEL -> "nautec.bubble_anchor.status.no_fuel";
            default -> "nautec.bubble_anchor.status.off";
        };
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.BUBBLE_ANCHOR.get();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(BubbleAnchorBlock::new);
    }
}
