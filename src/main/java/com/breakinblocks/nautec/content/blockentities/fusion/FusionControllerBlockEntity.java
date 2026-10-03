package com.breakinblocks.nautec.content.blockentities.fusion;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.fluid.FluidTank;
import com.breakinblocks.nautec.capabilities.fluid.SidedFluidHandler;
import com.breakinblocks.nautec.content.blocks.fusion.FusionControllerBlock;
import com.breakinblocks.nautec.content.menus.FusionControllerMenu;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTSounds;
import com.breakinblocks.nautec.utils.MachineSounds;
import com.breakinblocks.nautec.registries.NTCriteriaTriggers;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FusionControllerBlockEntity extends ContainerBlockEntity implements MenuProvider {
    public static final int FUEL_CAPACITY = 64_000;
    public static final int HEAT_STEPS = 1000;
    private static final int SCAN_INTERVAL = 40;
    private static final int COOL_TICKS = 400;
    private static final int VISUAL_STEPS = 24;
    private static final int HUM_PERIOD = 60;

    public static final int DATA_STATUS = 0;
    public static final int DATA_PROBLEM = 1;
    public static final int DATA_HEAT = 2;
    public static final int DATA_OUTPUT = 3;
    public static final int DATA_INJECTED = 5;
    public static final int DATA_PURITY = 7;
    public static final int DATA_CEILING = 8;
    public static final int DATA_FUEL = 10;
    public static final int DATA_ENERGY = 12;
    public static final int DATA_RADIUS = 14;
    public static final int DATA_INJECTORS = 15;
    public static final int DATA_COILS = 16;
    public static final int DATA_COUNT = 17;

    private final FluidTank fuel = new FluidTank(FUEL_CAPACITY) {
        @Override
        public boolean isValid(int index, @NotNull FluidResource resource) {
            return isFuel(resource.toStack(1));
        }

        @Override
        protected void onContentsChanged(int index, FluidStack stack) {
            super.onContentsChanged(index, stack);
            setChanged();
        }
    };
    private final ResourceHandler<FluidResource> fuelInput = new SidedFluidHandler(fuel, Pair.of(IOActions.INSERT, new int[]{0}));
    private final SimpleEnergyHandler energy = new SimpleEnergyHandler(NTConfig.fusionEnergyBuffer, 0, Integer.MAX_VALUE) {
        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    };

    private FusionStructure structure = FusionStructure.UNSCANNED;
    private final List<BlockCapabilityCache<EnergyHandler, @Nullable Direction>> outputs = new ArrayList<>();
    private boolean outputsDirty = true;

    private long heat;
    private double fuelCredit;
    private int output;
    private int injected;
    private float purity;
    private Status status = Status.INCOMPLETE;

    private boolean visualFormed;
    private @Nullable BlockPos visualCore;
    private int visualRadius;
    private int visualHeat;
    private int visualPower;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_STATUS -> status.ordinal();
                case DATA_PROBLEM -> structure.problem().ordinal();
                case DATA_HEAT -> heatPermille();
                case DATA_OUTPUT -> low(output);
                case DATA_OUTPUT + 1 -> high(output);
                case DATA_INJECTED -> low(injected);
                case DATA_INJECTED + 1 -> high(injected);
                case DATA_PURITY -> Math.round(purity * 100);
                case DATA_CEILING -> low(structure.ceiling());
                case DATA_CEILING + 1 -> high(structure.ceiling());
                case DATA_FUEL -> low(fuel.getFluidAmount());
                case DATA_FUEL + 1 -> high(fuel.getFluidAmount());
                case DATA_ENERGY -> low(energy.getAmountAsInt());
                case DATA_ENERGY + 1 -> high(energy.getAmountAsInt());
                case DATA_RADIUS -> structure.radius();
                case DATA_INJECTORS -> structure.injectors().size();
                case DATA_COILS -> structure.coils();
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

    public FusionControllerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.FUSION_CONTROLLER.get(), blockPos, blockState);
    }

    public static boolean isFuel(FluidStack stack) {
        return stack.is(NTFluids.SALT_WATER.getFluidType().get());
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

    public FusionStructure getStructure() {
        return structure;
    }

    public Status getStatus() {
        return status;
    }

    public int getOutput() {
        return output;
    }

    public int getInjected() {
        return injected;
    }

    public float getInjectedPurity() {
        return purity;
    }

    public long getHeat() {
        return heat;
    }

    public int heatPermille() {
        long ignition = Math.max(1, NTConfig.fusionIgnitionEnergy);
        return (int) Math.min(HEAT_STEPS, heat * HEAT_STEPS / ignition);
    }

    public FluidTank getFuelTank() {
        return fuel;
    }

    public SimpleEnergyHandler getEnergyStorage() {
        return energy;
    }

    public @Nullable EnergyHandler getEnergyOutput() {
        return energy;
    }

    public @Nullable ResourceHandler<FluidResource> getFuelInput() {
        return fuelInput;
    }

    public boolean hasPort(BlockPos pos) {
        return structure.formed() && structure.ports().contains(pos);
    }

    public boolean isVisualFormed() {
        return visualFormed;
    }

    public @Nullable BlockPos getVisualCore() {
        return visualCore;
    }

    public int getVisualRadius() {
        return visualRadius;
    }

    public float getVisualHeat() {
        return visualHeat / (float) VISUAL_STEPS;
    }

    public float getVisualPower() {
        return visualPower / (float) VISUAL_STEPS;
    }

    public Direction getFront() {
        return getBlockState().getValue(FusionControllerBlock.FACING);
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (structure == FusionStructure.UNSCANNED || (serverLevel.getGameTime() + worldPosition.hashCode()) % SCAN_INTERVAL == 0) {
            rescan(serverLevel);
        }
        run();
        pushEnergy(serverLevel);
        syncVisuals();
        if (status == Status.RUNNING) {
            MachineSounds.interval(level, worldPosition, NTSounds.OPTICS_HUM, HUM_PERIOD, 0.5f, 0.6f);
        }
    }

    public void rescan(ServerLevel serverLevel) {
        FusionStructure previous = this.structure;
        FusionStructure scanned = FusionStructure.scan(serverLevel, worldPosition, getFront());
        this.structure = scanned;
        if (!scanned.equals(previous)) {
            outputsDirty = true;
            for (BlockPos port : previous.ports()) {
                if (!scanned.ports().contains(port) && serverLevel.getBlockEntity(port) instanceof FusionPortBlockEntity portBE) {
                    portBE.link(null);
                }
            }
            for (BlockPos port : scanned.ports()) {
                if (serverLevel.getBlockEntity(port) instanceof FusionPortBlockEntity portBE) {
                    portBE.link(worldPosition);
                }
            }
            if (previous.formed() != scanned.formed()) {
                serverLevel.invalidateCapabilities(worldPosition);
            }
        }
    }

    private void run() {
        long ignition = Math.max(1, NTConfig.fusionIgnitionEnergy);
        this.output = 0;
        this.injected = 0;
        this.purity = 0;

        if (!structure.formed()) {
            cool(ignition);
            status = Status.INCOMPLETE;
            return;
        }

        int beamPower = 0;
        int strongPower = 0;
        double weightedPurity = 0;
        for (BlockPos pos : structure.injectors()) {
            if (level.getBlockEntity(pos) instanceof LaserInjectorBlockEntity injector) {
                int power = injector.getPower();
                beamPower += power;
                if (power > 0 && injector.getPurity() >= NTConfig.fusionMinPurity) {
                    strongPower += power;
                    weightedPurity += injector.getPurity() * (double) power;
                }
            }
        }
        int plasma = Math.min(collectorPower(structure.topCollector()), collectorPower(structure.bottomCollector()));
        this.injected = Math.min(strongPower, plasma);
        this.purity = strongPower > 0 ? (float) (weightedPurity / strongPower) : 0;

        if (fuel.isEmpty() && fuelCredit <= 0) {
            cool(ignition);
            status = Status.NO_FUEL;
            return;
        }
        if (injected <= 0) {
            cool(ignition);
            status = beamPower > 0 && strongPower == 0 ? Status.LOW_PURITY : Status.NO_BEAM;
            return;
        }
        if (heat < ignition) {
            heat = Math.min(ignition, heat + injected);
            status = Status.IGNITING;
            return;
        }

        double quality = Math.min(1.0, purity / 3.0);
        long potential = (long) (injected * (double) NTConfig.fusionFePerAp * quality);
        int space = energy.getCapacityAsInt() - energy.getAmountAsInt();
        int target = (int) Math.min(Math.min(potential, structure.ceiling()), space);
        if (target <= 0) {
            status = Status.BUFFER_FULL;
            return;
        }

        double needed = target / (double) Math.max(1, NTConfig.fusionFePerMb);
        if (fuelCredit < needed) {
            int take = (int) Math.ceil(needed - fuelCredit);
            fuelCredit += fuel.drain(take).getAmount();
        }
        double burned = Math.min(needed, fuelCredit);
        fuelCredit -= burned;
        int made = (int) Math.min(target, Math.floor(burned * NTConfig.fusionFePerMb));
        if (made <= 0) {
            cool(ignition);
            status = Status.NO_FUEL;
            return;
        }
        energy.set(energy.getAmountAsInt() + made);
        this.output = made;
        if (level instanceof ServerLevel serverLevel && serverLevel.getGameTime() % 20 == 0) {
            NTCriteriaTriggers.triggerNear(NTCriteriaTriggers.FUSION_RUNNING.get(), serverLevel, worldPosition, 24.0);
        }
        this.status = potential > structure.ceiling() ? Status.CONTAINMENT_LIMITED : Status.RUNNING;
    }

    private int collectorPower(@Nullable BlockPos pos) {
        if (pos != null && level.getBlockEntity(pos) instanceof FusionCollectorBlockEntity collector) {
            return collector.getPower();
        }
        return 0;
    }

    private void cool(long ignition) {
        if (heat > 0) {
            heat = Math.max(0, heat - Math.max(1, ignition / COOL_TICKS));
        }
    }

    private void pushEnergy(ServerLevel serverLevel) {
        if (outputsDirty) {
            outputsDirty = false;
            outputs.clear();
            if (structure.formed()) {
                List<BlockPos> sources = new ArrayList<>(structure.ports());
                sources.add(worldPosition);
                for (BlockPos source : sources) {
                    for (Direction direction : Direction.values()) {
                        BlockPos neighbor = source.relative(direction);
                        if (!structure.contains(neighbor)) {
                            outputs.add(BlockCapabilityCache.create(Capabilities.Energy.BLOCK, serverLevel, neighbor, direction.getOpposite()));
                        }
                    }
                }
            }
        }
        if (energy.getAmountAsInt() <= 0) {
            return;
        }
        for (BlockCapabilityCache<EnergyHandler, @Nullable Direction> cache : outputs) {
            EnergyHandler target = cache.getCapability();
            if (target != null) {
                EnergyHandlerUtil.move(energy, target, energy.getAmountAsInt(), null);
                if (energy.getAmountAsInt() <= 0) {
                    return;
                }
            }
        }
    }

    private void syncVisuals() {
        boolean formed = structure.formed();
        BlockPos core = structure.core();
        int radius = structure.radius();
        int heatStep = (int) Math.round(heatPermille() / (double) HEAT_STEPS * VISUAL_STEPS);
        int powerStep = output <= 0 ? 0 : Math.max(1, (int) Math.round(output / (double) Math.max(1, NTConfig.fusionMaxOutput) * VISUAL_STEPS));
        boolean active = status == Status.RUNNING || status == Status.CONTAINMENT_LIMITED;

        if (getBlockState().getValue(FusionControllerBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, getBlockState().setValue(FusionControllerBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
        }
        boolean changed = formed != visualFormed || radius != visualRadius || heatStep != visualHeat || powerStep != visualPower
                || (core == null ? visualCore != null : !core.equals(visualCore));
        if (changed) {
            this.visualFormed = formed;
            this.visualCore = core;
            this.visualRadius = radius;
            this.visualHeat = heatStep;
            this.visualPower = powerStep;
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putLong("heat", heat);
        out.putDouble("fuel_credit", fuelCredit);
        fuel.serialize(out.child("fuel"));
        energy.serialize(out.child("energy"));
        out.putBoolean("visual_formed", visualFormed);
        if (visualCore != null) {
            out.store("visual_core", BlockPos.CODEC, visualCore);
        }
        out.putInt("visual_radius", visualRadius);
        out.putInt("visual_heat", visualHeat);
        out.putInt("visual_power", visualPower);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.heat = in.getLongOr("heat", 0L);
        this.fuelCredit = in.getDoubleOr("fuel_credit", 0.0);
        fuel.deserialize(in.childOrEmpty("fuel"));
        energy.deserialize(in.childOrEmpty("energy"));
        this.visualFormed = in.getBooleanOr("visual_formed", false);
        this.visualCore = in.read("visual_core", BlockPos.CODEC).orElse(null);
        this.visualRadius = in.getIntOr("visual_radius", 0);
        this.visualHeat = in.getIntOr("visual_heat", 0);
        this.visualPower = in.getIntOr("visual_power", 0);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FusionControllerMenu(containerId, inventory, this);
    }

    public enum Status {
        INCOMPLETE,
        NO_FUEL,
        NO_BEAM,
        LOW_PURITY,
        IGNITING,
        RUNNING,
        CONTAINMENT_LIMITED,
        BUFFER_FULL;

        public String translationKey() {
            return "nautec.fusion.status." + name().toLowerCase(Locale.ROOT);
        }

        public boolean running() {
            return this == RUNNING || this == CONTAINMENT_LIMITED;
        }

        public static Status byId(int id) {
            Status[] values = values();
            return id >= 0 && id < values.length ? values[id] : INCOMPLETE;
        }
    }
}
