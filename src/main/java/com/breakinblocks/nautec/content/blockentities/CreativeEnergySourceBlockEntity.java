package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class CreativeEnergySourceBlockEntity extends BlockEntity implements EnergyHandler {
    private static final int MAX_ENERGY = Integer.MAX_VALUE;

    public CreativeEnergySourceBlockEntity(BlockPos pos, BlockState blockState) {
        super(NTBlockEntityTypes.CREATIVE_ENERGY_SOURCE.get(), pos, blockState);
    }

    @Override
    public long getAmountAsLong() {
        return MAX_ENERGY;
    }

    @Override
    public long getCapacityAsLong() {
        return MAX_ENERGY;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return amount;
    }
}
