package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public class LuckyFishingZoneBlockEntity extends BlockEntity {
    private int radius = 1;

    public LuckyFishingZoneBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.LUCKY_FISHING_ZONE.get(), pos, state);
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
        setChanged();
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        this.radius = input.getIntOr("radius", 1);
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("radius", this.radius);
    }
}
