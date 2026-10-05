package com.breakinblocks.nautec.content.blocks.multiblock.controller;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.api.utils.HorizontalDirection;
import com.breakinblocks.nautec.content.multiblocks.BioReactorMultiblock;
import com.breakinblocks.nautec.content.multiblocks.IndustrialBioReactorMultiblock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class IndustrialBioReactorBlock extends LaserBlock {
    public IndustrialBioReactorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(Multiblock.FORMED, false)
                .setValue(BioReactorMultiblock.ACTIVE, false)
                .setValue(IndustrialBioReactorMultiblock.ORIENTATION, HorizontalDirection.NORTH)
        );
    }

    @Override
    public boolean waterloggable() {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(Multiblock.FORMED, BioReactorMultiblock.ACTIVE, IndustrialBioReactorMultiblock.ORIENTATION));
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.INDUSTRIAL_BIO_REACTOR.get();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(IndustrialBioReactorBlock::new);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (state.getValue(Multiblock.FORMED)) {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }
        return InteractionResult.FAIL;
    }
}
