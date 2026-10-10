package com.breakinblocks.nautec.content.fluids;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.fluids.FluidTemplate;
import net.minecraft.resources.ResourceLocation;

public enum FluidTemplates implements FluidTemplate {
    MOLTEN_METAL(Nautec.rl("fluid/molten_fluid_still"),
            Nautec.rl("fluid/molten_fluid_flow"),
            Nautec.rl("fluid/molten_fluid_overlay")),
    OIL(Nautec.rl("fluid/oil_fluid_still"),
            Nautec.rl("fluid/oil_fluid_flow"),
            Nautec.rl("fluid/oil_overlay")),
    EAS(modFluidTexture("eas_fluid"),
            modFluidTexture("eas_fluid"),
            ResourceLocation.withDefaultNamespace("block/water_overlay")),
    ETCHING_ACID(modFluidTexture("etching_acid"),
            modFluidTexture("etching_acid"),
            ResourceLocation.withDefaultNamespace("block/water_overlay")),
    EXPERIENCE_ALGAE(modFluidTexture("experience_algae"),
            modFluidTexture("experience_algae"),
            ResourceLocation.withDefaultNamespace("block/water_overlay")),
    WATER(ResourceLocation.parse("block/water_still"),
            ResourceLocation.parse("block/water_flow"),
            ResourceLocation.withDefaultNamespace("block/water_overlay"));

    private final ResourceLocation still;
    private final ResourceLocation flowing;
    private final ResourceLocation overlay;

    FluidTemplates(ResourceLocation still, ResourceLocation flowing, ResourceLocation overlay) {
        this.still = still;
        this.flowing = flowing;
        this.overlay = overlay;
    }

    @Override
    public ResourceLocation getStillTexture() {
        return still;
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        return flowing;
    }

    @Override
    public ResourceLocation getOverlayTexture() {
        return overlay;
    }

    private static ResourceLocation modFluidTexture(String name) {
        return Nautec.rl("fluid/" + name);
    }
}
