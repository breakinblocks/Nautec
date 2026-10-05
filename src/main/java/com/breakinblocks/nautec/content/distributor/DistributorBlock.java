package com.breakinblocks.nautec.content.distributor;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.ContainerBlock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class DistributorBlock extends ContainerBlock {
    public DistributorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean tickingEnabled() {
        return true;
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.DISTRIBUTOR.get();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isSecondaryUseActive()) {
            if (player instanceof ServerPlayer serverPlayer) {
                DistributorLinking.toggle(serverPlayer, pos);
            } else if (level.isClientSide()) {
                DistributorLinking.toggleClient(player, pos);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(DistributorBlock::new);
    }
}
