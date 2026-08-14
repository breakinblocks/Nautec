package com.breakinblocks.nautec.content.blockentities.multiblock.controller;

import com.google.common.collect.ImmutableMap;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.blockentities.multiblock.MultiblockEntity;
import com.breakinblocks.nautec.api.multiblocks.MultiblockData;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.blocks.multiblock.part.DrainPartBlock;
import com.breakinblocks.nautec.content.multiblocks.DrainMultiblock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import com.breakinblocks.nautec.utils.BlockUtils;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DrainBlockEntity extends LaserBlockEntity implements MultiblockEntity {
    private static final int DRAIN_INTERVAL_TICKS = 20;
    private static final int VALVE_TRAVEL_TICKS = 12;
    private static final int VALVE_SPEED = 30;
    private static final int LID_OPEN_TRAVEL_TICKS = 72;
    private static final int LID_OPEN_SPEED = 3;
    private static final int LID_CLOSE_TRAVEL_TICKS = 36;
    private static final int LID_CLOSE_SPEED = -6;
    private static final int VALVE_TO_LID_DELAY = 60;
    private static final int LID_TO_VALVE_DELAY = 30;

    private MultiblockData multiblockData;

    private final Rotator valve = new Rotator();
    private final Rotator lid = new Rotator();

    private boolean closing;
    private int valveLidInterval;

    public DrainBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.DRAIN.get(), blockPos, blockState);
        addFluidTank(NTConfig.drainCapacity);
        this.multiblockData = MultiblockData.EMPTY;
    }

    public boolean hasOperatingPower() {
        return getPower() > NTConfig.drainPower;
    }

    public void open() {
        if (!hasOperatingPower()) {
            return;
        }

        this.valve.start(VALVE_TRAVEL_TICKS, VALVE_SPEED);
        level.playSound(null, worldPosition, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1, 1f);

        setOpen(true);
    }

    @Override
    public void onPowerChanged() {
        super.onPowerChanged();

        updatePowerAndBubbles();

    }

    public void close() {
        this.lid.start(LID_CLOSE_TRAVEL_TICKS, LID_CLOSE_SPEED);
        this.closing = true;
    }

    public boolean isMoving() {
        return lid.isMoving() || valve.isMoving();
    }

    public boolean isClosing() {
        return closing;
    }

    private void setOpen(boolean value) {
        BlockPos selfPos = worldPosition;
        BlockPos[] aroundSelf = BlockUtils.getBlocksAroundSelf3x3(selfPos);
        for (BlockPos blockPos : aroundSelf) {
            BlockState state = level.getBlockState(blockPos);
            if (state.hasProperty(DrainPartBlock.OPEN)) {
                level.setBlockAndUpdate(blockPos, state.setValue(DrainPartBlock.OPEN, value));
            }
        }
        BlockState selfState = level.getBlockState(selfPos);
        if (selfState.hasProperty(DrainPartBlock.OPEN)) {
            level.setBlockAndUpdate(selfPos, selfState.setValue(DrainPartBlock.OPEN, value));
        }
    }

    private boolean hasWater() {
        BlockPos selfPos = worldPosition.above();
        BlockPos[] aroundSelf = BlockUtils.getBlocksAroundSelf3x3(selfPos);
        for (BlockPos blockPos : aroundSelf) {
            if (!level.getBlockState(blockPos).getFluidState().is(FluidTags.WATER))
                return false;
        }
        return level.getBlockState(selfPos).getFluidState().is(FluidTags.WATER);
    }

    @Override
    public Set<Direction> getLaserInputs() {
        if (getBlockState().getValue(DrainMultiblock.FORMED)) {
            return ObjectSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
        }
        return ObjectSet.of();
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of();
    }

    @Override
    public void commonTick() {
        super.commonTick();

        if (getPower() == 0 && getBlockState().getValue(DrainPartBlock.HAS_POWER)) {
            updatePowerAndBubbles();
        }

        performRotation();

        performDraining();

    }

    private void updatePowerAndBubbles() {
        BlockPos[] aroundSelf = BlockUtils.getBlocksAroundSelfHorizontal(worldPosition);
        boolean hasPower = getPower() > 15;
        for (BlockPos pos : aroundSelf) {
            BlockState state = level.getBlockState(pos);
            if (state.hasProperty(DrainPartBlock.HAS_POWER)) {
                level.setBlockAndUpdate(pos, state.setValue(DrainPartBlock.HAS_POWER, hasPower));
            }
        }
        BlockState selfState = getBlockState();
        if (selfState.hasProperty(DrainPartBlock.HAS_POWER)) {
            level.setBlockAndUpdate(worldPosition, selfState.setValue(DrainPartBlock.HAS_POWER, hasPower));
        }

        updateBubbleColumns();
    }

    private boolean openAndFormed() {
        BlockState blockState = getBlockState();
        return blockState.hasProperty(DrainPartBlock.OPEN) && blockState.getValue(DrainPartBlock.OPEN) && blockState.getValue(DrainMultiblock.FORMED);
    }

    private void performDraining() {
        if (level.getGameTime() % DRAIN_INTERVAL_TICKS == 0 && !lid.isMoving() && hasOperatingPower()) {
            if (hasWater()) {
                if (openAndFormed()) {
                    if (level.getBiome(worldPosition).is(BiomeTags.IS_OCEAN)) {
                        getFluidTank().fill(new FluidStack(NTFluids.SALT_WATER.getStillFluid(), NTConfig.drainSaltWaterAmount));
                    }
                }
            }
        }
    }

    private void performRotation() {
        if (valve.tick()) {
            if (!closing) {
                this.valveLidInterval = VALVE_TO_LID_DELAY;
            } else {
                this.closing = false;

                setOpen(false);
            }
        }

        if (valveLidInterval > 0) {
            valveLidInterval--;

            if (valveLidInterval == 0) {
                if (!closing) {
                    lid.start(LID_OPEN_TRAVEL_TICKS, LID_OPEN_SPEED);
                } else {
                    valve.start(VALVE_TRAVEL_TICKS, -VALVE_SPEED);
                }
            }
        }

        if (lid.tick()) {
            if (closing) {
                this.valveLidInterval = LID_TO_VALVE_DELAY;
            } else {
                updateBubbleColumns();
            }
        }
    }

    private void updateBubbleColumns() {
        if (getPower() > 15) {
            BlockPos selfPos = worldPosition;
            BlockPos[] aroundSelf = BlockUtils.getBlocksAroundSelfHorizontal(selfPos);
            for (BlockPos blockPos : aroundSelf) {
                BlockState blockState = level.getBlockState(blockPos);
                BubbleColumnBlock.updateColumn(Blocks.BUBBLE_COLUMN, level, blockPos.above(), blockState);
            }
            BubbleColumnBlock.updateColumn(Blocks.BUBBLE_COLUMN, level, selfPos.above(), level.getBlockState(selfPos));
        }
    }

    public float getValveIndependentAngle(float partialTicks) {
        return valve.renderAngle(partialTicks);
    }

    public float getLidIndependentAngle(float partialTicks) {
        return lid.renderAngle(partialTicks);
    }

    private static final class Rotator {
        private float independentAngle;
        private float chasingVelocity;
        private int ticksRemaining;
        private int speed;

        void start(int ticks, int speed) {
            this.ticksRemaining = ticks;
            this.speed = speed;
        }

        boolean isMoving() {
            return ticksRemaining > 0;
        }

        boolean tick() {
            chasingVelocity += ((speed * 10 / 3f) - chasingVelocity) * .25f;
            independentAngle += chasingVelocity;

            if (ticksRemaining > 0 && --ticksRemaining == 0) {
                this.speed = 0;
                return true;
            }
            return false;
        }

        float renderAngle(float partialTicks) {
            return (independentAngle + partialTicks * chasingVelocity) / 360;
        }
    }

    @Override
    public <T> ImmutableMap<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        if (capability == Capabilities.Fluid.BLOCK) {
            return ImmutableMap.of(
                    Direction.DOWN, Pair.of(IOActions.EXTRACT, new int[]{0})
            );
        }
        return ImmutableMap.of();
    }

    @Override
    public MultiblockData getMultiblockData() {
        return multiblockData;
    }

    @Override
    public void setMultiblockData(MultiblockData data) {
        this.multiblockData = data;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (MultiblockEntity.UNFORMING.compareAndSet(false, true)) {
            try {
                MultiblockHelper.unform(NTMultiblocks.DRAIN.get(), pos, level, null);
            } finally {
                MultiblockEntity.UNFORMING.set(false);
            }
        }
        level.removeBlock(pos.above(), false);
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.store("multiblockData", CompoundTag.CODEC, saveMBData());
        out.putFloat("angle", this.lid.independentAngle);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.multiblockData = loadMBData(in.read("multiblockData", CompoundTag.CODEC).orElseGet(CompoundTag::new));
        this.lid.independentAngle = in.getFloatOr("angle", 0);
    }
}
