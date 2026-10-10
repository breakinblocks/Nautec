package com.breakinblocks.nautec.transfer.adapter;

import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.fluid.FluidResource;
import com.breakinblocks.nautec.transfer.transaction.Transaction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public record ExposedFluidHandler(ResourceHandler<FluidResource> handler) implements IFluidHandler {
    @Override
    public int getTanks() {
        return handler.size();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return handler.getResource(tank).toStack(handler.getAmountAsInt(tank));
    }

    @Override
    public int getTankCapacity(int tank) {
        return handler.getCapacityAsInt(tank, handler.getResource(tank));
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return !stack.isEmpty() && handler.isValid(tank, FluidResource.of(stack));
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        try (Transaction tx = Transaction.openNested()) {
            int filled = handler.insert(FluidResource.of(resource), resource.getAmount(), tx);
            if (action.execute()) {
                tx.commit();
            }
            return filled;
        }
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidResource fluid = FluidResource.of(resource);
        try (Transaction tx = Transaction.openNested()) {
            int drained = handler.extract(fluid, resource.getAmount(), tx);
            if (action.execute()) {
                tx.commit();
            }
            return fluid.toStack(drained);
        }
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        for (int index = 0; index < handler.size(); index++) {
            FluidResource fluid = handler.getResource(index);
            if (fluid.isEmpty() || handler.getAmountAsLong(index) <= 0) {
                continue;
            }
            try (Transaction tx = Transaction.openNested()) {
                int drained = handler.extract(fluid, maxDrain, tx);
                if (drained > 0) {
                    if (action.execute()) {
                        tx.commit();
                    }
                    return fluid.toStack(drained);
                }
            }
        }
        return FluidStack.EMPTY;
    }
}
