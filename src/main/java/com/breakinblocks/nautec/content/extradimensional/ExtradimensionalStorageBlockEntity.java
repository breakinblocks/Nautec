package com.breakinblocks.nautec.content.extradimensional;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.menus.ExtradimensionalStorageMenu;
import com.breakinblocks.nautec.content.resonance.ChunkLoadable;
import com.breakinblocks.nautec.content.resonance.NetworkBattery;
import com.breakinblocks.nautec.content.resonance.ResonanceActions;
import com.breakinblocks.nautec.content.resonance.ResonanceChunkLoading;
import com.breakinblocks.nautec.content.resonance.ResonanceNetwork;
import com.breakinblocks.nautec.content.resonance.ResonanceNetworks;
import com.breakinblocks.nautec.content.resonance.ResonanceNodeBlockEntity;
import com.breakinblocks.nautec.content.resonance.ResonanceTunable;
import com.breakinblocks.nautec.content.resonance.SatelliteGrid;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExtradimensionalStorageBlockEntity extends ContainerBlockEntity implements MenuProvider, ResonanceTunable, ChunkLoadable, NetworkBattery {
    public static final int MAX_TIER = 5;
    public static final int MAX_RING = MAX_TIER - 1;
    public static final int SIDES = 4;

    public static final int STATUS_ONLINE = ResonanceNodeBlockEntity.STATUS_ONLINE;
    public static final int STATUS_NO_NETWORK = ResonanceNodeBlockEntity.STATUS_NO_NETWORK;
    public static final int STATUS_NO_CORE = ResonanceNodeBlockEntity.STATUS_NO_CORE;

    public static final int DATA_STORED = 0;
    public static final int DATA_CAPACITY = 4;
    public static final int DATA_TIER = 8;
    public static final int DATA_PORTS = 9;
    public static final int DATA_STATUS = 10;
    public static final int DATA_CORES = 11;
    public static final int DATA_IN = 12;
    public static final int DATA_OUT = 14;
    public static final int DATA_CHUNK = 16;
    public static final int DATA_COUNT = 17;

    private static final int SCAN_INTERVAL = 20;
    private static final int VISUAL_STEPS = 32;

    public enum PortMode {
        OFF,
        INPUT,
        OUTPUT;

        public PortMode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private final ResonanceChunkLoading.Ticket ticket = new ResonanceChunkLoading.Ticket();
    private final PortMode[] modes = {PortMode.INPUT, PortMode.INPUT, PortMode.INPUT, PortMode.INPUT};
    private final long[] moved = new long[SIDES];
    private final Port[] ports = new Port[SIDES];
    private final Journal journal = new Journal();
    @SuppressWarnings("unchecked")
    private final BlockCapabilityCache<EnergyHandler, @Nullable Direction>[] targets = new BlockCapabilityCache[SIDES];
    private final int[] targetTiers = new int[SIDES];

    private long stored;
    private int tier = 1;
    private boolean scanRequested = true;
    private long flowIn;
    private long flowOut;
    private long tickIn;
    private long tickOut;
    private int visualFill = -1;

    private @Nullable UUID networkId;
    private String networkName = "";
    private @Nullable UUID joined;
    private boolean chunkLoading;
    private int status = STATUS_NO_NETWORK;
    private int cores;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            if (index >= DATA_STORED && index < DATA_STORED + 4) {
                return part(stored, index - DATA_STORED);
            }
            if (index >= DATA_CAPACITY && index < DATA_CAPACITY + 4) {
                return part(capacity(), index - DATA_CAPACITY);
            }
            return switch (index) {
                case DATA_TIER -> tier;
                case DATA_PORTS -> packedModes();
                case DATA_STATUS -> status;
                case DATA_CORES -> cores;
                case DATA_IN -> part(flowIn, 0);
                case DATA_IN + 1 -> part(flowIn, 1);
                case DATA_OUT -> part(flowOut, 0);
                case DATA_OUT + 1 -> part(flowOut, 1);
                case DATA_CHUNK -> ResonanceChunkLoading.state(chunkLoading);
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

    public ExtradimensionalStorageBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.EXTRADIMENSIONAL_STORAGE.get(), pos, state);
        for (int i = 0; i < SIDES; i++) {
            ports[i] = new Port(i);
        }
        this.tier = state.getValue(ExtradimensionalStorageBlock.TIER);
    }

    private static int part(long value, int index) {
        return (int) ((value >>> (16 * index)) & 0xFFFF);
    }

    public static long join(int... parts) {
        long value = 0;
        for (int i = 0; i < parts.length; i++) {
            value |= (long) (parts[i] & 0xFFFF) << (16 * i);
        }
        return value;
    }

    public ContainerData getData() {
        return data;
    }

    public int tier() {
        return tier;
    }

    public long getStored() {
        return stored;
    }

    public void setStored(long stored) {
        this.stored = Math.max(0, stored);
        setChanged();
    }

    public static long capacity(int tier) {
        return NTConfig.extradimensionalCapacity[Math.clamp(tier, 1, MAX_TIER) - 1];
    }

    public static int rate(int tier) {
        return NTConfig.extradimensionalTransfer[Math.clamp(tier, 1, MAX_TIER) - 1];
    }

    public long capacity() {
        return capacity(tier);
    }

    public int rate() {
        return rate(tier);
    }

    public float fill() {
        long capacity = capacity();
        return capacity <= 0 ? 0F : (float) Math.min(1.0, stored / (double) capacity);
    }

    public PortMode mode(Direction side) {
        return modes[side.get2DDataValue()];
    }

    public void setMode(Direction side, PortMode mode) {
        int index = side.get2DDataValue();
        if (modes[index] != mode) {
            modes[index] = mode;
            update();
            invalidatePorts();
        }
    }

    public void cyclePort(int index) {
        if (index >= 0 && index < SIDES) {
            setMode(Direction.from2DDataValue(index), modes[index].next());
        }
    }

    private int packedModes() {
        int packed = 0;
        for (int i = 0; i < SIDES; i++) {
            packed |= modes[i].ordinal() << (i * 2);
        }
        return packed;
    }

    public static PortMode unpackMode(int packed, Direction side) {
        return PortMode.values()[(packed >> (side.get2DDataValue() * 2)) & 3];
    }

    public BlockPos portPos(Direction side) {
        return worldPosition.relative(side, tier - 1);
    }

    public @Nullable EnergyHandler port(Direction side) {
        return side.getAxis().isHorizontal() ? ports[side.get2DDataValue()] : null;
    }

    private final class Port implements EnergyHandler {
        private final int side;

        private Port(int side) {
            this.side = side;
        }

        @Override
        public long getAmountAsLong() {
            return stored;
        }

        @Override
        public long getCapacityAsLong() {
            return capacity();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            if (modes[side] != PortMode.INPUT || amount <= 0) {
                return 0;
            }
            long room = capacity() - stored;
            long budget = rate() - moved[side];
            int accepted = (int) Math.max(0, Math.min(amount, Math.min(room, budget)));
            if (accepted > 0) {
                journal.updateSnapshots(transaction);
                stored += accepted;
                moved[side] += accepted;
            }
            return accepted;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            if (modes[side] != PortMode.OUTPUT || amount <= 0) {
                return 0;
            }
            long budget = rate() - moved[side];
            int given = (int) Math.max(0, Math.min(amount, Math.min(stored, budget)));
            if (given > 0) {
                journal.updateSnapshots(transaction);
                stored -= given;
                moved[side] += given;
            }
            return given;
        }
    }

    private final class Journal extends SnapshotJournal<long[]> {
        @Override
        protected long[] createSnapshot() {
            long[] snapshot = new long[SIDES + 1];
            snapshot[0] = stored;
            System.arraycopy(moved, 0, snapshot, 1, SIDES);
            return snapshot;
        }

        @Override
        protected void revertToSnapshot(long[] snapshot) {
            stored = snapshot[0];
            System.arraycopy(snapshot, 1, moved, 0, SIDES);
        }

        @Override
        protected void onRootCommit(long[] originalState) {
            setChanged();
        }
    }

    @Override
    public long batteryStored() {
        return status == STATUS_ONLINE ? stored : 0;
    }

    @Override
    public long batteryRoom() {
        return status == STATUS_ONLINE ? Math.max(0, capacity() - stored) : 0;
    }

    @Override
    public int batteryRate() {
        return status == STATUS_ONLINE ? rate() : 0;
    }

    @Override
    public int drawFe(int amount) {
        int taken = (int) Math.max(0, Math.min(amount, stored));
        if (taken > 0) {
            stored -= taken;
            tickOut += taken;
            setChanged();
        }
        return taken;
    }

    @Override
    public int storeFe(int amount) {
        int added = (int) Math.max(0, Math.min(amount, capacity() - stored));
        if (added > 0) {
            stored += added;
            tickIn += added;
            setChanged();
        }
        return added;
    }

    public void requestScan() {
        scanRequested = true;
    }

    public static void requestScans(Level level, BlockPos pos) {
        for (ExtradimensionalStorageBlockEntity core : nearby(level, pos)) {
            core.requestScan();
        }
    }

    private static List<ExtradimensionalStorageBlockEntity> nearby(Level level, BlockPos pos) {
        List<ExtradimensionalStorageBlockEntity> found = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -MAX_RING; dx <= MAX_RING; dx++) {
            for (int dz = -MAX_RING; dz <= MAX_RING; dz++) {
                cursor.set(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
                if (level.isLoaded(cursor) && level.getBlockEntity(cursor) instanceof ExtradimensionalStorageBlockEntity core) {
                    found.add(core);
                }
            }
        }
        return found;
    }

    public static @Nullable ExtradimensionalStorageBlockEntity owner(Level level, BlockPos pos) {
        for (ExtradimensionalStorageBlockEntity core : nearby(level, pos)) {
            if (ring(core.getBlockPos(), pos) < core.tier()) {
                return core;
            }
        }
        return null;
    }

    public static int ring(BlockPos core, BlockPos pos) {
        if (core.getY() != pos.getY()) {
            return Integer.MAX_VALUE;
        }
        return Math.max(Math.abs(pos.getX() - core.getX()), Math.abs(pos.getZ() - core.getZ()));
    }

    private static List<BlockPos> ringPositions(BlockPos core, int ring) {
        List<BlockPos> positions = new ArrayList<>(8 * ring);
        for (int dx = -ring; dx <= ring; dx++) {
            for (int dz = -ring; dz <= ring; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) == ring) {
                    positions.add(core.offset(dx, 0, dz));
                }
            }
        }
        return positions;
    }

    private boolean ringComplete(int ring) {
        for (BlockPos pos : ringPositions(worldPosition, ring)) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof ExtradimensionalFrameBlock frame) || frame.ring() != ring) {
                return false;
            }
            if (state.getValue(ExtradimensionalFrameBlock.PART).formed() && ring >= tier) {
                return false;
            }
        }
        return true;
    }

    public int scanTier() {
        int found = 1;
        for (int ring = 1; ring <= MAX_RING; ring++) {
            if (!ringComplete(ring)) {
                break;
            }
            found = ring + 1;
        }
        return found;
    }

    public static BlockState formedState(BlockState state, int ring, int tier, int dx, int dz) {
        boolean outer = ring == tier - 1;
        boolean corner = Math.abs(dx) == ring && Math.abs(dz) == ring;
        Direction facing;
        if (corner) {
            facing = dx < 0 ? (dz < 0 ? Direction.NORTH : Direction.WEST) : (dz < 0 ? Direction.EAST : Direction.SOUTH);
        } else if (dz == -ring) {
            facing = Direction.NORTH;
        } else if (dz == ring) {
            facing = Direction.SOUTH;
        } else if (dx == ring) {
            facing = Direction.EAST;
        } else {
            facing = Direction.WEST;
        }
        FramePart part;
        if (outer) {
            part = corner ? FramePart.CORNER : dx == 0 || dz == 0 ? FramePart.PORT : FramePart.EDGE;
        } else {
            part = corner ? FramePart.INNER_CORNER : FramePart.INNER_EDGE;
        }
        return state.setValue(ExtradimensionalFrameBlock.PART, part).setValue(ExtradimensionalFrameBlock.FACING, facing);
    }

    private void applyTier(int newTier) {
        int oldTier = tier;
        for (int ring = 1; ring <= MAX_RING; ring++) {
            boolean ours = ring < Math.max(oldTier, newTier);
            if (!ours) {
                break;
            }
            for (BlockPos pos : ringPositions(worldPosition, ring)) {
                BlockState state = level.getBlockState(pos);
                if (!(state.getBlock() instanceof ExtradimensionalFrameBlock frame) || frame.ring() != ring) {
                    continue;
                }
                BlockState wanted = ring < newTier
                        ? formedState(state, ring, newTier, pos.getX() - worldPosition.getX(), pos.getZ() - worldPosition.getZ())
                        : state.setValue(ExtradimensionalFrameBlock.PART, FramePart.LOOSE);
                if (wanted != state) {
                    level.setBlock(pos, wanted, Block.UPDATE_CLIENTS);
                }
            }
        }
        if (oldTier != newTier) {
            invalidatePorts();
            this.tier = newTier;
            BlockState core = getBlockState();
            if (core.getValue(ExtradimensionalStorageBlock.TIER) != newTier) {
                level.setBlock(worldPosition, core.setValue(ExtradimensionalStorageBlock.TIER, newTier), Block.UPDATE_ALL);
            }
            invalidatePorts();
            setChanged();
            update();
        }
    }

    private void invalidatePorts() {
        if (level == null) {
            return;
        }
        level.invalidateCapabilities(worldPosition);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            level.invalidateCapabilities(portPos(side));
        }
        for (int i = 0; i < SIDES; i++) {
            targets[i] = null;
        }
    }

    public void rescan() {
        if (level != null && !level.isClientSide()) {
            applyTier(scanTier());
        }
    }

    public void unform() {
        if (level != null && !level.isClientSide()) {
            applyTier(1);
        }
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        long tick = serverLevel.getGameTime();
        if (scanRequested || tick % SCAN_INTERVAL == 0) {
            scanRequested = false;
            rescan();
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            int index = side.get2DDataValue();
            if (modes[index] == PortMode.OUTPUT && stored > 0) {
                EnergyHandler target = target(serverLevel, side);
                if (target != null) {
                    EnergyHandlerUtil.move(ports[index], target, Integer.MAX_VALUE, null);
                }
            }
        }
        long in = tickIn;
        long out = tickOut;
        for (int i = 0; i < SIDES; i++) {
            if (modes[i] == PortMode.INPUT) {
                in += moved[i];
            } else if (modes[i] == PortMode.OUTPUT) {
                out += moved[i];
            }
            moved[i] = 0;
        }
        flowIn = in;
        flowOut = out;
        tickIn = 0;
        tickOut = 0;
        serverNetworkTick(serverLevel, tick);
        int fill = Math.round(fill() * VISUAL_STEPS);
        if (fill != visualFill && tick % 10 == 0) {
            visualFill = fill;
            requestSync();
        }
    }

    private @Nullable EnergyHandler target(ServerLevel serverLevel, Direction side) {
        int index = side.get2DDataValue();
        if (targets[index] == null || targetTiers[index] != tier) {
            BlockPos neighbour = portPos(side).relative(side);
            targets[index] = BlockCapabilityCache.create(Capabilities.Energy.BLOCK, serverLevel, neighbour, side.getOpposite());
            targetTiers[index] = tier;
        }
        return targets[index].getCapability();
    }

    private void serverNetworkTick(ServerLevel serverLevel, long tick) {
        if (tick % 20 == 0) {
            ticket.update(serverLevel, worldPosition, chunkLoading);
        }
        ResonanceNetwork network = networkId == null ? null : ResonanceNetworks.get(serverLevel.getServer()).get(networkId);
        if (network == null) {
            if (joined != null) {
                SatelliteGrid.leaveBattery(joined, this);
                joined = null;
            }
            if (networkId != null && tick % 20 == 0) {
                setNetwork(null);
            }
        } else {
            if (!network.name().equals(networkName)) {
                this.networkName = network.name();
                update();
            }
            if (joined == null) {
                SatelliteGrid.joinBattery(networkId, this);
                joined = networkId;
            }
        }
        SatelliteGrid.Link link = network == null ? SatelliteGrid.EMPTY : SatelliteGrid.link(network.id(), tick);
        this.status = network == null ? STATUS_NO_NETWORK : link.uplinks() == 0 ? STATUS_NO_CORE : STATUS_ONLINE;
        this.cores = link.uplinks();
    }

    @Override
    public @Nullable UUID getNetworkId() {
        return networkId;
    }

    public String getNetworkName() {
        return networkName;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public @Nullable ResonanceNetwork getNetwork() {
        if (networkId == null || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return ResonanceNetworks.get(serverLevel.getServer()).get(networkId);
    }

    @Override
    public void setNetwork(@Nullable ResonanceNetwork network) {
        UUID id = network == null ? null : network.id();
        if (joined != null && !joined.equals(id)) {
            SatelliteGrid.leaveBattery(joined, this);
            joined = null;
        }
        this.networkId = id;
        this.networkName = network == null ? "" : network.name();
        update();
    }

    @Override
    public int members(ResonanceNetwork network) {
        List<NetworkBattery> batteries = SatelliteGrid.batteries(network.id());
        boolean pending = network.id().equals(networkId) && !batteries.contains(this);
        return SatelliteGrid.members(network.id()).size() + SatelliteGrid.nodes(network.id()).size() + batteries.size() + (pending ? 1 : 0);
    }

    @Override
    public boolean isChunkLoading() {
        return chunkLoading;
    }

    @Override
    public void setChunkLoading(boolean chunkLoading) {
        this.chunkLoading = chunkLoading;
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            ticket.update(serverLevel, worldPosition, chunkLoading);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel serverLevel) {
            ticket.release(serverLevel, pos);
            unform();
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public void setRemoved() {
        if (joined != null) {
            SatelliteGrid.leaveBattery(joined, this);
            joined = null;
        }
        super.setRemoved();
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        Long energy = components.get(NTDataComponents.STORED_FE.get());
        if (energy != null) {
            this.stored = Math.max(0, energy);
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (stored > 0) {
            components.set(NTDataComponents.STORED_FE.get(), stored);
        }
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public void saveSettings(ValueOutput out) {
        ResonanceActions.copyNetwork(networkId, out);
        out.putInt("ports", packedModes());
        out.putBoolean("chunk_loading", chunkLoading);
    }

    @Override
    public boolean loadSettings(ValueInput in, ServerPlayer player) {
        boolean applied = ResonanceActions.pasteNetwork(player, this, in);
        if (in.getInt("ports").isPresent()) {
            int packed = in.getIntOr("ports", packedModes());
            for (Direction side : Direction.Plane.HORIZONTAL) {
                setMode(side, unpackMode(packed, side));
            }
            applied = true;
        }
        if (in.read("chunk_loading", Codec.BOOL).isPresent()) {
            setChunkLoading(in.getBooleanOr("chunk_loading", chunkLoading));
            applied = true;
        }
        return applied;
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putLong("stored", stored);
        out.putInt("tier", tier);
        out.putInt("ports", packedModes());
        if (networkId != null) {
            out.store("network", UUIDUtil.CODEC, networkId);
        }
        out.putString("network_name", networkName);
        out.putBoolean("chunk_loading", chunkLoading);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.stored = Math.max(0, in.getLongOr("stored", 0L));
        this.tier = Math.clamp(in.getIntOr("tier", tier), 1, MAX_TIER);
        int packed = in.getIntOr("ports", packedModes());
        for (Direction side : Direction.Plane.HORIZONTAL) {
            modes[side.get2DDataValue()] = unpackMode(packed, side);
        }
        this.networkId = in.read("network", UUIDUtil.CODEC).orElse(null);
        this.networkName = in.getStringOr("network_name", "");
        this.chunkLoading = in.getBooleanOr("chunk_loading", false);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ExtradimensionalStorageMenu(containerId, inventory, this);
    }

    public static @Nullable EnergyHandler framePort(Level level, BlockPos pos, BlockState state, @Nullable Direction side) {
        if (side == null || !(state.getBlock() instanceof ExtradimensionalFrameBlock frame)
                || state.getValue(ExtradimensionalFrameBlock.PART) != FramePart.PORT
                || state.getValue(ExtradimensionalFrameBlock.FACING) != side) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos.relative(side.getOpposite(), frame.ring()));
        if (blockEntity instanceof ExtradimensionalStorageBlockEntity core && core.tier() == frame.tier()) {
            return core.port(side);
        }
        return null;
    }

    public @Nullable EnergyHandler corePort(@Nullable Direction side) {
        return tier == 1 && side != null ? port(side) : null;
    }
}
