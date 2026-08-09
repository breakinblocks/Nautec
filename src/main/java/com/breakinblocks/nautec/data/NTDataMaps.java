package com.breakinblocks.nautec.data;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.data.maps.BacteriaObtainValue;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public final class NTDataMaps {
    public static final DataMapType<Block, BacteriaObtainValue> BACTERIA_OBTAINING = DataMapType.builder(
            Nautec.rl("bacteria_obtaining"),
            Registries.BLOCK,
            BacteriaObtainValue.CODEC
    ).synced(
            BacteriaObtainValue.CODEC,
            false
    ).build();
}
