package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.content.blockentities.CrystalCradleBlockEntity;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.utils.ItemUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrystalCradleBlock extends LaserBlock {
    public CrystalCradleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean waterloggable() {
        return true;
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.CRYSTAL_CRADLE.get();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(CrystalCradleBlock::new);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof CrystalCradleBlockEntity cradle)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }
        if (stack.is(NTItems.PRISMARINE_CRYSTAL_SEED.get())) {
            if (!level.isClientSide()) {
                if (cradle.insertSeed(stack)) {
                    stack.consume(1, player);
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
                } else {
                    player.sendOverlayMessage(Component.translatable("nautec.crystal_cradle.message.occupied").withStyle(ChatFormatting.GOLD));
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.is(NTItems.DORMANT_CRYSTAL_SEED.get())) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("nautec.crystal_cradle.message.dormant").withStyle(ChatFormatting.GOLD));
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof CrystalCradleBlockEntity cradle) || !cradle.hasSeed()) {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }
        if (!level.isClientSide()) {
            ItemStack seed = cradle.removeSeed();
            if (seed.isEmpty()) {
                player.sendOverlayMessage(Component.translatable("nautec.crystal_cradle.message.growing").withStyle(ChatFormatting.GOLD));
            } else {
                ItemUtils.giveItemToPlayer(player, seed);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CrystalCradleBlockEntity cradle) {
            boolean seeded = Boolean.TRUE.equals(stack.get(NTDataComponents.CRADLE_SEEDED));
            long growth = stack.getOrDefault(NTDataComponents.CRADLE_GROWTH, 0L);
            if (seeded) {
                cradle.restore(true, growth);
            }
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CrystalCradleBlockEntity cradle && cradle.hasSeed()) {
            for (ItemStack drop : drops) {
                if (drop.is(asItem())) {
                    drop.set(NTDataComponents.CRADLE_SEEDED, true);
                    drop.set(NTDataComponents.CRADLE_GROWTH, cradle.getGrowth());
                }
            }
        }
        return drops;
    }
}
