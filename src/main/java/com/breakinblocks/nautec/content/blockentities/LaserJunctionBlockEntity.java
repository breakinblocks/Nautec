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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

import static org.codehaus.plexus.util.StringUtils.capitalizeFirstLetter;
import java.util.List;

public class LaserJunctionBlockEntity extends LaserBlockEntity {
    private final Set<Direction> inputDirections;
    private final Set<Direction> outputDirections;
    private final int[] outputLimits = new int[Direction.values().length];

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
        int open = 0;
        for (Direction output : getLaserOutputs()) {
            if (laserDistances.getInt(output) > 0 && !loopsBack(output)) {
                open |= bit(output);
            }
        }
        if ((open & bit(direction)) == 0) {
            return 0;
        }
        int remaining = getPowerToTransfer();
        int free = open;
        boolean settled = false;
        while (!settled && free != 0) {
            settled = true;
            int share = remaining / Integer.bitCount(free);
            for (Direction output : Direction.values()) {
                int limit = outputLimits[output.get3DDataValue()];
                if ((free & bit(output)) != 0 && limit > 0 && limit <= share) {
                    free &= ~bit(output);
                    remaining -= limit;
                    settled = false;
                }
            }
        }
        if ((free & bit(direction)) == 0) {
            return outputLimits[direction.get3DDataValue()];
        }
        return remaining / Integer.bitCount(free);
    }

    private static int bit(Direction direction) {
        return 1 << direction.get3DDataValue();
    }

    public int getOutputLimit(Direction direction) {
        return outputLimits[direction.get3DDataValue()];
    }

    public void setOutputLimit(Direction direction, int limit) {
        int clamped = Math.max(0, limit);
        if (outputLimits[direction.get3DDataValue()] != clamped) {
            outputLimits[direction.get3DDataValue()] = clamped;
            update();
        }
    }

    public void setConnection(Direction direction, LaserJunctionBlock.ConnectionType type) {
        if (level != null) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(LaserJunctionBlock.CONNECTION[direction.get3DDataValue()], type));
        }
        inputDirections.remove(direction);
        outputDirections.remove(direction);
        if (type == LaserJunctionBlock.ConnectionType.INPUT) {
            inputDirections.add(direction);
        } else if (type == LaserJunctionBlock.ConnectionType.OUTPUT) {
            outputDirections.add(direction);
        }
        setChanged();
    }

    @Override
    public void saveSettings(ValueOutput out) {
        BlockState state = getBlockState();
        for (Direction direction : Direction.values()) {
            out.putString(direction.getSerializedName(), state.getValue(LaserJunctionBlock.CONNECTION[direction.get3DDataValue()]).getSerializedName());
        }
        out.putIntArray("OutputLimits", outputLimits.clone());
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
        loadLimits(in);
        level.setBlockAndUpdate(worldPosition, state);
        update();
        return true;
    }

    private void loadLimits(ValueInput in) {
        int[] saved = in.getIntArray("OutputLimits").orElse(new int[0]);
        for (int i = 0; i < outputLimits.length; i++) {
            outputLimits[i] = i < saved.length ? Math.max(0, saved[i]) : 0;
        }
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);

        out.store("InputDirections", Codec.STRING.listOf(), inputDirections.stream().map(Direction::getName).toList());
        out.store("OutputDirections", Codec.STRING.listOf(), outputDirections.stream().map(Direction::getName).toList());
        out.putIntArray("OutputLimits", outputLimits.clone());
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
        loadLimits(in);
    }
}
