package com.breakinblocks.nautec.content.resonance;

import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface NetworkBattery {
    long batteryStored();

    long batteryRoom();

    int batteryRate();

    int drawFe(int amount);

    int storeFe(int amount);

    boolean isRemoved();

    @Nullable Level getLevel();
}
