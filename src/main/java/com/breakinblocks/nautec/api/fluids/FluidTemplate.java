package com.breakinblocks.nautec.api.fluids;

import net.minecraft.resources.Identifier;

public interface FluidTemplate {
    Identifier getStillTexture();

    Identifier getFlowingTexture();

    Identifier getOverlayTexture();
}
