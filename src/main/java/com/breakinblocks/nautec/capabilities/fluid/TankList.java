package com.breakinblocks.nautec.capabilities.fluid;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

public final class TankList implements ResourceHandler<FluidResource> {
    private final List<FluidTank> tanks;

    public TankList(List<FluidTank> tanks) {
        this.tanks = tanks;
    }

    @Override
    public int size() {
        return tanks.size();
    }

    @Override
    public FluidResource getResource(int index) {
        return index < tanks.size() ? tanks.get(index).getResource(0) : FluidResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
        return index < tanks.size() ? tanks.get(index).getAmountAsLong(0) : 0;
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return index < tanks.size() ? tanks.get(index).getCapacityAsLong(0, resource) : 0;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return index < tanks.size() && tanks.get(index).isValid(0, resource);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return index < tanks.size() ? tanks.get(index).insert(0, resource, amount, transaction) : 0;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return index < tanks.size() ? tanks.get(index).extract(0, resource, amount, transaction) : 0;
    }
}
