package com.breakinblocks.nautec.content.resonance;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface ResonanceEndpoint {
    ResourceKey<Level> dimension();

    boolean interdimensional();

    boolean sending();

    int sendable();

    int receivable();

    void send(int amount);

    void receive(int amount);
}
