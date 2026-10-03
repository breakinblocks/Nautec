package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.menus.ResonancePylonMenu;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ResonancePylonBlockEntity extends ContainerBlockEntity implements MenuProvider, ResonanceEndpoint, ResonanceTunable {
    public static final int DATA_ENERGY = 0;
    public static final int DATA_CAPACITY = 2;
    public static final int DATA_FLOW = 4;
    public static final int DATA_MODE = 6;
    public static final int DATA_TIER = 7;
    public static final int DATA_COUNT = 8;
    private static final int VISUAL_HOLD = 20;

    private final SimpleEnergyHandler energy;
    private final EnergyHandler port;
    private final List<BlockCapabilityCache<EnergyHandler, @Nullable Direction>> neighbours = new ArrayList<>();

    private @Nullable UUID networkId;
    private String networkName = "";
    private boolean sendMode = true;
    private @Nullable UUID joined;

    private int sentThisTick;
    private int receivedThisTick;
    private int flow;
    private int flowAccumulator;
    private int activeTicks;
    private boolean visualActive;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_ENERGY -> low(energy.getAmountAsInt());
                case DATA_ENERGY + 1 -> high(energy.getAmountAsInt());
                case DATA_CAPACITY -> low(energy.getCapacityAsInt());
                case DATA_CAPACITY + 1 -> high(energy.getCapacityAsInt());
                case DATA_FLOW -> low(flow);
                case DATA_FLOW + 1 -> high(flow);
                case DATA_MODE -> sendMode ? 0 : 1;
                case DATA_TIER -> interdimensional() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public ResonancePylonBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.RESONANCE_PYLON.get(), pos, state);
        int capacity = isInterdimensional(state) ? NTConfig.abyssalPylonBuffer : NTConfig.resonancePylonBuffer;
        this.energy = new SimpleEnergyHandler(capacity, Integer.MAX_VALUE, Integer.MAX_VALUE) {
            @Override
            protected void onEnergyChanged(int previousAmount) {
                setChanged();
            }
        };
        this.port = new Port();
    }

    private static boolean isInterdimensional(BlockState state) {
        return state.getBlock() instanceof ResonancePylonBlock block && block.isInterdimensional();
    }

    public static int low(int value) {
        return value & 0xFFFF;
    }

    public static int high(int value) {
        return (value >>> 16) & 0xFFFF;
    }

    public static int join(int low, int high) {
        return (low & 0xFFFF) | ((high & 0xFFFF) << 16);
    }

    public ContainerData getData() {
        return data;
    }

    public EnergyHandler getPort() {
        return port;
    }

    public SimpleEnergyHandler getEnergyStorage() {
        return energy;
    }

    @Override
    public @Nullable UUID getNetworkId() {
        return networkId;
    }

    public String getNetworkName() {
        return networkName;
    }

    public boolean isSendMode() {
        return sendMode;
    }

    public boolean isVisualActive() {
        return visualActive;
    }

    public int getFlow() {
        return flow;
    }

    @Override
    public @Nullable ResonanceNetwork getNetwork() {
        if (networkId == null || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return ResonanceNetworks.get(serverLevel.getServer()).get(networkId);
    }

    @Override
    public int members(ResonanceNetwork network) {
        List<ResonancePylonBlockEntity> members = ResonanceGrid.members(network.id());
        boolean pending = network.id().equals(networkId) && !members.contains(this);
        return members.size() + (pending ? 1 : 0);
    }

    @Override
    public void setNetwork(@Nullable ResonanceNetwork network) {
        UUID id = network == null ? null : network.id();
        if (joined != null && !joined.equals(id)) {
            ResonanceGrid.leave(joined, this);
            joined = null;
        }
        this.networkId = id;
        this.networkName = network == null ? "" : network.name();
        setChanged();
        sync();
    }

    public void setSendMode(boolean send) {
        this.sendMode = send;
        setChanged();
        sync();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        sentThisTick = 0;
        receivedThisTick = 0;

        ResonanceNetwork network = networkId == null ? null : ResonanceNetworks.get(serverLevel.getServer()).get(networkId);
        if (network == null) {
            if (joined != null) {
                ResonanceGrid.leave(joined, this);
                joined = null;
            }
            if (networkId != null && level.getGameTime() % 20 == 0) {
                setNetwork(null);
            }
        } else {
            if (!network.name().equals(networkName)) {
                this.networkName = network.name();
                sync();
            }
            if (joined == null) {
                ResonanceGrid.join(networkId, this);
                joined = networkId;
            }
        }

        if (!sendMode) {
            push(serverLevel);
        }

        if (level.getGameTime() % 20 == 0) {
            flow = flowAccumulator / 20;
            flowAccumulator = 0;
        }
        if (activeTicks > 0) {
            activeTicks--;
        }
        boolean active = activeTicks > 0;
        if (active != visualActive) {
            visualActive = active;
            sync();
        }
    }

    private void push(ServerLevel serverLevel) {
        if (neighbours.isEmpty()) {
            for (Direction direction : Direction.values()) {
                neighbours.add(BlockCapabilityCache.create(Capabilities.Energy.BLOCK, serverLevel, worldPosition.relative(direction), direction.getOpposite()));
            }
        }
        for (BlockCapabilityCache<EnergyHandler, @Nullable Direction> cache : neighbours) {
            if (energy.getAmountAsInt() <= 0) {
                return;
            }
            if (serverLevel.getBlockEntity(cache.pos()) instanceof ResonancePylonBlockEntity) {
                continue;
            }
            EnergyHandler target = cache.getCapability();
            if (target != null) {
                EnergyHandlerUtil.move(energy, target, energy.getAmountAsInt(), null);
            }
        }
    }

    private void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private int throughput() {
        return interdimensional() ? NTConfig.abyssalPylonThroughput : NTConfig.resonancePylonThroughput;
    }

    @Override
    public ResourceKey<Level> dimension() {
        return level.dimension();
    }

    @Override
    public boolean interdimensional() {
        return isInterdimensional(getBlockState());
    }

    @Override
    public boolean sending() {
        return sendMode;
    }

    @Override
    public int sendable() {
        return Math.max(0, Math.min(energy.getAmountAsInt(), throughput() - sentThisTick));
    }

    @Override
    public int receivable() {
        return Math.max(0, Math.min(energy.getCapacityAsInt() - energy.getAmountAsInt(), throughput() - receivedThisTick));
    }

    @Override
    public void send(int amount) {
        energy.set(energy.getAmountAsInt() - amount);
        sentThisTick += amount;
        flowAccumulator += amount;
        activeTicks = VISUAL_HOLD;
    }

    @Override
    public void receive(int amount) {
        energy.set(energy.getAmountAsInt() + amount);
        receivedThisTick += amount;
        flowAccumulator += amount;
        activeTicks = VISUAL_HOLD;
    }

    @Override
    public void setRemoved() {
        if (joined != null) {
            ResonanceGrid.leave(joined, this);
            joined = null;
        }
        super.setRemoved();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        energy.serialize(out.child("energy"));
        if (networkId != null) {
            out.store("network", UUIDUtil.CODEC, networkId);
        }
        out.putString("network_name", networkName);
        out.putBoolean("send", sendMode);
        out.putBoolean("active", visualActive);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        energy.deserialize(in.childOrEmpty("energy"));
        this.networkId = in.read("network", UUIDUtil.CODEC).orElse(null);
        this.networkName = in.getStringOr("network_name", "");
        this.sendMode = in.getBooleanOr("send", true);
        this.visualActive = in.getBooleanOr("active", false);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ResonancePylonMenu(containerId, inventory, this);
    }

    private final class Port implements EnergyHandler {
        @Override
        public long getAmountAsLong() {
            return energy.getAmountAsLong();
        }

        @Override
        public long getCapacityAsLong() {
            return energy.getCapacityAsLong();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            return sendMode ? energy.insert(amount, transaction) : 0;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            return sendMode ? 0 : energy.extract(amount, transaction);
        }
    }
}
