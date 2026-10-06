package com.breakinblocks.nautec.content.conduits;

import java.util.Arrays;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class TapFace {
    public static final int MIN_PRIORITY = -99;
    public static final int MAX_PRIORITY = 99;
    public static final StreamCodec<RegistryFriendlyByteBuf, TapFace> STREAM_CODEC = StreamCodec.ofMember(TapFace::write, TapFace::read);

    private final FlowMode[] modes = new FlowMode[ConduitChannel.ALL.length];
    private final RedstoneMode[] redstone = new RedstoneMode[TapSide.ALL.length];
    private final TapFilter[] filters = new TapFilter[TapSide.ALL.length];
    private int priority;
    private DistributionMode distribution = DistributionMode.ROUND_ROBIN;
    private boolean disabled;

    public TapFace() {
        Arrays.fill(modes, FlowMode.INSERT);
        Arrays.fill(redstone, RedstoneMode.IGNORE);
        for (int i = 0; i < filters.length; i++) {
            filters[i] = new TapFilter();
        }
    }

    public static <E extends Enum<E>> E byId(E[] values, int id, E fallback) {
        return id >= 0 && id < values.length ? values[id] : fallback;
    }

    public static boolean validId(Enum<?>[] values, int id) {
        return id >= 0 && id < values.length;
    }

    public FlowMode mode(ConduitChannel channel) {
        return modes[channel.ordinal()];
    }

    public void setMode(ConduitChannel channel, FlowMode mode) {
        modes[channel.ordinal()] = mode;
    }

    public boolean anyMode(TapSide side) {
        for (FlowMode mode : modes) {
            if (side == TapSide.INPUT ? mode.extracts() : mode.inserts()) {
                return true;
            }
        }
        return false;
    }

    public RedstoneMode redstone(TapSide side) {
        return redstone[side.ordinal()];
    }

    public void setRedstone(TapSide side, RedstoneMode mode) {
        redstone[side.ordinal()] = mode;
    }

    public TapFilter filter(TapSide side) {
        return filters[side.ordinal()];
    }

    public int priority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = Math.max(MIN_PRIORITY, Math.min(MAX_PRIORITY, priority));
    }

    public DistributionMode distribution() {
        return distribution;
    }

    public void setDistribution(DistributionMode distribution) {
        this.distribution = distribution;
    }

    public boolean disabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void copyFrom(TapFace other) {
        System.arraycopy(other.modes, 0, modes, 0, modes.length);
        System.arraycopy(other.redstone, 0, redstone, 0, redstone.length);
        for (int i = 0; i < filters.length; i++) {
            filters[i].copyFrom(other.filters[i]);
        }
        priority = other.priority;
        distribution = other.distribution;
        disabled = other.disabled;
    }

    public TapFace copy() {
        TapFace copy = new TapFace();
        copy.copyFrom(this);
        return copy;
    }

    public void sanitize(HolderLookup.Provider registries) {
        for (TapFilter filter : filters) {
            filter.sanitize(registries);
        }
    }

    public void save(ValueOutput out) {
        int[] modeIds = new int[modes.length];
        for (int i = 0; i < modes.length; i++) {
            modeIds[i] = modes[i].ordinal();
        }
        out.putIntArray("modes", modeIds);
        out.putInt("priority", priority);
        out.putInt("distribution", distribution.ordinal());
        out.putBoolean("disabled", disabled);
        for (TapSide side : TapSide.ALL) {
            String key = side == TapSide.INPUT ? "input" : "output";
            out.putInt(key + "_redstone", redstone[side.ordinal()].ordinal());
            TapFilter filter = filters[side.ordinal()];
            if (!filter.isEmpty() || !filter.whitelist()) {
                filter.save(out.child(key + "_filter"));
            }
        }
    }

    public void load(ValueInput in) {
        int[] modeIds = in.getIntArray("modes").orElse(new int[0]);
        for (int i = 0; i < modes.length; i++) {
            modes[i] = i < modeIds.length ? byId(FlowMode.ALL, modeIds[i], FlowMode.INSERT) : FlowMode.INSERT;
        }
        setPriority(in.getIntOr("priority", 0));
        distribution = byId(DistributionMode.ALL, in.getIntOr("distribution", 0), DistributionMode.ROUND_ROBIN);
        disabled = in.getBooleanOr("disabled", false);
        for (TapSide side : TapSide.ALL) {
            String key = side == TapSide.INPUT ? "input" : "output";
            redstone[side.ordinal()] = byId(RedstoneMode.ALL, in.getIntOr(key + "_redstone", 0), RedstoneMode.IGNORE);
            TapFilter filter = filters[side.ordinal()];
            filter.clear();
            in.child(key + "_filter").ifPresent(filter::load);
        }
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        for (FlowMode mode : modes) {
            buffer.writeByte(mode.ordinal());
        }
        for (RedstoneMode mode : redstone) {
            buffer.writeByte(mode.ordinal());
        }
        buffer.writeVarInt(priority);
        buffer.writeByte(distribution.ordinal());
        buffer.writeBoolean(disabled);
        for (TapFilter filter : filters) {
            filter.write(buffer);
        }
    }

    private static TapFace read(RegistryFriendlyByteBuf buffer) {
        TapFace face = new TapFace();
        for (int i = 0; i < face.modes.length; i++) {
            face.modes[i] = byId(FlowMode.ALL, buffer.readByte(), FlowMode.INSERT);
        }
        for (int i = 0; i < face.redstone.length; i++) {
            face.redstone[i] = byId(RedstoneMode.ALL, buffer.readByte(), RedstoneMode.IGNORE);
        }
        face.setPriority(buffer.readVarInt());
        face.distribution = byId(DistributionMode.ALL, buffer.readByte(), DistributionMode.ROUND_ROBIN);
        face.disabled = buffer.readBoolean();
        for (TapFilter filter : face.filters) {
            filter.read(buffer);
        }
        return face;
    }
}
