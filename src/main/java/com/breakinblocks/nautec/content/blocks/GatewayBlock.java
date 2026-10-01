package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.ContainerBlock;
import com.breakinblocks.nautec.api.blocks.DisplayBlock;
import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.api.gateways.GatewayRing;
import com.breakinblocks.nautec.api.gateways.PackedGateway;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.content.blockentities.GatewayBlockEntity;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.network.OpenGatewayScreenPayload;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import com.breakinblocks.nautec.registries.NTSounds;
import com.breakinblocks.nautec.utils.MachineSounds;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GatewayBlock extends ContainerBlock implements DisplayBlock, SimpleWaterloggedBlock {
    public static final float FORMED_DESTROY_PROGRESS = 1F / 50F / 100F;
    public static final float FORMED_BLAST_RESISTANCE = 1200F;

    public GatewayBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(Multiblock.FORMED, false)
                .setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(Multiblock.FORMED, BlockStateProperties.WATERLOGGED));
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
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return state.getValue(Multiblock.FORMED) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(BlockStateProperties.WATERLOGGED, false);
    }

    @Override
    protected @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull LevelReader level, @NotNull ScheduledTickAccess tickAccess,
                                              @NotNull BlockPos pos, @NotNull Direction direction, @NotNull BlockPos neighborPos,
                                              @NotNull BlockState neighborState, @NotNull RandomSource random) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    public static int slotFor(BlockPos pos, Direction face, Vec3 hit) {
        double up = hit.y - pos.getY();
        double across = switch (face) {
            case NORTH -> 1 - (hit.x - pos.getX());
            case SOUTH -> hit.x - pos.getX();
            case EAST -> 1 - (hit.z - pos.getZ());
            case WEST -> hit.z - pos.getZ();
            default -> hit.x - pos.getX();
        };
        int right = across >= 0.5 ? 1 : 0;
        int lower = up < 0.5 ? 1 : 0;
        return lower * 2 + right;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof GatewayBlockEntity gateway)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }
        return useOnRing(stack, level, gateway, player, hitResult);
    }

    public static InteractionResult useOnRing(ItemStack stack, Level level, GatewayBlockEntity gateway, Player player, BlockHitResult hitResult) {
        if (stack.is(Tags.Items.TOOLS_WRENCH)) {
            return useWrench(level, gateway, player);
        }
        DyeColor colour = GatewayAddress.colourOf(stack.getItem());
        if (colour == null) {
            return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
        }

        int slot = gateway.isFormed()
                ? GatewayRing.slotAt(hitResult.getLocation(), gateway.getBlockPos(), gateway.getFront())
                : slotFor(gateway.getBlockPos(), hitResult.getDirection(), hitResult.getLocation());
        GatewayAddress updated = gateway.getAddress().withSlot(slot, colour);
        if (updated.equals(gateway.getAddress())) {
            return InteractionResult.CONSUME;
        }

        if (!level.isClientSide()) {
            gateway.setAddress(updated);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(updated.describe(), true);
            }
            MachineSounds.play(level, gateway.getBlockPos(), NTSounds.GATEWAY_RECODE, 0.8f, 1.0f + slot * 0.1f);
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult useWrench(Level level, GatewayBlockEntity gateway, Player player) {
        if (player.isShiftKeyDown()) {
            if (!gateway.isFormed()) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide()) {
                ItemStack packed = gateway.pack();
                if (packed != null && !player.getInventory().add(packed)) {
                    player.drop(packed, false);
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (gateway.isFormed()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            MultiblockHelper.form(NTMultiblocks.GATEWAY.get(), gateway.getBlockPos(), level, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return state.getValue(Multiblock.FORMED) ? FORMED_DESTROY_PROGRESS : super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return state.getValue(Multiblock.FORMED) ? FORMED_BLAST_RESISTANCE : super.getExplosionResistance(state, level, pos, explosion);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof GatewayBlockEntity gateway)) {
            return InteractionResult.PASS;
        }
        return openScreen(player, gateway);
    }

    public static InteractionResult openScreen(Player player, GatewayBlockEntity gateway) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new OpenGatewayScreenPayload(gateway.getBlockPos(), gateway.getAddress()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.getBlockEntity(pos) instanceof GatewayBlockEntity gateway) {
            GatewayAddress stored = stack.get(NTDataComponents.GATEWAY_ADDRESS.get());
            if (stored != null) {
                gateway.setAddress(stored);
            }
            PackedGateway packed = stack.get(NTDataComponents.GATEWAY_PACKED.get());
            if (packed != null) {
                gateway.unpack(packed, placer instanceof Player player ? player : null);
            } else {
                gateway.markPlacedByPlayer();
            }
        }
    }

    @Override
    protected @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (state.getValue(Multiblock.FORMED)) {
            return List.of();
        }
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
        return describe(gateway);
    }

    public static List<Component> describe(GatewayBlockEntity gateway) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("nautec.monocle.address").withStyle(ChatFormatting.WHITE)
                .append(gateway.getAddress().describe()));
        if (gateway.isFormed()) {
            lines.add(status(gateway));
            if (gateway.isWild()) {
                lines.add(Component.translatable("nautec.gateway.status.wild").withStyle(ChatFormatting.DARK_AQUA));
            }
        } else if (gateway.getBlockedAt() != null) {
            BlockPos blocked = gateway.getBlockedAt();
            lines.add(Component.translatable("nautec.gateway.status.blocked", blocked.getX(), blocked.getY(), blocked.getZ())
                    .withStyle(ChatFormatting.GOLD));
        } else if (gateway.isWaitingToBuild() || gateway.needsSelfHeal()) {
            lines.add(Component.translatable("nautec.gateway.status.building").withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(Component.translatable("nautec.gateway.status.unformed").withStyle(ChatFormatting.GRAY));
        }
        if (gateway.needsPower()) {
            lines.add(Component.translatable("nautec.gateway.power", gateway.getEnergy(), GatewayBlockEntity.ENERGY_CAPACITY,
                    GatewayBlockEntity.ENERGY_PER_TICK).withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }

    private static Component status(GatewayBlockEntity gateway) {
        if (gateway.isRedstoneLocked()) {
            return Component.translatable("nautec.gateway.status.redstone").withStyle(ChatFormatting.RED);
        }
        if (gateway.isOpen()) {
            return Component.translatable("nautec.gateway.status.open").withStyle(ChatFormatting.AQUA);
        }
        if (!gateway.isLinked()) {
            return Component.translatable("nautec.gateway.status.unlinked").withStyle(ChatFormatting.GRAY);
        }
        if (gateway.needsPower() && gateway.getEnergy() < GatewayBlockEntity.ENERGY_PER_TICK) {
            return Component.translatable("nautec.gateway.status.no_power").withStyle(ChatFormatting.GOLD);
        }
        return Component.translatable("nautec.gateway.status.ready").withStyle(ChatFormatting.WHITE);
    }
}
