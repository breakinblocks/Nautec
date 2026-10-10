package com.breakinblocks.nautec.content.blocks;

import net.minecraft.world.entity.item.ItemEntity;
import com.breakinblocks.nautec.utils.FluidInteractions;
import com.breakinblocks.nautec.transfer.fluid.FluidResource;
import com.breakinblocks.nautec.transfer.ResourceHandler;
import net.minecraft.world.InteractionHand;
import it.unimi.dsi.fastutil.ints.IntSets;
import com.breakinblocks.nautec.capabilities.fluid.TankList;
import com.breakinblocks.nautec.capabilities.RoleResourceHandler;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.content.blockentities.ConfinedSpawnerBlockEntity;
import com.breakinblocks.nautec.content.items.SpawnerConfinementMatrixItem;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class ConfinedSpawnerBlock extends LaserBlock {
    public ConfinedSpawnerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean waterloggable() {
        return false;
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.CONFINED_SPAWNER.get();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(ConfinedSpawnerBlock::new);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (!(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof ConfinedSpawnerBlockEntity confined)) {
            return drops;
        }
        for (ItemStack drop : drops) {
            if (drop.is(asItem())) {
                keepContents(drop, confined, params.getLevel());
            }
        }
        return drops;
    }

    private static void keepContents(ItemStack stack, ConfinedSpawnerBlockEntity confined, Level level) {
        BlockItem.setBlockEntityData(stack, confined.getType(), confined.saveCustomOnly(level.registryAccess()));
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.getAbilities().instabuild && level.getBlockEntity(pos) instanceof ConfinedSpawnerBlockEntity confined
                && confined.hasContents()) {
            ItemStack stack = new ItemStack(asItem());
            keepContents(stack, confined, level);
            ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof ConfinedSpawnerBlockEntity confined) || confined.getFluidTank().getFluidAmount() <= 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        ResourceHandler<FluidResource> drainOnly = new RoleResourceHandler<>(new TankList(confined.fluidTanks()), IntSets.EMPTY_SET,
                IntSets.singleton(0), FluidResource.EMPTY);
        if (level.isClientSide()) {
            return FluidInteractions.isFluidContainer(stack)
                    ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (FluidInteractions.interact(player, hand, drainOnly)) {
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isSecondaryUseActive() && level.getBlockEntity(pos) instanceof ConfinedSpawnerBlockEntity confined) {
            if (!level.isClientSide()) {
                SpawnerConfinementMatrixItem.release(level, pos, confined, player);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}
