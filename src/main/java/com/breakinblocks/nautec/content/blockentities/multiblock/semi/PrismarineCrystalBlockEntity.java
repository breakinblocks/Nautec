package com.breakinblocks.nautec.content.blockentities.multiblock.semi;

import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTCriteriaTriggers;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class PrismarineCrystalBlockEntity extends LaserBlockEntity {
    private boolean breaking;
    private boolean cultivated;
    private long startTick;
    private int duration;

    public PrismarineCrystalBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.PRISMARINE_CRYSTAL.get(), blockPos, blockState);
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(
                Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
        );
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public void commonTick() {
        super.commonTick();
        for (int offset = -3; offset <= 2; offset++) {
            if (offset != 0 && level.getBlockEntity(worldPosition.above(offset)) instanceof PrismarineCrystalPartBlockEntity part) {
                power += part.getPower();
            }
        }

        setPurity(3f);

        if (level instanceof ServerLevel serverLevel && serverLevel.getGameTime() % 40 == 0) {
            NTCriteriaTriggers.triggerNear(NTCriteriaTriggers.CRYSTAL_FOUND.get(), serverLevel, worldPosition, 10.0);
        }

        if (duration > 0 && isBreaking()) {
            duration--;
            if (duration == 0) {
                this.breaking = false;
            }
        }
    }

    public boolean isCultivated() {
        return cultivated;
    }

    public void setCultivated(boolean cultivated) {
        this.cultivated = cultivated;
        setChanged();
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putBoolean("cultivated", cultivated);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.cultivated = in.getBooleanOr("cultivated", false);
    }

    public boolean isBreaking() {
        return breaking;
    }

    public long getStartTick() {
        return startTick;
    }

    public int getDuration() {
        return duration;
    }

    public void playBreakAnimation() {
        this.breaking = true;
        this.startTick = level.getGameTime();
        this.duration = 10;
    }
}
