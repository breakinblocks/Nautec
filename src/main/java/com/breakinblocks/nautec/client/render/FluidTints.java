package com.breakinblocks.nautec.client.render;

import net.minecraft.client.renderer.block.FluidModel;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;

public final class FluidTints {
    private FluidTints() {
    }

    public static int colour(FluidModel model, FluidStack stack) {
        FluidTintSource tint = model.fluidTintSource();
        return tint == null ? 0xFFFFFFFF : tint.colorAsStack(stack);
    }
}
