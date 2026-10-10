package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.content.blocks.LaserJunctionBlock;
import net.minecraft.server.level.ServerPlayer;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

import static org.codehaus.plexus.util.StringUtils.capitalizeFirstLetter;
import java.util.List;

public class LaserJunctionBlockEntity extends LaserBlockEntity {
    private final Set<Direction> inputDirections;
    private final Set<Direction> outputDirections;

    public LaserJunctionBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.LASER_JUNCTION.get(), blockPos, blockState);
        this.inputDirections = new ObjectOpenHashSet<>();
        this.outputDirections = new ObjectOpenHashSet<>();
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return inputDirections;
    }

    public String getLaserInputsAsString() {
        Set<Direction> inputs = getLaserInputs();
        if (inputs.isEmpty()) {
            return "No inputs";
        }

        return inputs.stream()
                .map(direction -> capitalizeFirstLetter(direction.getName()))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
    @Override
    public Set<Direction> getLaserOutputs() {
        return outputDirections;
    }

    public String getLaserOutputsAsString() {
        Set<Direction> outputs = getLaserOutputs();
        if (outputs.isEmpty()) {
            return "No outputs";
        }

        return outputs.stream()
                .map(direction -> capitalizeFirstLetter(direction.getName()))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public void commonTick() {
        super.commonTick();
        transmitPower(this.power);
    }

    @Override
    protected int outgoingPower(Direction direction) {
        return getPowerToTransfer() / Math.max(1, connectedOutputs());
    }

    @Override
    public void saveSettings(ValueOutput out) {
        BlockState state = getBlockState();
        for (Direction direction : Direction.values()) {
            out.putString(direction.getSerializedName(), state.getValue(LaserJunctionBlock.CONNECTION[direction.get3DDataValue()]).getSerializedName());
        }
    }

    @Override
    public boolean loadSettings(ValueInput in, ServerPlayer player) {
        BlockState state = getBlockState();
        for (Direction direction : Direction.values()) {
            String name = in.getStringOr(direction.getSerializedName(), "");
            LaserJunctionBlock.ConnectionType type = null;
            for (LaserJunctionBlock.ConnectionType candidate : LaserJunctionBlock.ConnectionType.values()) {
                if (candidate.getSerializedName().equals(name)) {
                    type = candidate;
                }
            }
            if (type == null) {
                return false;
            }
            state = state.setValue(LaserJunctionBlock.CONNECTION[direction.get3DDataValue()], type);
            inputDirections.remove(direction);
            outputDirections.remove(direction);
            if (type == LaserJunctionBlock.ConnectionType.INPUT) {
                inputDirections.add(direction);
            } else if (type == LaserJunctionBlock.ConnectionType.OUTPUT) {
                outputDirections.add(direction);
            }
        }
        level.setBlockAndUpdate(worldPosition, state);
        setChanged();
        return true;
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);

        out.store("InputDirections", Codec.STRING.listOf(), inputDirections.stream().map(Direction::getName).toList());
        out.store("OutputDirections", Codec.STRING.listOf(), outputDirections.stream().map(Direction::getName).toList());
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);

        inputDirections.clear();
        for (String directionName : in.read("InputDirections", Codec.STRING.listOf()).orElse(List.of())) {
            Direction direction = Direction.byName(directionName);
            if (direction != null) {
                inputDirections.add(direction);
            }
        }

        outputDirections.clear();
        for (String directionName : in.read("OutputDirections", Codec.STRING.listOf()).orElse(List.of())) {
            Direction direction = Direction.byName(directionName);
            if (direction != null) {
                outputDirections.add(direction);
            }
        }
    }
}
