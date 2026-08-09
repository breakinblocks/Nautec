package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.BacteriaStatsSerializer;
import com.breakinblocks.nautec.content.bacteria.SimpleBacteriaStats;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NTBacteriaStatsSerializers {
    public static final DeferredRegister<BacteriaStatsSerializer<?, ?>> SERIALIZERS = DeferredRegister.create(NTRegistries.BACTERIA_STATS_SERIALIZER, Nautec.MODID);

    static {
        SERIALIZERS.register("simple", () -> SimpleBacteriaStats.Serializer.INSTANCE);
    }
}
