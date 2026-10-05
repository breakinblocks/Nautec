package com.breakinblocks.nautec.content.blocks.multiblock.part;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.api.utils.HorizontalDirection;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.IndustrialBioReactorPartBlockEntity;
import com.breakinblocks.nautec.content.items.tools.AquarineWrenchItem;
import com.breakinblocks.nautec.content.multiblocks.BioReactorMultiblock;
import com.breakinblocks.nautec.content.multiblocks.IndustrialBioReactorMultiblock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class IndustrialBioReactorPartBlock extends LaserBlock {
    public IndustrialBioReactorPartBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(Multiblock.FORMED, false)
                .setValue(IndustrialBioReactorMultiblock.LAYER, 0)
                .setValue(IndustrialBioReactorMultiblock.CELL, 0)
                .setValue(IndustrialBioReactorMultiblock.ORIENTATION, HorizontalDirection.NORTH)
                .setValue(BioReactorMultiblock.HATCH, false)
                .setValue(BioReactorMultiblock.ACTIVE, false)
        );
    }

    @Override
    public boolean waterloggable() {
        return false;
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.INDUSTRIAL_BIO_REACTOR_PART.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(
                Multiblock.FORMED,
                IndustrialBioReactorMultiblock.LAYER,
                IndustrialBioReactorMultiblock.CELL,
                IndustrialBioReactorMultiblock.ORIENTATION,
                BioReactorMultiblock.HATCH,
                BioReactorMultiblock.ACTIVE
        ));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(IndustrialBioReactorPartBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IndustrialBioReactorPartBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.getMainHandItem().getItem() instanceof AquarineWrenchItem) {
            return InteractionResult.PASS;
        }
        if (level.getBlockEntity(pos) instanceof IndustrialBioReactorPartBlockEntity part && part.getControllerPos() != null
                && level.getBlockEntity(part.getControllerPos()) instanceof MenuProvider controller) {
            if (!level.isClientSide()) {
                player.openMenu(controller, part.getControllerPos());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
