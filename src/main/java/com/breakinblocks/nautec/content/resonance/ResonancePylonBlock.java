package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.ContainerBlock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ResonancePylonBlock extends ContainerBlock {
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 12, 15);

    private final boolean interdimensional;

    public ResonancePylonBlock(Properties properties, boolean interdimensional) {
        super(properties);
        this.interdimensional = interdimensional;
    }

    public boolean isInterdimensional() {
        return interdimensional;
    }

    @Override
    public boolean tickingEnabled() {
        return true;
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.RESONANCE_PYLON.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof ResonancePylonBlockEntity pylon)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            ResonanceNetwork network = pylon.getNetwork();
            if (network != null && !ResonanceNetworks.canUse(serverPlayer, network)) {
                serverPlayer.sendOverlayMessage(Component.translatable("nautec.resonance.locked", network.name(), network.ownerName())
                        .withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
            serverPlayer.openMenu(pylon, pos);
            ResonanceSync.send(serverPlayer, pylon);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(properties -> new ResonancePylonBlock(properties, interdimensional));
    }
}
