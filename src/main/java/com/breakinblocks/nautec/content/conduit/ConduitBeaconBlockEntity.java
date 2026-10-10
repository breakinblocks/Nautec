package com.breakinblocks.nautec.content.conduit;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class ConduitBeaconBlockEntity extends LaserBlockEntity {
    public static final int SHAPE_INTERVAL = 40;
    public static final int EFFECT_INTERVAL = 40;
    public static final int EFFECT_TICKS = 260;
    public static final int MIN_FRAME = 16;
    private static final int BEAM_MEMORY = 5;
    private static final int REDSTONE_INTERVAL = 10;

    private final Map<Direction, Long> beamSides = new EnumMap<>(Direction.class);
    private int buffer;
    private int frameSize;
    private boolean water;
    private boolean running;
    private boolean checked;
    private boolean powered;
    private final List<BlockPos> frame = new ArrayList<>();

    public ConduitBeaconBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.CONDUIT_BEACON.get(), pos, state);
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return EnumSet.allOf(Direction.class);
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return Set.of();
    }

    @Override
    public void receivePower(int amount, Direction direction, BlockPos originPos) {
        super.receivePower(amount, direction, originPos);
        if (amount > 0 && level != null) {
            beamSides.put(direction.getOpposite(), level.getGameTime());
        }
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        long time = serverLevel.getGameTime();
        buffer = (int) Math.min(NTConfig.conduitBeaconBuffer, (long) buffer + getPower());
        if (!checked || time % SHAPE_INTERVAL == 0) {
            checkFrame(time);
            checked = true;
        }
        if (time % REDSTONE_INTERVAL == 0) {
            powered = checkRedstone();
        }

        boolean run = isFormed() && !powered && buffer >= NTConfig.conduitBeaconPowerUsage;
        if (run) {
            buffer -= NTConfig.conduitBeaconPowerUsage;
        }
        if (run != running) {
            running = run;
            setChanged();
        }
        if (running) {
            ConduitBeaconTracker.add(serverLevel, worldPosition);
            if (time % EFFECT_INTERVAL == 0) {
                applyEffects(serverLevel);
            }
        } else {
            ConduitBeaconTracker.remove(serverLevel, worldPosition);
        }
        if (getBlockState().getValue(ConduitBeaconBlock.ACTIVE) != running) {
            serverLevel.setBlock(worldPosition, getBlockState().setValue(ConduitBeaconBlock.ACTIVE, running), Block.UPDATE_CLIENTS);
        }
    }

    public void checkFrame(long time) {
        water = true;
        for (int x = -1; x <= 1 && water; x++) {
            for (int y = -1; y <= 1 && water; y++) {
                for (int z = -1; z <= 1 && water; z++) {
                    if (!level.isWaterAt(worldPosition.offset(x, y, z))) {
                        water = false;
                    }
                }
            }
        }
        int count = 0;
        frame.clear();
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    if (!isFrameSlot(x, y, z)) {
                        continue;
                    }
                    BlockPos pos = worldPosition.offset(x, y, z);
                    if (level.getBlockState(pos).isConduitFrame(level, pos, worldPosition)) {
                        frame.add(pos);
                        count++;
                    } else if (isBeamGap(x, y, z, time)) {
                        count++;
                    }
                }
            }
        }
        frameSize = count;
    }

    public boolean checkRedstone() {
        if (level.hasNeighborSignal(worldPosition)) {
            return true;
        }
        for (BlockPos pos : frame) {
            if (level.hasNeighborSignal(pos)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFrameSlot(int x, int y, int z) {
        int ax = Math.abs(x);
        int ay = Math.abs(y);
        int az = Math.abs(z);
        return (ax > 1 || ay > 1 || az > 1)
                && (x == 0 && (ay == 2 || az == 2) || y == 0 && (ax == 2 || az == 2) || z == 0 && (ax == 2 || ay == 2));
    }

    private boolean isBeamGap(int x, int y, int z, long time) {
        for (Map.Entry<Direction, Long> entry : beamSides.entrySet()) {
            Direction side = entry.getKey();
            if (time - entry.getValue() <= BEAM_MEMORY + SHAPE_INTERVAL
                    && side.getStepX() * 2 == x && side.getStepY() * 2 == y && side.getStepZ() * 2 == z) {
                return true;
            }
        }
        return false;
    }

    private void applyEffects(ServerLevel serverLevel) {
        int range = effectRange();
        AABB area = new AABB(worldPosition).inflate(range).expandTowards(0, serverLevel.getHeight(), 0);
        for (Player player : serverLevel.getEntitiesOfClass(Player.class, area)) {
            if (worldPosition.closerThan(player.blockPosition(), range) && player.isInWaterOrRain()) {
                player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, EFFECT_TICKS, 0, true, true));
            }
        }
    }

    public boolean isFormed() {
        return water && frameSize >= MIN_FRAME;
    }

    public boolean isRunning() {
        return running;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public int getBuffer() {
        return buffer;
    }

    public int effectRange() {
        return frameSize >= MIN_FRAME ? frameSize / 7 * 16 : 0;
    }

    public String statusKey() {
        if (running) {
            return "nautec.conduit_beacon.running";
        }
        if (!water) {
            return "nautec.conduit_beacon.no_water";
        }
        if (frameSize < MIN_FRAME) {
            return "nautec.conduit_beacon.no_frame";
        }
        if (powered) {
            return "nautec.conduit_beacon.redstone";
        }
        return "nautec.conduit_beacon.no_power";
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel serverLevel) {
            ConduitBeaconTracker.remove(serverLevel, worldPosition);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level instanceof ServerLevel serverLevel) {
            ConduitBeaconTracker.remove(serverLevel, worldPosition);
        }
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putInt("buffer", buffer);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.buffer = in.getIntOr("buffer", 0);
    }
}
