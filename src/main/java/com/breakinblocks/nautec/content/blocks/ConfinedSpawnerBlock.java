package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.content.blockentities.ConfinedSpawnerBlockEntity;
import com.breakinblocks.nautec.content.items.SpawnerConfinementMatrixItem;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
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
                try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Nautec.LOGGER)) {
                    TagValueOutput out = TagValueOutput.createWithContext(reporter, params.getLevel().registryAccess());
                    confined.saveCustomOnly(out);
                    out.discard("itemhandler");
                    BlockItem.setBlockEntityData(drop, confined.getType(), out);
                }
            }
        }
        return drops;
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
