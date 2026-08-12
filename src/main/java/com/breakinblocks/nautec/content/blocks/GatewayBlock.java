package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.ContainerBlock;
import com.breakinblocks.nautec.api.blocks.DisplayBlock;
import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.content.blockentities.GatewayBlockEntity;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GatewayBlock extends ContainerBlock implements DisplayBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 4, 16);

    public GatewayBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean tickingEnabled() {
        return true;
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.GATEWAY.get();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(GatewayBlock::new);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * The four address fins sit one per quadrant of the top face, so where you click picks the slot.
     */
    public static int slotFor(BlockPos pos, Vec3 hit) {
        double x = hit.x - pos.getX();
        double z = hit.z - pos.getZ();
        int east = x >= 0.5 ? 1 : 0;
        int south = z >= 0.5 ? 1 : 0;
        return south * 2 + east;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof GatewayBlockEntity gateway)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        DyeColor colour = GatewayAddress.colourOf(stack.getItem());
        if (colour == null) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        int slot = slotFor(pos, hitResult.getLocation());
        GatewayAddress updated = gateway.getAddress().withSlot(slot, colour);
        if (updated.equals(gateway.getAddress())) {
            return InteractionResult.CONSUME;
        }

        if (!level.isClientSide()) {
            gateway.setAddress(updated);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(updated.describe(), true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof GatewayBlockEntity gateway)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(gateway.getAddress().describe(), true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        GatewayAddress stored = stack.get(NTDataComponents.GATEWAY_ADDRESS.get());
        if (stored != null && level.getBlockEntity(pos) instanceof GatewayBlockEntity gateway) {
            gateway.setAddress(stored);
        }
    }

    @Override
    protected @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (!(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof GatewayBlockEntity gateway)) {
            return drops;
        }
        for (ItemStack drop : drops) {
            if (drop.is(asItem())) {
                drop.set(NTDataComponents.GATEWAY_ADDRESS.get(), gateway.getAddress());
            }
        }
        return drops;
    }

    @Override
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        if (!(level.getBlockEntity(blockPos) instanceof GatewayBlockEntity gateway)) {
            return List.of();
        }
        return List.of(Component.literal("Address: ").withStyle(ChatFormatting.WHITE)
                .append(gateway.getAddress().describe()));
    }
}
