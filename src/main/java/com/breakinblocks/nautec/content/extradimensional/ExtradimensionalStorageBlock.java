package com.breakinblocks.nautec.content.extradimensional;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.ContainerBlock;
import com.breakinblocks.nautec.content.resonance.ResonanceNetwork;
import com.breakinblocks.nautec.content.resonance.ResonanceNetworks;
import com.breakinblocks.nautec.content.resonance.ResonanceSync;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ExtradimensionalStorageBlock extends ContainerBlock {
    public static final IntegerProperty TIER = IntegerProperty.create("tier", 1, ExtradimensionalStorageBlockEntity.MAX_TIER);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 8, 16);

    public ExtradimensionalStorageBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(TIER, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(TIER));
    }

    @Override
    public boolean tickingEnabled() {
        return true;
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.EXTRADIMENSIONAL_STORAGE.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public static void open(ExtradimensionalStorageBlockEntity storage, ServerPlayer player) {
        ResonanceNetwork network = storage.getNetwork();
        if (network != null && !ResonanceNetworks.canUse(player, network)) {
            player.sendOverlayMessage(Component.translatable("nautec.resonance.locked", network.name(), network.ownerName())
                    .withStyle(ChatFormatting.RED));
            return;
        }
        player.openMenu(storage, storage.getBlockPos());
        ResonanceSync.send(player, storage);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof ExtradimensionalStorageBlockEntity storage)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            open(storage, serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(ExtradimensionalStorageBlock::new);
    }
}
