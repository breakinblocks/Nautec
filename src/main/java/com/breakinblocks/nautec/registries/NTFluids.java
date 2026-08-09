package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.fluids.EASFluid;
import com.breakinblocks.nautec.content.fluids.EtchingAcidFluid;
import com.breakinblocks.nautec.content.fluids.OilFluid;
import com.breakinblocks.nautec.content.fluids.SaltWaterFluid;
import com.breakinblocks.nautec.utils.FluidRegistrationHelper;

public final class NTFluids {
    public static final FluidRegistrationHelper HELPER = new FluidRegistrationHelper(NTBlocks.BLOCKS, NTItems.ITEMS, Nautec.MODID);

    public static final OilFluid OIL = HELPER.registerFluid(new OilFluid("oil"));
    public static final SaltWaterFluid SALT_WATER = HELPER.registerFluid(new SaltWaterFluid("saltwater"));
    public static final EASFluid EAS = HELPER.registerFluid(new EASFluid("eas"));
    public static final EtchingAcidFluid ETCHING_ACID = HELPER.registerFluid(new EtchingAcidFluid("etching_acid"));
}
