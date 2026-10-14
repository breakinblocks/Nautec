package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedAquaticCatalystBlockEntity extends AquaticCatalystBlockEntity {
    public AdvancedAquaticCatalystBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.ADVANCED_AQUATIC_CATALYST.get(), blockPos, blockState);
    }

    @Override
    public int fuelPerBurn() {
        return NTConfig.advancedCatalystFuelMultiplier;
    }

    @Override
    public double purityMultiplier() {
        return NTConfig.advancedCatalystPurityMultiplier;
    }
}
