package com.breakinblocks.nautec.capabilities.fluid;

import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.SidedResourceHandler;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntList;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public final class SidedFluidHandler extends SidedResourceHandler<FluidResource> {
    public SidedFluidHandler(ResourceHandler<FluidResource> inner, IOActions action, IntList slots) {
        super(inner, action, slots);
    }

    public SidedFluidHandler(ResourceHandler<FluidResource> inner, Pair<IOActions, int[]> actionSlots) {
        super(inner, actionSlots);
    }
}
