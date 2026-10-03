package com.breakinblocks.nautec.content.resonance;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ResonanceTunable {
    BlockPos getBlockPos();

    @Nullable UUID getNetworkId();

    @Nullable ResonanceNetwork getNetwork();

    void setNetwork(@Nullable ResonanceNetwork network);

    int members(ResonanceNetwork network);
}
