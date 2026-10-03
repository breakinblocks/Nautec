package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.api.blockentities.BeamScan;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.menus.SatelliteArrayMenu;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTCriteriaTriggers;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SatelliteArrayBlockEntity extends LaserBlockEntity implements MenuProvider, ResonanceTunable {
    public static final int STATUS_ONLINE = 0;
    public static final int STATUS_NO_NETWORK = 1;
    public static final int STATUS_NO_SATELLITE = 2;
    public static final int STATUS_SKY_BLOCKED = 3;
    public static final int STATUS_NO_UPLINK = 4;
    public static final int STATUS_NO_DOWNLINK = 5;

    public static final int DATA_POWER = 0;
    public static final int DATA_PURITY = 2;
    public static final int DATA_STATUS = 3;
    public static final int DATA_UPLINKS = 4;
    public static final int DATA_DOWNLINKS = 5;
    public static final int DATA_KIND = 6;
    public static final int DATA_COUNT = 7;

    public static final int LAUNCH_TICKS = 80;
    private static final int SKY_INTERVAL = 20;
    private static final int SYNC_INTERVAL = 10;
    private static final Set<Direction> PORTS = ObjectSet.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN);

    private @Nullable UUID networkId;
    private String networkName = "";
    private @Nullable UUID joined;
    private boolean satellite;
    private long launchedAt = Long.MIN_VALUE;
    private boolean sky = true;
    private int relay;
    private float relayPurity;
    private int status = STATUS_NO_NETWORK;
    private int uplinks;
    private int downlinks;
    private long lastSync;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_POWER -> ResonancePylonBlockEntity.low(relay);
                case DATA_POWER + 1 -> ResonancePylonBlockEntity.high(relay);
                case DATA_PURITY -> Math.round(relayPurity * 1000F);
                case DATA_STATUS -> status;
                case DATA_UPLINKS -> uplinks;
                case DATA_DOWNLINKS -> downlinks;
                case DATA_KIND -> isUplink() ? 1 : 0;
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

    public SatelliteArrayBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.SATELLITE_ARRAY.get(), pos, state);
    }

    public boolean isUplink() {
        return getBlockState().getBlock() instanceof SatelliteArrayBlock block && block.isUplink();
    }

    public ContainerData getData() {
        return data;
    }

    public ResourceKey<Level> dimension() {
        return level.dimension();
    }

    public boolean hasSatellite() {
        return satellite;
    }

    public long getLaunchedAt() {
        return launchedAt;
    }

    public boolean hasSky() {
        return sky;
    }

    public int getStatus() {
        return status;
    }

    public int getRelay() {
        return relay;
    }

    public float getRelayPurity() {
        return relayPurity;
    }

    public boolean transmitting() {
        return isUplink() && satellite && sky && networkId != null;
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return isUplink() ? PORTS : Set.of();
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return isUplink() ? Set.of() : PORTS;
    }

    @Override
    public BeamScan scanBeam(Direction direction) {
        BeamScan scan = super.scanBeam(direction);
        if (scan.connected() && level.getBlockEntity(scan.targetPos(worldPosition)) instanceof SatelliteArrayBlockEntity) {
            return new BeamScan(direction, BeamScan.Status.BLOCKED, scan.distance());
        }
        return scan;
    }

    @Override
    protected int outgoingPower(Direction direction) {
        int connected = connectedOutputs();
        return connected == 0 ? 0 : getPowerToTransfer() / connected;
    }

    @Override
    public @Nullable UUID getNetworkId() {
        return networkId;
    }

    public String getNetworkName() {
        return networkName;
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
            SatelliteGrid.leave(joined, this);
            joined = null;
        }
        this.networkId = id;
        this.networkName = network == null ? "" : network.name();
        setChanged();
        sync();
    }

    @Override
    public int members(ResonanceNetwork network) {
        List<SatelliteArrayBlockEntity> members = SatelliteGrid.members(network.id());
        boolean pending = network.id().equals(networkId) && !members.contains(this);
        return members.size() + (pending ? 1 : 0);
    }

    public boolean launch() {
        if (!isUplink() || satellite || level == null) {
            return false;
        }
        this.satellite = true;
        this.launchedAt = level.getGameTime();
        setChanged();
        sync();
        return true;
    }

    @Override
    public void commonTick() {
        if (level instanceof ServerLevel serverLevel) {
            serverTick(serverLevel);
        }
        boolean uplink = isUplink();
        if (!uplink) {
            transmitPower(relay);
            setPurity(relayPurity);
        }
        super.commonTick();
        if (!uplink) {
            this.power = relay;
        }
    }

    private void serverTick(ServerLevel serverLevel) {
        long tick = serverLevel.getGameTime();
        if (tick % SKY_INTERVAL == 0) {
            sky = SatelliteGrid.clearSky(serverLevel, worldPosition.above());
        }

        ResonanceNetwork network = networkId == null ? null : ResonanceNetworks.get(serverLevel.getServer()).get(networkId);
        if (network == null) {
            if (joined != null) {
                SatelliteGrid.leave(joined, this);
                joined = null;
            }
            if (networkId != null && tick % 20 == 0) {
                setNetwork(null);
            }
        } else {
            if (!network.name().equals(networkName)) {
                this.networkName = network.name();
                sync();
            }
            if (joined == null) {
                SatelliteGrid.join(networkId, this);
                joined = networkId;
            }
        }

        SatelliteGrid.Link link = network == null ? SatelliteGrid.EMPTY : SatelliteGrid.link(network.id(), serverLevel.dimension(), tick);
        int newStatus;
        int newRelay;
        float newPurity;
        if (isUplink()) {
            newStatus = network == null ? STATUS_NO_NETWORK : !satellite ? STATUS_NO_SATELLITE : !sky ? STATUS_SKY_BLOCKED
                    : link.downlinks() == 0 ? STATUS_NO_DOWNLINK : STATUS_ONLINE;
            newRelay = newStatus == STATUS_ONLINE ? getPower() : 0;
            newPurity = newStatus == STATUS_ONLINE ? getPurity() : 0F;
        } else {
            newStatus = network == null ? STATUS_NO_NETWORK : !sky ? STATUS_SKY_BLOCKED : link.uplinks() == 0 ? STATUS_NO_UPLINK : STATUS_ONLINE;
            newRelay = newStatus == STATUS_ONLINE ? link.share() : 0;
            newPurity = newStatus == STATUS_ONLINE ? link.purity() : 0F;
        }

        boolean changed = newStatus != status || newRelay != relay || Math.abs(newPurity - relayPurity) > 0.001F
                || link.uplinks() != uplinks || link.downlinks() != downlinks;
        boolean flipped = (newRelay > 0) != (relay > 0) || newStatus != status;
        if (!isUplink() && newRelay > 0 && relay <= 0) {
            NTCriteriaTriggers.triggerNear(NTCriteriaTriggers.SATELLITE_RELAY.get(), serverLevel, worldPosition, 32.0);
        }
        this.status = newStatus;
        this.relay = newRelay;
        this.relayPurity = newPurity;
        this.uplinks = link.uplinks();
        this.downlinks = link.downlinks();
        if (changed && (flipped || tick - lastSync >= SYNC_INTERVAL)) {
            lastSync = tick;
            setChanged();
            sync();
        }
    }

    private void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (satellite && level != null) {
            satellite = false;
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, new ItemStack(NTItems.PRISM_SATELLITE.get()));
        }
    }

    @Override
    public void setRemoved() {
        if (joined != null) {
            SatelliteGrid.leave(joined, this);
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
        if (networkId != null) {
            out.store("network", UUIDUtil.CODEC, networkId);
        }
        out.putString("network_name", networkName);
        out.putBoolean("satellite", satellite);
        out.putLong("launched_at", launchedAt);
        out.putBoolean("sky", sky);
        out.putInt("relay", relay);
        out.putFloat("relay_purity", relayPurity);
        out.putInt("status", status);
        out.putInt("uplinks", uplinks);
        out.putInt("downlinks", downlinks);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.networkId = in.read("network", UUIDUtil.CODEC).orElse(null);
        this.networkName = in.getStringOr("network_name", "");
        this.satellite = in.getBooleanOr("satellite", false);
        this.launchedAt = in.getLongOr("launched_at", Long.MIN_VALUE);
        this.sky = in.getBooleanOr("sky", true);
        this.relay = in.getIntOr("relay", 0);
        this.relayPurity = in.getFloatOr("relay_purity", 0F);
        this.status = in.getIntOr("status", STATUS_NO_NETWORK);
        this.uplinks = in.getIntOr("uplinks", 0);
        this.downlinks = in.getIntOr("downlinks", 0);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SatelliteArrayMenu(containerId, inventory, this);
    }
}
