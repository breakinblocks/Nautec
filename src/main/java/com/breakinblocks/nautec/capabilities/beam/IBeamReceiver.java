package com.breakinblocks.nautec.capabilities.beam;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface IBeamReceiver {
    void receiveBeam(int power, float purity, Direction travel, BlockPos origin, LongSet trail);
}
