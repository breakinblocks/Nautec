package com.breakinblocks.nautec.data.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record SubmarineModuleState(List<Integer> remaining, List<Integer> active, int boost, int stealth) {
    public static final SubmarineModuleState EMPTY = new SubmarineModuleState(List.of(), List.of(), 0, 0);
    public static final Codec<SubmarineModuleState> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(0, Integer.MAX_VALUE).listOf(0, 9).fieldOf("remaining").forGetter(SubmarineModuleState::remaining),
            Codec.intRange(0, Integer.MAX_VALUE).listOf(0, 9).fieldOf("active").forGetter(SubmarineModuleState::active),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("boost").forGetter(SubmarineModuleState::boost),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("stealth").forGetter(SubmarineModuleState::stealth)
    ).apply(i, SubmarineModuleState::new));

    public SubmarineModuleState {
        remaining = List.copyOf(remaining);
        active = List.copyOf(active);
    }
}
