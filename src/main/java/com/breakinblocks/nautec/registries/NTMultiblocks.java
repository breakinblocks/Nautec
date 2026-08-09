package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.content.multiblocks.AugmentationStationMultiblock;
import com.breakinblocks.nautec.content.multiblocks.BioReactorMultiblock;
import com.breakinblocks.nautec.content.multiblocks.DrainMultiblock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class NTMultiblocks {
    public static final DeferredRegister<Multiblock> MULTIBLOCKS = DeferredRegister.create(NTRegistries.MULTIBLOCK, Nautec.MODID);

    public static final Supplier<DrainMultiblock> DRAIN = MULTIBLOCKS.register("drain",
            DrainMultiblock::new);
    public static final Supplier<AugmentationStationMultiblock> AUGMENTATION_STATION = MULTIBLOCKS.register("augmentation_station",
            AugmentationStationMultiblock::new);
    public static final Supplier<BioReactorMultiblock> BIO_REACTOR = MULTIBLOCKS.register("bio_reactor",
            BioReactorMultiblock::new);
}
