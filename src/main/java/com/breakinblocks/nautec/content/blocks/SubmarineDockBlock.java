package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.content.blockentities.SubmarineDockBlockEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SubmarineDockBlock extends LaserBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 6, 16);

    public SubmarineDockBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean waterloggable() {
        return true;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(SubmarineDockBlock::new);
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.SUBMARINE_DOCK.get();
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        if (!(level.getBlockEntity(blockPos) instanceof SubmarineDockBlockEntity dock)) {
            return List.of();
        }

        return List.of(dock.isDocking()
                ? Component.literal("Sea Scout docked").withStyle(ChatFormatting.AQUA)
                : Component.literal("No Sea Scout on the pad").withStyle(ChatFormatting.GRAY));
    }
}
