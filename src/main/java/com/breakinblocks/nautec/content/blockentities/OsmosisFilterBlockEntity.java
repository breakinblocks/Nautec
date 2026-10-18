package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.fluid.FluidTank;
import com.breakinblocks.nautec.content.blocks.OsmosisFilterBlock;
import com.breakinblocks.nautec.content.menus.OsmosisFilterMenu;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.utils.DataSlots;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class OsmosisFilterBlockEntity extends LaserBlockEntity implements MenuProvider {
    public static final int DATA_RATE = 0;
    public static final int DATA_LIMIT = 2;
    public static final int DATA_POWER = 4;
    public static final int DATA_COUNT = 6;

    private static final Set<Direction> LASER_INPUTS = Set.of(Direction.values());
    private static final SlotRoles FLUID_ROLES = SlotRoles.of(new int[0], new int[]{0});

    private double carry;
    private double rate;
    private int fillLimit;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            int rounded = (int) Math.min(Integer.MAX_VALUE, Math.round(rate));
            return switch (index) {
                case DATA_RATE -> DataSlots.low(rounded);
                case DATA_RATE + 1 -> DataSlots.high(rounded);
                case DATA_LIMIT -> DataSlots.low(fillLimit);
                case DATA_LIMIT + 1 -> DataSlots.high(fillLimit);
                case DATA_POWER -> DataSlots.low(getPower());
                case DATA_POWER + 1 -> DataSlots.high(getPower());
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

    public OsmosisFilterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.OSMOSIS_FILTER.get(), blockPos, blockState);
        addFluidTank(tankCapacity(), fluidStack -> false);
        this.rate = ratePerSecond(0);
        this.fillLimit = fillLimit(rate);
    }

    public static int tankCapacity() {
        return Math.max(1, Math.max(NTConfig.osmosisFilterCapacity, NTConfig.osmosisFilterMaxRate));
    }

    public static double ratePerSecond(int power) {
        double base = NTConfig.osmosisFilterBaseRate;
        if (base <= 0) {
            return 0;
        }
        int step = NTConfig.osmosisFilterApStep;
        double rate = power > 0 && step > 0 ? base * Math.pow(2.0, power / (double) step) : base;
        return Math.min(rate, NTConfig.osmosisFilterMaxRate);
    }

    public static int fillLimit(double rate) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(NTConfig.osmosisFilterCapacity, Math.ceil(rate)));
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return LASER_INPUTS;
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return Set.of();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public SlotRoles fluidRoles() {
        return FLUID_ROLES;
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (level == null || level.isClientSide()) {
            return;
        }
        FluidTank tank = getFluidTank();
        rate = ratePerSecond(getPower());
        fillLimit = Math.min(fillLimit(rate), tank.getCapacity());
        FluidStack stored = tank.getFluid();
        int amount = stored.getAmount();
        boolean canProduce = rate > 0 && amount < fillLimit && (stored.isEmpty() || stored.is(Fluids.WATER));
        if (canProduce) {
            carry += rate / 20.0;
            int whole = (int) carry;
            carry -= whole;
            int add = Math.min(whole, fillLimit - amount);
            if (add > 0) {
                if (stored.isEmpty()) {
                    tank.setFluid(new FluidStack(Fluids.WATER, add));
                } else {
                    stored.grow(add);
                    update();
                }
                amount += add;
            }
        } else {
            carry = 0;
        }
        setActive(rate > 0 && amount < fillLimit);
    }

    private void setActive(boolean active) {
        BlockState state = getBlockState();
        if (state.hasProperty(OsmosisFilterBlock.ACTIVE) && state.getValue(OsmosisFilterBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(OsmosisFilterBlock.ACTIVE, active), Block.UPDATE_ALL);
        }
    }

    public double getRate() {
        return rate;
    }

    public int getFillLimit() {
        return fillLimit;
    }

    public FluidStack getWater() {
        return getFluidTank().getFluid();
    }

    public int getWaterAmount() {
        return getFluidTank().getFluidAmount();
    }

    public ContainerData getData() {
        return data;
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.carry = in.getDoubleOr("carry", 0);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putDouble("carry", this.carry);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new OsmosisFilterMenu(containerId, playerInventory, this);
    }
}
